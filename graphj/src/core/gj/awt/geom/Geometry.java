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
package gj.awt.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * Missing mathematical functions from the geom.* stuff
 */
public class Geometry {

  /** 
   * the maximum distance that the line segments used to approximate 
   * the curved segments are allowed to deviate from any point on the
   * original curve 
   */
  private static double DEFAULT_FLATNESS = 4;
  
  /**
   * Calculates the distance of a line and a point
   * @param lineStart line's start point
   * @param lineEnd line's end point
   * @param point the point
   */
  public static double getDistance(Point2D lineStart, Point2D lineEnd, Point2D point) {
    return Line2D.ptLineDist(lineStart.getX(), lineStart.getY(), lineEnd.getX(), lineEnd.getY(), point.getX(), point.getY());
  }
  
  /**
   * Calculates the distance of a line and a point
   * @param lineStartX,lineStartY line's start point
   * @param lineEndX,lineEndY line's end point
   * @param pointX,pointY the point
   */
  public static double getDistance(double lineStartX, double lineStartY, double lineEndX, double lineEndY, double pointX, double pointY) {
    return Line2D.ptLineDist(lineStartX, lineStartY, lineEndX, lineEndY, pointX, pointY);
  }
  
  /**
   * Calculates the angle between two vectors
   * <pre>
   *  (a1,a2) -> (b1,b2) -> atan( dy / dx )
   * 
   *   where dx = b1-a1
   *         dy = b2-a2
   * 
   *  (* taking the quadrant into consideration)
   * </pre>
   */
  public static double getAngle(Point2D vectorA, Point2D vectorB) {
    return Math.atan2( (vectorB.getY()-vectorA.getY()), (vectorB.getX()-vectorA.getX()) );
  }
  
  /**
   * Calculate the cross-product of two vectors. The cross-product
   * of two vectors points "upwards" when the rotation (using the
   * shorter way) which "twist" the first vector into the second 
   * one is a left rotation, otherwise it points "downwards". The 
   * length of the vector corresponds to the area of the 
   * parallelogram spanned by the vectors.
   * 
   * <pre>
   *   (a1,a2) -> (b1,b2) -> a1*b2 - b1*a2
   * </pre>
   */
  public static double getCrossProduct(Point2D vectorA, Point2D vectorB) {
    return getCrossProduct( vectorA.getX(), vectorA.getY(), vectorB.getX(), vectorB.getY());
  }
  
  /**
   * Calculate the cross-product of two vectors. The cross-product
   * of two vectors points "upwards" when the rotation (using the
   * shorter way) which "twist" the first vector into the second 
   * one is a left rotation, otherwise it points "downwards". The 
   * length of the vector corresponds to the area of the 
   * parallelogram spanned by the vectors.
   * 
   * <pre>
   *   (a1,a2) -> (b1,b2) -> a1*b2 - b1*a2
   * </pre>
   */
  public static double getCrossProduct(double vectorAx, double vectorAy, double vectorBx, double vectorBy) {
    return ( vectorAx*vectorBy ) - ( vectorBx*vectorAy );
  }
  
  /**
   * Returns the delta of two vectors.
   * <pre>
   *   (a1,a2) -> (b1,b2) -> (b1-a1, b2-a2)
   * </pre>
   */
  public static Point2D getDelta(Point2D vectorA, Point2D vectorB) {
    return new Point2D.Double(vectorB.getX()-vectorA.getX(), vectorB.getY()-vectorA.getY());
  }
  
  /**
   * The length of a vector
   * <pre>
   *  c^2=x^2+y^2 => c=sqt(x^2+y^2)
   * </pre>
   */    
  public static double getLength(double x, double y) {
    return Math.sqrt(x*x + y*y);
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param proximity fix for 'closest'
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param shape the shape described as a PathIterator
   * @return closest intersection or fix 
   */
  public static Point2D getClosestIntersection(Point2D fix, Point2D lineStart, Point2D lineEnd, PathIterator shape) {
    return new OpGetClosestIntersection(fix, lineStart, lineEnd, shape).getResult();
  }
  
  /**
   * Calculates the endpoint of a projection on a Shape
   * @param p1 the start of the projection
   * @param p2 the end of the projection
   * @param p3 the position of the shape
   * @param s the shape
   */
  public static Point2D getIntersection(Point2D p1, Point2D p2, Point2D p3, Shape s) {
    
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
  
  /**
   * Operation - intersect and find closest
   */
  private static class OpGetClosestIntersection extends SegmentConsumer {
    
    /** the closests intersection */
    private Point2D result = null;
    
    /** it's distance */
    private double distance = Double.MAX_VALUE;
    
    /** our criterias */
    private Point2D origin, lineStart, lineEnd;
    
    /**
     * Constructor
     */
    protected OpGetClosestIntersection(Point2D origin, Point2D lineStart, Point2D lineEnd, PathIterator shape) {
      // remember
      this.origin    = origin;
      this.lineStart = lineStart;
      this.lineEnd   = lineEnd;
      // iterate over line segments in shape
      PathIterator it = new FlatteningPathIterator(shape, DEFAULT_FLATNESS);
      ShapeHelper.iteratePath(it, this);
      // done
    }
    
    /**
     * Accessor - result
     */
    protected Point2D getResult() {
      return result;
    }
    
    /**
     * Callback
     */
    public boolean consumeLine(Point2D start, Point2D end) {
      Point2D p = getIntersection(lineStart, lineEnd, start, end);
      if (p!=null) {
        double d = p.distance(origin);
        if (d<distance) {
          distance = d;
          result = p;
        }
      }
      return true;
    }
    
  } //Op

  /**
   * Tests for intersection of two lines (segments of finite length)
   * @param aStart+aEnd line segment describing line A
   * @param bStart+bEnd line segement describing line B
   */
  public static boolean testIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // To test for crossing we hold a line and check both endpoints' being left/right of it
    //
    //        |         b
    //       /|        /b\
    //      / |       / b \
    //    aaaaaaaaa ----b----
    //      \ |         b
    //       \|         b
    //        |         b 

    Point2D vectorA = getDelta(aStart, aEnd),   // a-a
            vector1 = getDelta(aStart, bStart), // a-b1
            vector2 = getDelta(aStart, bEnd);   // a-b2

    // .. cross-product is '-' for 'left' and '+' for right
    // so we hope for xp(aa,ab1) * x(aa,ab2) < 0 because
    //
    //   + * + = + 
    //   - * - = + 
    //   + * - = -
    //
    if (getCrossProduct(vectorA,vector1) * getCrossProduct(vectorA,vector2) >0) {
      return false;
    }
    
    // The same for the other line
    Point2D vectorB = getDelta(bStart, bEnd);   // b-b
            vector1 = getDelta(bStart, aStart); // b-a1
            vector2 = getDelta(bStart, aEnd);   // b-a2
        
    if (getCrossProduct(vectorB,vector1) * getCrossProduct(vectorB,vector2) >0) {
      return false;
    }
  
    // Yes, they do
    return true;  
  }
  
  /**
   * Calculates the intersecting point of two lines (segments of finite length)
   * @param aStart+aEnd line segment describing line A
   * @param bStart+bEnd line segement describing line B
   * @return either intersecting point or null if lines are parallel or line segments don't cross
   */
  public static Point2D getIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // Do they intersect at all?
    if (!testIntersection(aStart,aEnd,bStart,bEnd)) {
      return null;
    }
    
    // We calculate the direction vectors a, b and c
    //
    //     AS     b BE
    //      |\    |/
    //    c-|  \ /
    //      |  / \  a
    //      |/     \|
    //     BS        \
    //                AE
    //
    // Note equations for lines AS-AE, BS-BE, BS-AS
    //
    //  y = AS + s*v_a
    //  y = BS + t*v_b
    //  y = BS + u*v_c
    //
    double 
      v_ax = aEnd.getX() - aStart.getX(),
      v_ay = aEnd.getY() - aStart.getY(),
      v_bx = bEnd.getX() - bStart.getX(),
      v_by = bEnd.getY() - bStart.getY(),
      v_cx = bStart.getX() - aStart.getX(),
      v_cy = bStart.getY() - aStart.getY();

    // Then we calculate the cross-product between
    // vectors b/a and b/c
    //
    //  cp_ba = v_a x v_b
    //  cp_bc = v_b x v_c
    //
    double cp_ba = getCrossProduct(v_bx,v_by,v_ax,v_ay);
    double cp_bc = getCrossProduct(v_bx,v_by,v_cx,v_cy);
    
    // A zero x-prod means that the lines are either
    // parallel or have coinciding endpoints
    if (cp_ba==0) {
      return null;
    }
    
    // So our factor s for lines AS-AE is
    //
    // s = cp_bc/cp_ba
    //
    double s = cp_bc/cp_ba;
    
    // The result is defined by
    //
    //  AS + s * v_a
    //
    return new Point2D.Double(
      aStart.getX()+s*v_ax,
      aStart.getY()+s*v_ay
    );
    
  }

  /**
   * Helper that negates a Point2D (new instance returned)
   */
  public static Point2D getNegative(Point2D p) {
    return new Point2D.Double(-p.getX(), -p.getY());
  }    
}
