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
import java.util.Iterator;

/**
 * a handle to a node in a graph
 */
public class Handle {
  
  /** graph */
  private MutableGraph theGraph;

  /** sequence of nodes */
  private Node theNode;

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
   * Constructor
   */
  private Handle(MutableGraph graph, Node node, Point2D pos) {
    
    // keep
    theGraph = graph;
    theNode  = node ;
    
//    // check if there's a better fit inside node
//    Object content = node.getContent();
//    if (content instanceof MutableGraph) {
//      pos = Geometry.getDelta(node.getPosition(), pos);
//      MutableGraph ig = (MutableGraph)content;
//      Node in = findHit(ig, pos);
//      if (in!=null) {
//        graphs.add(ig);
//        nodes .add(in);
//      }
//    }
    
    // done
  }
  
  /**
   * update arcs
   */
  private void updateArcs(Node node) {
    // update it's arcs
    ArcIterator it = new ArcIterator(node);
    while (it.next()) ArcHelper.update(it.arc);
    // done
  }
  
  /**
   * Sets selection's content
   */
  public void setContent(Object content) {
    theGraph.setContent(theNode, content);
  }

  /**
   * Sets selection's content
   */
  public Point2D getDistance(Point2D pos) {
    return Geometry.getDelta(theNode.getPosition(), pos);
  }
  
  /**
   * Set selection's shape
   */
  public void setShape(Shape shape) {  
    theGraph.setShape(theNode, shape);
    updateArcs(theNode);
  }
  
  /**
   * Gets selection's shape
   */
  public Shape getShape() {  
    return theNode.getShape();
  }
  
  /**
   * Delete the selection
   */
  public void delete() {
    theGraph.removeNode(theNode);
  }

  /**
   * Check for content
   */
  public boolean is(Node tst) {
    return theNode == tst;
  }
  
  /**
   * Move by delta
   */
  public void moveBy(Point2D delta) {
//    for (int i=0;i<nodes.size()-1;i++) 
//      delta = Geometry.getDelta( delta, ((Node)nodes.get(i)).getPosition());
    // move it
    ModelHelper.move(theNode, delta); 
    updateArcs(theNode);
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
