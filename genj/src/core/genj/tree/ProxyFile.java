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

import genj.util.*;
import genj.gedcom.*;

/**
 * A proxy knows how to draw a property in a tree's entity : FILE
 */
class ProxyFile extends Proxy {

  /**
   * Renders a PropertyFile in a tree
   */
  void renderSpecific(Property p, int dit, int pig, Rectangle box, TreeGraphics g) {

    PropertyFile file = (PropertyFile)p;
    ImgIcon icon = file.getValueAsIcon();
    if (icon==null) {
      return;
    }

    // Draw content
    g.drawBlob(icon,dit,pig,box.getLocation());

    // Done
  }
}
