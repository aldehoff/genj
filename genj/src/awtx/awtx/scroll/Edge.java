/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
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
package awtx.scroll;

import java.awt.*;

/**
 * Our special container for the Edge
 */
public class Edge extends Container {

  /**
   * Does the actual layouting of the sub-components
   */
  public void doLayout() {

    Component[] cs = getComponents();
    int x=0,h=getSize().height;

    Rectangle r = new Rectangle();
    for (int i=0;i<cs.length;i++) {

      r.x = x;
      r.y = 0;
      r.width = cs[i].getPreferredSize().width;
      r.height= h;

      cs[i].setBounds(r);

      x += r.width;
    }

  }

  /**
   * The minimum size of this component = preferred size
   */
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * The preferred size of this component.
   * height = max(sub-components' height), width = sum sub-components' width
   */
  public Dimension getPreferredSize() {
    Component[] cs = getComponents();
    int w=0,h=0;
    Dimension d;
    for (int i=0;i<cs.length;i++) {
      d = cs[i].getPreferredSize();
      h = Math.max(h,d.height);
      w += d.width;
    }
    return new Dimension(w,h);
  }
}
