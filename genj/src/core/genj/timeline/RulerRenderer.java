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
public class RulerRenderer extends ContentRenderer {
  
  /** ticks color */
  /*package*/ Color cTick = null;
  
  /** 
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics metrics) {
    return new Dimension(
      super.getDimension(model, metrics).width,
      metrics.getHeight()
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Graphics g, Model model) {
    
    // prepare some stuff
    FontMetrics fm = g.getFontMetrics();
    Dimension d = getDimension(model, fm);
    double
      from = Math.ceil(model.min),
      to   = Math.floor(model.max),
      cond = Math.max(1, pixels2cm(fm.stringWidth(" 0000 "))/cmPyear);
    Clip clip = new Clip(g, fm, model);
    // render background
    setColor(g, cBackground);
    g.fillRect(0,0,d.width,d.height);

    // render first year and last
    from += renderYear(g, model, d, fm, from, 0.0D);
    to -= renderYear(g, model, d, fm, to  , 1.0D);
    
    // recurse binary
    renderSpan(g, model, d, fm, from, to, cond, clip);
  }
  
  /**
   * Renders ticks recursively
   */
  private void renderSpan(Graphics g, Model model, Dimension d, FontMetrics fm, double from, double to, double cond, Clip clip) {
    
    // condition met?
    if (to-from<cond) return;
    
    // clipp'd out?
    if (to<clip.minYear||from>clip.maxYear) return;
    
    // calculate center year    
    double year = Math.rint((from+to)/2);
    
    // render
    double gone = renderYear(g, model, d, fm, year, 0.5D);
    
    // recurse into
    renderSpan(g, model, d, fm, Math.ceil(year+gone/2), to         , cond, clip);
    renderSpan(g, model, d, fm, from       , Math.floor(year-gone/2), cond, clip);
    
    // done
  }
  
  /**
   * Renders one year
   */
  private double renderYear(Graphics g, Model model, Dimension d, FontMetrics fm, double year, double align) {
    // what's the x for it
    int x = cm2pixels((year-model.min)*cmPyear);
    // draw a vertical line
    setColor(g, cTick);
    g.drawLine((int)(x - align), d.height-4, x, d.height);
    // draw the label
    setColor(g, cText);
    String s = Integer.toString((int)year);
    int fw = fm.stringWidth(s);
    int fd = fm.getDescent();
    g.drawString(s, x - (int)(align*fw), d.height-fd);
    // done
    return pixels2cm(fw)/cmPyear;
  }

} //RulerRenderer
