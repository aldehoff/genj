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

import genj.edit.EditView;
import genj.edit.Images;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * ActionChange - change the gedcom information
 */
/*package*/ abstract class AbstractChange extends ActionDelegate {
  
  /** resources */
  /*package*/ static Resources resources = Resources.get(AbstractChange.class);
  
  /** the gedcom we're working on */
  protected Gedcom gedcom;
  
  /** the manager in the background */
  protected ViewManager manager;
  
  /** the focus */
  protected Entity focus = null;
  
  /** image *new* */
  protected final static ImageIcon imgNew = Images.imgNew;
  
  /**
   * Constructor
   */
  /*package*/ AbstractChange(Gedcom ged, ImageIcon img, String text, ViewManager mgr) {
    gedcom = ged;
    manager = mgr;
    super.setImage(img);
    super.setText(text);
  }
  
  /** 
   * Returns the confirmation message
   */
  protected abstract String getConfirmMessage();
  
  /**
   * Returns options   */
  protected JComponent getOptions() {
    return null;
  }
  
  /**
   * @see genj.util.ActionDelegate#execute()
   */
  protected void execute() {
    
    // prepare confirmation message for user
    final JTextArea confirm = new JTextArea(getConfirmMessage(), 6, 40);
    confirm.setWrapStyleWord(true);
    confirm.setLineWrap(true);
    confirm.setEditable(false);

    // prepare options
    JComponent c = getOptions();
    if (c!=null) {
      c.addPropertyChangeListener(new PropertyChangeListener() {
        /** update confirm message */
        public void propertyChange(PropertyChangeEvent evt) {
          confirm.setText(getConfirmMessage());
        }
      });
    }

    // Recheck with the user
    int rc = manager.getWindowManager().openDialog(
      getClass().getName(), null, WindowManager.IMG_QUESTION, 
      new JComponent[]{ c, new JScrollPane(confirm)} , 
      new String[] { resources.getString("confirm.proceed", txt ), WindowManager.OPTION_CANCEL }, 
      target 
    );
    if (rc!=0)
      return;
    
    // lock gedcom
    if (!gedcom.startTransaction()) return;
    // let sub-class handle create
    try {
      change();
    } catch (GedcomException ex) {
      manager.getWindowManager().openDialog(null, null, WindowManager.IMG_ERROR, ex.getMessage(), (String[])null, target);
    }
    // unlock gedcom
    gedcom.endTransaction();
    // set focus?
    if (focus!=null) {
      // no editor open?
      if (manager.getInstances(EditView.class, gedcom).length==0) {
        new OpenForEdit(focus, manager).trigger();
      } else {
        // set current        
        manager.setContext(focus);
      }
    }
    // done
  }
  
  /**
   * perform the actual change
   */
  protected abstract void change() throws GedcomException;
  
} //Change

