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
package genj.util.swing;

import genj.util.ByteArray;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;

/**
 * Improved ImageIcon
 * <il>
 *  <li>can be read conveniently as resource for object or class
 *  <li>knows about image resolution 
 * </il> */
public class ImageIcon extends javax.swing.ImageIcon {

  /** 
   * Overriden default
   */
  public ImageIcon(byte[] data) {
    super(data);
  }
    /** 
   * Overriden default
   */
  public ImageIcon(Image image) {
    super(image);
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(Object from, String name) {
    this(from.getClass().getResourceAsStream(name));
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(Class from, String name) {
    this(from.getResourceAsStream(name));
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(InputStream in) {
    this(read(in));
  }
  
  // FIXME needs to be derived
  private Point dpi = ScreenResolutionScale.getSystemDPI();
   //new Point(200,200);
  
  /**
   * Returns resolution (dpi)
   */
  public Point getResolution() {
    return dpi;
  }

  /**
   * Size in inches
   */
  public Point2D getPhysicalSize() {
    return new Point2D.Double((double)getIconWidth()/dpi.x,(double)getIconHeight()/dpi.y);
  }
  
  /**
   * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  public ImageIcon paintIcon(Graphics g, int x, int y) {
    super.paintIcon(null, g, x, y);
    return this;
  }
  
  /**
   * Reads image data from input stream
   */
  private static byte[] read(InputStream in) {
    try {
      return new ByteArray(in).getBytes();
    } catch (IOException ex) {
      return null;
    }
  }
  
} //ImageIcon 
