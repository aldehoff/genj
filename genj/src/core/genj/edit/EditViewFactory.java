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
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.ContextMenuSupport;
import genj.view.ViewFactory;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory, ContextMenuSupport {
  
  /** resources we use */
  private Resources resources = EditView.resources;
  
  /** actions for creating entities */
  private final ActionCreate
    aChild     = new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_CHILD, "new.child"),
    aParent    = new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_PARENT, "new.parent"),
    aSpouse    = new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS, Gedcom.REL_SPOUSE, "new.spouse"),
    
    aIndi      = new ActionCreate(Images.imgNewIndi      , Gedcom.INDIVIDUALS ),
    aFam       = new ActionCreate(Images.imgNewFam       , Gedcom.FAMILIES    ),
    aNote      = new ActionCreate(Images.imgNewNote      , Gedcom.NOTES       ),
    aMedia     = new ActionCreate(Images.imgNewMedia     , Gedcom.MULTIMEDIAS ),
    aRepository= new ActionCreate(Images.imgNewRepository, Gedcom.REPOSITORIES),
    aSource    = new ActionCreate(Images.imgNewSource    , Gedcom.SOURCES     ),
    aSubmitter = new ActionCreate(Images.imgNewSubmitter , Gedcom.SUBMITTERS  )
  ;
  
  private final ActionDelete aDelete = new ActionDelete();

  /** actions on entities */
  private ActionDelegate[] gedcom2create = new ActionDelegate[] {
    aIndi, aFam, aNote, aMedia, aRepository, aSource, aSubmitter
  };
  
  /** actions on entities */
  private ActionDelegate[][] entity2create = new ActionDelegate[][] {
    // INDIVIDUALS
    { aDelete,aChild,aParent,aSpouse,aNote,aMedia }, 
    // FAMILIES
    { aDelete,aChild,aNote,aSpouse,aMedia }, 
    // MULTIMEDIAS
    { aDelete }, 
    // NOTES
    { aDelete }, 
    // SOURCES
    { aDelete }, 
    // SUBMITTERS
    { aDelete }, 
    // REPOSITORIES
    { aDelete }  
  };
  

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
   * @see genj.view.ViewFactory#createActions(Entity)
   */
  public List createActions(Entity entity) {
    // create the actions
    List result = new ArrayList();
    ActionDelegate[] actions = entity2create[entity.getType()];
    for (int a=0; a<actions.length; a++) result.add(actions[a]);
    // done
    return result;
  }
  
  /**
   * @see genj.view.ContextMenuSupport#createActions(Gedcom)
   */
  public List createActions(Gedcom gedcom) {
    // create the actions
    List result = new ArrayList();
    ActionDelegate[] actions = gedcom2create;
    for (int a=0; a<actions.length; a++) result.add(actions[a]);
    // done
    return result;
  }

  /**
   * ActionDelete - delete an entity
   */  
  private class ActionDelete extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionDelete() {
      super.setImage(Images.imgDelete);
      super.setText(EditView.resources.getString("delete"));
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
  private class ActionCreate extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionCreate(ImgIcon img, int t) {
      super.setImage(img);
      super.setText(EditView.resources.getString("new", Gedcom.getNameFor(t, false)));
    }
    /**
     * Constructor
     */
    private ActionCreate(ImgIcon img, int t, int r, String key) {
      super.setImage(img);
      super.setText(EditView.resources.getString("new", resources.getString(key)));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
/*
      // Recheck with the user
      String message = resources.getString(
        "new.confirm", new String[] {resources.getString(txt),Gedcom.getNameFor(type,false)}
      );

      int option = JOptionPane.showOptionDialog(
        EditView.this,
        message,
        resources.getString("new"),
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null, null, null
      );

      // .. OK or Cancel ?
      if (option != JOptionPane.OK_OPTION) {
        return;
      }

      // Lock write
      if (!startTransaction("Couldn't lock Gedcom for write")) {
        return;
      }

      // Stop editing old
      flushEditing(true);

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
