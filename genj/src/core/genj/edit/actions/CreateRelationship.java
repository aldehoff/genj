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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Relationship;
import genj.util.WordBuffer;
import genj.view.ViewManager;
import genj.view.widgets.SelectEntityWidget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

/**
 * Add an entity via relationship (new or existing)
 */
public class CreateRelationship extends AbstractChange {

  /** the relationship */
  private Relationship relationship;

  /** the referenced entity */
  private Entity existing;

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

    WordBuffer result = new WordBuffer('\n');
    
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
    
    // A warning?
    String warning = relationship.getWarning();
    if (warning!=null) 
      result.append( "**Note**: " + warning );

    // combine
    return result.toString();
  }

  /**
   * @see genj.edit.actions.AbstractChange#getOptions()
   */
  protected JComponent getOptions() {

    final SelectEntityWidget result = new SelectEntityWidget(getTargetType(), gedcom.getEntities(getTargetType()), "*New*");
    result.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // grab current selection (might be null)
        existing = result.getEntity();
        refresh();
      }
    });
    
    return result;
  }

  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // create the entity if necessary
    if (existing==null) {
      // focus always changes to new that we create now
      existing = gedcom.createEntity(getTargetType());
      focus = existing;
      focus.addDefaultProperties();
      // perform the relationship to new
      relationship.apply(existing);
    } else {
      // perform the relationship to existing
      focus = relationship.apply(existing);
    }
    // done
  }

} //CreateRelationship

