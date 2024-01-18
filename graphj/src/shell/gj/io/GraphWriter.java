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
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
    
    // sort arcs so that they're written in order
    // to be recreated on load
    Arc[] arcs = (Arc[])g.getArcs().toArray(new Arc[g.getArcs().size()]);
    Arrays.sort(arcs, new ArcComparator());

    // write 'em
    for (int i=0;i<arcs.length;i++) {
      writeArc(arcs[i]);
    }
    
    pop();
  }

  /**
   * KnowHow about which arc to write first
   */  
  private static class ArcComparator implements Comparator {
    /**
     * callback - compare
     */
    public int compare(Object a, Object b) {
      return compare((Arc)a, (Arc)b);
    }
    /**
     * Which arc to write first (as,ae) || (bs,be)
     * 
     * (1) a,b are loops starting at x => order of a,b in as 
     * 
     *     x == as == ae == bs == be
     * 
     * (2) a,b are dups between x,y => order of a,b in as
     *  
     *     x == as == bs, y == ae == be ||
     *     x == as == be, y == ae == bs
     * 
     * (3) a,b connect in x => order of a,b in ...
     * 
     *     x == as == bs || x == as == be ||     ... as
     *     x == ae == bs || x == ae == be        ... ae
     * 
     * (4) a,b are not connected => we don't care
     */
    private int compare(Arc a, Arc b) {    
      // check nodes
      Node 
        as = a.getStart(),
        ae = a.getEnd  (),
        bs = b.getStart(),
        be = b.getEnd  ();
      // arcs that touch?
      if (as==bs||as==be)
        return compare(as, a, b);
      if (ae==bs||ae==be)
        return compare(ae, a, b);
      // unknown
      return 0;
    }
    /**
     * The index of arc a vs. b in n.getArcs()
     */
    private int compare(Node n, Arc a, Arc b) {
      List arcs = n.getArcs();
      return arcs.indexOf(a) - arcs.indexOf(b);
    }
  } //ArcComparator

  /**
   * Write - Arc
   */
  private void writeArc(Arc arc) throws IOException {
    
    ElementInfo info = new ElementInfo();
    info.put("id", getId(arc));
    info.put("s", getId(arc.getStart()));
    info.put("e", getId(arc.getEnd  ()));
    push("arc",info,false);
      writeShape(arc.getPath(),-1);
    pop();
  }

  /**
   * Write - Shapes
   */
  private void writeShapes(Graph g) throws IOException {
    // starting shapes
    push("shapes",null,false);
    // loop through nodes
    Iterator it = g.getNodes().iterator();
    while (it.hasNext()) {
      // check known shape - wish that (but it is not)
      //  new GeneralPath(shape).equals(new GeneralPath(shape))
      Shape s = ((Node)it.next()).getShape();
      if (!shapes2ids.containsKey(s))
        writeShape(s, shapes2ids.size()+1);
      // next
    }
    // done
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
    
    // gather element information
    ElementInfo info = new ElementInfo();
    info.put("id", getId(n));
    info.put("x", n.getPosition().getX());
    info.put("y", n.getPosition().getY());
    info.put("sid", shapes2ids.get(n.getShape()));

    // no graph contained?
    Object content = n.getContent();
    if (!(content instanceof Graph)) {
      info.put("c", n.getContent());
      push("node",info,true);
      return;
    }
    
    // open node for contained graph
    push("node",info,false);
    
    // do the graph
    write((Graph)content);

    // done
    pop();
  }
  
  /**
   * Get id for node
   */
  private Object getId(Node node) {
    // lookup
    Object result = nodes2ids.get(node);
    if (result==null) {
      result = new Integer(nodes2ids.size()+1);
      nodes2ids.put(node, result);
    }
    // don
    return result;
  }

  /**
   * Get id for arc
   */
  private Object getId(Arc arc) {
    // lookup
    Object result = arcs2ids.get(arc);
    if (result==null) {
      result = new Integer(arcs2ids.size()+1);
      arcs2ids.put(arc, result);
    }
    // done
    return result;
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

} //GraphWriter

