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
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyNote;
import genj.gedcom.Relationship;
import genj.io.FileAssociation;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.view.ContextSupport;
import genj.view.ViewFactory;
import genj.view.ViewManager;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ContextSupport {
  
  /** resources we use */
  private final static Resources resources = EditView.resources;

  /** a noop is used for separators in returning actions */  
  private final static ActionDelegate aNOOP = ActionDelegate.NOOP;
  
  /** images we use for new entities */
  private final static ImageIcon[] newImages = new ImageIcon[] {
    Images.imgNewIndi,
    Images.imgNewFam,
    Images.imgNewMedia,
    Images.imgNewNote,
    Images.imgNewSource,
    Images.imgNewSubmitter,
    Images.imgNewRepository,
  };

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
    return resources.getString("title" + (abbreviate?".short":""));
  }
  
  /**
   * @see genj.view.ContextSupport#createActions(Property)
   */
  public List createActions(Property property) {
    // create the actions
    List result = new ArrayList();
    // add FileAssociations for PropertyFile
    if (property instanceof PropertyFile) { 
      createActions(result, (PropertyFile)property); 
    }
    // everything but a note can get a note attached
    if (!(property instanceof PropertyNote))
      result.add(new Create(property.getGedcom(), Gedcom.NOTES       , new Relationship.LinkedBy(property,Gedcom.NOTES)));
    // event can get can get an obje|SOUR attached
    if (property instanceof PropertyEvent) {
      result.add(new Create(property.getGedcom(), Gedcom.MULTIMEDIAS , new Relationship.LinkedBy(property,Gedcom.MULTIMEDIAS)));
      result.add(new Create(property.getGedcom(), Gedcom.SOURCES     , new Relationship.LinkedBy(property,Gedcom.SOURCES)));
    }
    // delete possible
    result.add(new PDelete(property));
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
    // add standards
    result.add(ActionDelegate.NOOP);
    if (!(entity instanceof Note)) 
      result.add(new Create(entity.getGedcom(), Gedcom.NOTES       , new Relationship.LinkedBy(entity.getProperty(),Gedcom.NOTES)));
    if (entity instanceof Indi||entity instanceof Fam) {
      result.add(new Create(entity.getGedcom(), Gedcom.MULTIMEDIAS , new Relationship.LinkedBy(entity.getProperty(),Gedcom.MULTIMEDIAS)));
      result.add(new Create(entity.getGedcom(), Gedcom.SOURCES     , new Relationship.LinkedBy(entity.getProperty(),Gedcom.SOURCES)));
      result.add(new Create(entity.getGedcom(), Gedcom.SUBMITTERS  , new Relationship.LinkedBy(entity.getProperty(),Gedcom.SUBMITTERS)));
      result.add(new Create(entity.getGedcom(), Gedcom.REPOSITORIES, new Relationship.LinkedBy(entity.getProperty(),Gedcom.REPOSITORIES)));
    }
    // add delete
    result.add(ActionDelegate.NOOP);
    result.add(new EDelete(entity));
    // add an "edit in EditView"
    if (ViewManager.getInstance().getOpenViews(EditView.class).isEmpty()) {
      result.add(ActionDelegate.NOOP);
      result.add(new Edit(entity));
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
    createActions(result, gedcom);
    // done
    return result;
  }

  /**
   * Create actions for Gedcom
   */
  private void createActions(List result, Gedcom gedcom) {
    result.add(new Create(gedcom, Gedcom.INDIVIDUALS , null));
    result.add(new Create(gedcom, Gedcom.FAMILIES    , null));
    result.add(new Create(gedcom, Gedcom.NOTES       , null));
    result.add(new Create(gedcom, Gedcom.MULTIMEDIAS , null));
    result.add(new Create(gedcom, Gedcom.REPOSITORIES, null));
    result.add(new Create(gedcom, Gedcom.SOURCES     , null));
    result.add(new Create(gedcom, Gedcom.SUBMITTERS  , null));
  }
  
  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi) {
    result.add(new Create(indi.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.ChildOf(indi)));
    if (indi.getNoOfParents()<2)
      result.add(new Create(indi.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.ParentOf(indi)));
    result.add(new Create(indi.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.SpouseOf(indi)));
    result.add(new Create(indi.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.SiblingOf(indi)));
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam) {
    result.add(new Create(fam.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.ChildIn(fam)));
    if (fam.getNoOfSpouses()<2)
      result.add(new Create(fam.getGedcom(), Gedcom.INDIVIDUALS , new Relationship.ParentIn(fam)));
  }
  
  /**
   * Create actions for PropertyFile
   */
  /*package*/ static void createActions(List result, PropertyFile file) {
    
    // find suffix
    String suffix = file.getValue();
    if (suffix==null) return;
    int i = suffix.lastIndexOf('.');
    if (i<0) return;
    suffix = suffix.substring(i+1);
    // lookup associations
    Iterator it = FileAssociation.get(suffix).iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next(); 
      result.add(new External(file,fa));
    }
    // done
  }
  
  
  /**
   * ActionEdit - edit an entity
   */
  private class Edit extends ActionDelegate {
    /** the entity to edit */
    private Entity candidate;
    /**
     * Constructor
     */
    private Edit(Entity entity) {
      candidate = entity;
      setImage(Images.imgView);
      setText(EditView.resources.getString("edit", getTitle(false)));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      EditView.preselectEntity = candidate;
      ViewManager.getInstance().openView(EditViewFactory.this, candidate.getGedcom());
    }
  }
  
  /**
   * ActionChange - change the gedcom information
   */
  private abstract class Change extends ActionDelegate {
    /** the gedcom we're working on */
    protected Gedcom gedcom;
    /** the entity that should receive the focus after creation */
    protected Entity result;
    /**
     * Constructor
     */
    protected Change(Gedcom ged, ImageIcon img, String text) {
      gedcom = ged;
      super.setImage(img);
      super.setText(text);
    }
    /** 
     * Returns the confirmation message
     */
    protected abstract String getConfirmMessage();
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      
      // prepare text for user
      JTextArea text = new JTextArea(getConfirmMessage(), 4, 40);
      text.setWrapStyleWord(true);
      text.setLineWrap(true); 

      // Recheck with the user
      int option = JOptionPane.showConfirmDialog(
        target, new Object[]{ new JScrollPane(text), new JLabel(resources.getString("confirm.proceed"))}, resources.getString("title"), 0
      );
      // .. Yes or NO?
      if (option != JOptionPane.YES_OPTION) {
        return;
      }
      // lock gedcom
      if (!gedcom.startTransaction()) return;
      // let sub-class handle create
      try {
        change();
      } catch (GedcomException e) {
        Debug.log(Debug.ERROR, this, "Unexpected problem while changing gedcom", e);
      }
      // unlock gedcom
      gedcom.endTransaction();
      // set focus?
      if (result!=null) {
        // no editor open?
        if (ViewManager.getInstance().getOpenViews(EditView.class).isEmpty()) {
          EditView.preselectEntity = result;
          ViewManager.getInstance().openView(EditViewFactory.this, gedcom);
        }
        // set current        
        ViewManager.getInstance().setCurrentEntity(result);
      }
      // done
    }
    /**
     * perform the actual change
     */
    protected abstract void change() throws GedcomException;
  } //Change

  /**
   * Create- creates an entity
   */
  private class Create extends Change{
    /** the type we're creating */
    private int type;
    /** the relationship */
    private Relationship relationship;
    /**
     * Constructor
     */
    private Create(Gedcom ged, int typ, Relationship relatshp) {
      super(ged, newImages[typ], resources.getString("new", relatshp==null ? Gedcom.getNameFor(typ, false) : relatshp.getName()));
      type = typ;
      relationship = relatshp;
    }
    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    protected String getConfirmMessage() {
      // You are about to create a {0} in {1}!
      String about = resources.getString("confirm.new", new Object[]{ Gedcom.getNameFor(type,false), gedcom});
      // This entity will not be connected ... / This entity will be {0}.
      String detail = resources.getString(relationship==null?"confirm.new.unrelated":"confirm.new.related", relationship);
      // done
      return about + '\n' + detail;
    }
    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    protected void change() throws GedcomException {
      // create the entity
      result = gedcom.createEntity(type, null);
      result.getProperty().addDefaultProperties();
      // perform the relationship
      if (relationship!=null) relationship.apply(result);
      // done
    }
  } //Create
  
  /**
   * EDelete - delete an entity
   */  
  private class EDelete extends Change {
    /** the candidate to delete */
    private Entity candidate;
    /**
     * Constructor
     */
    private EDelete(Entity entity) {
      super(entity.getGedcom(), Images.imgDelete, EditView.resources.getString("delete"));
      candidate = entity;
    }
    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    protected String getConfirmMessage() {
      // You are about to delete {0} of type {1} from {2}! Deleting this ...
      return resources.getString("confirm.del", new String[] { 
        candidate.getId(), Gedcom.getNameFor(candidate.getType(),false), gedcom.getName() 
      });
    }
    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    protected void change() throws GedcomException {
      candidate.getGedcom().deleteEntity(candidate);
    }
  } //EDelete

  /**
   * PDelete - delete a property
   */  
  private class PDelete extends Change {
    /** the candidate to delete */
    private Property candidate;
    /**
     * Constructor
     */
    private PDelete(Property property) {
      super(property.getGedcom(), Images.imgDelete, EditView.resources.getString("delete"));
      candidate = property;
    }
    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    protected String getConfirmMessage() {
      // a veto?
      String veto = candidate.getDeleteVeto(); 
      // You are about to delete {0} of type {1} from {2}! Deleting this ...
      String msg = resources.getString("confirm.del.prop", new String[] { 
        candidate.getTag(), candidate.getEntity().getId() 
      });
      // result
      return veto==null ? msg : msg + '\n' + veto; 
    }
    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    protected void change() throws GedcomException {
      candidate.getParent().delProperty(candidate);
    }
  } //PDelete
  
  /**
   * External action    */
  private static class External extends ActionDelegate {
    /** the wrapped association */
    private FileAssociation association;
    /** the wrapped file */
    private PropertyFile file;
    /**
     * Constructor     */
    /*package*/ External(PropertyFile f, FileAssociation fa) {
      association = fa;
      file = f;
      super.setImage(file.getImage(false));
      super.setText(association.getAction()+" (*."+association.getSuffix()+" external)");
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      File f = file.getFile();
      if (f==null) return;
      association.execute(new String[]{ f.toString() });
    }
  } //External

} //EditViewFactory
