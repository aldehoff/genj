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
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * Missing shape functionality from the geom.* stuff
 */
public class ShapeHelper implements PathIteratorKnowHow {
  
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
   * @param x,y offset applied to all coordinates in values
   * @param sx,sy scaling applied to all coordinates
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
  public static void iteratePath(PathIterator iterator, SegmentConsumer consumer) {
    
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
          goon = consumer.consumeLine(lastPosition, nextPosition);
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

}
