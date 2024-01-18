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
import gj.awt.geom.ShapeHelper;
import gj.util.ArcHelper;
import gj.util.ModelHelper;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
    setContent(conTent);
    setShape(shApe==null ? new Rectangle() : shApe);    
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
   * Revalidate node constraints
   */
  protected void revalidate(boolean shrink) {
    
    // FIXME this should try to shift the contained graph nodes' origin
    // to optimize space requirement
    
    // do we contain a graph?
    if (content instanceof ShellGraph) {
      
      // check our bounds
      Rectangle2D bounds = ModelHelper.getBounds(((ShellGraph)content).getNodes());
      
      // find maximum
      double max = 1.0D;
      while (!ShapeHelper.createShape(shape, max, null).contains(bounds))
        max *= 2.0D;
        
      if (max==1.0D&&!shrink)
        return;
        
      // find minimum
      double min = 1.0D;
      while (ShapeHelper.createShape(shape, min, null).contains(bounds)) 
        min *= 0.5D;
    
      // binary search for best fit 
      for (int i=0;i<8;i++) {
          
        double pivt = (min+max)/2;
    
        if (ShapeHelper.createShape(shape, pivt, null).contains(bounds)) {
          max = pivt;
        } else {
          min = pivt;
        }
    
      }
       
      // set shape 
      shape = ShapeHelper.createShape(shape, max, null);
    }
          
    // update arcs
    ArcHelper.updateArcs(arcs);
    
    // done
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
    // move
    position = Geometry.add(position, delta);
    ArcHelper.updateArcs(arcs);
    // notify
    graph.revalidate();
    // done
  }
  
  /**
   * Change shape
   */
  public void setShape(Shape set) {

    // change
    shape = set;
    
    // revalidate
    revalidate(false);
    
    // notify
    graph.revalidate();
    
    // done
  }

  /**
   * Change content
   */
  public void setContent(Object set) {

    // was a graph?
    if (content instanceof ShellGraph)
      ((ShellGraph)content).removeNotify();

    // change
    content = set;
    
    // is it a graph?
    if (content instanceof ShellGraph)
      ((ShellGraph)content).addNotify(this);

    // done
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