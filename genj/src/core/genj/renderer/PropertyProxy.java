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
  
  /** a zero space dimension for reuse */  
  protected final static Dimension ZERO_DIMENSION = new Dimension(0,0);
  
  /**
   * 
   */
  public Dimension getSize(FontMetrics metrics, Property prop, boolean isText, boolean isImage) {
    return getSize(metrics, prop.getImage(false), prop.getValue(), isText, isImage);
  }
  
  /**
   * 
   */
  protected Dimension getSize(FontMetrics metrics, ImgIcon img, String txt, boolean isText, boolean isImage) {
    Dimension result = new Dimension(0,0);
    // text?
    if (isText&&(null!=txt)) {
      result.width += metrics.stringWidth(txt);
      result.height = Math.max(result.height, metrics.getHeight());
    }
    // image?
    if (isImage) {
      result.width += img.getIconWidth() + metrics.charWidth(' ');
      result.height = Math.max(result.height, img.getIconHeight());
    }
    // done
    return result;
  }
  
  /**
   * 
   */
  public void render(Graphics g, FontMetrics metrics, Rectangle bounds, Property prop, boolean isText, boolean isImage) {
    render(g,metrics,bounds,prop.getImage(false),prop.getValue(),isText,isImage);
  }

  /**
   * 
   */
  protected void render(Graphics g, FontMetrics metrics, Rectangle bounds, ImgIcon img, String txt, boolean isText, boolean isImage) {
    int x = bounds.x;
    int y = bounds.y;
    // image?
    if (isImage) {
      img.paintIcon(g,x,y+(bounds.height-img.getIconHeight())/2);
      x += img.getIconWidth() + metrics.charWidth(' ');
    }
    // text?
    if (isText) {
      g.drawString(txt, x, y+metrics.getAscent());
    }
    // done
  }

  /**
   * 
   */
  public float getVerticalAlignment(FontMetrics metrics) {  
    float h = metrics.getHeight();
    float d = metrics.getDescent();
    return (h-d)/h;
  }  
  
} //PropertyProxy
