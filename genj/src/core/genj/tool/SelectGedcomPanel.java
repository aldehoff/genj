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

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import genj.gedcom.*;
import genj.util.*;
import genj.app.*;

/**
 * A panel for selecting a Gedcom (has to be loaded into ControlCenter)
 * (used in MergeTransaction [twice] and VerifyTransaction)
 */
public class SelectGedcomPanel extends JPanel {

  /** the transaction that this panel is used in */
  private Transaction           transaction;

  /** a combobox to choose from */
  private JComboBox             chooser;

  /** static text */
  private final static String[] header = {
    "Please select a Gedcom file:"
  };

  /**
   * Construktor
   * @param transaction the Transaction this Panel is used in
   */
  public SelectGedcomPanel(String explanation, Vector gedcoms) {

    // Prepare components
    GridBagHelper helper = new GridBagHelper(this);

    // .. heading description
    int row=0;
    for (int i=0;i<header.length;i++,row++) {
      helper.add(new JLabel(header[i]),0,row,1,1,helper.GROW_HORIZONTAL);
    }

    // .. candidate
    chooser = new JComboBox(gedcoms);
    helper.add(chooser,0,row++,1,1,helper.GROW_HORIZONTAL);

    // .. explanation
    JTextPane tExplanation = new JTextPane();
    tExplanation.setEnabled(false);
    tExplanation.setText(explanation);
    JScrollPane sExplanation = new JScrollPane(
      tExplanation,
      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );
    sExplanation.setPreferredSize(new Dimension(64,64));

    helper.add(sExplanation,0,row++,1,1,helper.GROW_BOTH);

    // Done
  }

  /**
   * Bound property support
   */
  public void addActionListener(ActionListener listener) {
    chooser.addActionListener(listener);
  }

  /**
   * Returns the selected Gedcom
   */
  public Gedcom getGedcom() {
    return (Gedcom)chooser.getSelectedItem();
  }

  /**
   * Bound property support
   */
  public void removeActionListener(ActionListener listener) {
    chooser.removeActionListener(listener);
  }

  /**
   * Sets the selected Gedcom
   */
  public void setCandidate(Gedcom which) {
    chooser.setSelectedItem(which);
  }

}

