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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

public class GraphicsHelper {

  /**
   * render text
   */
  public static void render(Graphics2D graphics, String str, Rectangle2D box, double xalign, double yalign) {
    
    FontMetrics fm = graphics.getFontMetrics();
    LineMetrics lm = fm.getLineMetrics(str, graphics);

    float h = 0;
    String[] lines = str.split("\\\n");
    float[] ws = new float[lines.length];
    for (int i=0;i<lines.length;i++) {
      Rectangle2D r = fm.getStringBounds(lines[i], graphics);
      ws[i] = (float)r.getWidth();
      h = Math.max(h,(float)r.getHeight());
    }

    Shape clip = graphics.getClip();
    graphics.clip(box);
    
    for (int i=0;i<lines.length;i++) {
      double x = Math.max(box.getX(), box.getCenterX() - ws[i]*xalign);
      double y = Math.max(box.getY(), box.getY() + (box.getHeight()-lines.length*h)*yalign + i*h + h - lm.getDescent()); 
      
      graphics.drawString(lines[i], (float)x, (float)y);
    }
    
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
  
  public static Icon getIcon(int size, Shape shape) {
    return new ShapeAsIcon(size, shape);
  }
  
  /**
   * A shape as icon
   */
  private static class ShapeAsIcon implements Icon {

    private Dimension size;
    private Shape shape;

    private ShapeAsIcon(int size, Shape shape) {
      this.size = new Dimension(size, size);
      this.shape = shape;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(c.getForeground());
      g.translate(x, y);
      ((Graphics2D) g).fill(shape);
      g.translate(-x, -y);
    }

    public int getIconWidth() {
      return size.width;
    }

    public int getIconHeight() {
      return size.height;
    }

  }
}
