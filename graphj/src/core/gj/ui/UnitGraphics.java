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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Stack;


/**
 * A graphics wrapper that knows how to handle units
 * for translations during drawing
 */
public class UnitGraphics {
  
  /** predefined factors */
  public static final double
    // pixels 1:1
    PIXELS      = 1.0D,
    // inches 1:dpi
    INCHES      = Toolkit.getDefaultToolkit().getScreenResolution(),
    // cm     1:0.393701*inches
    CENTIMETERS = 0.393701D * INCHES;
  
  /** the wrapped */
  private Graphics2D graphics;
  
  /** stack of transformations */
  private Stack stackTransformations = new Stack();

  /** resolution to use */
  public double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
  
  /** a stack of push'd clips */
  private Stack clipStack = new Stack();
  
  /** the horizontal/vertical unit */
  private double xunit, yunit;
  
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
      at.scale(sx,sy);
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
    FontMetrics fm = graphics.getFontMetrics();
    int 
      w  = fm.stringWidth(txt),
      dx = -w/2,
      dy = fm.getHeight()/2-fm.getDescent();
    graphics.drawString(txt, units2pixels(x, xunit)+dx, units2pixels(y, yunit)+dy);
  }
  
  /**
   * Set a color
   */
  public void setColor(Color color) {
    if (color!=null) graphics.setColor(color);
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
      h = (int)Math.floor(y2*xunit)-y;
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
   * unit2pixels
   */
  public static int units2pixels(double d, double unit) {
    return (int)(d*unit);
  }

  /**
   * pixels2unit
   */
  public static double pixels2units(int i, double unit) {
    return ((double)i)*unit;
  }

} //UnitGraphics
