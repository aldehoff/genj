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

import gj.awt.geom.Path;
import gj.awt.geom.PathIteratorKnowHow;
import gj.awt.geom.ShapeHelper;
import gj.layout.ArcLayout;
import gj.model.Arc;
import gj.model.MutableGraph;
import gj.model.Node;
import gj.util.ModelHelper;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
  
  /** the graph we're creating */
  private MutableGraph graph;

  /** identity support */
  private Map 
    nodes2arcs = new HashMap(),
    ids2nodes = new HashMap(),
    ids2arcs = new HashMap(),
    ids2shapes = new HashMap();

  /** a base layout we use for fixing arcs' paths */
  private ArcLayout alayout = new ArcLayout();
  
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
  public void read(MutableGraph graph) throws IOException {
    
    try {
      
      // keep the graph
      this.graph = graph;
      
      // parse
      XMLHandler handler = new XMLHandler(new GraphHandler());
      parser.parse(in, handler);
      
      // post-work : node's arcs
      Iterator it = nodes2arcs.keySet().iterator();
      while (it.hasNext()) {
        // a node and its arcs
        Node node = (Node)it.next();
        List arcs = (List)nodes2arcs.get(node);
        if (arcs==null) continue;
        // lookup arcs
        for (int a=0; a<arcs.size(); a++) {
          arcs.set(a, ids2arcs.get(arcs.get(a)));
        }
        // set on node
        graph.setOrder(node, arcs);
        // next
      }
      
      // post-work : graph's bounds
      graph.getBounds().setRect(ModelHelper.getBounds(graph.getNodes()));
      
      // done
    } catch (SAXException e) {
      throw new IOException("Couldn't read successfully because of SAXException ("+e.getMessage()+")");
    }
  }
  
  /**
   * Handling structure elements
   */
  private abstract class ElementHandler {
    protected ElementHandler start(String name, Attributes atts) { return this; }
    protected void end(String name) {} 
  }
  
  /**
   * Handling structure elements - Graph
   */
  private class GraphHandler extends ElementHandler {
    protected ElementHandler start(String name, Attributes atts) {
      if ("node".equals(name)) return new NodeHandler(atts);
      if ("arc".equals(name)) return new ArcHandler(atts);
      if ("shape".equals(name)) return new ShapeHandler(atts);
      return this;
    }
  }
  
  /**
   * Handling structure elements - Node
   */
  private class NodeHandler extends ElementHandler {
    protected NodeHandler(Attributes atts) {
      // find a shape
      Shape shape = (Shape)ids2shapes.get(atts.getValue("sid"));
      if (shape==null) shape = DEFAULT_SHAPE;
      // its position
      Point2D pos = new Point2D.Double(Double.parseDouble(atts.getValue("x")), Double.parseDouble(atts.getValue("y")));
      // the content
      String content = atts.getValue("c");
      // create the node
      Node node = graph.createNode(pos,shape,content);
      // it's arcs
      List arcs = new ArrayList(5);
      for (int a=0;;a++) {
        String aid = atts.getValue("aid"+a);
        if (aid==null) break;
        arcs.add(aid);
      }
      if (!arcs.isEmpty()) nodes2arcs.put(node,arcs);
      // keep it
      ids2nodes.put(atts.getValue("id"), node);
    }
  }
  
  /**
   * Handling structure elements - Arc
   */
  private class ArcHandler extends ElementHandler {
    private ShapeHandler shapeHandler;
    private Arc arc;
    protected ArcHandler(Attributes atts) {
      Node
        s = (Node)ids2nodes.get(atts.getValue("s")),
        e = (Node)ids2nodes.get(atts.getValue("e"));
      arc = graph.createArc(s, e, new Path());
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
        alayout.layout(arc);
      }
    }    
  }

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
  }
  
  
  /**
   * A SAX 'Default' Handler that knows how to read a Graph
   */
  private class XMLHandler extends DefaultHandler {
    
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


  } // EOC
}

