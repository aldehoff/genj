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
import java.awt.event.*;
import awtx.*;

/**
 * A heading of our Table (lighweight) component
 */
public class Header extends Component {

  FontMetrics fm;
  int         rowHeight;
  Table       table;

  /**
   * Constructor
   */
  public Header(Table pTable) {

    // Remember
    table = pTable;

    // Done
  }

  /**
   * Calculates a few drawing parms
   */
  public void addNotify() {

    super.addNotify();

    Insets insets = table.getCellInsets();
    Font font = getFont();
    fm = getFontMetrics(font);
    rowHeight = fm.getHeight() + insets.top + insets.bottom;
  }

  /**
   * Returns the size of rendering content
   */
  public Dimension getPreferredSize() {
    return new Dimension(16,rowHeight);
  }

  /**
   * Paints this content's data (the header information)
   */
  public void paint(Graphics g) {

    Rectangle rcell = new Rectangle();
    Rectangle clip = g.getClipBounds();

    int[] colWidths = table.getColumnWidths();
    Insets cellInsets = table.getInsets();
    int sortedCol = table.getSortedColumn();
    int sortedDir = table.getSortedDir();
    CellRenderer[] renderers = table.getColumnHeaderRenderers();

    // Draw Header
    int hpos=0;
    for (int col=0;col<table.getNumColumns();col++) {

      // .. calculate Rectangle
      rcell.x = hpos;
      rcell.y = 0;
      rcell.width = colWidths[col];
      rcell.height = rowHeight;

      // .. clip
      g.clipRect(
      rcell.x     +cellInsets.left,
      rcell.y     +cellInsets.top ,
      rcell.width -cellInsets.left-cellInsets.right,
      rcell.height-cellInsets.top -cellInsets.bottom
      );

      // .. draw header
      renderers[col].render(
      g,
      rcell,
      table.getHeaderAt(col),
      fm
      );

      // .. sorting?
      if (sortedCol==col)
      renderSortingTag(g,rcell,sortedDir==table.UP);

      // .. restore clip
      g.setClip(clip);

      // .. advance xposition
      hpos += colWidths[col];

      // .. next column
    }
    // Done
  }

  /**
   * Render a small tag indicating sorting direction
   */
  private void renderSortingTag(Graphics g, Rectangle r, boolean upwards) {

    //    x1,y1 --- x2,y1
    //       \       /
    //        \     /
    //         \   /
    //          \ /
    //         x3,y2

    int x1 = r.x+r.width-12,
      x2 = r.x+r.width-2,
      x3 = r.x+r.width-7;

    int dir = upwards?1:-1;

    int y1 = r.y+(r.height/2)-(3*dir),
      y2 = r.y+(r.height/2)+(4*dir);

    int xs[] = {
      x1,x2,x3,x1
    };

    int ys[] = {
      y1,y1,y2,y1
    };

    g.fillPolygon(xs,ys,3);

  }
}
