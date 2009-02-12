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
import gj.layout.Layout2D;
import gj.layout.LayoutAlgorithm;
import gj.layout.LayoutAlgorithmException;
import gj.model.Graph;
import gj.model.Vertex;
import gj.util.EdgeLayoutHelper;
import gj.util.ModelHelper;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A Layout that arranges nodes in a circle with the
 * least amount of line intersections
 */
public class CircularLayoutAlgorithm implements LayoutAlgorithm {
  
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
   * @see gj.layout.LayoutAlgorithm#apply(Graph, Layout2D, Rectangle2D, Collection)
   */
  public Shape apply(Graph graph, Layout2D layout, Rectangle2D bounds, Collection<Shape> debugShapes) throws LayoutAlgorithmException {
    
    // no purpose in empty|1-ary graph
    if (graph.getVertices().size() < 2) 
      return bounds;
    
    // create a CircularGraph
    CircularGraph cgraph = new CircularGraph(graph, isSingleCircle);
    
    // analyze the circle(s)
    Iterator<CircularGraph.Circle> it = cgraph.getCircles().iterator();
    double x=0,y=0;
    while (it.hasNext()) {
      
      // look at a circle
      CircularGraph.Circle circle = (CircularGraph.Circle)it.next();
      layout(graph, layout, circle, x, y);
      
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
  private void layout(Graph graph, Layout2D layout, CircularGraph.Circle circle, double cx, double cy) {
    
    // nodes
    List<Vertex> nodes = new ArrayList<Vertex>(circle.getNodes());
    
    // one node only?
    if (nodes.size()==1) {
      Vertex one = nodes.get(0);
      layout.setPositionOfVertex(one, new Point2D.Double(cx,cy));
      layout.setTransformOfVertex(one, null);
      return;
    }
    
    // nodes' degrees and global circumference
    double[] sizes = new double[nodes.size()];
    double circumference = 0;
    
    // analyze nodes in circle
    for (int n=0;n<nodes.size();n++) {
        
      // .. its size - the length of vector (x,y)
      Rectangle2D bounds = layout.getShapeOfVertex(nodes.get(n)).getBounds2D();
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
    for (int n=0;n<nodes.size();n++) {
      double x = (int)(cx + Math.sin(radian)*radius);
      double y = (int)(cy + Math.cos(radian)*radius);
      Vertex node = nodes.get(n);
      layout.setPositionOfVertex(node, new Point2D.Double(x,y));
      layout.setTransformOfVertex(node, null);

      radian += TWOPI*sizes[n]/circumference;
    }
    
    radian=0;
    
  }

} //CircularLayout
