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
package genj.tree;

import java.awt.*;

import genj.gedcom.*;
import java.util.*;

/**
 * A proxy knows how to draw a property in a tree's entity : MLE
 */
class ProxyMLE extends Proxy {

  /**
   * Renders a Property in a tree
   */
  /*package*/ void renderSpecific(Property p, int dit, int pig, Rectangle box, TreeGraphics g) {

    g.setColor(Color.black);

    // Really multiline?
    Property.LineIterator lines = p.getLineIterator();
    if (lines == null) {

      // Get value
      String s = p.getValue();
      if (s==null)
        return;

      // Draw content
      g.drawString(s,dit,pig,box.x,box.y+g.charHeight-g.charDescent);

    } else {

      int y = box.y;
      while (lines.hasMoreValues()) {
        String line = lines.getNextValue();
        g.drawString(line,dit,pig,box.x,y+g.charHeight-g.charDescent);
        y += g.charHeight;
      }
    }

    // Done

  }
}
