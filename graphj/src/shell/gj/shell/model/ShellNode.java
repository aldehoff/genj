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
package gj.shell.model;

import gj.awt.geom.Geometry;
import gj.util.ArcHelper;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Node for Shell
 */
public class ShellNode implements gj.model.Node {
  
  private Point2D position = new Point2D.Double();
  
  private Shape shape = null;
  
  private ArrayList arcs = new ArrayList();
  
  private Object content;
  
  private ShellGraph graph;
  
  /**
   * Constructor
   */
  protected ShellNode(ShellGraph grAph, Shape shApe, Object conTent) {
    graph = grAph;
    shape = shApe==null ? new Rectangle() : shApe;
    content = conTent;
  }
  
  /**
   * Add an arc
   */
  protected void addArc(ShellArc arc) {
    arcs.add(arc);
  }

  /**
   * Remove an arc
   */
  protected void removeArc(ShellArc arc) {
    arcs.remove(arc);
  }

  /**
   * Check if a point lies within node
   */
  protected boolean contains(Point2D point) {
    return shape.contains(point.getX()-position.getX(),point.getY()-position.getY());   
  }
  
  /**
   * Sets the location
   */
  public void setPosition(Point2D pos) {
    // check parents' bounds
    position = pos;
    // update arcs
    ArcHelper.updateArcs(arcs);
  }

  /**
   * Delete this node
   */
  public void delete() {
    graph.removeNode(this);
    Iterator it = arcs.iterator();
    while (!arcs.isEmpty()) {
      ShellArc arc = (ShellArc)arcs.get(0);
      arc.delete();
    }
  }
  
  /**
   * @see gj.model.Node#getShape()
   */
  public Shape getShape() {
    return shape;
  }
  
  /**
   * @see gj.model.Node#getArcs()
   */
  public List getArcs() {
    return arcs;
  }

  /**
   * @see gj.model.Node#getContent()
   */
  public Object getContent() {
    return content;
  }

  /**
   * @see gj.model.Node#getPosition()
   */
  public Point2D getPosition() {
    return position;
  }
  
  /**
   * Move by delta 
   */
  public void translate(Point2D delta) {
    // FIXME check parent's bounds
    position = Geometry.add(position, delta);
    ArcHelper.updateArcs(arcs);
  }
  
  /**
   * Change shape
   */
  public void setShape(Shape set) {
    // FIXME make sure children fit in + check parent's bound
    
    // change
    shape = set;
    
    // update arcs
    ArcHelper.updateArcs(arcs);
    
    // done
  }

  /**
   * Change content
   */
  public void setContent(Object set) {

    // FIXME check parent's bounds (set==graph?)
    
    // change
    content = set;
    
  }
  
  /**
   * The graph of this node
   */
  public ShellGraph getGraph() {
    return graph;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return content!=null ? content.toString() : super.toString();
  }


} //ShellNode