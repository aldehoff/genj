/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
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

import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;
import java.awt.geom.Rectangle2D;

/**
 * Dots per inch
 */
public class DPI {

  public final static Key KEY = new DPIHintKey();

  public final static double INCH = 2.54D;

  private int horizontal, vertical;
  
  public int horizontal() {
    return horizontal;
  }
  
  public int vertical() {
    return vertical;
  }
  
  public DPI(int horizontal, int vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }
  
  /**
   * resolve DPI From graphics
   */
  public static DPI get(Graphics2D graphics) {
    DPI dpi = (DPI)graphics.getRenderingHint(KEY);
    if (dpi==null)
      dpi = Options.getInstance().getDPI();
    return dpi;
  }
  
  public Rectangle2D toPixel(Rectangle2D inches) {
    return new Rectangle2D.Double(
      inches.getX() * horizontal,
      inches.getY() * vertical,
      inches.getWidth() * horizontal,
      inches.getHeight() * vertical
    );
  }
  
  @Override
  public String toString() {
    return horizontal+" by "+vertical+" dpi";
  }
 
  /**
   * a rendering hint for hinting at dpi
   */
  private static class DPIHintKey extends Key {
    
    private DPIHintKey() {
      super(0);
    }

    @Override
    public boolean isCompatibleValue(Object val) {
      return val instanceof DPI;
    }
  }  
}
