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
package gj.layout.tree;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * An Orientation
 */
public class Orientation {
  
  /** orientation's transformation */
  private AffineTransform transform, inverse;
  
  /**
   * Constructor 0-359
   */
  protected Orientation(double theta) {

    // transform degree 0-359 into 2*Math.PI
    theta = theta * 2*Math.PI / 360; 
       
    try {
      transform = AffineTransform.getRotateInstance(theta);
      inverse = transform.createInverse();
    } catch (NoninvertibleTransformException e) {
      // n/a
    }
  }

  /**
   * Returns the 2D bounds for a surface with lat/lon's
   */
  protected Rectangle getBounds(Contour c) {
    
    GeneralPath gp = new GeneralPath(new Rectangle(
      c.west, c.north, c.east-c.west, c.south-c.north
    ));
    
    gp.transform(transform);
    
    return gp.getBounds();
  }

  /**
   * Returns the surface with lat/lon's for given 2D bounds
   */
  protected Contour getContour(Rectangle2D r2d) {
    
    GeneralPath gp = new GeneralPath(r2d);
    gp.transform(inverse);
    Rectangle r = gp.getBounds(); 

    return new Contour(r.y,r.x,r.x+r.width,r.y+r.height);    
           
  }

  /**
   * @see gj.layout.tree.Orientation.tst#getPoint(int, int)
   */
  public Point getPoint(int lat, int lon) {
    Point result = new Point(lon, lat);
    transform.transform(result, result);
    return result;
  }
  
  /**
   * @see gj.layout.tree.Orientation.tst#getLatitude(java.awt.geom.Point2D)
   */
  public int getLatitude(Point2D p) {
    Point result = new Point();
    inverse.transform(p, result);
    return result.y;
  }

  /**
   * @see gj.layout.tree.Orientation.tst#getLongitude(java.awt.geom.Point2D)
   */
  public int getLongitude(Point2D p) {
    Point result = new Point();
    inverse.transform(p, result);
    return result.x;
  }

} //Orientation

