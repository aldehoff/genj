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
package gj.shell;

import gj.awt.geom.Geometry;
import gj.model.MutableGraph;
import gj.model.Node;
import gj.util.ArcHelper;
import gj.util.ArcIterator;
import gj.util.ModelHelper;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * a handle to a node in a graph
 */
public class Handle {
  
  /** graphs for nodes */
  private Map node2graph = new HashMap();

  /** hierarchy - parents of nodes */
  private Map node2parent = new HashMap();

  /** node that handle refers to */
  private Node node;

  /**
   * try to get a handle for given graph and position
   */
  public static Handle get(MutableGraph graph, Point pos) {
    
    // try to find hit in nodes
    Node hit = findHit(graph, pos);
    if (hit!=null)
      return new Handle(graph, hit, pos);

    // nothing found    
    return null;
  }
  
  /**
   * Test for hit
   */
  private static Node findHit(MutableGraph graph, Point2D pos) {
    
    // look through nodes
    Iterator nodes = graph.getNodes().iterator();
    while (nodes.hasNext()) {

      // check position
      Node node = (Node)nodes.next();
      Point2D center = node.getPosition();
      
      if (node.getShape().contains(pos.getX()-center.getX(),pos.getY()-center.getY())) 
        return node;
    }
    
    // not found
    return null;
  }
  
  /**
   * update arcs
   */
  private static void updateArcs(Node node) {
    // update it's arcs
    ArcIterator it = new ArcIterator(node);
    while (it.next()) ArcHelper.update(it.arc);
    // done
  }
  
  /**
   * Constructor
   */
  private Handle(MutableGraph grAph, Node noDe, Point2D pos) {
    
    // keep node and its graph
    node  = noDe ;
    node2graph.put(node, grAph);
    
    // check if there's a better hit in contained node
    while (node.getContent() instanceof MutableGraph) {
      // .. hit inside graph in node (adjust position)
      pos = Geometry.getDelta(node.getPosition(), pos);
      MutableGraph graph = (MutableGraph)node.getContent();
      Node child = findHit(graph, pos);
      // .. check
      if (child==null) break;
      // .. keep parent
      node2parent.put(child, node);
      node2graph.put(child, graph);        
      node = child;
    }
    
    // done
  }
  
  /**
   * Graph for node
   */
  private MutableGraph getGraph(Node node) {
    return (MutableGraph)node2graph.get(node);
  }
  
  /**
   * Parent for node
   */
  private Node getParent(Node node) {
    return (Node)node2parent.get(node);
  }
  
  /**
   * Sets selection's content
   */
  public void setContent(Object content) {
    getGraph(node).setContent(node, content);
  }

  /**
   * Sets selection's content
   */
  public Point2D getDistance(Point2D pos) {
    return Geometry.getDelta(node.getPosition(), pos);
  }
  
  /**
   * Set selection's shape
   */
  public void setShape(Shape shape) {  
    getGraph(node).setShape(node, shape);
    updateArcs(node);
  }
  
  /**
   * Gets selection's shape
   */
  public Shape getShape() {  
    return node.getShape();
  }
  
  /**
   * Delete the selection
   */
  public void delete() {
    getGraph(node).removeNode(node);
  }

  /**
   * Check for content
   */
  public boolean is(Node tst) {
    return node == tst;
  }
  
  /**
   * Move by delta
   */
  public void moveBy(Point2D delta) {
    // move it
    ModelHelper.move(node, delta); 
    updateArcs(node);
    // check if there's a parent 
//    Node child = node;
//    Node parent = getParent(node);
//    while (parent!=null) {
//      Rectangle2D bounds = ModelHelper.getBounds(getGraph(child).getNodes());
//      getGraph(parent).setShape(parent, bounds);
//      child = parent;
//      
//    }
    // go up the hierarchy
//    for (int i=nodes.size();;) {
//      // our graph
//      Graph graph = (Graph)graphs.get(--i);
//      if (i==0) break;
//      // calculate size
//      Rectangle2D bounds = ModelHelper.getBounds(graph.getNodes());
//      // set on parent
//      ((Node)nodes.get(i-1)).;
//      // next
//    }
    // done
  }

} //Handle
