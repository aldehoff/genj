/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.io;

import gj.awt.geom.PathIteratorKnowHow;
import gj.awt.geom.ShapeHelper;
import gj.model.Arc;
import gj.model.Factory;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read a graph
 */
public class GraphReader implements PathIteratorKnowHow  {
  
  /** statics - default */
  private final static Shape DEFAULT_SHAPE = new Rectangle2D.Double(-16,-16,32,32);
  
  /** the parser we're using */
  private SAXParser parser;
  
  /** the input we're reading from */
  private InputStream in;
  
  /** the factory we're using */
  private Factory factory;
  
  /** the graph we're creating */
  private Graph result;

  /** identity support */
  private Map 
    ids2nodes = new HashMap(),
    ids2arcs = new HashMap(),
    ids2shapes = new HashMap();

  /** a base layout we use for fixing arcs' paths */
  private ArcHelper alayout = new ArcHelper();
  
  /**
   * Constructor
   */
  public GraphReader(InputStream in) throws IOException {
    
    // Try to get a parser
    try {
      parser = SAXParserFactory.newInstance().newSAXParser();
    } catch (Throwable t) {
      throw new IOException("Couldn't initialize SAXParser for reading ("+t.getMessage()+")");
    }
    
    // remember in
    this.in = in;
    
    // done
  }
  
  /**
   * Read - Graph
   */
  public Graph read(Factory faCtory) throws IOException {
    
    // remember
    factory = faCtory;
    
    // create a graph
    result = factory.createGraph();
    
    try {
      
      // parse
      XMLHandler handler = new XMLHandler(new GraphHandler(result));
      parser.parse(in, handler);
      
    } catch (SAXException e) {
      throw new IOException("Couldn't read successfully because of SAXException ("+e.getMessage()+")");
    }

    // done
    return result;
  }
  
  /**
   * Handle an error during read
   */
  protected void error(String message) {
    throw new RuntimeException(message);
  }
  
  /**
   * Handling structure elements
   */
  private abstract class ElementHandler {
    protected ElementHandler start(String name, Attributes atts) { return this; }
    protected void end(String name) {} 
  } //ElementHandler
  
  /**
   * Handling structure elements - Graph
   */
  private class GraphHandler extends ElementHandler {
    private Graph graph;
    protected GraphHandler(Graph grAph) {
      graph = grAph;
    }
    protected ElementHandler start(String name, Attributes atts) {
      if ("node".equals(name)) return new NodeHandler(graph, atts);
      if ("arc".equals(name)) return new ArcHandler(graph, atts);
      if ("shape".equals(name)) return new ShapeHandler(atts);
      return this;
    }
  } //GraphHandler
  
  /**
   * Handling structure elements - Node
   */
  private class NodeHandler extends ElementHandler {
    private Graph graph;
    private String id = null;
    private Shape shape = null;
    private Point2D pos = null;
    private Object content = null;
    protected NodeHandler(Graph grAph, Attributes atts) {
      graph = grAph;
      // the id
      id = atts.getValue("id");
      if (id==null)
        error("expected id=");
      // find a shape
      shape = (Shape)ids2shapes.get(atts.getValue("sid"));
      if (shape==null) shape = DEFAULT_SHAPE;
      // its position
      pos = new Point2D.Double(Double.parseDouble(atts.getValue("x")), Double.parseDouble(atts.getValue("y")));
      // the content
      content = atts.getValue("c");
    }
    protected ElementHandler start(String name, Attributes atts) {
      if (!"graph".equals(name)) 
        error("expected graph");
      content = factory.createGraph();
      return new GraphHandler((Graph)content);
    }
    protected void end(String name) {
      // create the node
      Node node = factory.createNode(graph, shape, content);
      node.getPosition().setLocation( pos );
      // keep it
      ids2nodes.put(id, node);
    }
  } //NodeHandler
  
  /**
   * Handling structure elements - Arc
   */
  private class ArcHandler extends ElementHandler {
    private ShapeHandler shapeHandler;
    private Arc arc;
    protected ArcHandler(Graph graph, Attributes atts) {
      Node
        s = (Node)ids2nodes.get(atts.getValue("s")),
        e = (Node)ids2nodes.get(atts.getValue("e"));
      arc = factory.createArc(graph, s, e);
      ids2arcs.put(atts.getValue("id"),arc);
    }
    protected ElementHandler start(String name, Attributes atts) {
      if ("shape".equals(name)) {
        shapeHandler = new ShapeHandler(atts);
        return shapeHandler;
      }
      return this;
    }
    protected void end(String name) {
      if (shapeHandler!=null) {
        arc.getPath().set(shapeHandler.getResult());
      } else {
        ArcHelper.update(arc);
      }
    }    
  } //ArcHandler

  /**
   * Handling structure elements - Shape
   */
  private class ShapeHandler extends ElementHandler {
    private double[] values = new double[100];
    private int size=0;
    private String id;
    private Shape result;
    protected ShapeHandler(Attributes atts) {
      id = atts.getValue("id");
    }
    protected ElementHandler start(String name, Attributes atts) {
      for (int i=0;i<SEG_NAMES.length;i++) {
        if (SEG_NAMES[i].equals(name)) {
          values[size++]=i;
          for (int j=0;j<SEG_SIZES[i];j++) {
            values[size++]=Double.parseDouble(atts.getValue("v"+j));
          }
          break;
        }
      }
      return this;
    }
    protected void end(String name) {
      if (!"shape".equals(name)) return;
      values[size++]=-1;
      result = ShapeHelper.createShape(0,0,1,1,values);
      if (id!=null) ids2shapes.put(id, result);
    }
    protected Shape getResult() {
      return result;
    }
  } //ShapeHandler
  
  
  /**
   * A SAX 'Default' Handler that knows how to read xml 
   * stacking element handlers
   */
  private final class XMLHandler extends DefaultHandler {
    
    private Stack stack = new Stack();
    
    /**
     * Constructor
     */
    public XMLHandler(ElementHandler root) {
      stack.push(root);
    }    

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI,String localName,String qName,Attributes atts)throws SAXException {
      ElementHandler current = (ElementHandler)stack.peek();
      ElementHandler next = current.start(qName,atts);
      stack.push(next);
    }
    

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
      ElementHandler current = (ElementHandler)stack.pop();
      current.end(qName);
    }


  } //XMLHandler
  
} //GraphReader
