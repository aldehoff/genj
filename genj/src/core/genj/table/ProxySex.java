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
package genj.table;

import java.awt.*;
import awtx.table.*;
import genj.gedcom.*;
import genj.util.ImgIcon;

/**
 * Proxy that knows how to handle specific property : SEX
 */
public class ProxySex implements CellRenderer {

  /**
   * Renders the cells content
   */
  public void render(Graphics g, Rectangle rect, Object o, FontMetrics fm) {

    // Object?
    if (o==null) {
      return;
    }

    ImgIcon icon = ((PropertySex)o).getImage(true);

    g.drawImage(
      icon.getImage(),
      rect.x+rect.width /2-icon.getIconWidth ()/2,
      rect.y+rect.height/2-icon.getIconHeight()/2,
      null
    );

    // Done
  }
}
