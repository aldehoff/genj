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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextFieldWidget;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
class ProxyEntity extends Proxy {

  /** members */
  private JTextField tfield;
  private AbstractButton bchange;
  private EditView edit;

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
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    // Look for entity
    if (!(property instanceof Entity)) return null;
    Entity e = (Entity)property;

    // Label
    JLabel lid = new JLabel(EditView.resources.getString("proxy.id_of_entity"));
    in.add(lid);

    // ID ?
    tfield = new TextFieldWidget(e.getId(), 80);
    tfield.setToolTipText(resources.getString("proxy.enter_id_here"));
    tfield.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        bchange.setEnabled(true);
      }
      public void insertUpdate(DocumentEvent e) {
        bchange.setEnabled(true);
      }
      public void removeUpdate(DocumentEvent e) {
        bchange.setEnabled(true);
      }
    });
    in.add(tfield);
    
    bchange = new ButtonHelper().setContainer(in).setEnabled(false).create(new ActionChange());

    // Done
    return tfield;
  }

  /**
   * Action for changing ID
   */
  private class ActionChange extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionChange() {
      setText(resources.getString("proxy.change"));
      setEnabled(false);
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // Warn about this action's side-effects
      int rc = JOptionPane.showConfirmDialog(
        edit.getFrame(),
        EditView.resources.getString("proxy.change_id?"),
        EditView.resources.getString("warning"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
      );
      if (rc==JOptionPane.NO_OPTION) return;

      // Calc parms
      Gedcom gedcom = property.getGedcom();
      Entity entity = property.getEntity();
      
      // Can I write ?
      if (!gedcom.startTransaction()) return;

      // Do the change
      gedcom.setId(entity,tfield.getText());
      
      // done
      gedcom.endTransaction();
      bchange.setEnabled(false);
    }

  } //ActionChange
  
} //ProxyEntity
