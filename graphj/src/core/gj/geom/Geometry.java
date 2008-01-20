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
package gj.geom;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Missing mathematical functions from the geom.* stuff
 */
public class Geometry {
  
  public final static double 
    ONE_RADIAN = 2 * Math.PI,
    QUARTER_RADIAN = ONE_RADIAN/4,
    HALF_RADIAN = ONE_RADIAN/2;

  /** 
   * the maximum distance that the line segments used to approximate 
   * the curved segments are allowed to deviate from any point on the
   * original curve 
   */
  private static double DEFAULT_FLATNESS = 4;
  
  /**
   * Calculate the radian for given degree
   */
  public static double getRadian(double degree) {
    return degree/360*Geometry.ONE_RADIAN;
  }

  /**
   * Calculates the closest point from a list of points
   */
  public static Point2D getClosest(Point2D point, List<Point2D> points) {
    if (points.size()==0)
      throw new IllegalArgumentException();
    // assume first
    Point2D result = (Point2D)points.get(0);
    double distance = result.distance(point);
    // loop over rest
    for (int i=1;i<points.size();i++) {
      Point2D p = (Point2D)points.get(i);
      double d = p.distance(point);
      if (d<distance) {
        result = p;
        distance = d;
      }
    }
    // done
    return result;
  }
  
  /**
   * Calculates the "maximum" that the given shape extends into the given direction
   */
  public static Point2D getMax(Shape shape, double axis) {
    return new OpGetMax(shape, axis).getResult();
  }
  
  /**
   * Operation - calc maximum of a shape extending into a direction 
   */
  private static class OpGetMax extends SegmentConsumer {
    
    private double sinaxis, cosaxis; 
    private double max = Double.NEGATIVE_INFINITY;
    private Point2D.Double result = new Point2D.Double();

    /**
     * Constructor
     */
    public OpGetMax(Shape shape, double axis) {
      sinaxis = Math.sin(axis);
      cosaxis = Math.cos(axis);
      
      ShapeHelper.iterateShape(new FlatteningPathIterator(shape.getPathIterator(null), DEFAULT_FLATNESS), this);
    }

    /**
     * Check points of a segment
     */
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      
      double delta = sinaxis * end.getX() - cosaxis * end.getY();
      if (delta>max) {
        max = delta;
        result.setLocation(end);
      }

      // continue
      return true;
    }
    
    /**
     * the result
     */
    Point2D getResult() {
      return max == Double.NEGATIVE_INFINITY ? (Point2D)null : result;
    }
  } //OpOriginOfTangent
  
  /**
   * Calculates the distance of two shapes along the given axis. 
   * For non-'parallel' shapes the result is Double.MAX_VALUE.
   * @param shape1 first shape
   * @param shape2 second shape
   * @param axis radian of axis (zero is north for vertical distance of two shapes)
   * @return distance
   */
  public static double getDistance(Shape shape1, Shape shape2, double axis) {
    return new OpShapeShapeDistance(shape1, shape2, axis).getResult();
  }
  
  /**
   * Operation - calculate distance of two shapes
   */
  private static class OpShapeShapeDistance extends SegmentConsumer {
    
    private double result = Double.POSITIVE_INFINITY;
    
    /** the axis vector we're measuring distance on */
    private double axis;
    private Point2D vector;

    /** the current shape we're intersecting against */
    private Shape intersectWith;
    
    private Line2D line;
    
    /**
     * Constructor
     */
    protected OpShapeShapeDistance(Shape shape1, Shape shape2, double axis) throws IllegalArgumentException {

      // calculate
      Rectangle2D area = getBounds(shape2, getBounds(shape1, null));
      double span = getLength(area.getWidth(), area.getHeight());
      
      // keep an axis vector
      vector = new Point2D.Double(
          Math.sin(axis)*span,
          -Math.cos(axis)*span
      );
      
      // iterate over shape1 intersecting lines along the axis with shape2
      intersectWith = shape2;
      this.axis = axis;
      ShapeHelper.iterateShape(new FlatteningPathIterator(shape1.getPathIterator(null), DEFAULT_FLATNESS), this);
      
      // iterate over shape2 intersecting lines along the axis with shape1
      intersectWith = shape1;
      this.axis = axis + Geometry.ONE_RADIAN/2;
      ShapeHelper.iterateShape(new FlatteningPathIterator(shape2.getPathIterator(null), DEFAULT_FLATNESS), this);
      
      // done
    }
    
    /**
     * The result
     */
    protected double getResult() {
      return result;
    }

    /**
     * only expecting lines to consume
     */
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      
      // create a line along axis going through 'end' 
      Point2D
        a = new Point2D.Double(end.getX()+vector.getX(), end.getY()+vector.getY()),
        b = new Point2D.Double(end.getX()-vector.getX(), end.getY()-vector.getY());

      // intersect line (a,b) with shape
      ArrayList<Point2D> is = new ArrayList<Point2D>(10);
      getIntersections(a, b, intersectWith, is );
      
      // calculate smallest distance
      for (Point2D i : is) 
        result = Math.min(result, Math.sin(axis)*(i.getX()-end.getX()) - Math.cos(axis)*(i.getY()-end.getY()));
      
      // continue
      return true;
    }
  } //OpShapeShapeDistance
  
  /**
   * Calculates the distance of a line (infinite) and a shape
   * @param lineStart line's start point
   * @param lineEnd line's end point
   * @param shape the shape
   */
  public static double getDistance(Point2D lineStart, Point2D lineEnd, PathIterator shape) {
    return new OpLineShapeDistance(lineStart, lineEnd, shape).getResult();
  }
  
  /**
   * Operation - calculate distance of line and shape
   */
  private static class OpLineShapeDistance extends SegmentConsumer {
    
    /** resulting distance */
    private double delta = Double.MAX_VALUE;
    
    /** our criterias */
    private double lineStartX, lineStartY, lineEndX, lineEndY;
    
    /**
     * Constructor
     */
    protected OpLineShapeDistance(Point2D lineStart, Point2D lineEnd, PathIterator shape) {
      // remember
      lineStartX = lineStart.getX();
      lineStartY = lineStart.getY();
      lineEndX   = lineEnd.getX();
      lineEndY   = lineEnd.getY();
      // iterate over line segments in shape
      PathIterator it = new FlatteningPathIterator(shape, DEFAULT_FLATNESS);
      ShapeHelper.iterateShape(it, this);
      // done
    }
    
    /**
     * Accessor - result
     */
    protected double getResult() {
      return delta;
    }
    
    /**
     * Callback - since we're using a flattening path iterator
     * only lines have to be consumed
     */
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      // calculate distance of line segment's start/end
      delta = Math.min(delta, Line2D.ptLineDist(lineStartX, lineStartY, lineEndX, lineEndY, start.getX(), start.getY()));
      delta = Math.min(delta, Line2D.ptLineDist(lineStartX, lineStartY, lineEndX, lineEndY, end  .getX(), end  .getY()));
      // continue
      return true;
    }
    
  } //OpLineShapeDistance
  
  /**
   * Calculates the minimum distance of a point and line segments in shape
   * @return distance or 0 for containment
   */
  public static double getDistance(Point2D point, PathIterator shape) {
    return new OpPointShapeDistance(point, shape).getResult();
  }
  
  /**
   * Operation - calculate distance of line and shape
   */
  private static class OpPointShapeDistance extends SegmentConsumer {
    private double result = Double.MAX_VALUE;
    private Point2D point;
    
    /**
     * Constructor
     */
    protected OpPointShapeDistance(Point2D point, PathIterator shape) {
      this.point = point;
      ShapeHelper.iterateShape(new FlatteningPathIterator(shape, DEFAULT_FLATNESS), this);
    }
    
    /**
     * The result
     */
    protected double getResult() {
      return result;
    }
    
    /**
     * @see gj.geom.SegmentConsumer#consumeLine(java.awt.geom.Point2D, java.awt.geom.Point2D)
     */
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      
      double distance = Line2D.ptSegDist(start.getX(), start.getY(), end.getX(), end.getY(), point.getX(), point.getY());
      result = Math.min(result, distance);
      return result!=0;
    }
    
  } //OpPointShapeDistance
  
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
    return (  Math.atan2( (vectorB.getY()-vectorA.getY()), (vectorB.getX()-vectorA.getX()) ) - 2*Math.PI/4) % (2*Math.PI);
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
   * Calculate the midpoint between two other points
   */
  public static Point2D getPoint(Point2D a, Point2D b) {
    return new Point2D.Double( (a.getX()+b.getX())/2, (a.getY()+b.getY())/2 );    
  }
  
  public static Point2D getPoint(Point2D origin, double radian, double distance) {
    return new Point2D.Double( origin.getX() + Math.sin(radian)*distance, origin.getY() - Math.cos(radian) * distance);
  }
  
  /**
   * substracts a from b
   * <pre>
   *   (a1,a2) -> (b1,b2) -> (b1-a1, b2-a2)
   * </pre>
   */
  public static Point2D getDelta(Point2D vectorA, Point2D vectorB) {
    return new Point2D.Double(vectorB.getX()-vectorA.getX(), vectorB.getY()-vectorA.getY());
  }
  
  /**
   * adds a to b
   * <pre>
   *   (a1,a2) -> (b1,b2) -> (a1+b1, a2+b2)
   * </pre>
   */
  public static Point2D getSum(Point2D vectorA, Point2D vectorB) {
    return new Point2D.Double(vectorB.getX()+vectorA.getX(), vectorB.getY()+vectorA.getY());
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
  
  public static double getLength(Point2D v) {
    return getLength(v.getX(), v.getY());
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param shape the shape
   */
  public static void getIntersections(Point2D lineStart, Point2D lineEnd, Shape shape, Collection<Point2D> result) {
    new OpLineShapeIntersections(lineStart, lineEnd, shape.getPathIterator(null), result);
  }
  
  /**
   * Calculates the intersecting points of a line and a shape
   * @param lineStart start of line
   * @param lineEnd end of line
   * @param shapePos position of shape
   * @param shape the shape 
   */
  public static void getIntersections(Point2D lineStart, Point2D lineEnd, Point2D shapePos, Shape shape, Collection<Point2D> result) {
    new OpLineShapeIntersections(lineStart, lineEnd, shape.getPathIterator(AffineTransform.getTranslateInstance(shapePos.getX(), shapePos.getY())), result);
  }
  
  /**
   * Operation - intersect line and shape
   */
  private static class OpLineShapeIntersections extends SegmentConsumer {
    
    /** the intersections */
    private Collection<Point2D> result;
    
    /** it's distance */
    private double distance = Double.MAX_VALUE;
    
    /** our criterias */
    private Point2D lineStart, lineEnd;
    
    /**
     * Constructor
     */
    protected OpLineShapeIntersections(Point2D lineStart, Point2D lineEnd, PathIterator shape, Collection<Point2D> result) {
      // remember
      this.lineStart = lineStart;
      this.lineEnd   = lineEnd;
      this.result    = result;
      // iterate over line segments in shape
      PathIterator it = new FlatteningPathIterator(shape, DEFAULT_FLATNESS);
      ShapeHelper.iterateShape(it, this);
      // done
    }
    
    /**
     * Callback - since we're using a flattening path iterator
     * only lines have to be consumed
     */
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      Point2D p = getIntersection(lineStart, lineEnd, start, end);
      if (p!=null) 
        result.add(p);
      return true;
    }
    
  } //Op

  /**
   * Tests for intersection of two lines (segments of finite length)
   * @param aStart line segment describing line A
   * @param aEnd line segment describing line A
   * @param bStart line segment describing line B
   * @param bEnd line segment describing line B
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
   * Calculates the intersecting point of two lines
   * @param aStart line segment describing line A
   * @param aEnd line segment describing line A
   * @param bStart line segment describing line B
   * @param bEnd line segment describing line B
   * @return either intersecting point or null if lines are parallel or line segments don't cross
   */
  public static Point2D getIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // finite - do they intersect at all?
    if (!testIntersection(aStart,aEnd,bStart,bEnd)) 
      return null;
    
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
   * Calculate the intersection point of two infinite lines defined by respective point and angle
   */
  public static Point2D getIntersection(Point2D pointA, double radianA, Point2D pointB, double radianB) {
    return getLineIntersection(
        pointA, 
        new Point2D.Double(pointA.getX() + Math.sin(radianA), pointA.getY() - Math.cos(radianA)),
        pointB,
        new Point2D.Double(pointB.getX() + Math.sin(radianB), pointB.getY() - Math.cos(radianB))
    );
  }
  
  /**
   * Calculate the intersection point of two infinite lines defined by two points each
   */
  public static Point2D getLineIntersection(Point2D aStart, Point2D aEnd, Point2D bStart, Point2D bEnd) {
    
    // a1*x + b1*y + c1 = 0 is line 1
    double a1 = aEnd.getY() - aStart.getY();
    double b1 = aStart.getX() - aEnd.getX();
    double c1 = aEnd.getX()*aStart.getY() - aStart.getX()*aEnd.getY();  
    
    // a2*x + b2*y + c2 = 0 is line 2 
    double a2 = bEnd.getY()-bStart.getY();
    double b2 = bStart.getX()-bEnd.getX();
    double c2 = bEnd.getX()*bStart.getY() - bStart.getX()*bEnd.getY();

    double denom = a1*b2 - a2*b1;
    if (denom==0)
      return null;

    return new Point2D.Double( (b1*c2 - b2*c1)/denom , (a2*c1 - a1*c2)/denom);
  }

  /**
   * Calculate the 2D bounds of given iterator
   */
  public static Rectangle2D getBounds(Shape shape, Rectangle2D result) {
  	return new OpShapeBounds(shape, result).getResult();
  }

  /**
   * Operation - calculate bounds of path iterator
   */
  private static class OpShapeBounds extends SegmentConsumer {
    
    private Rectangle2D result;
    
    /**
     * Constructor
     */
    protected OpShapeBounds(Shape shape, Rectangle2D result) {
      this.result = result;
      ShapeHelper.iterateShape(shape.getPathIterator(null), this);
    }
    
    /**
     * Result
     */
    protected Rectangle2D getResult() {
      return result;
    }
    
    private void add(Point2D p) {
      if (result==null) {
        result = new Rectangle2D.Double(p.getX(),p.getY(),0,0);
      } else {
        result.add(p);
      }
    }
    
    @Override
    public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end) {
      add(start);
      add(ctrl1);
      add(ctrl2);
      add(end);
      return true;
    }
    @Override
    public boolean consumeLine(Point2D start, Point2D end) {
      add(start);
      add(end);
      return true;
    }
    @Override
    public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end) {
      add(start);
      add(ctrl);
      add(end);
      return true;
    }
  } //OpBounds

} //Geometry
