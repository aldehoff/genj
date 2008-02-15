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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

/**
 * A 2D path - missing from the jawa.awt.geom.* stuff
 */
public class Path implements Shape, PathIteratorKnowHow {
  
  private final static Logger LOG = Logger.getLogger("genj.geom");

  /** a general path we keep */
  private GeneralPath gp = new GeneralPath();
  
  /** the angles we keep */
  private double firstAngle = Double.NaN;
  private double lastAngle = Double.NaN;
  
  /** the lastPoint we keep */
  private Point2D.Double lastPoint = new Point2D.Double();
  
  /** whether we've been started */
  private boolean isStarted = false;
  
  /**
   * Constructor
   */
  public Path() {
  }
  
  /**
   * Constructor
   */
  public Path(Path that, AffineTransform at) {
    this.gp = new GeneralPath(that.gp);
    this.gp.transform(at);
    this.firstAngle = that.firstAngle;
    this.lastAngle = that.lastAngle;
    this.lastPoint = that.lastPoint;
    this.isStarted = that.isStarted;
  }
  
  /**
   * Constructor
   */
  public Path(Shape that) {
    
    ShapeHelper.iterateShape(that, new PathConsumer() {

      public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end) {
        if (!isStarted)
          start(start);
        if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        curveTo(ctrl1, ctrl2, end);
        return true;
      }

      public boolean consumeLine(Point2D start, Point2D end) {
        if (!isStarted)
          start(start);
        if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        lineTo(end);
        return true;
      }

      public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end) {
        if (!isStarted)
          start(start);
        if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        quadTo(ctrl, end);
        return true;
      }
      
      
    });
    
  }
  
  /**
   * Accessor - lastPoint
   */
  public synchronized Point2D getLastPoint() {
    return new Point2D.Double(lastPoint.x, lastPoint.y);
  }
  
  /**
   * Accessor - lastAngle
   */
  public synchronized double getLastAngle() {
    return lastAngle;
  }

  /**
   * Translate by Point
   */
  public synchronized void translate(Point2D delta) {
    translate(delta.getX(), delta.getY());
  }

  /**
   * Translate by dx,dy
   */
  public synchronized void translate(double dx, double dy) {
    gp.transform(AffineTransform.getTranslateInstance(dx,dy));
    lastPoint.setLocation(
      lastPoint.getX()+dx,
      lastPoint.getY()+dy
    );
  }
  
  /**
   * start the path
   */
  public synchronized Path start(Point2D p) {
    
    // start this
    if (isStarted)
      throw new IllegalArgumentException("start twice");
    isStarted = true;

    // setup move
    gp.moveTo((float)p.getX(), (float)p.getY());
    
    // remember 'last' point
    lastPoint.setLocation(p.getX(), p.getY());

    // done
    return this;
  }
  
  /**
   * check for continuation
   */
  private void checkContinue() {
    if (!isStarted)
      throw new IllegalArgumentException("continue without start");
  }
  
  /**
   * @see java.awt.geom.GeneralPath#lineTo(float, float)
   */
  public synchronized Path lineTo(Point2D p) {
    // check 
    checkContinue();
    // add opening angle
    if (Double.isNaN(firstAngle))
      firstAngle = Geometry.getAngle(lastPoint, p);
    // do the line
    gp.lineTo((float)p.getX(), (float)p.getY());
    // remember closing angle & position
    lastAngle = Geometry.getAngle(lastPoint,p);
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }
  
  /**
   * @see java.awt.geom.GeneralPath#quadTo(float, float, float, float)
   */
  public synchronized Path quadTo(Point2D c, Point2D p) {
    // check
    checkContinue();
    // add opening angle
    if (Double.isNaN(firstAngle))
      firstAngle = Geometry.getAngle(lastPoint, c);
    // do the quad curve
    gp.quadTo((float)c.getX(), (float)c.getY(), (float)p.getX(), (float)p.getY());
    // remember closing angle & position
    lastAngle = Geometry.getAngle(c,p);
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }
  
  /**
   * @see java.awt.geom.GeneralPath#curveTo(float, float, float, float, float, float)
   */
  public synchronized Path curveTo(Point2D c1, Point2D c2, Point2D p) {
    // check
    checkContinue();
    // add opening angle
    if (Double.isNaN(firstAngle))
      firstAngle = Geometry.getAngle(lastPoint, c1);
    // do the curve
    gp.curveTo((float)c1.getX(), (float)c1.getY(), (float)c2.getX(), (float)c2.getY(), (float)p.getX(), (float)p.getY());
    // remember closing angle
    lastAngle = Geometry.getAngle(c2,p);
    // remember position
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }

  /**
   * @see java.awt.Shape#contains(double, double, double, double)
   */
  public boolean contains(double x, double y, double w, double h) {
    return gp.contains(x,y,w,h);
  }

  /**
   * @see java.awt.Shape#contains(double, double)
   */
  public boolean contains(double x, double y) {
    return gp.contains(x,y);
  }

  /**
   * @see java.awt.Shape#contains(Point2D)
   */
  public boolean contains(Point2D p) {
    return gp.contains(p);
  }

  /**
   * @see java.awt.Shape#contains(Rectangle2D)
   */
  public boolean contains(Rectangle2D r) {
    return gp.contains(r);
  }

  /**
   * @see java.awt.Shape#getBounds()
   */
  public Rectangle getBounds() {
    return gp.getBounds();
  }

  /**
   * @see java.awt.Shape#getBounds2D()
   */
  public Rectangle2D getBounds2D() {
    return gp.getBounds2D();
  }
  
  /**
   * @see java.awt.Shape#getPathIterator(AffineTransform, double)
   */
  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return gp.getPathIterator(at,flatness);
  }

  /**
   * @see java.awt.Shape#getPathIterator(AffineTransform)
   */
  public PathIterator getPathIterator(AffineTransform at) {
    return gp.getPathIterator(at);
  }

  /**
   * @see java.awt.Shape#intersects(double, double, double, double)
   */
  public boolean intersects(double x, double y, double w, double h) {
    return gp.intersects(x,y,w,h);
  }

  /**
   * @see java.awt.Shape#intersects(Rectangle2D)
   */
  public boolean intersects(Rectangle2D r) {
    return gp.intersects(r);
  }

}
