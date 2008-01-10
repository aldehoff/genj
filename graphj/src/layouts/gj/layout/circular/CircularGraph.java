/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.layout.circular;

import gj.model.Graph;
import gj.util.ModelHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * A graph that is broken down in circles
 */
/*package*/ class CircularGraph {

  /** the circles in a graph */
  private Set<Circle> circles;
  
  /** the mapping between node and its circle */
  private Map<Object, Circle> node2circle;
  
  /**
   * Constructor
   */
  /*package*/ CircularGraph(Graph graph, boolean isSingleCircle) {
    
    // anything to do?
    if (graph.getVertices().isEmpty()) 
      return;
    
    // prepare our nodes and their initial circles
    circles = new HashSet<Circle>();
    node2circle = new HashMap<Object,Circle>(graph.getVertices().size());
    
    // simple for isSingleCircle=true
    if (isSingleCircle) {
      new Circle(graph);
      return;
    }
    
    // find circles for all
    Set<Object> unvisited = new HashSet<Object>(ModelHelper.toList(graph.getVertices()));
    while (!unvisited.isEmpty()) 
      findCircles(graph, unvisited.iterator().next(), null, new Stack<Object>(), unvisited);

    // done    
  }
  
  /**
   * Find circles starting at given node
   */
  private void findCircles(Graph graph, Object node, Object parent, Stack<Object> path, Set<Object> unvisited) {
    
    // have we been here before?
    if (path.contains(node)) {
      Circle circle = getCircle(node);
      circle.fold(path, node);
      return;
    }
    
    // now its visited
    unvisited.remove(node);
    
    // create a circle for it
    new Circle(Collections.singleton(node));

    // add current node to stack
    path.push(node);
    
    // recurse into neighbours traversing via arcs
    Iterator<?> neighbours = graph.getNeighbours(node).iterator();
    while (neighbours.hasNext()) {
      Object neighbour = neighbours.next();
      // don't go back
      if (neighbour==node||neighbour==parent)
        continue;
      // recurse into child
      findCircles(graph, neighbour, node, path, unvisited);
    }
    
    // take current node of stack again
    path.pop();
    
    // done
  }
  
  /**
   * Accessor - the circles
   */
  /*package*/ Collection<Circle> getCircles() {
    return circles;
  }
  
  /**
   * Accessor - a circle
   */
  /*package*/ Circle getCircle(Object node) {
    Circle result = node2circle.get(node);
    if (result==null)
      result = new Circle(Collections.singleton(node));
    return result;
  }
  
  /**
   * The circle in a graph
   */
  /*package*/ class Circle extends HashSet<Object> {

    /**
     * Creates a new circle
     */
    Circle(Graph graph) {
      for (Object vertex : graph.getVertices())
        add(vertex);
      circles.add(this);
    }
    
    /**
     * Creates a new circle
     */
    Circle(Collection<?> nodes) {
      addAll(nodes);
      circles.add(this);
    }
    
    /**
     * Add a node
     */
    @Override
    public boolean add(Object node) {
      // let super do its thing
      boolean rc = super.add(node);
      // remember node->this
      if (rc)
        node2circle.put(node, this);
      // done
      return rc;
    }
    
    /**
     * Folds all elements in path down to stop into this
     * circle. Folded nodes' circles are merged.
     */
    void fold(Stack<?> path, Object stop) {
      
      // Loop through stack elements
      for (int i=path.size()-1;;i--) {
        // get next (=previous) the node in the stack
        Object node = path.get(i);
        // back at the stop?
        if (node==stop) 
          break;
        // grab its circle
        Circle other = getCircle(node);
        addAll(other);
        circles.remove(other);
        // next stack element
      }
      
      // done
    }
    
    /**
     * Accessor - the nodes
     */
    /*package*/ Set<Object> getNodes() {
      return this;
    }
    
  } //Circle  
  
} //CircularGraph
