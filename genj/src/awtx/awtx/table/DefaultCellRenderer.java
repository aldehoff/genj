/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx.table;

import java.awt.*;

/**
 * Default implementation of CellRenderer
 */
public class DefaultCellRenderer implements CellRenderer {

  private int alignment = LEFT;

  public final static int
  LEFT   = 0,
  CENTER = 1,
  RIGHT  = 2;

  /**
   * Constructor with default alignment
   */
  public DefaultCellRenderer() {
  }

  /**
   * Constructor with given alignment
   */
  public DefaultCellRenderer(int pAlignment) {
    alignment=pAlignment;
  }

  /**
   * Renders a header cell
   */
  public void render(Graphics g, Rectangle rect, Object o, FontMetrics fm) {

    if (o==null)
      return;

    String s = o.toString();

    // Alignment?
    int off = 0;
    switch (alignment) {
      case LEFT:
      break;
      case RIGHT:
      off = rect.width-fm.stringWidth(s);
      break;
      case CENTER:
      off = (rect.width-fm.stringWidth(s))/2;
      break;
    }

    // Render
    g.drawString(
      s,
      rect.x+off,
      rect.y+rect.height-fm.getMaxDescent()
    );

    // Done
  }      
}
