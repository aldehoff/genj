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

import genj.common.SelectEntityWidget;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Relationship;
import genj.util.WordBuffer;
import genj.util.swing.NestedBlockLayout;
import genj.view.ViewManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Add an entity via relationship (new or existing)
 */
public class CreateRelationship extends AbstractChange {

  /** the relationship */
  private Relationship relationship;

  /** the referenced entity */
  private Entity existing;

  /** check for forcing id */
  private JCheckBox checkID;
  
  /** text field for entering id */
  private JTextField requestID;
  
  /**
   * Constructor
   */
  public CreateRelationship(Relationship relatshp, ViewManager manager) {
    super(relatshp.getGedcom(), relatshp.getImage().getOverLayed(imgNew), resources.getString("new", relatshp.getName()), manager);
    relationship = relatshp;
  }

  /**
   * Target Type
   */
  private String getTargetType() {
    return relationship.getTargetType();
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {

    WordBuffer result = new WordBuffer("\n");
    
    // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
    // This {0} will be {1}.
    result.append( existing==null ?
      resources.getString("confirm.new", new Object[]{ Gedcom.getName(getTargetType(),false), gedcom}) :
      resources.getString("confirm.use", new Object[]{ existing.getId(), gedcom})
    );
    
    // relationship detail
    result.append( resources.getString("confirm.new.related", relationship.getDescription()) );

    // Entity comment?
    result.append( resources.getString("confirm."+getTargetType()) );
    
    // A warning already?
    String warning = relationship.getWarning(existing);
    if (warning!=null) 
      result.append( "**Note**: " + warning );

    // combine
    return result.toString();
  }

  /**
   * Override content components to show to user 
   */
  protected JPanel getDialogContent() {
    
    JPanel result = new JPanel(new NestedBlockLayout("<col><row><select wx=\"1\"/></row><row><text wx=\"1\" wy=\"1\"/></row><row><check/><text/></row></col>"));

    // create selector
    final SelectEntityWidget select = new SelectEntityWidget(getTargetType(), gedcom.getEntities(getTargetType()), resources.getString("select.new"));
    select.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // grab current selection (might be null)
        existing = select.getEntity();
        // can the user force an id now?
        if (existing!=null) checkID.setSelected(false);
        checkID.setEnabled(existing==null);
        refresh();
      }
    });
 
    // prepare id checkbox and textfield
    requestID = new JTextField(gedcom.getNextAvailableID(getTargetType()), 8);
    requestID.setEditable(false);
    
    checkID = new JCheckBox(resources.getString("assign_id"));
    checkID.getModel().addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        requestID.setEditable(checkID.isSelected());
        if (checkID.isSelected())  requestID.requestFocusInWindow();
      }
    });
    
    // wrap it up
    result.add(select);
    result.add(getConfirmComponent());
    result.add(checkID);
    result.add(requestID);
   
    // done
    return result;
  }

  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // create the entity if necessary
    if (existing==null) {
      // check id
      String id = null;
      if (requestID.isEditable()) {
        id = requestID.getText();
        if (gedcom.getEntity(getTargetType(), id)!=null)
          throw new GedcomException(resources.getString("assign_id_error", id));
      }
      // focus always changes to new that we create now
      existing = gedcom.createEntity(getTargetType(), id);
      focus = existing;
      focus.addDefaultProperties();
      // perform the relationship to new
      relationship.apply(existing, true);
    } else {
      // perform the relationship to existing
      focus = relationship.apply(existing, false);
    }
    // done
  }

} //CreateRelationship

