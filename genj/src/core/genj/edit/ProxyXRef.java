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
import genj.gedcom.IconValueAvailable;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import genj.util.swing.ButtonHelper;
import genj.util.swing.ImageIcon;
import genj.view.ViewManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A proxy for a property that links entities
 */
class ProxyXRef extends Proxy implements ActionListener  {

  /** the editor */
  /*package*/ EditView edit;

  /** the textfield we use */
  private JTextField tfield;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    // Has something been edited ?
    if (!hasChanged()) return;

    // Store changed value
    prop.setValue(tfield.getText());

    // Done
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    // get entity
    Entity target = ((PropertyXRef)prop).getReferencedEntity();
    if (target!=null) {
      boolean sticky = edit.setSticky(false);
      ViewManager.getInstance().setCurrentEntity(target);
      edit.setSticky(sticky);
    }
    // done 
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {

    // Already Linked?
    if (tfield==null) {
      return false;
    }

    String id = ((PropertyXRef)prop).getReferencedId();
    return !tfield.getText().equals(id);
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    // Remember property & edit
    prop=setProp;
    this.edit=edit;

    // Calculate reference information
    PropertyXRef pxref = (PropertyXRef) prop;

    // Valid link ?
    if (pxref.getReferencedEntity()!=null) {
      
      // Create a link/jump button
      Property p = pxref.getReferencedEntity();
      JButton b = createButton(
        EditView.resources.getString("proxy.jump_to",pxref.getReferencedEntity().getId()),
        "JUMP",
        true,
        this,
        p.getImage(true)
      );
      in.add(b);
      
      // Hack to show image for referenced Blob|Image
      ImageIcon img = null;
      if (p instanceof IconValueAvailable) {
        img = ((IconValueAvailable)p).getValueAsIcon();
      }
      JComponent preview;
      if (img!=null) {
        preview = new JLabel(img);
      } else {
        preview = new JTextArea(p.toString());
        preview.setEnabled(false);
      }

      JScrollPane jsp = new JScrollPane(preview);
      jsp.setAlignmentX(0F);
      in.add(jsp);
      
      // done
      return;
    }

    // Not valid link ?
    tfield = createTextField(
      pxref.getReferencedId(),
      "!VALUE",
      null,
      EditView.resources.getString("proxy.enter_id_here")
    );
    in.add(tfield);
    ButtonHelper.requestFocusFor(tfield);

    // Done
  }

}
