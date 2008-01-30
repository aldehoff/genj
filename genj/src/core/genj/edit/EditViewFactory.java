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

import genj.crypto.Enigma;
import genj.edit.actions.CreateAlias;
import genj.edit.actions.CreateAssociation;
import genj.edit.actions.CreateChild;
import genj.edit.actions.CreateEntity;
import genj.edit.actions.CreateParent;
import genj.edit.actions.CreateSibling;
import genj.edit.actions.CreateSpouse;
import genj.edit.actions.CreateXReference;
import genj.edit.actions.DelEntity;
import genj.edit.actions.DelProperty;
import genj.edit.actions.OpenForEdit;
import genj.edit.actions.Redo;
import genj.edit.actions.RunExternal;
import genj.edit.actions.SetPlaceHierarchy;
import genj.edit.actions.SetSubmitter;
import genj.edit.actions.SwapSpouses;
import genj.edit.actions.TogglePrivate;
import genj.edit.actions.Undo;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyNote;
import genj.gedcom.PropertyPlace;
import genj.gedcom.PropertyRepository;
import genj.gedcom.PropertySource;
import genj.gedcom.PropertySubmitter;
import genj.gedcom.Submitter;
import genj.io.FileAssociation;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.view.ActionProvider;
import genj.view.ViewContext;
import genj.view.ViewFactory;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ActionProvider {
    
  /** a noop is used for separators in returning actions */  
  private final static Action2 aNOOP = Action2.NOOP;
  
  /**
   * @see genj.view.ViewFactory#createView(genj.gedcom.Gedcom, genj.util.Registry, java.awt.Frame)
   */
  public JComponent createView(String title, Gedcom gedcom, Registry registry, ViewManager manager) {
    return new EditView(title, gedcom, registry, manager);
  }

  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return Images.imgView;
  }
  
  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle(boolean abbreviate) {
    return EditView.resources.getString("title" + (abbreviate?".short":""));
  }

// FIXME need to provide for auto-open edit on selection
//  /**
//   * Callback - context change information
//   */
//  public void handleContextSelectionEvent(ContextSelectionEvent event) {
//    ViewContext context = event.getContext();
//    ViewManager manager = context.getManager();
//    // editor needed?
//    if (!Options.getInstance().isOpenEditor)
//      return;
//    // what's the entity
//    Entity[] entities = context.getEntities();
//    if (entities.length!=1)
//      return;
//    Entity entity = entities[0];
//    // noop if EditView non-sticky or current is open
//    EditView[] edits = EditView.getInstances(context.getGedcom());
//    for (int i=0;i<edits.length;i++) {
//      if (!edits[i].isSticky()||edits[i].getEntity()==entity) 
//        return;
//    }
//    // open
//    new OpenForEdit(context, manager).trigger();
//  }
  
  /**
   * @see genj.view.ActionProvider#createActions(Entity[], ViewManager)
   */
  public List createActions(Property[] properties, ViewManager manager) {
    List result = new ArrayList();
    // not accepting any entities here
    for (int i = 0; i < properties.length; i++) 
      if (properties[i] instanceof Entity) return result;
    // Toggle "Private"
    if (Enigma.isAvailable())
      result.add(new TogglePrivate(properties[0].getGedcom(), Arrays.asList(properties), manager));
    // Delete
    result.add(new DelProperty(properties, manager));
    // done
    return result;
  }

  /**
   * @see genj.view.ContextSupport#createActions(Property)
   */
  public List createActions(Property property, ViewManager manager) {
    
    // create the actions
    List result = new ArrayList();
    
    // FileAssociationActions for PropertyFile
    if (property instanceof PropertyFile)  
      createActions(result, (PropertyFile)property); 
      
    // Place format for PropertyFile
    if (property instanceof PropertyPlace)  
      result.add(new SetPlaceHierarchy((PropertyPlace)property, manager)); 
      
    // Check what xrefs can be added
    MetaProperty[] subs = property.getNestedMetaProperties(0);
    for (int s=0;s<subs.length;s++) {
      // NOTE REPO SOUR SUBM (BIRT|ADOP)FAMC
      Class type = subs[s].getType();
      if (type==PropertyNote.class||
          type==PropertyRepository.class||
          type==PropertySource.class||
          type==PropertySubmitter.class||
          type==PropertyFamilyChild.class||
          type==PropertyMedia.class 
        ) {
        // .. make sure @@ forces a non-substitute!
        result.add(new CreateXReference(property,subs[s].getTag(), manager));
        // continue
        continue;
      }
    }
    
    // Add Association to this one (*only* for events)
    if (property instanceof PropertyEvent)
      result.add(new CreateAssociation(property, manager));
    
    // Toggle "Private"
    if (Enigma.isAvailable())
      result.add(new TogglePrivate(property.getGedcom(), Collections.singletonList(property), manager));
    
    // Delete
    if (!property.isTransient()) 
      result.add(new DelProperty(property, manager));

    // done
    return result;
  }

  /**
   * @see genj.view.ViewFactory#createActions(Entity)
   */
  public List createActions(Entity entity, ViewManager manager) {
    // create the actions
    List result = new ArrayList();
    
    // indi?
    if (entity instanceof Indi) createActions(result, (Indi)entity, manager);
    // fam?
    if (entity instanceof Fam) createActions(result, (Fam)entity, manager);
    // submitter?
    if (entity instanceof Submitter) createActions(result, (Submitter)entity, manager);
    
    // separator
    result.add(Action2.NOOP);

    // Check what xrefs can be added
    MetaProperty[] subs = entity.getNestedMetaProperties(0);
    for (int s=0;s<subs.length;s++) {
      // NOTE||REPO||SOUR||SUBM
      Class type = subs[s].getType();
      if (type==PropertyNote.class||
          type==PropertyRepository.class||
          type==PropertySource.class||
          type==PropertySubmitter.class||
          type==PropertyMedia.class
          ) {
        result.add(new CreateXReference(entity,subs[s].getTag(), manager));
      }
    }

    // add delete
    result.add(Action2.NOOP);
    result.add(new DelEntity(entity, manager));
    
    // add an "edit in EditView"
    EditView[] edits = EditView.getInstances(entity.getGedcom());
    if (edits.length==0) {
      result.add(Action2.NOOP);
      result.add(new OpenForEdit(new ViewContext(entity), manager));
    }
    // done
    return result;
  }

  /**
   * @see genj.view.ContextMenuSupport#createActions(Gedcom)
   */
  public List createActions(Gedcom gedcom, ViewManager manager) {
    // create the actions
    List result = new ArrayList();
    result.add(new CreateEntity(gedcom, Gedcom.INDI, manager));
    result.add(new CreateEntity(gedcom, Gedcom.FAM , manager));
    result.add(new CreateEntity(gedcom, Gedcom.NOTE, manager));
    result.add(new CreateEntity(gedcom, Gedcom.OBJE, manager));
    result.add(new CreateEntity(gedcom, Gedcom.REPO, manager));
    result.add(new CreateEntity(gedcom, Gedcom.SOUR, manager));
    result.add(new CreateEntity(gedcom, Gedcom.SUBM, manager));

    result.add(Action2.NOOP);
    result.add(new Undo(gedcom, gedcom.canUndo()));
    result.add(new Redo(gedcom, gedcom.canRedo()));

    // done
    return result;
  }

  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi, ViewManager manager) {
    result.add(new CreateChild(indi, manager, true));
    result.add(new CreateChild(indi, manager, false));
    result.add(new CreateParent(indi, manager));
    result.add(new CreateSpouse(indi, manager));
    result.add(new CreateSibling(indi, manager, true));
    result.add(new CreateSibling(indi, manager, false));
    result.add(new CreateAlias(indi, manager));
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam, ViewManager manager) {
    result.add(new CreateChild(fam, manager, true));
    result.add(new CreateChild(fam, manager, false));
    if (fam.getNoOfSpouses()<2)
      result.add(new CreateParent(fam, manager));
    if (fam.getNoOfSpouses()!=0)
      result.add(new SwapSpouses(fam, manager));
  }
  
  /**
   * Create actions for Submitters
   */
  private void createActions(List result, Submitter submitter, ViewManager manager) {
    result.add(new SetSubmitter(submitter, manager));
  }
  
  /**  
   * Create actions for PropertyFile
   */
  public static void createActions(List result, PropertyFile file) {

    // find suffix
    String suffix = file.getSuffix();
      
    // lookup associations
    List assocs = FileAssociation.getAll(suffix);
    if (assocs.isEmpty()) {
      result.add(new RunExternal(file));
    } else {
      for (Iterator it = assocs.iterator(); it.hasNext(); ) {
        FileAssociation fa = (FileAssociation)it.next(); 
        result.add(new RunExternal(file,fa));
      }
    }
    // done
  }

} //EditViewFactory
