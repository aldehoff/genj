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

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gj.awt.geom.Dimension2D;
import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.MutableGraph;
import gj.model.Node;
import gj.util.ArcIterator;

/**
 * @see gj.model.MutableGraph
 */
public class MutableGraphImpl implements MutableGraph {
  
  /** the bounds */
  private Rectangle2D bounds = new Rectangle2D.Double();

  /** the contained nodes */
  protected Set nodes = new HashSet(10);
  private Set immutableNodes = Collections.unmodifiableSet(nodes);

  /** the contained arcs */
  protected Set arcs = new HashSet(10);
  private Set immutableArcs= Collections.unmodifiableSet(arcs);

  /** listeners */
  private List listeners = new ArrayList(); 
  
  /**
   * Constructor
   */
  public MutableGraphImpl() {
  }

  /**
   * Constructor - creates a mutable graph with nodes and arcs
   * that are clones of those in given graph (node.getPosition(),
   * node.getShape(), node.getContent(), arc.getPath() are shared!)
   */
  public MutableGraphImpl(Graph graph) {

    // clone nodes
    Map orig2clone = new HashMap(graph.getNodes().size());
    Iterator it = graph.getNodes().iterator();
    while (it.hasNext()) {
      Node orig = (Node)it.next();
      Node clone = createNode(orig.getPosition(), orig.getShape(), orig.getContent());
      orig2clone.put(orig,clone);
    }
    
    // clone arcs
    it = graph.getArcs().iterator();
    while (it.hasNext()) {
      Arc orig = (Arc)it.next();
      createArc((Node)orig2clone.get(orig.getStart()), (Node)orig2clone.get(orig.getEnd()), orig.getPath() );
    }
    
    // done
  }

  /**
   * @see MutableGraph#createArc(Node, Node)
   */
  public Arc createArc(Node from, Node to, Path path) {

    NodeImpl 
      iFrom = getImpl(from),
      iTo   = getImpl(to);
      
    if (path==null) path = new Path();

    ArcImpl arc = new ArcImpl(iFrom, iTo, path);
    iFrom.addArc(arc);
    iTo.addArc(arc);

    arcs.add(arc);

    return arc;
  }
  
  /**
   * @see MutableGraph#removeArc(Arc)
   */
  public void removeArc(Arc arc) {
    getImpl(arc).disconnect();
    arcs.remove(arc);
  }

  /**
   * @see MutableGraph#createNode(Point2D, Shape, Object)
   */
  public Node createNode(Point2D position, Shape shape, Object content) {
    NodeImpl node = new NodeImpl(position, shape, content);
    nodes.add(node);
    return node;
  }

  /**
   * @see MutableGraph#removeNode(Node)
   */
  public void removeNode(Node node) {
    getImpl(node);
    
    List arcs = node.getArcs();
    while (arcs.size()>0) {
      removeArc((ArcImpl)arcs.get(0));
    }
    nodes.remove(node);
  }

  /**
   * @see Graph#getNodes()
   */
  public Set getNodes() {
    return immutableNodes;
  }

  /**
   * @see Graph#getArcs()
   */
  public Set getArcs() {
    return immutableArcs;
  }

  /**
   * @see Graph#getBounds()
   */
  public Rectangle2D getBounds() {
    // done
    return bounds;
  }

  /**
   * @see MutableGraph#setShape(Node, Shape)
   */
  public void setShape(Node node, Shape shape) {
    getImpl(node).setShape(shape);
  }
  
  /**
   * @see MutableGraph#setContent(Node, Object)
   */
  public void setContent(Node node, Object content) {
    getImpl(node).setContent(content);
  }

  /**
   * @see MutableGraph#setOrder(Node, List)
   */
  public void setOrder(Node node, List arcs) {
    getImpl(node).setOrder(arcs);
  }

  /**
   * Helper that returns NodeImpl for given Node
   */
  protected final NodeImpl getImpl(Node node) {
    if (!nodes.contains(node))
      throw new IllegalArgumentException("Node "+node+" has to be part of this Graph");
    return (NodeImpl)node;   
  }
  
  /**
   * Helper that returns ArcImpl for given Node
   */
  protected final ArcImpl getImpl(Arc arc) {
    if (!arcs.contains(arc))
      throw new IllegalArgumentException("Arc "+arc+" has to be part of this Graph");
    return (ArcImpl)arc;
  }


}