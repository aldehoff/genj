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
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import genj.gedcom.*;
import genj.util.*;
import genj.app.*;

/**
 * A (generic) Panel for showing Options in a Transaction
 */
public class OptionsPanel extends JPanel {

  /** the transaction that this panel is used in */
  private Transaction transaction;

  /** a couple of checkboxes used for the options */
  private JCheckBox[] cbOptions;

  /**
   * Constructor
   */
  public OptionsPanel(Transaction transaction,String header,String[][] options) {

    this.transaction = transaction;

    // Prepare layout
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

    // Prepare options
    add(new JLabel(header));

    cbOptions = new JCheckBox[options.length];

    for (int o=0;o<options.length;o++) {

      // .. create component
      JCheckBox jcb = new JCheckBox();
      jcb.setText       (            options[o][0]  );
      jcb.setToolTipText(            options[o][1]  );
      jcb.setEnabled    ( "1".equals(options[o][2]) );
      jcb.setSelected   ( "1".equals(options[o][3]) );

      // .. add it
      cbOptions[o] = jcb;
      add(jcb);

      // .. next
    }

    // Done
  }

  /**
   * Return state of options
   */
  public boolean[] getState() {

    boolean[] result = new boolean[cbOptions.length];

    for (int j=0;j<cbOptions.length;j++) {
      result[j] = cbOptions[j].isSelected();
    }

    return result;
  }

}
