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
import genj.gedcom.Indi;
import genj.print.PrintRenderer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ContextMenuSupport;
import genj.view.ViewFactory;
import genj.view.ViewManager;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ContextMenuSupport {
  
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
    result.add(new ActionDelete(entity));
    // add an "edit in EditView"
    if (!ViewManager.getInstance().isOpenView(EditView.class)) {
      result.add(ActionDelegate.NOOP);
      result.add(new ActionEdit(entity));
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
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS , gedcom));
    result.add(new ActionCreate(Images.imgNewFam       , Gedcom.FAMILIES    , gedcom));
    result.add(new ActionCreate(Images.imgNewNote      , Gedcom.NOTES       , gedcom));
    result.add(new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS , gedcom));
    result.add(new ActionCreate(Images.imgNewRepository, Gedcom.REPOSITORIES, gedcom));
    result.add(new ActionCreate(Images.imgNewSource    , Gedcom.SOURCES     , gedcom));
    result.add(new ActionCreate(Images.imgNewSubmitter , Gedcom.SUBMITTERS  , gedcom));
  }
  
  /**
   * Create actions for Individual
   */
  private void createActions(List result, Indi indi) {
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_CHILD, "new.child"  , indi));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_PARENT, "new.parent", indi));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_SPOUSE, "new.spouse", indi));
    result.add(new ActionCreate(Images.imgNewNote      , Gedcom.NOTES      , indi ));
    result.add(new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS, indi ));
  }
  
  /**
   * Create actions for Families
   */
  private void createActions(List result, Fam fam) {
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_CHILD , "new.child"  , fam));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_PARENT, "new.husband", fam));
    result.add(new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_SPOUSE, "new.wife"   , fam));
    result.add(new ActionCreate(Images.imgNewNote      , Gedcom.NOTES      , fam));
    result.add(new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS, fam));
  }
  
  /**
   * ActionEdit - edit an entity
   */
  private class ActionEdit extends ActionDelegate {
    /** the entity to edit */
    private Entity candidate;
    /**
     * Constructor
     */
    private ActionEdit(Entity entity) {
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
   * ActionDelete - delete an entity
   */  
  private static class ActionDelete extends ActionDelegate {
    /** the candidate to delete */
    private Entity candidate;
    /**
     * Constructor
     */
    private ActionDelete(Entity entity) {
      // preset
      super.setImage(Images.imgDelete);
      super.setText(EditView.resources.getString("delete"));
      // remember
      candidate = entity;
      // done
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
    }
  } //ActionDelete

  /**
   * ActionCreate - create an entity
   */  
  private static class ActionCreate extends ActionDelegate {
    /** confirmation message */
    private String message;
    /** the confirmation type */
    private boolean isWarning = false;
    /**
     * Constructor
     */
    private ActionCreate(ImgIcon img, int t, Gedcom gedcom) {
      String type = Gedcom.getNameFor(t,false);
      // preset
      super.setImage(img);
      super.setText(EditView.resources.getString("new", type));
      // calculate message
      message = 
        resources.getString("new.confirm", new String[] { 
          type, gedcom.getName(), type 
        })
        + '\n' +
        resources.getString("new.warning");
      isWarning = true;
      // done
    }
    /**
     * Constructor
     */
    private ActionCreate(ImgIcon img, int t, Entity entity) {
      String type = Gedcom.getNameFor(t,false);
      // preset
      super.setImage(img);
      super.setText(EditView.resources.getString("new", type));
      // calculate message
      message = 
        resources.getString("new.confirm", new String[] { 
          type, entity.getId(), type
        });
      // done
    }
    /**
     * Constructor
     */
    private ActionCreate(ImgIcon img, int t, int r, String key, Entity entity) {
      String type = Gedcom.getNameFor(t,false);
      String name = resources.getString(key);
      // preset
      super.setImage(img);
      super.setText(EditView.resources.getString("new", name));
      // calculate message
      message = 
        resources.getString("new.confirm", new String[] { 
          name, entity.getId(), type
        });
      // done
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {

      // prepare text for user
      JTextArea text = new JTextArea(message, 4, 40);
      text.setWrapStyleWord(true);
      text.setLineWrap(true); 

      // Recheck with the user
      int option = JOptionPane.showOptionDialog(
        null, new JScrollPane(text), resources.getString("title"),
        JOptionPane.OK_CANCEL_OPTION,
        isWarning ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE,
        null, null, null
      );
      
      // .. OK or Cancel ?
      if (option != JOptionPane.OK_OPTION) {
        return;
      }

  /*
      // Lock write
      if (!startTransaction("Couldn't lock Gedcom for write")) {
        return;
      }

      // Try to create
      Entity old = entity;
      Entity created = null;
      try {
        switch (type) {
          case Gedcom.INDIVIDUALS:
            created = gedcom.createIndi("", "", 0, relation, entity);
            break;
          case Gedcom.NOTES:
            created = gedcom.createNote(entity);
            break;
          case Gedcom.MULTIMEDIAS:
            created = gedcom.createMedia(entity);
            break;
        }
      } catch (GedcomException ex) {
        JOptionPane.showMessageDialog(
          getFrame(),
          ex.getMessage(),
          EditView.resources.getString("error"),
          JOptionPane.ERROR_MESSAGE
        );
      }

      // End transaction
      endTransaction();
 */
    }
  } //ActionCreate

} //EditViewFactory
