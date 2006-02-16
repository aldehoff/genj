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
import genj.edit.actions.CreateEntity;
import genj.edit.actions.CreateRelationship;
import genj.edit.actions.DelEntity;
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
import genj.gedcom.PropertyXRef;
import genj.gedcom.Relationship;
import genj.gedcom.Submitter;
import genj.gedcom.Relationship.XRefBy;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ImageIcon;
import genj.view.ActionProvider;
import genj.view.Context;
import genj.view.ContextListener;
import genj.view.ContextSelectionEvent;
import genj.view.ViewFactory;
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ActionProvider, ContextListener {
    
  /** a noop is used for separators in returning actions */  
  private final static ActionDelegate aNOOP = ActionDelegate.NOOP;
  
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
  
  /**
   * Callback - context change information
   */
  public void handleContextSelectionEvent(ContextSelectionEvent event) {
    Context context = event.getContext();
    ViewManager manager = context.getManager();
    // editor needed?
    if (!Options.getInstance().isOpenEditor)
      return;
    // what's the entity
    Entity entity = context.getEntity();
    // noop if EditView non-sticky or current is open
    EditView[] edits = EditView.getInstances(context.getGedcom());
    for (int i=0;i<edits.length;i++) {
      if (!edits[i].isSticky()||edits[i].getEntity()==entity) 
        return;
    }
    // open
    new OpenForEdit(context, manager).trigger();
  }
  
  /**
   * @see genj.view.ActionProvider#createActions(Entity[], ViewManager)
   */
  public List createActions(Entity[] entities, ViewManager manager) {
    // not supported
    return null;
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
          (type==PropertyMedia.class&&genj.gedcom.Options.getInstance().isAllowNewOBJEctEntities) 
        ) {
        // .. make sure @@ forces a non-substitute!
        result.add(new CreateRelationship(new XRefBy(property, (PropertyXRef)subs[s].create("@@")), manager));
        // continue
        continue;
      }
    }
    
    // Add Association to this one (*only* for events)
    if (property instanceof PropertyEvent)
      result.add(new CreateRelationship(new Relationship.Association(property), manager));
    
    // Toggle "Private"
    if (Enigma.isAvailable())
      result.add(new TogglePrivate(property, manager));

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
    result.add(ActionDelegate.NOOP);

    // Check what xrefs can be added
    MetaProperty[] subs = entity.getNestedMetaProperties(0);
    for (int s=0;s<subs.length;s++) {
      // NOTE||REPO||SOUR||SUBM
      Class type = subs[s].getType();
      if (type==PropertyNote.class||
          type==PropertyRepository.class||
          type==PropertySource.class||
          type==PropertySubmitter.class||
          (type==PropertyMedia.class&&genj.gedcom.Options.getInstance().isAllowNewOBJEctEntities) 
          ) {
        // .. make sure @@ forces a non-substitute!
        result.add(new CreateRelationship(new Relationship.XRefBy(entity, (PropertyXRef)subs[s].create("@@")), manager));
      }
    }

    // add delete
    result.add(ActionDelegate.NOOP);
    result.add(new DelEntity(entity, manager));
    
    // add an "edit in EditView"
    EditView[] edits = EditView.getInstances(entity.getGedcom());
    if (edits.length==0) {
      result.add(ActionDelegate.NOOP);
      result.add(new OpenForEdit(new Context(entity), manager));
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
    if (genj.gedcom.Options.getInstance().isAllowNewOBJEctEntities)
      result.add(new CreateEntity(gedcom, Gedcom.OBJE, manager));
    result.add(new CreateEntity(gedcom, Gedcom.REPO, manager));
    result.add(new CreateEntity(gedcom, Gedcom.SOUR, manager));
    result.add(new CreateEntity(gedcom, Gedcom.SUBM, manager));

    result.add(ActionDelegate.NOOP);
    result.add(new Undo(gedcom));
    result.add(new Redo(gedcom));

    // done
    return result;
  }

  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi, ViewManager manager) {
    result.add(new CreateRelationship(new Relationship.ChildOf(indi), manager));
    result.add(new CreateRelationship(new Relationship.ParentOf(indi), manager));
    result.add(new CreateRelationship(new Relationship.SpouseOf(indi), manager));
    result.add(new CreateRelationship(new Relationship.SiblingOf(indi), manager));
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam, ViewManager manager) {
    result.add(new CreateRelationship(new Relationship.ChildIn(fam), manager));
    if (fam.getNoOfSpouses()<2)
      result.add(new CreateRelationship(new Relationship.ParentIn(fam), manager));
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
    Iterator it = FileAssociation.getAll(suffix).iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next(); 
      result.add(new RunExternal(file,fa));
    }
    // done
  }

} //EditViewFactory
