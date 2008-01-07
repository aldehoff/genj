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
package gj.util;

import gj.geom.Geometry;
import gj.geom.Path;
import gj.layout.Layout2D;
import gj.model.DirectedGraph;
import gj.model.Graph;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simplified layout for arcs */
public class EdgeLayoutHelper {

  /**
   * Calculate shape of all arcs in graph
   */
  public static void setShapes(Graph graph, Layout2D layout) {
    for (Object edge : graph.getEdges()) 
      setShape(graph, layout, edge);
  }
  
  /**
   * Calculate a shape for an arc
   */
  public static void setShape(Graph graph, Layout2D layout, Object edge) {
    Iterator<?> vertices = graph.getVerticesOfEdge(edge).iterator();
    Object
      from = vertices.next(),
      to = vertices.next();
    int direction = (graph instanceof DirectedGraph) ? ((DirectedGraph)graph).getDirectionOfEdge(edge, from) : 0;
    setShape(graph, layout, edge, from, to, direction);
  }
  
  /**
   * Calculate a shape for an arc
   */
  public static void setShape(Graph graph, Layout2D layout, Object edge, Object from, Object to, int direction) {
    Shape
      sfrom= layout.getShapeOfVertex(from),
      sto  = layout.getShapeOfVertex(to);
    Point2D
      pfrom= layout.getPositionOfVertex(from),
      pto  = layout.getPositionOfVertex(to);
    
    layout.setShapeOfEdge(edge, getShape(pfrom,sfrom,pto,sto, direction));
  }
  
  /**
   * path with a line going through points between two shapes
   * @param points a sequence of points describing the path
   * @param s1 shape positioned at the first point
   * @param s2 shape positioned at the last point
   */  
  public static Shape getShape(Point2D[] points, Shape s1, Shape s2, int direction) {
    
    // A simple line through points
    Path result = new Path();
    
    // intersect the first segment with s1
    Point2D
      a = calcEnd(points[1], points[0], s1),
      b = calcEnd(points[points.length-2], points[points.length-1], s2);
    
    // add the points to this path
    result.start(false, a);
    for (int i=1;i<points.length-1;i++) {
      result.lineTo(points[i]);
    }
    result.lineTo(b);
    
    result.end(true);
    
    // done
    return result;
  }

  /**
   * Creates a connection between given points between two shapes
   * @param p1 the starting point
   * @param s1 the shape sitting at p1
   * @param p2 the ending point
   * @param s2 the shape sitting at p2
   */
  public static Shape getShape(Point2D p1, Shape s1, Point2D p2, Shape s2, int direction) {
    
    // A loop for p1==p2
    if (p1.equals(p2)) {
      
      Rectangle2D bounds = s1.getBounds2D();

      double 
        w = bounds.getMaxX()+bounds.getWidth()/4,
        h = bounds.getMaxY()+bounds.getHeight()/4;

      Point2D
        a = p1,
        b = new Point2D.Double(a.getX()+w, a.getY()  ),
        c = new Point2D.Double(a.getX()+w, a.getY()+h),
        d = new Point2D.Double(a.getX()  , a.getY()+h);
        
      return getShape(new Point2D[]{a,b,c,d,a}, s1, s1, direction);
    }

    Point2D 
    	a = calcEnd(p2, p1, s1),
    	b = calcEnd(p1, p2, s2);
    
    // A simple line
    Path result = new Path();
    result.start(direction<0, a);
    result.lineTo(b);
    result.end(direction>0);
   
    // done
    return result; 
  }
  
  private static Point2D calcEnd(Point2D from, Point2D to, Shape shape) {
    
    ArrayList<Point2D> points = new ArrayList<Point2D>();
    Geometry.getIntersections(from, to, to, shape, points);
    
    return points.isEmpty() ? to : Geometry.getClosest(from, points);

  }

} //ArcLayout
