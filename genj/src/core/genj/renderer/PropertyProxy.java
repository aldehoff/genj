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
package genj.renderer;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import genj.gedcom.Property;
import genj.util.ImgIcon;

/**
 * 
 */
public class PropertyProxy {
  
  /** our preferences when drawing properties */
  public final static int
    PREFER_DEFAULT      = 0,
    PREFER_IMAGE        = 1,
    PREFER_TEXT         = 2,
    PREFER_IMAGEANDTEXT = 3;
  
  /** a zero space dimension for reuse */  
  protected final static Dimension ZERO_DIMENSION = new Dimension(0,0);

  /** a default PropertyProxy */
  private final static PropertyProxy DEFAULT_PROPERTY_PROXY = new PropertyProxy();

  /** 
   * static accessor  
   */
  public static PropertyProxy get(String name) {
    try {
      return (PropertyProxy)Class.forName("genj.renderer.Property"+name+"Proxy").newInstance();
    } catch (Throwable t) {
      return DEFAULT_PROPERTY_PROXY;
    }
  }  
  
  /**
   * 
   */
  public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
    return getSize(metrics, prop.getImage(false), prop.getValue(), preference);
  }
  
  /**
   * 
   */
  protected Dimension getSize(FontMetrics metrics, ImgIcon img, String txt, int preference) {
    Dimension result = new Dimension(0,0);
    // text?
    if (isText(preference)) {
      if (null!=txt) {
        result.width += metrics.stringWidth(txt);
        result.height = Math.max(result.height, metrics.getHeight());
      }
    }
    // image?
    if (isImage(preference)) {
      result.width += img.getIconWidth() + metrics.charWidth(' ');
      result.height = Math.max(result.height, img.getIconHeight());
    }
    // done
    return result;
  }
  
  /**
   * 
   */
  public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
    render(g,bounds,prop.getImage(false),prop.getValue(),preference);
  }

  /**
   * 
   */
  protected void render(Graphics g, Rectangle bounds, ImgIcon img, String txt, int preference) {
    // image?
    if (isImage(preference)) render(g, bounds, img);
    // text?
    if (isText(preference)) render(g, bounds, txt);
    // done
  }
  
  /**
   * 
   */
  protected void render(Graphics g, Rectangle bounds, ImgIcon img) {
    img.paintIcon(g,bounds.x,bounds.y+(bounds.height-img.getIconHeight())/2);
    int skip = img.getIconWidth() + g.getFontMetrics().charWidth(' ');
    bounds.x += skip;
    bounds.width -= skip;
  }

  /**
   * 
   */
  protected void render(Graphics g, Rectangle bounds, String txt) {
    // check whether we'll have to zoom
    FontMetrics fm = g.getFontMetrics();
//    float zoom = bounds.width/fm.stringWidth(txt);
//      g.setFont(g.getFont().deriveFont(AffineTransform.getScaleInstance(zoom, zoom)));
//      Font oldf = g.getFont();
//      Font newf = oldf.deriveFont(oldf.getSize()*zoom);
//      System.out.println(oldf+">"+newf);
//      g.setFont(newf);
//      fm = g.getFontMetrics();
//((Graphics2D)g).scale()
//    }
    // by default we place the texts base at the bottom of bounds
    int y = bounds.y+bounds.height;
    // if bounds is high enough we patch up by fm's descent
    if (bounds.height>=fm.getAscent()) y -= fm.getDescent();
    // and paint
    g.drawString(txt, bounds.x, y);
  }

  /**
   * 
   */
  public float getVerticalAlignment(FontMetrics metrics) {  
    float h = metrics.getHeight();
    float d = metrics.getDescent();
    return (h-d)/h;
  }  
  
  /**
   * Check whether to draw image or not
   */
  protected boolean isImage(int preference) {
    return preference==PREFER_IMAGE||preference==PREFER_IMAGEANDTEXT;
  }
  
  /**
   * Check whether to draw text or not
   */
  protected boolean isText(int preference) {
    return preference==PREFER_TEXT||preference==PREFER_IMAGEANDTEXT||preference==PREFER_DEFAULT;
  }
  
} //PropertyProxy
