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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A default implementation for
 * @see gj.model.Node
 */
public class DefaultNode implements Node {
  
  /** the default shape */
  private static Shape EMPTY_SHAPE = new Rectangle();

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
  /*package*/ DefaultNode(Point2D position, Shape shape, Object content) {
    
    this.position = position!=null ? position : new Point2D.Double();
    this.content = content;
    this.shape = shape!=null ? shape : EMPTY_SHAPE;
  }
  
  /**
   * Keeps track of one more incoming/outgoing arc
   */
  /*package*/ void addArc(DefaultArc arc) {
    arcs.add(arc);
  }
  
  /**
   * Forgets about one arc
   */
  /*package*/ void removeArc(DefaultArc arc) {
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
   * String representation
   */
  public String toString() {
    if (content==null) {
      return super.toString();
    } else {
      return content.toString();
    }
  }
  
} //DefaultNode
