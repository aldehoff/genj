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
package genj.edit.beans;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

import java.awt.BorderLayout;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
public class SimpleValueBean extends PropertyBean {

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {
    if (tfield!=null) {
      property.setValue(tfield.getText());
    }
  }

  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return !property.isReadOnly();
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    setLayout(new BorderLayout());

    // readOnly()?
    if (property.isReadOnly()) {
      add(BorderLayout.NORTH, new JLabel(property.getDisplayValue()));
    } else {
      tfield = new TextFieldWidget(property.getDisplayValue(), 8);
      tfield.addChangeListener(changeSupport);
      add(BorderLayout.NORTH, tfield);
      super.defaultFocus = tfield;
    }
    
    // Done
  }

} //ProxyUnknown
