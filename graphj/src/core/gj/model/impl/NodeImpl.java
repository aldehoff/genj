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

import gj.model.Node;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @see gj.model.Node
 */
public class NodeImpl implements Node {
  
  /** the default shape */
  private static Shape DEFAULT_SHAPE = new  Ellipse2D.Double(-16,-16,32,32);

  /** the content of this node */
  private Object content;
  
  /** the position of this node */
  private Point2D position;
  
  /** the shape of this node */
  private Shape shape;
  
  /** all arcs from/to this node */
  private List 
    arcs = new ArrayList(3), 
    immutableArcs = Collections.unmodifiableList(arcs);

  /**
   * Constructor
   */  
  /*package*/ NodeImpl(Point2D position, Shape shape, Object content) {
    
    this.position = position;
    this.content = content;
    this.shape = shape;
    
    if (this.shape==null) {
      this.shape = DEFAULT_SHAPE;
    }
  }
  
  /**
   * Keeps track of one more incoming/outgoing arc
   */
  /*package*/ void addArc(ArcImpl arc) {
    arcs.add(arc);
  }
  
  /**
   * Forgets about one arc
   */
  /*package*/ void removeArc(ArcImpl arc) {
    // an arc could be kept twice in case of a loop
    while (arcs.remove(arc)) {};
  }

  /**
   * Sets the order of arcs
   */
  /*package*/ void setOrder(List order) {
      
    // validity check
    if (!arcs.containsAll(order)||!order.containsAll(arcs))
      throw new IllegalArgumentException("List of arcs has to be the same set of arcs");
      
    // keep new order
    arcs.clear();
    arcs.addAll(order);
    
    // done
  }
  
  /**
   * @see Node#getPosition()
   */
  public Point2D getPosition() {
    return position;
  }
  
  /**
   * @see Node#getContent()
   */
  public Object getContent() {
    return content;
  }

  /**
   * @see Node#getArcs()
   */
  public List getArcs() {
    return immutableArcs;
  }
  
  /**
   * @see Node#getShape()
   */
  public Shape getShape() {
    return shape;
  }
  
  /**
   * @see MutableNode#setShape(Shape)
   */
  public void setShape(Shape set) {
    shape = set;
  }
  
  /**
   * @see MutableNode#setContent(Object)
   */
  public void setContent(Object set) {
    content = set;
  }
  
  /**
   * String representation
   */
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }
  
}
