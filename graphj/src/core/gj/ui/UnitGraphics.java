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
package gj.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;


/**
 * A graphics wrapper that knows how to handle units
 * for translations during drawing
 */
public class UnitGraphics {

  /** the wrapped */
  private Graphics2D graphics;
  
  /** stack of transformations */
  private Stack stackTransformations = new Stack();

  /** a stack of push'd clips */
  private Stack clipStack = new Stack();
  
  /** the horizontal/vertical unit */
  private double xunit, yunit;

  /** a line */
  private Line2D.Double line = new Line2D.Double();
  
  /** the fontmetrics */
  private FontMetrics fontMetrics;

  /** dot-per-centimeters */
  private static Point2D.Double dpc = new Point2D.Double(
    Toolkit.getDefaultToolkit().getScreenResolution(),
    Toolkit.getDefaultToolkit().getScreenResolution()
  );
  
  /**
   * Resolution for centimeters
   */
  public static Point2D getDPC() {
    // 1 cm = 0.393701*inches
    return new Point2D.Double(0.393701D * dpc.x, 0.393701D * dpc.y);
  }
  
  /**
   * Accessor - resolution for centimeters
   */    
  public static void setDPC(Point2D set) {
    // 1 cm = 0.393701*inches
    dpc.x = set.getX() / 0.393701D;
    dpc.y = set.getY() / 0.393701D;
  }
  
  /**
   * Constructor
   */
  public UnitGraphics(Graphics graphcs, double unitX, double unitY) {
    this((Graphics2D)graphcs, unitX, unitY);
  }

  /**
   * Constructor
   */
  public UnitGraphics(Graphics2D graphcs, double unitX, double unitY) {
    graphics = graphcs;
    fontMetrics = graphics.getFontMetrics();
    xunit = unitX;
    yunit = unitY;
  }

  /**
   * Set antialiasing
   */
  public void setAntialiasing(boolean set) {
    graphics.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      set ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF
    );
  }
  
  /**
   * Translates
   */
  public void translate(double x, double y) {
    graphics.translate(units2pixels(x, xunit), units2pixels(y, yunit));
  }

  /**
   * Draw a line 
   */
  public void draw(double x1, double y1, double x2, double y2) {
    draw(x1,y1,x2,y2,0,0);
  }
  
  /**
   * Draw a line with delta
   */
  public void draw(double x1, double y1, double x2, double y2, int dx, int dy) {
    line.x1=x1*xunit+dx;
    line.y1=y1*yunit+dy;
    line.x2=x2*xunit+dx;
    line.y2=y2*yunit+dy;
    graphics.draw(line);
  }

  /** 
   * Draw a shape at given position
   */
  public void draw(Shape shape, double x, double y, boolean fill) {
    draw(shape,x,y,1,1,0,fill);
  }

  /** 
   * Draw a shape at given position
   */
  public void draw(Shape shape, double x, double y, double sx, double sy, double alpha, boolean fill) {

    // maybe we do a transformation
    AffineTransform at = null;

    // translate
    x *= xunit;
    y *= yunit;
    if (x!=0||y!=0) {
      if (at==null) at = new AffineTransform();
      at.translate(x, y);
    }
    
    // FIXME: Cache scaled shapes
    sx *= xunit;
    sy *= yunit;
    if (sx!=1.0D||sy!=1.0D) {
      if (at==null) at = new AffineTransform();
      at.scale(Double.isNaN(sx)?1.0D:sx,Double.isNaN(sy)?1.0D:sy);
    }

    // rotate
    if (alpha!=0) {
      if (at==null) at = new AffineTransform();
      at.rotate(alpha);
    }

    // create a general path from shape 
    if (at!=null) {
      GeneralPath gp = new GeneralPath(shape);
      gp.transform(at);
      shape = gp;
    }

    // draw
    if (fill) graphics.fill(shape);
    else graphics.draw(shape);

    // done    
  }    
  
  /**
   * Draw text at given position
   */
  public void draw(String txt, double x, double y) {
    draw(txt, x, y, 0.5D);
  }

  /**
   * Draw text at given position
   */
  public void draw(String txt, double x, double y, double align) {
    draw(txt, x, y, align, 0, fontMetrics.getHeight()/2-fontMetrics.getDescent());
  }

  /**
   * Draw text at given position
   */
  public void draw(String txt, double x, double y, double align, int dx, int dy) {
    
    // fix alignment
    dx += - (int)( fontMetrics.stringWidth(txt) * align);

    // draw      
    graphics.drawString(txt, units2pixels(x, xunit)+dx, units2pixels(y, yunit)+dy);
  }
  
  /**
   * Draw Image at fiven position
   */
  public void draw(Image img, double x, double y) {
    int 
      ix = units2pixels(x, xunit),
      iy = units2pixels(y, yunit) - img.getHeight(null)/2;
    graphics.drawImage(img, ix, iy, null);
  }
  
  /**
   * Set a color
   */
  public void setColor(Color color) {
    if (color!=null) graphics.setColor(color);
  }
  
  /**
   * Set a font
   */
  public void setFont(Font font) {
    graphics.setFont(font);
    fontMetrics = graphics.getFontMetrics();
  }
  
  /**
   * Returns a Graphics object for direct rendering   */
  public Graphics getGraphics() {
    return graphics;
  }
  
  /** 
   * Returns the FontMetrics
   */
  public FontMetrics getFontMetrics() {
    return graphics.getFontMetrics();
  }

  /**
   * Pops a saved AffineTransformation
   */
  public void popTransformation() {
    graphics.setTransform((AffineTransform)stackTransformations.pop());
  }

  /**
   * Pushes a AffineTransformation for later
   */
  public void pushTransformation() {
    stackTransformations.push(graphics.getTransform());
  }
  
  /**
   * Pushes a pop'able clip
   */
  public void pushClip(double x, double y, Rectangle2D r) {
    pushClip(x+r.getX(), y+r.getY(), x+r.getMaxX(), y+r.getMaxY());
  }
  
  /**
   * Pushes a pop'able clip
   */
  public void pushClip(double x1, double y1, double x2, double y2) {
    clipStack.push(graphics.getClipBounds());
    int 
      x = (int)Math.ceil(x1*xunit),
      y = (int)Math.ceil(y1*yunit),
      w = (int)Math.floor(x2*xunit)-x,
      h = (int)Math.floor(y2*yunit)-y;
    graphics.clipRect(x,y,w,h);
  }
  
  /**
   * Pop's a previously push'd clip
   */
  public void popClip() {
    Rectangle r = (Rectangle)clipStack.pop();
    graphics.setClip(r.x,r.y,r.width,r.height);
  }
  
  /**
   * Returns the clip in unit space
   */
  public Rectangle2D getClip() {
    Rectangle r = graphics.getClipBounds();
    return new Rectangle2D.Double(
      r.getMinX()/xunit,
      r.getMinY()/yunit,
      r.getWidth()/xunit,
      r.getHeight()/yunit
    );
  }

  /**
   * dimension to pixels
   */
  public Rectangle units2pixels(Rectangle2D bounds) {
    return new Rectangle(
      units2pixels(bounds.getMinX(), xunit),
      units2pixels(bounds.getMinY(), yunit),
      units2pixels(bounds.getWidth(), xunit),
      units2pixels(bounds.getHeight(), yunit)
    );
  }

  /**
   * unit2pixels
   */
  public Point units2pixels(Point2D point) {
    return new Point(
      units2pixels(point.getX(), xunit),
      units2pixels(point.getY(), yunit)
    );
  }

  /**
   * points2pixels
   */
  public Point2D pixels2units(Point point) {
    return new Point2D.Double( ((double)point.x)/xunit, ((double)point.y)/yunit);
  }

  /**
   * unit2pixels
   */
  public static int units2pixels(double d, double unit) {
    return (int)(d*unit);
  }

  /**
   * pixels2unit
   */
  public static double pixels2units(int i, double unit) {
    return ((double)i)/unit;
  }

} //UnitGraphics
