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

import genj.edit.actions.CreateEntity;
import genj.edit.actions.CreateRelationship;
import genj.edit.actions.DelEntity;
import genj.edit.actions.DelProperty;
import genj.edit.actions.OpenForEdit;
import genj.edit.actions.RunExternal;
import genj.edit.actions.SetSubmitter;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.Relationship;
import genj.gedcom.Submitter;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ImageIcon;
import genj.view.ContextSupport;
import genj.view.ViewFactory;
import genj.view.ViewManager;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ContextSupport {
    
  /** a noop is used for separators in returning actions */  
  private final static ActionDelegate aNOOP = ActionDelegate.NOOP;
  
  /**
   * @see genj.view.ViewFactory#createView(genj.gedcom.Gedcom, genj.util.Registry, java.awt.Frame)
   */
  public JComponent createView(Gedcom gedcom, Registry registry, Frame frame) {
    return new EditView(gedcom,registry,frame);
  }

  /**
   * @see genj.view.ViewFactory#getDefaultDimension()
   */
  public Dimension getDefaultDimension() {
    return new Dimension(256,480);
  }
  
  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return Images.imgView;
  }
  
  /**
   * @see genj.view.ViewFactory#getKey()
   */
  public String getKey() {
    return "edit";
  }

  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle(boolean abbreviate) {
    return EditView.resources.getString("title" + (abbreviate?".short":""));
  }
  
  /**
   * @see genj.view.ContextSupport#createActions(Property)
   */
  public List createActions(Property property) {
    
    // create the actions
    List result = new ArrayList();
    
    // FileAssociationActions for PropertyFile
    if (property instanceof PropertyFile)  
      createActions(result, (PropertyFile)property); 
      
    // Check what xrefs can be added
    MetaProperty[] subs = property.getMetaProperties(0);
    for (int s=0;s<subs.length;s++) {
      // create Relationship.XRef where applicable
      MetaProperty sub = subs[s]; 
      if (Relationship.XRefBy.isApplicable(sub)) {
        Relationship rel = new Relationship.XRefBy(property, sub);
        result.add(new CreateRelationship(rel));
      }
      // .. next
    }

    // delete possible
    result.add(ActionDelegate.NOOP);
    result.add(new DelProperty(property));
    
    // done
    return result;
  }

  /**
   * @see genj.view.ViewFactory#createActions(Entity)
   */
  public List createActions(Entity entity) {
    // create the actions
    List result = new ArrayList();
    
    // indi?
    if (entity instanceof Indi) createActions(result, (Indi)entity);
    // fam?
    if (entity instanceof Fam) createActions(result, (Fam)entity);
    // submitter?
    if (entity instanceof Submitter) createActions(result, (Submitter)entity);
    
    // separator
    result.add(ActionDelegate.NOOP);

    // Check what xrefs can be added
    MetaProperty[] subs = entity.getMetaProperties(0);
    for (int s=0;s<subs.length;s++) {
      // create Relationship.XRef where applicable
      MetaProperty sub = subs[s]; 
      if (Relationship.XRefBy.isApplicable(sub)) {
        Relationship rel = new Relationship.XRefBy(entity,sub);
        result.add(new CreateRelationship(rel));
      }
      // .. next
    }

    // add delete
    result.add(ActionDelegate.NOOP);
    result.add(new DelEntity(entity));
    
    // add an "edit in EditView"
    if (ViewManager.getInstance().getOpenViews(EditView.class, entity.getGedcom()).isEmpty()) {
      result.add(ActionDelegate.NOOP);
      result.add(new OpenForEdit(entity));
    }
    // done
    return result;
  }
  
  /**
   * @see genj.view.ContextMenuSupport#createActions(Gedcom)
   */
  public List createActions(Gedcom gedcom) {
    // create the actions
    List result = new ArrayList();
    result.add(new CreateEntity(gedcom, Gedcom.INDIVIDUALS ));
    result.add(new CreateEntity(gedcom, Gedcom.FAMILIES    ));
    result.add(new CreateEntity(gedcom, Gedcom.NOTES       ));
    result.add(new CreateEntity(gedcom, Gedcom.MULTIMEDIAS ));
    result.add(new CreateEntity(gedcom, Gedcom.REPOSITORIES));
    result.add(new CreateEntity(gedcom, Gedcom.SOURCES     ));
    result.add(new CreateEntity(gedcom, Gedcom.SUBMITTERS  ));
    // done
    return result;
  }

  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi) {
    result.add(new CreateRelationship(new Relationship.ChildOf(indi)));
    if (indi.getNoOfParents()<2)
      result.add(new CreateRelationship(new Relationship.ParentOf(indi)));
    result.add(new CreateRelationship(new Relationship.SpouseOf(indi)));
    result.add(new CreateRelationship(new Relationship.SiblingOf(indi)));
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam) {
    result.add(new CreateRelationship(new Relationship.ChildIn(fam)));
    if (fam.getNoOfSpouses()<2)
      result.add(new CreateRelationship(new Relationship.ParentIn(fam)));
  }
  
  /**
   * Create actions for Submitters
   */
  private void createActions(List result, Submitter submitter) {
    result.add(new SetSubmitter(submitter));
  }
  
  /**
   * Create actions for PropertyFile
   */
  /*package*/ static void createActions(List result, PropertyFile file) {

    // find suffix
    String suffix = file.getSuffix();
      
    // lookup associations
    Iterator it = FileAssociation.get(suffix).iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next(); 
      result.add(new RunExternal(file,fa));
    }
    // done
  }

} //EditViewFactory
