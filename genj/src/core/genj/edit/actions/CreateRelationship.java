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
import genj.gedcom.PropertyComparator;
import genj.gedcom.Relationship;
import genj.util.swing.ChoiceWidget;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;

/**
 * Add an entity via relationship (new or existing)
 */
public class CreateRelationship extends AbstractChange {

  /** the target type */
  private int targetType;

  /** the relationship */
  private Relationship relationship;

  /** the referenced entity */
  private Entity existing;

  /**
   * Constructor
   */
  public CreateRelationship(Relationship relatshp, ViewManager manager) {
    super(relatshp.getGedcom(), relatshp.getImage().getOverLayed(imgNew), resources.getString("new", relatshp.getName(false)), manager);
    relationship = relatshp;
  }

  /**
   * @see genj.edit.actions.CreateRelationship#execute()
   */
  protected void execute() {
    // check if we have to choose a target type
    int[] types = relationship.getTargetTypes();
    if (types.length>1) {
      // collect names of types
      String[] names = new String[types.length];
      for (int n=0;n<types.length;n++) names[n] = Gedcom.getNameFor(types[n], false);
      // show dialog
      int choice = manager.getWindowManager().openDialog(
        null,
        relationship.getName(false),
        WindowManager.IMG_QUESTION,
        relationship.getName(true),
        names,
        target
      );
      if (choice<0) return;
      // set targetType
      targetType = types[choice];
    } else {
      targetType = types[0];
    }

    // continue
    super.execute();
  }

  /**
   * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
   */
  protected String getConfirmMessage() {

    // You are about to create a {0} in {1}! / You are about to reference {0} in {1}!
    // This {0} will be {1}.
    String about = existing==null ?
      resources.getString("confirm.new", new Object[]{ Gedcom.getNameFor(targetType,false), gedcom})
     :
      resources.getString("confirm.use", new Object[]{ existing.getId(), gedcom});

    // relationship detail
    String detail = resources.getString("confirm.new.related", relationship.getName(true) );

    // Entity comment?
    String comment = resources.getString("confirm."+Gedcom.getTagFor(targetType));

    // combine
    return about + '\n' + detail + '\n' + comment ;
  }

  /**
   * @see genj.edit.actions.AbstractChange#getOptions()
   */
  protected JComponent getOptions() {

    // selection of existing
    List ents = gedcom.getEntities(targetType);

    // sort list
    if (targetType == gedcom.INDIVIDUALS) {
      // Individiauls are sorted by name
      Collections.sort(ents, new PropertyComparator("INDI:NAME"));
    }
    else {
      Collections.sort(ents);
    }
    ents.add(0, "*New*" );

    final ChoiceWidget result = new ChoiceWidget(ents);
    result.setEditable(false);
    result.setSelectedIndex(0);
    result.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Object item = e.getItem();
        existing = item instanceof Entity ? (Entity)item : null;
        // refresh abstract change
        refresh();
      }

    });

    // done
    return result;
  }

  /**
   * @see genj.edit.EditViewFactory.Change#change()
   */
  protected void change() throws GedcomException {
    // create the entity if necessary
    if (existing==null) {
      // focus always changes to new that we create now
      focus = gedcom.createEntity(targetType);
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

