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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Icon;

/**
 * Improved ImageIcon */
public class ImageIcon extends javax.swing.ImageIcon {

  /** zoomed instances */
  private Map cache = new WeakHashMap();

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
   * Alternative Constructor   */
  public ImageIcon(Icon icon) {
    super(read(icon));
  }

  /**
   * Alternative Constructor
   */
  public ImageIcon(InputStream in) {
    this(read(in));
  }
  
  /**
   * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  public ImageIcon paintIcon(Graphics g, int x, int y) {
    super.paintIcon(null, g, x, y);
    return this;
  }
  
  /**
   * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  public ImageIcon paintIcon(Graphics g, int x, int y, double zoom) {
    
    // No real zoom ?
    if (zoom==1.0) return paintIcon(g,x,y);

    // Calc w & h
    int w = (int)(super.getIconWidth ()*zoom),
        h = (int)(super.getIconHeight()*zoom);
        
    // no change?
    if (w==getIconWidth()&&h==getIconHeight()) return paintIcon(g,x,y);

    // Known Image ?
    String key = w+"x"+h;
    ImageIcon zoomed = (ImageIcon)cache.get(key);
    if (zoomed==null) {
      // .. zoom!
      zoomed = new ImageIcon(getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT));
      // .. remember
      cache.put(key, zoomed);
    }

    // Paint
    zoomed.paintIcon(g,x,y);

    // Done
    return this;
  }
  
  /**
   * Creates an image from an icon
   */
  private static Image read(Icon icon) {
    Image img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    icon.paintIcon(null, img.getGraphics(), 0, 0);
    return img;    
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
