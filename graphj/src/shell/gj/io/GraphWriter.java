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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import gj.awt.geom.PathIteratorKnowHow;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

/**
 * Write a graph
 */
public class GraphWriter implements PathIteratorKnowHow {

  /** an empty string */
  private final static String EMPTY = "                                                         ";
  
  /** the out */
  private PrintWriter out;
  
  /** stack */
  private Stack stack = new Stack();
  
  /** elements mapped to ids */
  private Map 
    nodes2ids  = new HashMap(),
    arcs2ids   = new HashMap(),
    shapes2ids = new HashMap();

  /**
   * Constructor
   */  
  public GraphWriter(OutputStream out) throws IOException {
    this.out = new PrintWriter(out);
  }

  /**
   * Write - Graph
   */
  public void write(Graph g) throws IOException {
    // open the graph
    push("graph",null,false);
    // shapes
    writeShapes(g);
    // nodes
    writeNodes(g);
    // arcs
    writeArcs(g);
    // done
    pop();
    out.flush();
  }

  /**
   * Write - Arcs
   */
  private void writeArcs(Graph g) throws IOException {
    push("arcs",null,false);
    Iterator it = g.getArcs().iterator();
    while (it.hasNext()) {
      Arc arc = (Arc)it.next();
      writeArc(arc);
    }
    pop();
  }

  /**
   * Write - Arc
   */
  private void writeArc(Arc arc) throws IOException {
    
    ElementInfo info = new ElementInfo();
    info.put("id", arcs2ids.get(arc));
    info.put("s", nodes2ids.get(arc.getStart()));
    info.put("e", nodes2ids.get(arc.getEnd  ()));
    push("arc",info,false);
      writeShape(arc.getPath(),-1);
    pop();
  }

  /**
   * Write - Shapes
   */
  private void writeShapes(Graph g) throws IOException {
    push("shapes",null,false);
    Iterator it = g.getNodes().iterator();
    while (it.hasNext()) {
      Shape s = ((Node)it.next()).getShape();
      if (!shapes2ids.containsKey(s))
        writeShape(s, shapes2ids.size()+1);
    }
    pop();
  }
  
  /**
   * Write - Shape
   */
  private void writeShape(Shape shape, int sid) throws IOException {

    ElementInfo info = new ElementInfo();
    if (sid>=0) {
      info.put("id", sid);
      shapes2ids.put(shape,new Integer(sid));
    }
    push("shape",info,false);
    PathIterator it = shape.getPathIterator(null);
    double[] segment = new double[6];
    while (!it.isDone()) {
      int type = it.currentSegment(segment);
      writeSegment(type,segment);
      it.next();
    };
    pop();
  }
  
  /**
   * Write - Segment
   */
  private void writeSegment(int type, double[] segment) throws IOException {
    ElementInfo info = new ElementInfo();
    for (int i=0;i<SEG_SIZES[type];i++) {
      info.put("v"+i, segment[i]);
    }
    push(SEG_NAMES[type],info,true);
  }
  
  
  /**
   * Write - Nodes
   */
  private void writeNodes(Graph g) throws IOException {
    push("nodes",null,false);
    Iterator nodes = g.getNodes().iterator();
    while (nodes.hasNext()) {
      Node node = (Node)nodes.next();
      writeNode(node);
    }
    pop();
  }
  
  /**
   * Write - Node
   */
  private void writeNode(Node n) throws IOException {
    
    int nid = nodes2ids.size()+1;
    nodes2ids.put(n, new Integer(nid));

    ElementInfo info = new ElementInfo();
    info.put("id", nid);
    info.put("x", n.getPosition().getX());
    info.put("y", n.getPosition().getY());
    info.put("c", n.getContent());
    info.put("sid", shapes2ids.get(n.getShape()));
    
    Iterator it = n.getArcs().iterator();
    for (int a=0;it.hasNext();a++) {
      Object arc = it.next();
      Object aid = arcs2ids.get(arc);
      if (aid==null) {
        aid = new Integer(arcs2ids.size()+1);
        arcs2ids.put(arc, aid);
      }
      info.put("aid"+a, aid );
    }
    
    push("node",info,true);

  }
  
  /**
   * Push element
   */
  private void push(String tag, ElementInfo info, boolean close) {
    StringBuffer b = new StringBuffer();
    b.append('<').append(tag);
    if (info!=null) info.append(b);
    if (close) {
      write(b.append("/>").toString());
    } else {
      write(b.append('>').toString());
      stack.push(tag);
    }
  }
  
  /**
   * Pop element
   */
  private void pop() {
    write("</"+stack.pop()+">");
  }
  
  /**
   * Write txt
   */
  private void write(String txt) {
    out.print(EMPTY.substring(0,stack.size()));
    out.println(txt);
  }

  /**
   * Element information
   */
  public static class ElementInfo {
    private ArrayList list = new ArrayList(6);
    public void put(Object key, double val) {
      list.add(key);
      list.add(new Double(val));
    }    
    public void put(Object key, int val) {
      list.add(key);
      list.add(new Integer(val));
    }    
    public void put(Object key, Object val) {
      list.add(key);
      list.add(val);
    }    
    public void append(StringBuffer b) {
      Iterator it = list.iterator();
      while (it.hasNext()) {
        String key = it.next().toString();
        String val = it.next().toString();
        b.append(' ').append(key).append("=\"").append(val).append("\"");
      }
    }
  } //ElementInfo
}

