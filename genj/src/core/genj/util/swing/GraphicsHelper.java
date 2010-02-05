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
package genj.util.swing;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

public class GraphicsHelper {

  /**
   * render text
   */
  public static void render(Graphics2D graphics, String str, Rectangle2D box, double xalign, double yalign) {
    
    FontMetrics fm = graphics.getFontMetrics();
    Rectangle2D r = fm.getStringBounds(str, graphics);
    LineMetrics lm = fm.getLineMetrics(str, graphics);
    
    float
      w = (float)r.getWidth(),
      h = (float)r.getHeight();
      
    double x = Math.max(box.getX(), box.getCenterX() - w*xalign);
    double y = Math.max(box.getY(), box.getCenterY() - h*yalign + h - lm.getDescent()); 
    
    Shape clip = graphics.getClip();
    graphics.clip(box);
    graphics.drawString(str, (float)x, (float)y);
    graphics.setClip(clip);
  }
  
  /**
   * render text
   */
  public static void render(Graphics2D graphics, String str, double x, double y, double xalign, double yalign) {
    
    FontMetrics fm = graphics.getFontMetrics();
    Rectangle2D r = fm.getStringBounds(str, graphics);
    LineMetrics lm = fm.getLineMetrics(str, graphics);
    
    float
      w = (float)r.getWidth(),
      h = (float)r.getHeight();
      
    x = x- w*xalign;
    y = y - h*yalign + h - lm.getDescent(); 
      
    graphics.drawString(str, (float)x, (float)y);
  }
  
}
