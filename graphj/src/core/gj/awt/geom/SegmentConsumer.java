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

import java.awt.geom.Point2D;

/**
 * Abstract base-type to an implementation that knows how to handle
 * line-segments line,quad-curve,cubic-curve
 */
public abstract class SegmentConsumer {

  /**
   * Callback - line
   */
  public boolean consumeLine(Point2D start, Point2D end) {
    return true;
  }
  
  /**
   * Callback - quad curve
   */
  public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end) {
    return true;
  }
  
  /**
   * Callback - cubic curve
   */
  public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end) {
    return true;
  }

}
