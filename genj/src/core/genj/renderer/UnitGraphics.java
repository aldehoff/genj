package genj.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
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
  
  /** the unit */
  private double unit = PIXELS;
  
  /**
   * Constructor
   */
  public UnitGraphics(Graphics graphcs, double unitt) {
    this((Graphics2D)graphcs, unitt);
  }

  /**
   * Constructor
   */
  public UnitGraphics(Graphics2D graphcs, double unitt) {
    graphics = graphcs;
    unit = unitt;
    graphics.setStroke(new SingleBitStroke());
  }
  
  /**
   * Translates
   */
  public void translate(double x, double y) {
    graphics.translate(units2pixels(x), units2pixels(y));
  }

  /** 
   * Draw a shape at given position
   */
  public void draw(Shape shape, double x, double y, boolean fill) {
    pushTransformation();
    graphics.translate(units2pixels(x), units2pixels(y));
    graphics.scale(unit,unit);
    if (fill) graphics.fill(shape);
    else graphics.draw(shape);
    popTransformation();
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
    graphics.drawString(txt, units2pixels(x)+dx, units2pixels(y)+dy);
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
  public void pushClip(double x, double y, double w, double h) {
    clipStack.push(graphics.getClipBounds());
    graphics.setClip(units2pixels(x),units2pixels(y),units2pixels(w),units2pixels(h));
  }
  
  /**
   * Pop's a previously push'd clip
   */
  public void popClip() {
    Rectangle r = (Rectangle)clipStack.pop();
    graphics.setClip(r.x,r.y,r.width,r.height);
  }
  
  /**
   * units2pixels
   */
  public int units2pixels(double d) {
    return units2pixels(d, unit);
  }
  
  /**
   * pixels2units
   */
  public double pixels2units(int i) {
    return pixels2units(i, unit);
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

  /**
   * Single bit Stroke
   */
  private class SingleBitStroke extends BasicStroke {
    /** @see java.awt.BasicStroke#getLineWidth() */
    public float getLineWidth() {
      return (float)(1/unit);
    }
  } //SingleBitStroke
  

} //UnitGraphics
