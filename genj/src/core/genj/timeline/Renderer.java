/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.timeline;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Stack;

/**
 * Generic superclass of all renderers
 */
public abstract class Renderer {
  
  /** resolution to use */
  /*package*/ double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
  
  /** a stack of push'd clips */
  private Stack clipStack = new Stack();

  /**
   * Convert cm into pixels
   */
  public final int cm2pixels(double cm) {
    // 1 Centimeters = 0.393701  Inches
    return inches2pixels(cm*0.393701);
  }
  
  /**
   * Convert inches into pixels
   */
  public final int inches2pixels(double inches) {
    // pixels = inches * dpi
    return (int)(inches*dpi);
  }
  
  /**
   * Convert pixels into cm
   */
  public final double pixels2cm(int pixels) {
    // 1 inch = 2.54 cm
    return pixels2inches(pixels)*2.54;
  }

  /**
   * Convert pixels into inches
   */
  public final double pixels2inches(int pixels) {
    // pixels = inches * dpi
    return ((double)pixels)/dpi;
  }
  
  /**
   * Pushes a pop'able clip
   */
  protected void pushClip(Graphics g, int x, int y, int w, int h) {
    clipStack.push(g.getClipBounds());
    g.clipRect(x,y,w,h);
  }
  
  /**
   * Pop's a previously push'd clip
   */
  protected void popClip(Graphics g) {
    Rectangle r = (Rectangle)clipStack.pop();
    g.setClip(r.x,r.y,r.width,r.height);
  }
  
} //Renderer
