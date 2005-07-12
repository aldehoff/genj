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

import genj.edit.Images;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.view.Context;
import genj.view.ViewManager;
import genj.window.WindowManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
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
  protected Property focus = null;
  
  /** image *new* */
  protected final static ImageIcon imgNew = Images.imgNewEntity;
  
  private JTextArea confirm;

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
   * Show a dialog for errors
   */  
  protected void handleThrowable(String phase, Throwable t) {
    manager.getWindowManager().openDialog("err", "Error", WindowManager.ERROR_MESSAGE, t.getMessage(), WindowManager.ACTIONS_OK, getTarget());
  }
  
  /** 
   * Returns the confirmation message - null if none
   */
  protected abstract String getConfirmMessage();
  
  /**
   * Return the dialog content to show to the user   */
  protected JPanel getDialogContent() {
    JPanel result = new JPanel(new NestedBlockLayout("<col><text wx=\"1\" wy=\"1\"/></col>"));
    result.add(getConfirmComponent());
    return result;
  }
  
  protected JComponent getConfirmComponent() {
    if (confirm==null) {
      confirm = new JTextArea(getConfirmMessage(), 6, 40);
      confirm.setWrapStyleWord(true);
      confirm.setLineWrap(true);
      confirm.setEditable(false);
    }
    return new JScrollPane(confirm);
  }
  
  /** 
   * Callback to update confirm text
   */
  protected void refresh() {
    // might be no confirmation showing
    if (confirm!=null)
      confirm.setText("<html>"+getConfirmMessage());
  }
  
  /**
   * @see genj.util.ActionDelegate#execute()
   */
  protected void execute() {
    
    // prepare confirmation message for user
    String msg = getConfirmMessage();
    if (msg!=null) {
  
      // prepare actions
      String[] actions = { resources.getString("confirm.proceed", getText()),  WindowManager.TXT_CANCEL };
      
      // Recheck with the user
      int rc = manager.getWindowManager().openDialog(getClass().getName(), getText(), WindowManager.QUESTION_MESSAGE, getDialogContent(), actions, getTarget() );
      if (rc!=0)
        return;
    }
        
    // lock gedcom
    gedcom.startTransaction();
    // let sub-class handle create
    try {
      change();
    } catch (Throwable t) {
      manager.getWindowManager().openDialog(null, null, WindowManager.ERROR_MESSAGE, t.getMessage(), WindowManager.ACTIONS_OK, getTarget());
    }
    // unlock gedcom
    gedcom.endTransaction();
    // set focus?
    if (focus!=null) 
      manager.fireContextSelected(new Context(focus));
    // done
  }
  
  /**
   * perform the actual change
   */
  protected abstract void change() throws GedcomException;
  
} //Change

