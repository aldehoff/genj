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

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.util.ImgIcon;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * last, first
 * first last
 * first last suffix
 */
public class PropertyFileProxy extends PropertyProxy {

  /**
   * @see genj.renderer.PropertyProxy#getSize(FontMetrics, Property, boolean, boolean)
   */
  public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
    ImgIcon img = getImage(prop);
    if (img==null) return ZERO_DIMENSION;
    return new Dimension(img.getIconWidth(), img.getIconHeight());
  }

  /**
   * @see genj.renderer.PropertyProxy#render(Graphics, FontMetrics, Rectangle, Property, boolean, boolean)
   */
  public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
    // grab the image
    ImgIcon img = getImage(prop);
    if (img==null) return;
    int
      h = img.getIconHeight(),
      w = img.getIconWidth ();
    // check if we should zoom
    double zoom = Math.min(
      Math.min(1.0D, ((double)bounds.width )/w),
      Math.min(1.0D, ((double)bounds.height)/h)
    );
    img.paintIcon(g, 
      (int)(bounds.x + (bounds.width -w*zoom)/2), 
      (int)(bounds.y + (bounds.height-h*zoom)/2), 
      zoom
    );
    // done
  }
  
  /**
   * 
   */
  public float getVerticalAlignment(FontMetrics metrics) {  
    return 1.0F;
  }
  
  /**
   * Helper to get the image of PropertyFile
   */
  private ImgIcon getImage(Property prop) {
    if (!(prop instanceof PropertyFile)) 
      return null;
    PropertyFile file = (PropertyFile)prop;
    return file.getValueAsIcon();
  }  

} //PropertyNameProxy
