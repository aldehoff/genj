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
 * A rTable (lighweight) component
 */
public class Content extends Component {

  FontMetrics fm;
  int         rowHeight = 10;
  Dimension   sizeOfData = new Dimension();
  Table       table;

  final static Color
  colSelection = new Color(160,160,255),
  colBackground= Color.white,
  colForeground= Color.black;

  final static int
  HEADER = 4000000;

  /**
   * Constructor
   */
  public Content(Table pTable) {

    // Remember
    table = pTable;

    // Done
  }

  /**
   * Calculates parms
   */
  public void addNotify() {
    super.addNotify();

    Font font = getFont();
    fm = getFontMetrics(font);
    Insets insets = table.getCellInsets();
    rowHeight = fm.getHeight() + insets.top + insets.bottom;

  }

  /**
   * Converts given column to horizontal position
   */
  public int convertCol2Pos(int column) {

    // Argument o.k.?
    if (column>=table.getNumColumns())
      throw new IllegalArgumentException();

    // Loop through columns
    int[] colWidths = table.getColumnWidths();
    int result = 0;
    int col=0;
    while (true) {

      // .. that's it?
      if (col==column)
      break;

      // .. add width
      result += colWidths[col];

      // .. next
      col++;
    }

    // Done
    return result;
  }

  /**
   * Converts given horizontal position to (0-based) column
   */
  public int convertPos2Col(int pos) {

    int result = -1;

    // Look through columns
    int[] colWidths = table.getColumnWidths();
    for (int col=0;;col++) {

      // .. no more column?
      if (col>=table.getNumColumns())
      return result;

      // .. hit the column ?
      if (pos-colWidths[col]<=0) {
      // .. remember column
      result=col;
      // .. that's it
      return result;
      }

      // .. decrement pos
      pos-=colWidths[col];
    }

  }

  /**
   * Converts given vertical position to (0-based) row
   */
  public int convertPos2Row(int pos) {

    // Find row
    int row = pos/rowHeight;
    if (row>=table.getNumRows())
      return -1;
    return row;
  }

  /**
   * Converts given vertical row to position
   */
  public int convertRow2Pos(int row) {
    return row*rowHeight;
  }

  /**
   * Returns the preferred size of content
   */
  public Dimension getPreferredSize() {

    // Width&Height
    sizeOfData.width =0;
    sizeOfData.height=0;
    Insets cellInsets = table.getCellInsets();

    int[] colWidths = table.getColumnWidths();
    for (int col=0;col<colWidths.length;col++)
      sizeOfData.width+=colWidths[col];

    sizeOfData.height = table.getNumRows()*rowHeight;

    // Done
    return sizeOfData;
  }

  /**
   * Paints this content's data
   */
  public void paint(Graphics g) {

    // Fill Background
    g.setColor(colBackground);
    g.fillRect(
      0,
      0,
      getSize().width,
      getSize().height
    );

    // Rendering
    renderContent(g);

    // Done
  }

  /**
   * Helper which draws the grid content
   */
  private void renderContent(Graphics g) {

    int[] colWidths = table.getColumnWidths();
    Insets cellInsets = table.getCellInsets();
    CellRenderer[] renderers = table.getColumnRenderers();

    Rectangle clip  = g.getClipBounds();

    // Draw Rows
    Rectangle rcell = new Rectangle();

    int startRow = clip.y / rowHeight;
    int startCol  = 0;
    int startXPos = 0;

    while (startXPos+colWidths[startCol]<clip.x) {
      startXPos += colWidths[startCol++];
      if (startCol==colWidths.length)
        return;
    }

    int ypos = startRow*(rowHeight);
    for (int row=startRow;row<table.getNumRows();row++) {

      int xpos = startXPos;

      // Draw Scrolling Cells
      for (int col=startCol;col<table.getNumColumns();col++) {

        // .. calculate Rectangle
        rcell.x = xpos;
        rcell.y = ypos;
        rcell.width = colWidths[col];
        rcell.height = rowHeight;

        // .. visible?
        if (rcell.x+rcell.width>clip.x) {

          // .. draw background
          if (table.isSelectedRow(table.getTranslatedRow(row))) {
            g.setColor(colSelection);
            g.fillRect(rcell.x,rcell.y,rcell.width,rcell.height);
          }

          // .. draw right border
          if (row>=0) {
            g.setColor(colForeground);
            g.drawLine(
              rcell.x+colWidths[col]-1,
              rcell.y,
              rcell.x+colWidths[col]-1,
              rcell.y+rowHeight-1
            );
          }

          // .. include insets
          rcell.x += cellInsets.left;
          rcell.y += cellInsets.top ;
          rcell.width -= cellInsets.left + cellInsets.right;
          rcell.height -= cellInsets.top + cellInsets.bottom;

          // .. clip
          g.clipRect(
            rcell.x,
            rcell.y,
            rcell.width,
            rcell.height
          );

          // .. draw content
          g.setColor(colForeground);
          renderers[col].render(
            g,
            rcell,
            table.getModel().getObjectAt(table.getTranslatedRow(row),col),
            fm
          );

          // .. clip
          g.setClip(clip);

        }

        // .. advance xposition
        xpos += colWidths[col];

        // .. enough drawn?
        if (rcell.x>clip.x+clip.width)
          break;

        // .. next Cell
      }

      // ... next Row
      ypos += rowHeight;

      // .. draw bottom line
      if (row>=0)
        g.drawLine(0,ypos-1,xpos-1,ypos-1);

      // .. enough drawn?
      if (ypos>clip.y+clip.height)
        break;
    }

    // Done
  }
}
