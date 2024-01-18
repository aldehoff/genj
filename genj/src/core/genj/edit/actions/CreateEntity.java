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
package genj.edit.actions;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.swing.NestedBlockLayout;
import genj.view.ViewManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Add a new entity  
 */
public class CreateEntity extends AbstractChange {

  /** the type of the added entity*/
  private String etag;
  
  /** text field for entering id */
  private JTextField requestID;
  
  /**
   * Constructor
   */
  public CreateEntity(Gedcom ged, String tag, ViewManager manager) {
    super(ged, Gedcom.getEntityImage(tag).getOverLayed(imgNew), resources.getString("new", Gedcom.getName(tag, false) ), manager);
    etag = tag;
  }
  
  /**
   * 
   */
  protected JComponent getOptions() {

    // prepare id checkbox and textfield
    requestID = new JTextField(gedcom.getNextAvailableID(etag), 8);
    requestID.setEditable(false);
    
    final JCheckBox check = new JCheckBox(resources.getString("assign_id"));
    check.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        requestID.setEditable(check.isSelected());
        if (check.isSelected())  requestID.requestFocusInWindow();
      }
    });
    
    // wrap up
    JPanel panel = new JPanel(new NestedBlockLayout("<row><check/><id/></row>"));
    panel.add(check);
    panel.add(requestID);
    
    // done
    return panel;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    // You are about to create a {0} in {1}!
    String about = resources.getString("confirm.new", new Object[]{ Gedcom.getName(etag,false), gedcom});
    // This entity will not be connected ... 
    String detail = resources.getString("confirm.new.unrelated");
    // Entity comment?
    String comment = resources.getString("confirm."+etag);
    // done
    return about + '\n' + detail + '\n' + comment ;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // check id
    String id = null;
    if (requestID.isEditable()) {
      id = requestID.getText();
      if (gedcom.getEntity(etag, id)!=null)
        throw new GedcomException(resources.getString("assign_id_error", id));
    }
    // create the entity
    focus = gedcom.createEntity(etag, id);
    focus.addDefaultProperties();
    // done
  }
  
} //Create

