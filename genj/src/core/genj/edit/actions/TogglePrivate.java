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

import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.util.ActionDelegate;
import genj.view.ViewManager;
import genj.window.WindowManager;

/**
 * TogglePrivate - toggle "private" of a property
 */
public class TogglePrivate extends ActionDelegate {
  
  /** the property */
  private Property property;
  
  /** the view manager */
  private ViewManager manager;
  
  /**
   * Constructor
   */
  public TogglePrivate(Property prop, ViewManager mgr) {
    manager = mgr;
    property = prop;
    setImage(MetaProperty.IMG_PRIVATE); 
    setText(AbstractChange.resources.getString(!prop.isPrivate()?"private":"public"));
  }
  
  /**
   * @see genj.util.ActionDelegate#execute()
   */
  protected void execute() {

    // check if the user wants to do it recursively
    int recursive = 0;
    if (property.getNoOfProperties()>0) {
      recursive = manager.getWindowManager().openDialog(
        null,
        txt,
        WindowManager.IMG_QUESTION,
        AbstractChange.resources.getString("recursive"),
        WindowManager.OPTIONS_YES_NO,
        target
      );
    }

    // check gedcom
    Gedcom gedcom = property.getGedcom();
    String pwd = gedcom.getPassword();
    
    if (pwd==Gedcom.PASSWORD_UNKNOWN) {
      manager.getWindowManager().openDialog(
        null, 
        txt, 
        WindowManager.IMG_WARNING, 
        "This Gedcom file contains encrypted information that has to be decrypted before changing private/public status of information", 
        WindowManager.OPTIONS_OK, 
        target);
      return;              
    }
    
    if (pwd==Gedcom.PASSWORD_NOT_SET) {
      
      pwd = manager.getWindowManager().openDialog(
        null,
        txt,
        WindowManager.IMG_QUESTION,
        AbstractChange.resources.getString("password", gedcom.getName()),
        "",
        target
      );
      
      // canceled?
      if (pwd==null)
        return;
    }

    // change it
    if (gedcom.startTransaction()) {
      gedcom.setPassword(pwd); 
      property.setPrivate(!property.isPrivate(), recursive==0);
      gedcom.endTransaction();
    }

    // done
  }
  
} //OpenForEdit

