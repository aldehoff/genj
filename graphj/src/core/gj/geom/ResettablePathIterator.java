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
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * A PathIterator that an be reset
 */
public class ResettablePathIterator implements PathIterator {
  
  /** a wrapped shape */
  private Shape shape;
  
  /** a wrapped transformation */
  private AffineTransform transform;
  
  /** current path iterator */
  private PathIterator wrapped;
  
  /**
   * Constructor
   */
  public ResettablePathIterator(Shape shape, AffineTransform transform) {
    this.shape = shape;
    this.transform = transform;
    
    reset();
  }

  /**
   * Constructor
   */
  public ResettablePathIterator(Shape shape, Point2D pos) {
    this(shape, AffineTransform.getTranslateInstance(pos.getX(), pos.getY()));
  }

  /**
   * Constructor
   */
  public ResettablePathIterator(Shape shape) {
    this(shape, (AffineTransform)null);
  }
  
  /**
   * Reset the path iterator
   */
  public void reset() {
    wrapped = shape.getPathIterator(transform);
  }
  
  /**
   * @see java.awt.geom.PathIterator#getWindingRule()
   */
  public int getWindingRule() {
    return wrapped.getWindingRule();
  }

  /**
   * @see java.awt.geom.PathIterator#next()
   */
  public void next() {
    wrapped.next();
  }

  /**
   * @see java.awt.geom.PathIterator#isDone()
   */
  public boolean isDone() {
    return wrapped.isDone();
  }

  /**
   * @see java.awt.geom.PathIterator#currentSegment(double[])
   */
  public int currentSegment(double[] coords) {
    return wrapped.currentSegment(coords);
  }

  /**
   * @see java.awt.geom.PathIterator#currentSegment(float[])
   */
  public int currentSegment(float[] coords) {
    return wrapped.currentSegment(coords);
  }

} //ResettablePathIterator
