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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gj.awt.geom.Geometry;
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
  private double padNodes = 4.0D;
  
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
   * @see gj.layout.Layout#applyTo(Graph)
   */
  public void applyTo(Graph graph) throws LayoutException {
    
    // no purpose in empty|1-ary graph
    if (graph.getNodes().size()<2) return;
    
    // create a CircularGraph
    CircularGraph cgraph = new CircularGraph(graph, isSingleCircle);
    
    // analyze the circle(s)
    Iterator it = cgraph.getCircles().iterator();
    double x=0,y=0;
    while (it.hasNext()) {
      
      // look at a circle
      CircularGraph.Circle circle = (CircularGraph.Circle)it.next();
      layout(circle, x, y);
      
      // next
      x+=160;
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
   * layout a circle
   */
  private void layout(CircularGraph.Circle circle, double cx, double cy) {
    
    // nodes
    Node[] nodes = new Node[circle.getNodes().size()];
    circle.getNodes().toArray(nodes);
    
    // one node only?
    if (nodes.length==1) {
      nodes[0].getPosition().setLocation(cx,cy);
      return;
    }
    
    // nodes' degrees and global circumference
    double[] degrees = new double[nodes.length];
    double circumference = 0;
    
    // analyze nodes in circle
    for (int n=0;n<nodes.length;n++) {
        
      // .. its size - the length of vector (x,y)
      Rectangle2D bounds = nodes[n].getShape().getBounds2D();
      double size = Geometry.getLength(bounds.getWidth()+padNodes, bounds.getHeight()+padNodes);
        
      // .. keep what we need
      degrees[n] = size;
        
      // .. increase circ
      circumference += size;
    }
      
    // calculate radius (c=2PIr => r=c/2/PI)
    double radius = circumference/TWOPI;
      
    // calculate degrees
    double pointer = 0;
    for (int n=0;n<nodes.length;n++) {
      double ds = degrees[n]*TWOPI/circumference;
      degrees[n] = pointer + ds/2;
      pointer += ds;
    }

    // put 'em in a circle
    for (int n=0;n<nodes.length;n++) {
      double x = cx + Math.sin(degrees[n])*radius;
      double y = cy + Math.cos(degrees[n])*radius;
      nodes[n].getPosition().setLocation(x,y);
    }
    
  }

} //CircularLayout
