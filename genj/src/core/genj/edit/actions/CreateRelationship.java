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

import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * Add an entity via relationship (new or existing) 
 */
public class CreateRelationship extends AbstractChange {
  
  /** the type of the added entity*/
  private int type;
  
  /** the relationship */
  private Relationship relationship;
  
  /** the referenced entity */
  private Entity existing;
  
  /**
   * Constructor
   */
  public CreateRelationship(Gedcom ged, int typ, Relationship relatshp) {
    super(ged, newImages[typ], resources.getString("new", relatshp.getName()));
    type = typ;
    relationship = relatshp;
  }
  
  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {
    // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
    // This entity will be {0}.
    String about = existing==null ?
      resources.getString("confirm.new", new Object[]{ Gedcom.getNameFor(type,false), gedcom})
     :
      resources.getString("confirm.use", new Object[]{ existing.getId(), gedcom});
    String detail = resources.getString("confirm.new.related", relationship);
    return about + '\n' + detail;
  }
  
  /**
   * @see genj.edit.actions.AbstractChange#getOptions()
   */
  protected JComponent getOptions() {
    
    // selection of existing
    Vector ents = new Vector(gedcom.getEntities(type));
    ents.add(0, "*New*" );
    
    JComboBox result = new JComboBox(ents) {
      protected void fireActionEvent() {
        super.fireActionEvent();
        existing = getSelectedIndex()>0 ? (Entity)getSelectedItem() : null;
        super.firePropertyChange( "selection", -1, getSelectedIndex());
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
      focus = gedcom.createEntity(type, null);
      focus.getProperty().addDefaultProperties();
      // perform the relationship to new
      relationship.apply(focus);
    } else {
      // perform the relationship to existing
      relationship.apply(existing);
    }
    // done
  }
  
} //Relate

//  /**
//   * Create an association 
//   */
//  /*package*/ static class Link extends Change {
//    /** where we link from */
//    private Property from;
//    /**
//     * Constructor
//     */
//    /*package*/ Link(Property frOm) {
//      super(frOm.getGedcom(), Images.imgNewLink, "Add Link To ...");
//      // remember
//      from = frOm;
//    }
//    /**
//     * @see genj.edit.EditViewFactory.Change#change()
//     */
//    protected void change() throws GedcomException {
//    }
//    /**
//     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
//     */
//    protected Object getConfirmMessage() {
//      return new JLabel("Foo");
//    }
//  } //Associate

