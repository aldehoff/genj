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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.print.PrintRenderer;
import genj.util.ActionDelegate;
import genj.util.Debug;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ContextPopupSupport;
import genj.view.ContextSupport;
import genj.view.ViewFactory;
import genj.view.ViewManager;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ContextSupport {
  
  /** resources we use */
  private final static Resources resources = EditView.resources;

  /** a noop is used for separators in returning actions */  
  private final static ActionDelegate aNOOP = ActionDelegate.NOOP;

  /**
   * @see genj.app.ViewFactory#createSettingsComponent(Component)
   */
  public JComponent createSettingsComponent(Component view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createPrintRenderer(Component)
   */
  public PrintRenderer createPrintRenderer(Component view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createViewComponent(Gedcom, Registry, Frame)
   */
  public Component createViewComponent(Gedcom gedcom, Registry registry, Frame frame) {
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
  public ImgIcon getImage() {
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
    // one dummy
    ActionDelegate ad = new ActionDelegate() {
      protected void execute() {
      }
    };
    ad.setText("Foo");
    result.add(ad);
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
    // add delete
    result.add(ActionDelegate.NOOP);
    result.add(new Delete(entity));
    // add an "edit in EditView"
    if (!ViewManager.getInstance().isOpenView(EditView.class)) {
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
    result.add(new CreateStandalone(gedcom, Gedcom.INDIVIDUALS , Images.imgNewIndi      ));
    result.add(new CreateStandalone(gedcom, Gedcom.FAMILIES    , Images.imgNewFam       ));
    result.add(new CreateStandalone(gedcom, Gedcom.NOTES       , Images.imgNewNote      ));
    result.add(new CreateStandalone(gedcom, Gedcom.MULTIMEDIAS , Images.imgNewMedia     ));
    result.add(new CreateStandalone(gedcom, Gedcom.REPOSITORIES, Images.imgNewRepository));
    result.add(new CreateStandalone(gedcom, Gedcom.SOURCES     , Images.imgNewSource    ));
    result.add(new CreateStandalone(gedcom, Gedcom.SUBMITTERS  , Images.imgNewSubmitter ));
  }
  
  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi) {
    result.add(new CreateConnected(indi, Gedcom.NOTES, Images.imgNewNote));
    /*
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0, "new.child"  , indi));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0, "new.parent", indi));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0, "new.spouse", indi));
    result.add(new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS, indi ));
    */
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam) {
    /*
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0 , "new.child"  , fam));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0, "new.husband", fam));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, 0, "new.wife"   , fam));
    result.add(new ActionCreate(Images.imgNewNote      , Gedcom.NOTES      , fam));
    result.add(new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS, fam));
    */
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
      EditView ev = (EditView)ViewManager.getInstance().openView(EditViewFactory.this, candidate.getGedcom());
      ev.setSticky(false);
      ev.setEntity(candidate);
    }
  }
  
  /**
   * ActionChange - change the gedcom information
   */
  private static abstract class Change extends ActionDelegate {
    /** the gedcom we're working on */
    protected Gedcom gedcom;
    /** the entity that should receive the focus after creation */
    protected Entity focus;
    /**
     * Constructor
     */
    protected Change(Gedcom ged, ImgIcon img, String text) {
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
        null, new Object[]{ new JScrollPane(text), new JLabel(resources.getString("confirm.proceed"))}, resources.getString("title"), 0
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
      if (focus!=null) ViewManager.getInstance().setCurrentEntity(focus);
      // done
    }
    /**
     * perform the actual change
     */
    protected abstract void change() throws GedcomException;
  } //Change

  /**
   * ActionCreate - create an entity
   */
  private static abstract class Create extends Change { 
    /** the type that is created */
    protected int type;
    /**
     * Constructor
     */
    protected Create(Gedcom ged, int typ, ImgIcon img, String text) {
      super(ged, img, text);
      type   = typ;
    }
  } //Create
  
  /**
   * CreateStandalone - create a standalone entity
   */
  private static class CreateStandalone extends Create {
    /**
     * Constructor
     */
    private CreateStandalone(Gedcom gedcom, int type, ImgIcon img) {
      super(gedcom, type, img, EditView.resources.getString("new", Gedcom.getNameFor(type,false)));
    }
    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    protected String getConfirmMessage() {
      // You are about to create a {0} in {1}! This entity is not connected ...
      return resources.getString("confirm.new.disconnected", new String[] { 
        Gedcom.getNameFor(type,false), gedcom.getName() 
      });
    }
    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    protected void change() throws GedcomException {
      focus = gedcom.createEntity(type, null);
      focus.getProperty().addDefaultProperties();
    }
  } //CreateStandalone 
  
  /**
   * CreateConnected - creates a connected entity
   */
  private static class CreateConnected extends Create {
    /** entity owning the created */
    private Entity owner;
    /**
     * Constructor
     */
    private CreateConnected(Entity ownr, int typ, ImgIcon img) {
      super(ownr.getGedcom(), typ, img, EditView.resources.getString("new", Gedcom.getNameFor(typ,false)));
      owner = ownr;
    }
    /**
     * @see genj.edit.EditViewFactory.Change#getConfirmMessage()
     */
    protected String getConfirmMessage() {
      // You are about to add a {0} to {1} in {2}!
      return resources.getString("confirm.new.connected", new String[] { 
        Gedcom.getNameFor(type,false), owner.getId(), gedcom.getName() 
      });
    }
    /**
     * @see genj.edit.EditViewFactory.Change#change()
     */
    protected void change() throws GedcomException {
      // create the entity
      focus = gedcom.createEntity(type, null);
      focus.getProperty().addDefaultProperties();
      // add it to the owner
      
      // done
    }
  } //CreateConnected

  /**
   * ActionDelete - delete an entity
   */  
  private static class Delete extends Change {
    /** the candidate to delete */
    private Entity candidate;
    /**
     * Constructor
     */
    private Delete(Entity entity) {
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
  } //ActionDelete

} //EditViewFactory
