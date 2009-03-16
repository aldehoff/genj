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

import static gj.geom.PathIteratorKnowHow.SEG_CLOSE;
import static gj.geom.PathIteratorKnowHow.SEG_CUBICTO;
import static gj.geom.PathIteratorKnowHow.SEG_LINETO;
import static gj.geom.PathIteratorKnowHow.SEG_MOVETO;
import static gj.geom.PathIteratorKnowHow.SEG_QUADTO;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Missing shape functionality from the geom.* stuff
 */
public class ShapeHelper {
  
  /** 
   * the maximum distance the line segments used to approximate 
   * the curved segments are allowed to deviate from its control points
   * @see FlatteningPathIterator#FlatteningPathIterator(PathIterator, double, int)
   */
  private static double defaultFlatness = 0.5;
  
  /**
   * Change default flatness
   * @see ShapeHelper#defaultFlatness
   */
  public void setDefaultFlatness(double flatness) {
    defaultFlatness = Math.min(flatness, defaultFlatness);
  }

  /**
   * Creates a shape for given path. The path is constructed as such
   * <ul>
   *  <li>SEG_MOVETO, p1.x, p2.x
   *  <li>SEG_LINETO, pi.x, pi.x
   * </ul>
   */
  public static Shape createShape(Point2D ... points) {
    if (points.length<2)
      throw new IllegalArgumentException("Need minimum of 2 points for shape");
    
    GeneralPath result = new GeneralPath();
    result.moveTo( (float)points[0].getX(), (float)points[0].getY());
    for (int i = 1; i < points.length; i++) {
      result.lineTo( (float)points[i].getX(), (float)points[i].getY());
    }
    
    return result;
  }
  
  /**
   * Creates a shape for given path and offset. The path is
   * constructed according to the array of double-values:
   * <ul>
   *  <li>SEG_MOVETO, x, y
   *  <li>SEG_LINETO, x, y
   *  <li>SEG_QUADTO, c1x, c1y, x, y
   *  <li>SEG_CUBICTO, c1x, c1y, c2x, c2y, x, y
   *  <li>SEG_CLOSE
   * </ul>
   * 
   * @param x offset applied to all coordinates in values
   * @param y offset applied to all coordinates in values
   * @param sx scaling applied to all coordinates
   * @param sy scaling applied to all coordinates
   * @param values array describing shape 
   *         (segtype [ [, cx1, xy1 [, cx2, cy2] ] , x, y ])*
   */
  public static Shape createShape(double x, double y, double sx, double sy, double[] values) {
    
    // This is the result we're creating
    GeneralPath result = new GeneralPath();

    // Looping gather float(!) values      
    for (int i=0;i<values.length;) {
      double type = values[i++];
      if (type==-1) break;
      switch ((int)type) {
        case SEG_MOVETO:
          result.moveTo ((float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy)); break;
        case SEG_LINETO:
          result.lineTo ((float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy)); break;
        case SEG_QUADTO:
          result.quadTo ((float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy),(float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy)); break;
        case SEG_CUBICTO:
          result.curveTo((float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy),(float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy),(float)((values[i++]-x)*sx),(float)((values[i++]-y)*sy)); break;
        case SEG_CLOSE:
          result.closePath();
      }        
    }
    
    // Done
    return result;        
  }

  /**
   * Applies a PathConsumer to given Path
   */
  public static void iterateShape(Shape shape, Point2D pos, FlattenedPathConsumer consumer) {
    iterateShape(shape.getPathIterator(AffineTransform.getTranslateInstance(pos.getX(), pos.getY())), consumer);
  }
  public static void iterateShape(Shape shape, FlattenedPathConsumer consumer) {
    iterateShape(shape.getPathIterator(null), consumer);
  }
  public static void iterateShape(PathIterator iterator, final FlattenedPathConsumer consumer) {
    iterateShape(new FlatteningPathIterator(iterator, defaultFlatness), 
      new PathConsumer() {
        public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end) {
          throw new IllegalStateException("unexpected cubic curve");
        }
        public boolean consumeLine(Point2D start, Point2D end) {
          return consumer.consumeLine(start, end);
        }
        public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end) {
          throw new IllegalStateException("unexpected quad curve");
        }
      }
    );
  }
  public static void iterateShape(Shape shape, PathConsumer consumer) {
    iterateShape(shape.getPathIterator(null), consumer);
  }
  public static void iterateShape(PathIterator iterator, PathConsumer consumer) {

    // Loop through the path
    double[] segment = new double[6];
    Point2D lastPosition = new Point2D.Double();
    Point2D nextPosition = new Point2D.Double();
    Point2D ctrl1Position = new Point2D.Double();
    Point2D ctrl2Position = new Point2D.Double();
    Point2D movePosition = new Point2D.Double();
  
    boolean goon = true;      
    while (!iterator.isDone()) {
      // .. get the current segment
      switch (iterator.currentSegment(segment)) {
        case (PathIterator.SEG_MOVETO) :
          nextPosition.setLocation(segment[0],segment[1]);
          movePosition.setLocation(nextPosition);
          break;
        case (PathIterator.SEG_LINETO) :
          nextPosition.setLocation(segment[0], segment[1]);
          goon = consumer.consumeLine(lastPosition, nextPosition);
          break;
        case (PathIterator.SEG_CLOSE) :
          if (movePosition.equals(lastPosition))
            break;
          goon = consumer.consumeLine(lastPosition, movePosition);
          break;
        case (PathIterator.SEG_QUADTO) :
          ctrl1Position.setLocation(segment[0],segment[1]);
          nextPosition.setLocation(segment[2],segment[3]);
          goon = consumer.consumeQuadCurve(lastPosition, ctrl1Position, nextPosition);
          break;
        case (PathIterator.SEG_CUBICTO) :
            ctrl1Position.setLocation(segment[0],segment[1]);
            ctrl2Position.setLocation(segment[2],segment[3]);
            nextPosition.setLocation(segment[4],segment[5]);
            goon = consumer.consumeCubicCurve(lastPosition, ctrl1Position, ctrl2Position, nextPosition);
          break;
      }
      
      // .. continue?
      if (!goon) {
        break;
      }
      
      // .. remember 'new' last position
      lastPosition.setLocation(nextPosition);
      
      // .. next
      iterator.next();
    }
    
    // Done
  }  

  /**
   * Calculate a scaled shape
   */
  public static Shape createShape(Shape shape, double scale, Point2D origin) {
    GeneralPath gp = new GeneralPath(shape); 
    if (origin!=null)
      gp.transform(AffineTransform.getTranslateInstance(origin.getX(),origin.getY()));
    gp.transform(AffineTransform.getScaleInstance(scale,scale));
    return gp;
  }
  
  /**
   * Transform a shape to flattened points
   */
  public static List<Point2D> getPoints(Shape shape, Point2D pos) {
    return getPoints(shape.getPathIterator(AffineTransform.getTranslateInstance(pos.getX(), pos.getY())));
  }
  
  /**
   * Transform a shape to flattened points
   */
  public static List<Point2D> getPoints(PathIterator shape) {
    
    final List<Point2D> result = new ArrayList<Point2D>();
    
    iterateShape(shape, new FlattenedPathConsumer() {
      public boolean consumeLine(Point2D start, Point2D end) {
        if (result.isEmpty() || !result.get(result.size()-1).equals(start))
          result.add(new Point2D.Double(start.getX(), start.getY()));
        // continue
        return true;
      }
    });
    
    return result;
  }
    
} //ShapeHelper
