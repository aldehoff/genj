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

import gj.ui.UnitGraphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A renderer knowing how to render a ruler for the timeline
 */
public class RulerRenderer extends ContentRenderer {
  
  /** ticks color */
  /*package*/ Color cTick = null;
  
  /** a tick */
  private final static Shape TICK = calcTick();
  
  /** 
   * Calculates the model size in pixels
   */
  public Dimension getDimension(Model model, FontMetrics metrics) {
    return new Dimension(
      super.getDimension(model, metrics).width,
      metrics.getHeight()+1
    );
  }
  
  /**
   * Renders the model
   */
  public void render(Graphics g, Model model) {
    
    // prepare UnitGraphics
    UnitGraphics graphics = new UnitGraphics(g, 
      UnitGraphics.CENTIMETERS*cmPyear, 
      g.getFontMetrics().getHeight()+1
    );
    graphics.translate(-model.min,0);
    
    // prepare some stuff
    FontMetrics fm = g.getFontMetrics();
    double
      from  = Math.ceil(model.min),
      to    = Math.floor(model.max),
      width = graphics.pixels2units(fm.stringWidth(" 0000 "), UnitGraphics.CENTIMETERS*cmPyear);

    // render background
    renderBackground(graphics, model);

    // render first year and last
    renderYear(graphics, model, fm, from, 0.0D);
    renderYear(graphics, model, fm, to  , 1.0D);
    
    from += width;
    to += -width;
    
    // recurse binary
    renderSpan(graphics, model, fm, from, to, width);
    
    // done
  }
  
  /**
   * Renders ticks recursively
   */
  private void renderSpan(UnitGraphics g, Model model, FontMetrics fm, double from, double to, double width) {

    // condition met (ran out of space)?
    if (to-from<width||to-from<1) return;

    // clipp'd out?
    if (g.isClipped(from, 0, to, 1)) return;
    
    // calculate center year    
    double year = Math.rint((from+to)/2);

    // still nough' space?
    if (year-from<width/2||to-year<width/2) {
      return;
    }

    // render
    renderYear(g, model, fm, year, 0.5D);
    
    // recurse into
    renderSpan(g, model, fm, year+width/2, to         , width);
    renderSpan(g, model, fm, from       , year-width/2, width);
    
    // done
  }
  
  /**
   * Renders one year
   */
  private void renderYear(UnitGraphics g, Model model,  FontMetrics fm, double year, double align) {
    // draw a vertical line
    g.setColor(cTick);
    g.draw(TICK, year, 1, Double.NaN, Double.NaN, 0, true);
    
    // draw the label
    g.setColor(cText);
    g.draw(Integer.toString((int)year), year, 1, align, 0, -fm.getDescent());
    
    // done
  }

  /**
   * Generates a tick
   */
  private static Shape calcTick() {
    Polygon result = new Polygon();
    result.addPoint(0,0);
    result.addPoint(3,-3);
    result.addPoint(-3,-3);
    return result;
  }
  
} //RulerRenderer
