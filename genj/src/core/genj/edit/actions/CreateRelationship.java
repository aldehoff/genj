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
import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Relationship;

import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * Add an entity via relationship (new or existing) 
 */
public class CreateRelationship extends AbstractChange {
  
  /** the relationship */
  private Relationship relationship;
  
  /** the referenced entity */
  private Entity existing;
  
  /** the target type */
  private int target;
  
  /**
   * Constructor
   */
  public CreateRelationship(Relationship relatshp) {
    super(relatshp.getGedcom(), relatshp.getImage(), resources.getString("new", relatshp.getName()));
    relationship = relatshp;
    target = relationship.getTargetType();
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    
    // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
    // This {0} will be {1}.
    String about = existing==null ?
      resources.getString("confirm.new", new Object[]{ Gedcom.getNameFor(target,false), gedcom})
     :
      resources.getString("confirm.use", new Object[]{ existing.getId(), gedcom});

    // relationship detail      
    String detail = resources.getString("confirm.new.related", relationship );
    
    // Entity comment?
    String comment = resources.getString("confirm."+Gedcom.getTagFor(target));
    
    // combine
    return about + '\n' + detail + '\n' + comment ;
  }
  
  /**
   * @see genj.edit.actions.AbstractChange#getOptions()
   */
  protected JComponent getOptions() {
    
    // selection of existing
    List ents = gedcom.getEntities(target);
    Collections.sort(ents);
    ents.add(0, "*New*" );
    
    JComboBox result = new JComboBox(ents.toArray()) {
      protected void fireActionEvent() {
        super.fireActionEvent();
        existing = getSelectedIndex()>0 ? (Entity)getSelectedItem() : null;
        super.firePropertyChange( "message", 0, 1);
      }
    };
    
    return result;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // create the entity if necessary
    if (existing==null) {
      // focus always changes to new that we create now
      focus = gedcom.createEntity(target, null);
      focus.addDefaultProperties();
      // perform the relationship to new
      relationship.apply(focus);
    } else {
      // perform the relationship to existing
      focus = relationship.apply(existing);
    }
    // done
  }
  
} //CreateRelationship

