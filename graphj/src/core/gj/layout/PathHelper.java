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
package gj.layout;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import gj.awt.geom.Geometry;
import gj.awt.geom.Path;
import gj.model.Node;

/**
 * A helper for layouting Paths
 */
public class PathHelper {

  /**
   * Creates a simple path between two nodes
   */
  public static Path create(Node from, Node to) {
    Path result = new Path();
    update(result, from.getPosition(), from.getShape(), to.getPosition(), to.getShape());
    return result;
  }

  /**
   * Updates a simple path between two nodes
   */
  public static Path update(Path path, Node start, Node end) {
    return update(path,start.getPosition(),start.getShape(),end.getPosition(),end.getShape());
  }

  /**
   * Updates given path with a line going through points between two shapes
   * @param path the path to update
   * @param points a sequence of points describing the path
   * @param s1 shape positioned at the first point
   * @param s2 shape positioned at the last point
   */  
  public static Path update(Path path, Point2D[] points, Shape s1, Shape s2) {
    
    // clean things up initially
    path.reset();
    
    // intersect the first segment with s1
    Point2D
      a = calculateProjection(points[1], points[0], points[0], s1),
      b = calculateProjection(points[points.length-2], points[points.length-1], points[points.length-1], s2);
    
    // add the points to this path
    path.moveTo(a);
    for (int i=1;i<points.length-1;i++) {
      path.lineTo(points[i]);
    }
    path.lineTo(b);
    
    // done
    return path;
  }

  /**
   * @see #update(Path, Point2D, Shape, Point2D, Shape, int, boolean)
   */
  public static Path update(Path path, Point2D p1, Shape s1, Point2D p2, Shape s2) {
    return update(path,p1,s1,p2,s2,0,false);
  }

  /**
   * Updates given path with a line between given points between two shapes
   * @param p1 the starting point
   * @param s1 the shape sitting at p1
   * @param p2 the ending point
   * @param s2 the shape sitting at p2
   * @param i the sequential number of this path as one of the paths between p1 and p2
   * @param reversed whether this path follows the general direction of the other paths between p1 and p2
   */  
  public static Path update(Path path, Point2D p1, Shape s1, Point2D p2, Shape s2, int i, boolean reversed) {
    
    // clean things up initially
    path.reset();
    
    // A loop for p1==p2
    if (p1.equals(p2)) {
      
      Rectangle2D bounds = s1.getBounds2D();

      double 
        w = bounds.getMaxX()+10,
        h = bounds.getMaxY()+10;

      Point2D
        a = p1,
        b = new Point2D.Double(a.getX()+w, a.getY()  ),
        c = new Point2D.Double(a.getX()+w, a.getY()+h),
        d = new Point2D.Double(a.getX()  , a.getY()+h);
        
      update(path,new Point2D[]{a,b,c,d,a}, s1, s1);
      
      return path;
    }

    // Parallels for i>1
    if (i>0) {
      
      i++;

      double lastAngle = Geometry.getAngle(p1,p2);

      double RIGHTANGLE = 2*Math.PI/4;
      
      double 
        c     = ((i&1)==0?1:-1)*(i>>1)*6,
        alpha = lastAngle + (reversed?RIGHTANGLE:-RIGHTANGLE),
        dx    = Math.cos(alpha)*c,
        dy    = Math.sin(alpha)*c;

      Point2D
        a = new Point2D.Double(p1.getX() + dx, p1.getY() + dy ),
        b = new Point2D.Double(p2.getX() + dx, p2.getY() + dy );

      path.moveTo(calculateProjection(b, a, p1, s1));
      path.lineTo(calculateProjection(a, b, p2, s2));
      
      return path;        
    }
    
    // A simple line
    path.moveTo(calculateProjection(p2, p1, p1, s1));
    path.lineTo(calculateProjection(p1, p2, p2, s2));
   
    // done
    return path; 
  }

  /**
   * Calculates the endpoint of a projection on a Shape
   * @param p1 the start of the projection
   * @param p2 the end of the projection
   * @param p3 the position of the shape
   * @param s the shape
   */
  private static Point2D calculateProjection(Point2D p1, Point2D p2, Point2D p3, Shape s) {
    
    // intersect the projection start-end with the shape    
    if (s!=null) {
      Point2D p = Geometry.getClosestIntersection(
        p1, p1, p2,
        s.getPathIterator(AffineTransform.getTranslateInstance(p3.getX(), p3.getY()))
      );
      if (p!=null) return p;
    }

    // no intersections -> projection doesn't stop
    return p2;
  }


}
