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
import genj.util.swing.ImageIcon;
import genj.view.ViewManager;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * ActionChange - change the gedcom information
 */
/*package*/ abstract class AbstractChange extends ActionDelegate {
  
  /** resources */
  /*package*/ static Resources resources = new Resources(AbstractChange.class);
  
  /** images we use for new entities */
  protected final static ImageIcon[] newImages = new ImageIcon[] {
    Images.imgNewIndi,
    Images.imgNewFam,
    Images.imgNewMedia,
    Images.imgNewNote,
    Images.imgNewSource,
    Images.imgNewSubmitter,
    Images.imgNewRepository,
  };
  
  /** the gedcom we're working on */
  protected Gedcom gedcom;
  
  /** the focus */
  protected Entity focus = null;
  
  /**
   * Constructor
   */
  /*package*/ AbstractChange(Gedcom ged, ImageIcon img, String text) {
    gedcom = ged;
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
    final JTextArea confirm = new JTextArea(getConfirmMessage(), 4, 40);
    confirm.setWrapStyleWord(true);
    confirm.setLineWrap(true);
    confirm.setEnabled(true);

    // prepare options
    List options = new ArrayList();
    
    JComponent c = getOptions();
    if (c!=null) {
      c.addPropertyChangeListener(new PropertyChangeListener() {
        /** update confirm message */
        public void propertyChange(PropertyChangeEvent evt) {
          confirm.setText(getConfirmMessage());
        }
      });
      options.add(c);
    }
    options.add(new JScrollPane(confirm));
    options.add(new JLabel(resources.getString("confirm.proceed")));

    // Recheck with the user
    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(target, options.toArray(), txt, 0)) {
      return;
    }
    
    // lock gedcom
    if (!gedcom.startTransaction()) return;
    // let sub-class handle create
    try {
      change();
    } catch (GedcomException ex) {
      JOptionPane.showMessageDialog(
        target,
        ex.getMessage(),
        ex.toString(),
        JOptionPane.ERROR_MESSAGE
      );
    }
    // unlock gedcom
    gedcom.endTransaction();
    // set focus?
    if (focus!=null) {
      // no editor open?
      if (ViewManager.getInstance().getOpenViews(EditView.class, gedcom).isEmpty()) {
        EditView.open(focus);
      }
      // set current        
      ViewManager.getInstance().setCurrentEntity(focus);
    }
    // done
  }
  
  /**
   * perform the actual change
   */
  protected abstract void change() throws GedcomException;
  
} //Change

