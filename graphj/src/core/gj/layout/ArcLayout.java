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

import gj.awt.geom.Geometry;
import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Node;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A simplified layout for arcs */
public class ArcLayout {

  /**
   * Updates a path of an arc
   */
  public Path layout(Arc arc) {
    return layout(arc.getPath(), arc.getStart(), arc.getEnd());
  }

  /**
   * Updates a simple path between two nodes
   */
  public Path layout(Path path, Node start, Node end) {
    return layout(path,start.getPosition(),start.getShape(),end.getPosition(),end.getShape());
  }

  /**
   * Updates given path with a line going through points between two shapes
   * @param path the path to update
   * @param points a sequence of points describing the path
   * @param s1 shape positioned at the first point
   * @param s2 shape positioned at the last point
   */  
  public Path layout(Path path, Point2D[] points, Shape s1, Shape s2) {
    
    // clean things up initially
    path.reset();
    
    // intersect the first segment with s1
    Point2D
      a = getIntersection(points[1], points[0], points[0], s1),
      b = getIntersection(points[points.length-2], points[points.length-1], points[points.length-1], s2);
    
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
   * Updates given path with a line between given points between two shapes
   * @param p1 the starting point
   * @param s1 the shape sitting at p1
   * @param p2 the ending point
   * @param s2 the shape sitting at p2
   */
  public Path layout(Path path, Point2D p1, Shape s1, Point2D p2, Shape s2) {
    
    // clean things up initially
    path.reset();
    
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
        
      layout(path,new Point2D[]{a,b,c,d,a}, s1, s1);
      
      return path;
    }

    // A simple line
    path.moveTo(getIntersection(p2, p1, p1, s1));
    path.lineTo(getIntersection(p1, p2, p2, s2));
   
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
  public Point2D getIntersection(Point2D p1, Point2D p2, Point2D p3, Shape s) {
    
    // intersect the projection start-end with the shape    
    if (s!=null) {
      Point2D p = Geometry.getClosestIntersection(
        p1, 
        p1, p2,
        s.getPathIterator(AffineTransform.getTranslateInstance(p3.getX(), p3.getY()))
      );
      if (p!=null) return p;
    }

    // no intersections -> projection doesn't stop
    return p2;
  }

} //ArcLayout
