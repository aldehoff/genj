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
import genj.view.ViewFactory;

/**
 * The factory for the TableView
 */
public class EditViewFactory implements ViewFactory {
  
  /** actions for creating entities */
  private final ActionCreate
    actionChild  = new ActionCreate(Images.imgNewIndi  , Gedcom.INDIVIDUALS, Gedcom.REL_CHILD, "new.child"),
    actionParent = new ActionCreate(Images.imgNewIndi  , Gedcom.INDIVIDUALS, Gedcom.REL_PARENT, "new.parent"),
    actionSpouse = new ActionCreate(Images.imgNewIndi  , Gedcom.INDIVIDUALS, Gedcom.REL_SPOUSE, "new.spouse"),
    actionNote   = new ActionCreate(Images.imgNewNote  , Gedcom.NOTES, 0, "new.note"),
    actionMedia  = new ActionCreate(Images.imgNewMedia , Gedcom.MULTIMEDIAS, 0, "new.media")
  ;
  
  private final ActionDelete actionDelete = new ActionDelete();
  
  /** actions on entities */
  private ActionDelegate[][] entity2create = new ActionDelegate[][] {
    // INDIVIDUALS
    { actionDelete,actionChild,actionParent,actionSpouse,actionNote,actionMedia }, 
    // FAMILIES
    { actionDelete,actionChild,actionNote,actionSpouse,actionMedia }, 
    // MULTIMEDIAS
    { actionDelete }, 
    // NOTES
    { actionDelete }, 
    // SOURCES
    { actionDelete }, 
    // SUBMITTERS
    { actionDelete }, 
    // REPOSITORIES
    { actionDelete }  
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
    for (int a=0; a<actions.length; a++) {
      result.add(actions[a]);
    }
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
    private ActionCreate(ImgIcon img, int t, int r, String txt) {
      super.setImage(img);
      super.setText(EditView.resources.getString("new", EditView.resources.getString(txt)));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
    }
  } //ActionCreate

} //EditViewFactory
