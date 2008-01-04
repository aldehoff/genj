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

import gj.geom.Geometry;
import gj.layout.AbstractLayoutAlgorithm;
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.util.EdgeLayoutHelper;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/**
 * A Layout that arranges nodes in a circle with the
 * least amount of line intersections
 */
public class CircularLayoutAlgorithm extends AbstractLayoutAlgorithm implements LayoutAlgorithm {
  
  /** constant */
  private final static double TWOPI = 2*Math.PI;
  
  /** padding */
  private double padNodes = 12.0D;
  
  /** whether we're generating a single circle or not */
  private boolean isSingleCircle = true;
  
  /** an arc layout we use */
  private EdgeLayoutHelper alayout = new EdgeLayoutHelper();

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
   * @see gj.layout.LayoutAlgorithm#apply(Graph, Layout2D, Rectangle2D)
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds) throws LayoutAlgorithmException {
    
    // no purpose in empty|1-ary graph
    if (graph.getNumVertices() < 2) 
      return bounds;
    
    // create a CircularGraph
    CircularGraph cgraph = new CircularGraph(graph, isSingleCircle);
    
    // analyze the circle(s)
    Iterator<CircularGraph.Circle> it = cgraph.getCircles().iterator();
    double x=0,y=0;
    while (it.hasNext()) {
      
      // look at a circle
      CircularGraph.Circle circle = (CircularGraph.Circle)it.next();
      layout(layout, circle, x, y);
      
      // next
      x+=160;
    }
    
    // update the arcs
    EdgeLayoutHelper.setShapes(graph, layout);
    
    // done
    return ModelHelper.getBounds(graph, layout);
  } 
  
  /**
   * layout a circle
   */
  private void layout(Layout2D layout, CircularGraph.Circle circle, double cx, double cy) {
    
    // nodes
    Object[] nodes = circle.getNodes().toArray();
    
    // one node only?
    if (nodes.length==1) {
      ModelHelper.setPosition(layout, nodes[0], cx,cy);
      return;
    }
    
    // nodes' degrees and global circumference
    double[] sizes = new double[nodes.length];
    double circumference = 0;
    
    // analyze nodes in circle
    for (int n=0;n<nodes.length;n++) {
        
      // .. its size - the length of vector (x,y)
      Rectangle2D bounds = layout.getShapeOfVertex(nodes[n]).getBounds2D();
      double size = Geometry.getLength(bounds.getWidth()+padNodes, bounds.getHeight()+padNodes);
        
      // .. keep what we need
      sizes[n] = size;
        
      // .. increase circ
      circumference += size;
    }
      
    // calculate radius (c=2PIr => r=c/2/PI)
    double radius = circumference/TWOPI;
      
    // put 'em in a circle
    double radian = 0;
    for (int n=0;n<nodes.length;n++) {
      double x = (int)(cx + Math.sin(radian)*radius);
      double y = (int)(cy + Math.cos(radian)*radius);
      ModelHelper.setPosition(layout, nodes[n], x, y);

      radian += TWOPI*sizes[n]/circumference;
    }
    
    radian=0;
    
  }

} //CircularLayout
