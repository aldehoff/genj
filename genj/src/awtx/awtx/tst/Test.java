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
package awtx.tst;

import java.awt.*;
import java.awt.event.*;
import awtx.table.*;
import awtx.*;

/**
 * Simple Test for AWTx
 */
public class Test {

  /**
   * Starts the test
   */
  public static void main(String[] args) {

    //ComponentProvider.setMode(ComponentProvider.HEAVYWEIGHT);

    // Create Table
    TableModel model = new AbstractTableModel() {
      // LCD
      public int getNumRows() {
        return 20;
      }
      public int getNumColumns() {
        return 5;
      }
      public Object getObjectAt(int row, int column) {
        return ""+((row*10000)+column);
      }
      public Object getHeaderAt(int column) {
        return "H"+column;
      }
      public int compareRows(int first, int second, int column) {
        int ifirst  = new Integer((String)getObjectAt(first ,column)).intValue();
        int isecond = new Integer((String)getObjectAt(second,column)).intValue();
        return ifirst-isecond;
      }
      // EOC
    };

    final Table table = new Table();
    table.setModel(model);

    PopupProvider pprovider = new PopupProvider() {
      // LCD
      public void providePopup(Component c, int x, int y) {
        // Create popup
        PopupMenu popup = new PopupMenu();
        popup.add("HI");
        // Show it
        c.add(popup);
        popup.show(c,x,y);
        // Done
      }
      // EOC
    };
    table.setPopupProvider(pprovider);

    CellRenderer renderers[] = new CellRenderer[] {
      null,
      new DefaultCellRenderer(DefaultCellRenderer.RIGHT),
      new DefaultCellRenderer(DefaultCellRenderer.CENTER)
    };
    table.setCellRenderers(renderers);
    table.setSortable(true);

    Scala scala = new Scala();
    table.add2Edge(scala);

    // Some Frames
    openFrame(table);

    // Done for now
  }

  /**
   * Helper for creation of a Frame
   */
  private static void openFrame(final Component comp) {

    Container c = ComponentProvider.createContainer();

    ActionListener alistener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("DONE"))
        System.exit(0);
      }
    };

    Component button = ComponentProvider.createButton(null,"Done",null,"DONE",alistener);

    c.setLayout(new BorderLayout());
    c.add("Center",new Rootpane(comp));
    c.add("South",button);

    Window frame = ComponentProvider.createDialog(
      null,
      "Testing",
      c,
      null
    );

    frame.setSize(new Dimension(480,256));
    frame.show();

  }

}
