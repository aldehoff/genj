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

/**
 * A 2D path - missing from the jawa.awt.geom.* stuff
 */
public class Path implements Shape, PathIteratorKnowHow {

  /** an arrow-head pointing upwards */
  private final static Shape ARROW_HEAD = ShapeHelper.createShape(0,0,1,1,new double[]{
      ShapeHelper.SEG_MOVETO, 0, 0, 
      ShapeHelper.SEG_LINETO, -5, -7, 
      ShapeHelper.SEG_MOVETO,  5, -7, 
      ShapeHelper.SEG_LINETO, 0, 0
  });
  
  /** a general path we keep */
  private GeneralPath gp = new GeneralPath();
  
  /** the lastAngle we keep */
  private double lastAngle;
  
  /** the lastPoint we keep */
  private Point2D.Double lastPoint = new Point2D.Double();
  
  /** whether we've been started */
  private boolean isStarted = false;
  
  /** whether we've been ended */
  private boolean isEnded = false;
  
  /** whether we still need a start arrow */
  private boolean needsStartArrow = false;
  
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
  
//  /**
//   * Sets to given shape
//   */
//  public synchronized Path set(Shape shape) {
//    
//    // reset what we've got
//    gp.reset();
//
//    // look at shape
//    Point2D.Double p = new Point2D.Double();
//    PathIterator it = shape.getPathIterator(null);
//    float[] vals = new float[6];
//    while (!it.isDone()) {
//      
//      int type = it.currentSegment(vals);
//      int i = -1;
//      switch (type) {
//        case SEG_CLOSE:
//          throw new IllegalArgumentException("Path cannot be closed");
//        case SEG_CUBICTO:
//          gp.curveTo(vals[0],vals[1],vals[2],vals[3],vals[4],vals[5]); 
//          i=4;
//          break;
//        case SEG_LINETO:
//          gp.lineTo(vals[0],vals[1]); 
//          i=0;
//          break;
//        case SEG_MOVETO:
//          gp.moveTo(vals[0],vals[1]); 
//          i=0;
//          break;
//        case SEG_QUADTO:
//          gp.quadTo(vals[0],vals[1],vals[2],vals[3]); 
//          i=2;
//          break;
//      }
//
//      lastPoint.setLocation(p.getX(),p.getY());
//      p.setLocation(vals[i+0],vals[i+1]);
//      it.next();
//    }
//
//    // update last...
//    lastAngle = Geometry.getAngle(lastPoint,p);
//    lastPoint.setLocation(p.getX(), p.getY());
//
//    // done    
//    return this;
//  }
  
  /**
   * start the path
   */
  public synchronized Path start(boolean arrow, Point2D p) {
    
    // start this
    if (isStarted)
      throw new IllegalArgumentException("start twice");
    isStarted = true;

    // setup move
    gp.moveTo((float)p.getX(), (float)p.getY());
    
    // remember 'last' point
    lastPoint.setLocation(p.getX(), p.getY());

    // remember arrow
    needsStartArrow = arrow;

    // done
    return this;
  }
  
  /**
   * check for continuation
   */
  private void checkContinue() {
    if (!isStarted)
      throw new IllegalArgumentException("continue without start");
    if (isEnded)
      throw new IllegalArgumentException("continue after end");
  }
  
  /**
   * @see java.awt.geom.GeneralPath#lineTo(float, float)
   */
  public synchronized Path lineTo(Point2D p) {
    // check 
    checkContinue();
    // add opening arrrow
    if (needsStartArrow) {
      AffineTransform at = AffineTransform.getTranslateInstance(lastPoint.getX(), lastPoint.getY());
      at.rotate(Geometry.getAngle(lastPoint, p)+Math.PI);
      gp.append(ARROW_HEAD.getPathIterator(at), false);
      needsStartArrow = false;
    }
    // do the line
    gp.lineTo((float)p.getX(), (float)p.getY());
    // remember angle
    lastAngle = Geometry.getAngle(lastPoint,p);
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }
  
  /**
   * @see java.awt.geom.GeneralPath#quadTo(float, float, float, float)
   */
  public synchronized Path quadTo(Point2D c, Point2D p) {
    checkContinue();
    // FIXME add opening arrow
    gp.quadTo((float)c.getX(), (float)c.getY(), (float)p.getX(), (float)p.getY());
    // FIXME compute lastAngle
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }
  
  /**
   * @see java.awt.geom.GeneralPath#curveTo(float, float, float, float, float, float)
   */
  public synchronized Path curveTo(Point2D c1, Point2D c2, Point2D p) {
    checkContinue();
    // FIXME add opening arrow
    gp.curveTo((float)c1.getX(), (float)c1.getY(), (float)c2.getX(), (float)c2.getY(), (float)p.getX(), (float)p.getY());
    // FIXME compute lastAngle
    lastPoint.setLocation(p.getX(), p.getY());
    return this;
  }

  /**
   * End
   */
  public synchronized void end(boolean arrow) {

    // check status
    if (!isStarted)
      throw new IllegalArgumentException("end without start");
    if (isEnded)
      throw new IllegalArgumentException("end after end");
    isEnded = true;
    
    // add arrow
    if (arrow) {
      AffineTransform at = AffineTransform.getTranslateInstance(lastPoint.getX(), lastPoint.getY());
      at.rotate(lastAngle);
      gp.append(ARROW_HEAD.getPathIterator(at), false);
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
