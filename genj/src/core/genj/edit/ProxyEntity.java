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

import java.awt.*;
import java.awt.event.*;

import genj.gedcom.*;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
class ProxyEntity extends Proxy implements ActionListener, DocumentListener {

  /** members */
  private JTextField tfield;
  private JButton bchange;
  private EditView edit;

  /**
   * When button is pressed
   */
  public void actionPerformed(ActionEvent e) {

    // Warn about this action's side-effects
    int rc = JOptionPane.showConfirmDialog(
      edit.getFrame(),
      EditView.resources.getString("proxy.change_id?"),
      EditView.resources.getString("warning"),
      JOptionPane.YES_NO_OPTION,
      JOptionPane.WARNING_MESSAGE
    );

    if (rc==JOptionPane.NO_OPTION) {
      return;
    }

    // Calc parms
    Gedcom gedcom = prop.getGedcom();
    Entity entity = prop.getEntity();

    // Can I write ?
    if (!gedcom.startTransaction()) {
      return;
    }

    // Do the change
    try {

      // .. id
      gedcom.setIdOf(entity,tfield.getText());

      // .. button off
      bchange.setEnabled(false);

    } catch (GedcomException ex) {

      // .. error
      JOptionPane.showMessageDialog(
        edit.getFrame(),
        ex.getMessage(),
        EditView.resources.getString("error"),
        JOptionPane.ERROR_MESSAGE
      );

    } finally {
      gedcom.endTransaction();
    }

    // Done
  }

  /**
   * Trigger for Document changes
   */
  public void changedUpdate(DocumentEvent e) {
    bchange.setEnabled(true);
  }

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Trigger for Document changes
   */
  public void insertUpdate(DocumentEvent e) {
    bchange.setEnabled(true);
  }

  /**
   * Trigger for Document changes
   */
  public void removeUpdate(DocumentEvent e) {
    bchange.setEnabled(true);
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property prop, EditView edit) {

    // Store some data
    this.edit=edit;
    this.prop=prop;

    // Look for entity
    Entity e = (Entity)prop;

    // Label
    JLabel lid = new JLabel(EditView.resources.getString("proxy.id_of_entity"));
    in.add(lid);

    // ID ?
    tfield = createTextField(e.getId(), "!VALUE", this, EditView.resources.getString("proxy.enter_id_here"));
    in.add(tfield);

    bchange = createButton(
      EditView.resources.getString("proxy.change"),
      "CHANGE",
      false,
      this,
      null
    );
    in.add(bchange);

    tfield.requestFocus();

    // Done
  }

}
