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
package gj.model.impl;

import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @see gj.model.MutableGraph
 */
public class DefaultGraph implements Graph {
  
  /** the contained nodes */
  protected Collection nodes = new HashSet(10);
  private Collection immutableNodes = Collections.unmodifiableCollection(nodes);

  /** the contained arcs */
  protected Collection arcs = new HashSet(10);
  private Collection immutableArcs= Collections.unmodifiableCollection(arcs);

  /** listeners */
  private List listeners = new ArrayList(); 
  
  /**
   * Constructor
   */
  public DefaultGraph() {
  }

  /**
   * Constructor - creates a mutable graph with nodes and arcs
   * that are clones of those in given graph (node.getPosition(),
   * node.getShape(), node.getContent(), arc.getPath() are shared!)
   */
  public DefaultGraph(Graph graph) {

    // clone nodes
    Map orig2clone = new HashMap(graph.getNodes().size());
    Iterator it = graph.getNodes().iterator();
    while (it.hasNext()) {
      Node orig = (Node)it.next();
      Node clone = addNode(orig.getPosition(), orig.getShape(), orig.getContent());
      orig2clone.put(orig,clone);
    }
    
    // clone arcs
    it = graph.getArcs().iterator();
    while (it.hasNext()) {
      Arc orig = (Arc)it.next();
      addArc((Node)orig2clone.get(orig.getStart()), (Node)orig2clone.get(orig.getEnd()), orig.getPath() );
    }
    
    // done
  }

  /**
   * @see MutableGraph#createArc(Node, Node)
   */
  protected Arc addArc(Node from, Node to, Path path) {

    DefaultNode 
      iFrom = getImpl(from),
      iTo   = getImpl(to);
      
    DefaultArc arc = new DefaultArc(iFrom, iTo, path);
    iFrom.addArc(arc);
    iTo.addArc(arc);

    arcs.add(arc);

    return arc;
  }
  
  /**
   * @see MutableGraph#removeArc(Arc)
   */
  protected void removeArc(Arc arc) {
    getImpl(arc).disconnect();
    arcs.remove(arc);
  }

  /**
   * @see MutableGraph#createNode(Point2D, Shape, Object)
   */
  protected Node addNode(Point2D position, Shape shape, Object content) {
    DefaultNode node = new DefaultNode(position, shape, content);
    nodes.add(node);
    return node;
  }

  /**
   * @see MutableGraph#removeNode(Node)
   */
  protected void removeNode(Node node) {
    getImpl(node);
    
    List arcs = node.getArcs();
    while (arcs.size()>0) {
      removeArc((DefaultArc)arcs.get(0));
    }
    nodes.remove(node);
  }

  /**
   * @see Graph#getNodes()
   */
  public Collection getNodes() {
    return immutableNodes;
  }

  /**
   * @see Graph#getArcs()
   */
  public Collection getArcs() {
    return immutableArcs;
  }

  /**
   * Helper that returns NodeImpl for given Node
   */
  protected final DefaultNode getImpl(Node node) {
    if (!nodes.contains(node))
      throw new IllegalArgumentException("Node "+node+" has to be part of this Graph");
    return (DefaultNode)node;   
  }
  
  /**
   * Helper that returns ArcImpl for given Node
   */
  protected final DefaultArc getImpl(Arc arc) {
    if (!arcs.contains(arc))
      throw new IllegalArgumentException("Arc "+arc+" has to be part of this Graph");
    return (DefaultArc)arc;
  }


}