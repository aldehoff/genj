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
package genj.tool;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A panel that shows the result of a verification
 * (used in VerifyTransaction)
 */
class VerifyResultPanel extends JPanel {

  /** component showing the changes */
  private JList listOfChanges;

  /**
   * Constructor
   */
  /*package*/ VerifyResultPanel() {

    super(new BorderLayout());

    // State
    listOfChanges = new JList();

    // Layout
    add(new JLabel("The following changes have been made:"),"North");
    add(new JScrollPane(listOfChanges),"Center");

    // Done
  }

  /**
   * Set the result to be shown
   * @param entsWithNewIDs a list of entities which got new ids
   */
  public void setResult(Vector entsWithNewIDs) {

    if (entsWithNewIDs.size()==0) {
      entsWithNewIDs.addElement("-None-");
    }

    listOfChanges.setListData(entsWithNewIDs);

    // Done
  }
}
