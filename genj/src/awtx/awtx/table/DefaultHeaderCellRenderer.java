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
 * Default implementation of CellRenderer for cells in header
 */
public class DefaultHeaderCellRenderer implements CellRenderer {

  /**
   * Implementation of the rendering
   */
  public void render(Graphics g, Rectangle rect, Object o, FontMetrics fm) {

    // .. draw background
    g.setColor(Color.lightGray);
    g.fillRect(rect.x,rect.y,rect.width,rect.height);
    g.setColor(Color.white);
    g.drawLine(rect.x,rect.y,rect.x,rect.y+rect.height);
    g.drawLine(rect.x,rect.y,rect.x+rect.width,rect.y);
    g.setColor(Color.darkGray);
    g.drawLine(rect.x+rect.width-1,rect.y,rect.x+rect.width-1,rect.y+rect.height);
    g.drawLine(rect.x,rect.y+rect.height-1,rect.x+rect.width,rect.y+rect.height-1);

    // .. draw header object
    if (o!=null) {
      String s = o.toString();
      g.setColor(Color.black);
      g.drawString(
        s,
        rect.x+rect.width/2-fm.stringWidth(s)/2,
        rect.y+rect.height-fm.getMaxDescent()
      );
    }

    // done
  }
}
