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
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * Missing shape functionality from the geom.* stuff
 */
public class ShapeHelper implements PathIteratorKnowHow {
  
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
  public static void iterateShape(Shape shape, SegmentConsumer consumer) {
    iterateShape(shape.getPathIterator(null), consumer);
  }
  public static void iterateShape(PathIterator iterator, SegmentConsumer consumer) {
    
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
    
} //ShapeHelper
