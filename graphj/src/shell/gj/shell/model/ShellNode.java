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

import gj.awt.geom.ShapeHelper;
import gj.util.ArcHelper;

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
  
  private Position pos = new Position();
  
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
    return shape.contains(point.getX()-pos.getX(),point.getY()-pos.getY());   
  }
  
  /**
   * Revalidate node constraints
   */
  protected void revalidate(boolean shrink) {
    
    revalidateShape(shrink);    
          
    // update arcs
    ArcHelper.updateArcs(arcs);
    
    // done
  }  
  
  /**
   * Makes sure the shape is appropriate for the content
   */
  private void revalidateShape(boolean shrinkIfAble) {

    // don't care if not containing graph?
    if (!(content instanceof ShellGraph))
      return;
    ShellGraph nested = (ShellGraph)content;
      
    // check our bounds
    Rectangle2D bounds = nested.getBounds();
   
    // find maximum
    double max = 1.0D;
    while (!ShapeHelper.createShape(shape, max, null).contains(bounds))
      max *= 2.0D;
      
    // shape is big enough - shrink it?
    if (max==1.0D&&!shrinkIfAble)
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
    
    // done
  }
  
  /**
   * Sets the location
   */
  public void setPosition(Point2D set) {
    pos.setLocation(set);
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
    return pos;
  }
  
  /**
   * Move by delta 
   */
  public void translate(Point2D delta) {
    pos.translate(delta);
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

  /**
   * Any contained graph
   */
  public ShellGraph getContainedGraph() {
    return content instanceof ShellGraph ? (ShellGraph)content : null;
  }
 
  /**
   * Our own position
   */
  private class Position extends Point2D {
    private double x,y;
    /** */
    public double getX() {
      return x;
    }
    /** */
    public double getY() {
      return y;
    }
    /** */
    public void setLocation(double sx, double sy) {
      // remember
      x = sx;
      y = sy;
      // update arcs
      ArcHelper.updateArcs(arcs);
      // notify
      graph.revalidate();
    }
    /** */
    public void translate(Point2D delta) {
      setLocation(x+delta.getX(), y+delta.getY());
    }
  } //Position
  
} //ShellNode