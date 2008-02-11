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
import gj.model.Edge;
import gj.model.Graph;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A simplified layout for arcs */
public class EdgeLayoutHelper {

  /**
   * Calculate shape of all arcs in graph
   */
  public static void setShapes(Graph graph, Layout2D layout) {
    
    for (Edge edge : graph.getEdges()) { 
      setPath(edge, layout);
    }
    
  }
  
  /**
   * Calculate a shape for an arc
   */
  public static void setPath(Edge edge, Layout2D layout) {
    layout.setPathOfEdge(edge, getPath(edge, layout));
  }
  
  public static Path getPath(Edge edge, Layout2D layout) {
    Shape
    sfrom= layout.getShapeOfVertex(edge.getStart()),
    sto  = layout.getShapeOfVertex(edge.getEnd());
  Point2D
    pfrom= layout.getPositionOfVertex(edge.getStart()),
    pto  = layout.getPositionOfVertex(edge.getEnd());
  
    return getShape(pfrom,sfrom,pto,sto);
  }
  
  /**
   * path with a line going through points between two shapes
   * @param points a sequence of points describing the path (first point is the origin of the shape)
   * @param s1 shape positioned at the first point
   * @param s2 shape positioned at the last point
   */  
  public static Path getPath(Point2D[] points, Shape s1, Shape s2) {
    
    // A simple line through points
    Path result = new Path();
    
    // intersect the first segment with s1
    Point2D
      a = calcEnd(points[1], points[0], s1),
      b = calcEnd(points[points.length-2], points[points.length-1], s2);
    
    double cx = points[0].getX(), cy = points[0].getY();
    
    // add the points to this path
    result.start(new Point2D.Double( a.getX() - cx, a.getY() - cy));
    for (int i=1;i<points.length-1;i++) {
      result.lineTo( new Point2D.Double( 
          points[i].getX() - cx, 
          points[i].getY() - cy
        ));
    }
    result.lineTo(new Point2D.Double( b.getX() - cx, b.getY() - cy));
    
    // done
    return result;
  }

  /**
   * Creates a connection between given points between two shapes
   * @param p1 the starting point (origin of shape)
   * @param s1 the shape sitting at p1
   * @param p2 the ending point
   * @param s2 the shape sitting at p2
   */
  public static Path getShape(Point2D p1, Shape s1, Point2D p2, Shape s2) {

    Point2D 
    	a = calcEnd(p2, p1, s1),
    	b = calcEnd(p1, p2, s2);
    
    // A simple line
    Path result = new Path();
    result.start(new Point2D.Double(a.getX()-p1.getX(), a.getY()-p1.getY()));
    result.lineTo(new Point2D.Double(b.getX()-p1.getX(), b.getY()-p1.getY()));
    
    // done
    return result; 
  }
  
  private static Point2D calcEnd(Point2D from, Point2D to, Shape shape) {
    
    ArrayList<Point2D> points = new ArrayList<Point2D>();
    Geometry.getIntersections(from, to, to, shape, points);
    
    return points.isEmpty() ? to : Geometry.getClosest(from, points);

  }

  public static void translate(Edge edge, Point2D delta, Layout2D layout) {
    layout.setPathOfEdge(edge, new Path(layout.getPathOfEdge(edge), AffineTransform.getTranslateInstance(delta.getX(), delta.getY())));
    
  }

} //ArcLayout
