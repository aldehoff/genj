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
package genj.edit;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import genj.util.swing.DateEntry;
import genj.gedcom.*;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : DATE
 */
class ProxyDate extends Proxy implements ItemListener {

  /** members */
  private boolean formatChanged = false;
  private int currentDate;
  private JComboBox combo;
  private DateEntry deOne, deTwo;

  private static final boolean drange[] = {
    false,
    true,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false
  };

  /**
   * Finish proxying edit for property Date
   */
  protected void finish() {

    // Something changed ?
    if (!hasChanged()) {
      return;
    }

    PropertyDate p = (PropertyDate)prop;

    // Remember format
    p.setFormat(combo.getSelectedIndex());

    // Remember One
    p.getStart().set(deOne.getDay(),deOne.getMonth(),deOne.getYear());

    // Remember Two
    if ( p.isRange() )
      p.getEnd().set(deTwo.getDay(),deTwo.getMonth(),deTwo.getYear());

    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return ( (deOne.hasChanged()) || (deTwo.hasChanged()) || (formatChanged) );
  }

  /**
   * Trigger for changes in editing components
   */
  public void itemStateChanged(ItemEvent e) {

    formatChanged = true;

    if (PropertyDate.isRange(combo.getSelectedIndex()))
      deOne.getParent().add(deTwo);
    else
      deOne.getParent().remove(deTwo);

    deOne.getParent().invalidate();
    deOne.getParent().validate();
    // Done
  }          

  /**
   * Starts Proxying edit for property Date by filling a vector with
   * components to edit this property
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    prop=setProp;
    PropertyDate p = (PropertyDate)prop;

    // Components
    combo = new JComboBox();
    combo.setAlignmentX(0);
    combo.setEditable(false);
    combo.setMaximumSize(new Dimension(Integer.MAX_VALUE,combo.getPreferredSize().height));

    for (int i = 0; i <= PropertyDate.LAST_ATTRIB; i++) {
      combo.addItem(PropertyDate.getLabelForFormat(i));
    }
    in.add(combo);
    combo.addItemListener(this);

    deOne = new DateEntry(p.getStart().getDay(),p.getStart().getMonth(),p.getStart().getYear());
    deOne.setAlignmentX(0);
    in.add(deOne);

    deTwo = new DateEntry(p.getEnd().getDay(),p.getEnd().getMonth(),p.getEnd().getYear());
    deTwo.setAlignmentX(0);

    combo.setSelectedIndex( p.getFormat() );

//    in.add(Box.createVerticalGlue());

    // Done
    deOne.requestFocus();

  }

}
