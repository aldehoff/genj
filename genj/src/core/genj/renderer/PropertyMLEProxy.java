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
import genj.gedcom.PropertyName;
import genj.util.WordBuffer;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * proxy for rendering MLE
 */
public class PropertyMLEProxy extends PropertyProxy {

  /**
   * @see genj.renderer.PropertyProxy#getSize(FontMetrics, Property, boolean, boolean)
   */
  public Dimension getSize(FontMetrics metrics, Property prop, int preference) {
    // check lines 
    Property.LineIterator it = prop.getLineIterator();
    if (it==null) return new Dimension(0,0);
    // count 'em
    int lines = 0;
    int width = 0;
    while (it.hasMoreValues()) {
      width = Math.max(width, metrics.stringWidth(it.getNextValue()));
      lines++;
    }
    // done
    return new Dimension(width, metrics.getHeight()*lines);
  }

  /**
   * @see genj.renderer.PropertyProxy#render(Graphics, FontMetrics, Rectangle, Property, boolean, boolean)
   */
  public void render( Graphics g, Rectangle bounds, Property prop, int preference) {
    // get lines
    Property.LineIterator it = prop.getLineIterator();
    if (it==null) return;
    // paint
    Rectangle clip = g.getClipBounds();
    int 
      h = g.getFontMetrics().getHeight(),
      m = clip.y + clip.height;
    Rectangle r = new Rectangle();
    r.x = bounds.x;
    r.y = bounds.y;
    r.width = bounds.width;
    r.height= h;
    while (it.hasMoreValues()) {
      // .. line at a time
      String line = it.getNextValue();
      super.render(g, r, line);
      // .. movin' down
      r.y += h;
      // .. break if not visible anymore
      if (r.y>m) break;
    }
    // done
  }
  
  /**
   * Helper to get the name of PropertyName
   */
  private String getName(Property prop) {
    if (!(prop instanceof PropertyName)||!prop.isValid()) 
      return prop.getValue();
    PropertyName name = (PropertyName)prop;
    WordBuffer b = new WordBuffer().setFiller(", ");
    b.append(name.getLastName());
    b.append(name.getFirstName());
    return b.toString();
  }  

} //PropertyNameProxy
