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

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gj.layout.AbstractLayout;
import gj.layout.Layout;
import gj.layout.LayoutException;
import gj.layout.PathHelper;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.util.ModelHelper;

/**
 * A Layout that arranges nodes in a circle with the
 * least amount of line intersections
 */
public class CircularLayout extends AbstractLayout implements Layout {
  
  /** constant */
  private final static double TWOPI = 2*Math.PI;
  
  /** padding */
  private double padNodes = 16.0D;
  
  /** start degree (0-1) */
  private double startDegree = 0.0D;
  
  /** whether we're generating a single circle or not */
  private boolean isSingleCircle = true;

  /**
   * Getter - is single circle
   */
  public boolean isSingleCircle() {
    return isSingleCircle;
  }
  
  /**
   * Setter - is single circle
   */
  public void setSingleCircle(boolean set) {
    isSingleCircle=set;
  }
  
  /**
   * Getter - padding
   */
  public double getPadding() {
    return padNodes;
  }
  
  /**
   * Setter - padding
   */
  public void setPadding(double set) {
    padNodes=set;
  }
  
  /**
   * Getter - startDegree
   */
  public double getStartDegree() {
    return startDegree;
  }
  
  /**
   * Setter - startDegree
   */
  public void setStartDegree(double set) {
    startDegree=set;
  }
  
  /**
   * @see gj.layout.Layout#applyTo(Graph)
   */
  public void applyTo(Graph graph) throws LayoutException {
    
    // value check
    startDegree = Math.max(0.0D,Math.min(1.0D,startDegree));
    
    // analyze the circle
    Circle circle = new Circle(graph.getNodes());
    
    // put 'em in a circle
    int count = graph.getNodes().size();
    for (int n=0;n<circle.nodes.length;n++) {
      double x = Math.sin(circle.degrees[n])*circle.radius;
      double y = Math.cos(circle.degrees[n])*circle.radius;
      circle.nodes[n].getPosition().setLocation(x,y);
    }
    
    // update the arcs
    Iterator arcs = graph.getArcs().iterator();
    while (arcs.hasNext()) {
      Arc arc = (Arc)arcs.next();
      PathHelper.update(arc.getPath(),arc.getStart(),arc.getEnd());
    }
    
    // done
    graph.getBounds().setRect(ModelHelper.getBounds(graph.getNodes()));
  }

  /**
   * The circle in a graph
   */
  private class Circle {
    
    /** nodes */
    protected Node[] nodes;
    
    /** nodes' degrees */
    protected double[] degrees;
    
    /** radius */
    protected double radius = 0;
    
    /**
     * Constructor
     */
    protected Circle(Set members) {
      
      // prepare space
      nodes = new Node[members.size()];
      degrees = new double[nodes.length];
      
      // gather these values
      double circumference = 0;
      
      // analyze nodes in circle
      Iterator it = members.iterator();
      for (int n=0;it.hasNext();n++) {
        
        // .. the node
        Node node = (Node)it.next();
        Rectangle2D bounds = node.getShape().getBounds2D();
        double size = Math.max(bounds.getWidth(), bounds.getHeight()) + padNodes;
        
        // .. keep what we need
        nodes[n] = node;
        degrees[n] = size;
        
        // .. increase circ
        circumference += size;
      }
      
      // calculate radius (c=2PIr => r=c/2/PI)
      radius = circumference/TWOPI;
      
      // calculate degrees
      double pointer = startDegree*TWOPI;
      for (int n=0;n<nodes.length;n++) {
        double ds = degrees[n]*TWOPI/circumference;
        degrees[n] = pointer + ds/2;
        pointer += ds;
      }

      // Done      
    }
    
  } //Circle
  
}
