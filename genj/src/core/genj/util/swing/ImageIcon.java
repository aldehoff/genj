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
import genj.util.ImageSniffer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Improved ImageIcon
 * <il>
 *  <li>can be read conveniently as resource for object or class
 *  <li>knows about image resolution 
 * </il> */
public class ImageIcon extends javax.swing.ImageIcon {
  
  /** name */
  private String name;

  /** dpi */
  private Point dpi = null;

  /** 
   * Overriden default
   */
  public ImageIcon(String name, byte[] data) {
    super(data);

    // keep name
    this.name = name;

    // sniff resolution
    String msg;
    
    ImageSniffer is = new ImageSniffer(new ByteArrayInputStream(data));
    dpi = new Point(is.getDPIx(), is.getDPIy());
    
//    if (is.getSuffix()!=null) {
//      msg = getIconWidth()+"x"+getIconHeight()+'/'+is.getWidth()+'x'+is.getHeight()+'/'+is.getDPIx()+'x'+is.getDPIy();
//    } else {
//      msg = "sniff failed";
//    }
//    
//    Debug.log(Debug.INFO, this, name+'['+msg+']');

    // done
  }
    /**
   * Alternative Constructor
   */
  public ImageIcon(Object from, String resource) {
    this(from.getClass(), resource);
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(Class from, String resource) {
    this(from.getName()+'#'+resource, from.getResourceAsStream(resource));
  }
  
  /**
   * Alternative Constructor
   */
  public ImageIcon(String name, InputStream in) {
    this(name, read(in));
  }
  
  /**
   * Returns resolution (dpi)
   * @param target the target dpi which will be used if no dpi information is available
   */
  public Point getResolution(Point target) {
    if (dpi.x<0||dpi.y<0) return target;
    return dpi;
  }

  /**
   * Size in inches
   */
  public Point2D getPhysicalSize(Point target) {
    Point dpi = getResolution(target);
    return new Point2D.Double((double)getIconWidth()/dpi.x,(double)getIconHeight()/dpi.y);
  }
  
  /**
   * Size in target space (dpi)
   */
  public Dimension getSize(Point dpiTarget) {
    Point2D size = getPhysicalSize(dpiTarget);
    return new Dimension((int)(size.getX()*dpiTarget.x), (int)(size.getY()*dpiTarget.y));
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
      
      // read the bytes
      return new ByteArray(in).getBytes();
      
    } catch (IOException ex) {
      return null;
    }
  }
  
} //ImageIcon 
