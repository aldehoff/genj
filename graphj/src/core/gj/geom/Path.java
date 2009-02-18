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

import static gj.geom.Geometry.*;

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
public class Path implements Shape {
  
  private final static Logger LOG = Logger.getLogger("genj.geom");

  /** a general path we keep */
  private GeneralPath gp = new GeneralPath();
  
  /** the angles we keep */
  private double firstAngle = Double.NaN;
  private double lastAngle = Double.NaN;
  
  /** the lastPoint we keep */
  private Point2D.Double firstPoint = null;
  private Point2D.Double lastPoint = null;
  private boolean isInverted = false;
  
  /**
   * Constructor
   */
  public Path() {
  }
  
  /**
   * Constructor
   */
  public Path(Path that, AffineTransform at) {
    copy(that, at);
  }
  
  private void copy(Path that, AffineTransform at) {
    this.gp = new GeneralPath(that.gp);
    if (at!=null)
      this.gp.transform(at);
    this.firstAngle = that.firstAngle;
    this.lastAngle = that.lastAngle;
    this.firstPoint = that.firstPoint;
    this.lastPoint = that.lastPoint;
  }
  
  /**
   * Constructor
   */
  public Path(Shape that) {
    
    if (that instanceof Path) {
      copy((Path)that, null);
      return;
    }
    
    ShapeHelper.iterateShape(that, new PathConsumer() {

      public boolean consumeCubicCurve(Point2D start, Point2D ctrl1, Point2D ctrl2, Point2D end) {
        if (firstPoint==null)
          start(start);
        else if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        curveTo(ctrl1, ctrl2, end);
        return true;
      }

      public boolean consumeLine(Point2D start, Point2D end) {
        if (firstPoint==null)
          start(start);
        else if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        lineTo(end);
        return true;
      }

      public boolean consumeQuadCurve(Point2D start, Point2D ctrl, Point2D end) {
        if (firstPoint==null)
          start(start);
        else if (!lastPoint.equals(start))
          throw new IllegalArgumentException("gap in path between "+lastPoint+" and "+start);
        quadTo(ctrl, end);
        return true;
      }
      
      
    });
    
  }
  
  /**
   * Invert
   */
  public synchronized void setInverted() {
    Point2D.Double p = lastPoint; lastPoint = firstPoint; firstPoint = p;
    double a = lastAngle; lastAngle = firstAngle + HALF_RADIAN; firstAngle = a + HALF_RADIAN;
    isInverted = !isInverted;
  }
  
  public boolean isInverted() {
    return isInverted;
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
    if (firstPoint!=null)
      throw new IllegalArgumentException("start twice");

    // setup move
    gp.moveTo((float)p.getX(), (float)p.getY());
    
    // remember 'first/last' point
    firstPoint = new Point2D.Double(p.getX(), p.getY());
    lastPoint = new Point2D.Double(p.getX(), p.getY());

    // done
    return this;
  }
  
  /**
   * check for continuation
   */
  private void checkContinue() {
    if (firstPoint==null)
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
      firstAngle = Geometry.getAngle(firstPoint, p);
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
      firstAngle = Geometry.getAngle(firstPoint, c);
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
      firstAngle = Geometry.getAngle(firstPoint, c1);
    // do the curve
    gp.curveTo((float)c1.getX(), (float)c1.getY(), (float)c2.getX(), (float)c2.getY(), (float)p.getX(), (float)p.getY());
    // remember closing angle
    lastAngle = Geometry.getAngle(c2,p);
    // remember position
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }
  

  /**
   * Append a path element following the circle of radius around center for arc fromRadian toRadian
   * @param center center of circle
   * @param radius radius of circle
   * @param fromRadian radian to start
   * @param toRadian radian to end
   */
  public void arcTo(Point2D center, double radius, double fromRadian, double toRadian) {

    // easy case
    if (fromRadian==toRadian)
      return;
    
    // make sure no cubic curve spans more than a quarter radian
    double radians = toRadian-fromRadian;
    int segments = (int)Math.ceil(Math.abs(radians/QUARTER_RADIAN));
    Point2D from = getPoint(center, fromRadian, radius);
    for (int s=1;s<=segments;s++) {
      
      Point2D to = getPoint(center, fromRadian+s*(radians/segments), radius);
      
      // for very close radians the control point calculation won't work
      if (from.distance(to)<1) {
        lineTo(to);
      } else {
        // first intersect lines perpendicular to [center>p1] & [center>p3] (negative reciprocals)
        Point2D i = getLineIntersection(
          from, new Point2D.Double( from.getX() - (from.getY()-center.getY()), from.getY() + (from.getX()-center.getX()) ), 
          to, new Point2D.Double( to.getX() - (to.getY()-center.getY()), to.getY() + (to.getX()-center.getX()) )
          );
        
        // calculate control points half way [p2>i] & [p3>i]
        double kappa = 0.5522847498;//0.5522847498307933984022516322796;
        Point2D c1 = new Point2D.Double( from.getX() + (i.getX()-from.getX())*kappa , from.getY() + (i.getY()-from.getY())*kappa );
        Point2D c2 = new Point2D.Double( to.getX() + (i.getX()-to.getX())*kappa , to.getY() + (i.getY()-to.getY())*kappa );
        curveTo(c1, c2, to);
      }
      
      // next
      from = to;
    }
    
    // done
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
