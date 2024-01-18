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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class RulerRenderer extends Renderer {
  
  /** centimeters per year */
  /*package*/ double cmPyear = Double.NaN;
  
  /** 
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics metrics) {
    return new Dimension(
      cm2pixels(model.getSpan()*cmPyear),
      metrics.getHeight()
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Model model, Graphics g) {
    // use our dimension
    Dimension d = getDimension(model, g.getFontMetrics());
    // loop through years
    g.setColor(Color.black);
    for (int y=(int)model.getMinimum();y<=(int)model.getMaximum();y++) {
      // what's the x for it
      int x = cm2pixels((y-model.getMinimum())*cmPyear);
      // draw a line
      g.drawLine(x,0,x,d.height);
    }
    //g.drawLine(0,0,d.width,d.height);
    // done
  }

} //RulerRenderer
