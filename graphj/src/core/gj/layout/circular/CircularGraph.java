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
package gj.layout.circular;

import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ArcIterator;
import gj.util.ModelHelper;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A graph that is broken down in circles
 */
public class CircularGraph {

  /** the circles in a graph */
  private Set circles;
  
  /** the mapping between node and its circle */
  private Map node2circle;
  
  /**
   * Constructor
   */
  public CircularGraph(Graph graph, boolean isSingleCircle) {
    
    // anything to do?
    if (graph.getNodes().isEmpty()) return;
    
    // prepare our nodes and their initial circles
    circles = new HashSet(isSingleCircle ? 1 : graph.getNodes().size());
    node2circle = new HashMap(graph.getNodes().size());
    
    Circle circle = !isSingleCircle ? null : new Circle(graph.getNodes());
    Iterator it = graph.getNodes().iterator();
    while (it.hasNext()) {
      Node node = (Node)it.next();
      if (!isSingleCircle) circle = new Circle(node);
      node2circle.put(node, circle);
    }
    
    // start looking for circles while keeping track of what we looked at
    if (!isSingleCircle) {
      Set unvisited = new HashSet(graph.getNodes());
      while (!unvisited.isEmpty()) {
        findCircles((Node)unvisited.iterator().next(), null, new Stack(), unvisited);
      }
    }

    // done    
  }
  
  /**
   * Find circles starting at given node
   */
  private void findCircles(Node node, Arc backtrack, Stack path, Set unvisited) {
    
    // have we been here before?
    if (path.contains(node)) {
      Circle circle = (Circle)node2circle.get(node);
      circle.fold(path, node);
      return;
    }
    
    // now its visited
    unvisited.remove(node);

    // add current node to stack
    path.push(node);
    
    // recurse into children traversing via arcs
    ArcIterator arcs = new ArcIterator(node);
    while (arcs.next()) {
      // don't go twice
      if (!arcs.isFirst) continue;
      // don't go back
      if (arcs.isDup(backtrack)) continue;
      // don't regard loops
      if (arcs.isLoop) continue;
      // recurse into child
      findCircles(ModelHelper.getOther(arcs.arc, node), arcs.arc, path, unvisited);
    }
    
    // take current node of stack again
    path.pop();
    
    // done
  }
  
  /**
   * Accessor - the circles
   */
  public Collection getCircles() {
    return circles;
  }
  
  /**
   * The circle in a graph
   */
  public class Circle extends HashSet {

    /**
     * Creates a new circle
     */
    Circle(Collection nodes) {
      super.addAll(nodes);
      circles.add(this);
    }
    
    /**
     * Creates a new circle with one elements
     */
    Circle(Node node) {
      super.add(node);
      circles.add(this);
    }
    
    /**
     * Folds all elements in path down to stop into this
     * circle. Folded nodes' circles are merged.
     */
    void fold(Stack path, Node stop) {
      
      // Loop through stack elements
      for (int i=path.size()-1;;i--) {
        // get next (=previous) the node in the stack
        Node node = (Node)path.get(i);
        // back at the stop?
        if (node==stop) break;
        // grab its circle
        Circle other = (Circle)node2circle.get(node);
        addAll(other);
        node2circle.put(node, this);
        circles.remove(other);
        // next stack element
      }
      
      // done
    }
    
    /**
     * Accessor - the nodes
     */
    public Set getNodes() {
      return this;
    }
    
  } //Circle  
  
} //CircularGraph
