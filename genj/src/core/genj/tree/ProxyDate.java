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

/**
 * A proxy knows how to draw a property in a tree's entity : DATE
 */
class ProxyDate extends Proxy {

  /**
   * Renders the property into a graphics context for a tree
   */
  void renderSpecific(Property p, int dit, int pig, Rectangle box, TreeGraphics g) {

    PropertyDate date = (PropertyDate)p;

    // Prepare
    g.setColor(Color.black);

    // Draw 1st line of date information
    if (!p.isValid()) {
      render(dit, pig, box, g, date.toString(false,true), false);
      return;
    }
    if (g.isAbbreviateDates) {
      render(dit, pig, box, g, date.toString(true,true), false);
      return;
    }

    render(dit, pig, box, g, date.getStart().toString(), false);
    if (date.isRange()) render(dit, pig, box, g, date.getEnd().toString(), true);
    
    // Done
  }
  
  /**
   * Renders a data line
   */
  private void render(int dit, int pig, Rectangle box, TreeGraphics g, String txt, boolean newline) {
    g.drawString(
      txt,
      dit,
      pig,
      box.x,
      box.y + (newline?2:1)*g.charHeight - g.charDescent
    );
  }
}
