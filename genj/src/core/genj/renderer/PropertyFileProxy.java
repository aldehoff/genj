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
import genj.util.swing.ImageIcon;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.text.html.ImageView;

/**
 * last, first
 * first last
 * first last suffix
 */
public class PropertyFileProxy extends PropertyProxy {

  /** an replacement for a 'broken' image */  
  private final static ImageIcon broken = new ImageIcon(new ImageView(null).getNoImageIcon());

  /**
   * @see genj.renderer.PropertyProxy#getSize(FontMetrics, Property, boolean, boolean)
   */
  public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
    ImageIcon img = getImage(prop);
    return new Dimension(img.getIconWidth(), img.getIconHeight());
  }

  /**
   * @see genj.renderer.PropertyProxy#render(Graphics, FontMetrics, Rectangle, Property, boolean, boolean)
   */
  public void render(Graphics g, Rectangle bounds, Property prop, int preference) {
    // grab the image
    ImageIcon img = getImage(prop);
    int
      h = img.getIconHeight(),
      w = img.getIconWidth ();
    // check if we should zoom
    double zoom = Math.min(
      Math.min(1.0D, ((double)bounds.width )/w),
      Math.min(1.0D, ((double)bounds.height)/h)
    );
    img.paintIcon(g, bounds.x, bounds.y, zoom);
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
  private ImageIcon getImage(Property prop) {
    ImageIcon result = null;
    if (prop instanceof PropertyFile) { 
      PropertyFile file = (PropertyFile)prop;
      result = file.getValueAsIcon();
    }
    if (result==null) result = broken;
    return result;
  }  

} //PropertyNameProxy
