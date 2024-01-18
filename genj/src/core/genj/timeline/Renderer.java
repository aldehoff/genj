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

import java.awt.Toolkit;

/**
 * Generic superclass of all renderers
 */
public abstract class Renderer {
  
  /** resolution to use */
  /*package*/ double dpi = Toolkit.getDefaultToolkit().getScreenResolution();

  /**
   * Convert cm into pixels
   */
  protected final int cm2pixels(double cm) {
    // 1 Centimeters = 0.393701  Inches
    return inches2pixels(cm*0.393701);
  }
  
  /**
   * Convert inches into pixels
   */
  protected final int inches2pixels(double inches) {
    // pixels = inches * dpi
    return (int)(inches*dpi);
  }
  
  /**
   * Convert pixels into cm
   */
  protected final double pixels2cm(int pixels) {
    // 1 inch = 2.54 cm
    return pixels2inches(pixels)*2.54;
  }

  /**
   * Convert pixels into inches
   */
  protected final double pixels2inches(int pixels) {
    // pixels = inches * dpi
    return ((double)pixels)/dpi;
  }
  
  
} //Renderer
