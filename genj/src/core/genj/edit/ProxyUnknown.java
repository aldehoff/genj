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
import javax.swing.event.*;
import java.util.Vector;

import genj.gedcom.Property;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
class ProxyUnknown extends Proxy implements DocumentListener {

  /** members */
  private boolean changed;
  private JTextField tfield;

  /**
   * Change notification
   */
  public void changedUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    // Has something been edited ?
    if ( !hasChanged() )
      return;

    // Store changed value
    prop.setValue(tfield.getText());

    // Done
    return;
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return changed;
  }

  /**
   * Document event - insert
   */
  public void insertUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Document event - remove
   */
  public void removeUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    prop=setProp;

    // Setup controls
    tfield = createTextField(prop.getValue(), "!VALUE", this, null);
    in.add(tfield);
    tfield.requestFocus();
    // Done
  }

}
