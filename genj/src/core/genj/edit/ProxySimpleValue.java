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

import java.awt.BorderLayout;

import genj.util.swing.TextFieldWidget;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
class ProxySimpleValue extends Proxy {

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  protected void commit() {
    if (tfield!=null) {
      property.setValue(tfield.getText());
    }
  }

  /**
   * Nothing to edit
   */  
  protected boolean isEditable() {
    return !property.isReadOnly();
  }

  /**
   * Start editing a property through proxy
   */
  protected Editor getEditor() {

    Editor result = new Editor();
    result.setLayout(new BorderLayout());

    // readOnly()?
    if (property.isReadOnly()) {
      result.add(BorderLayout.NORTH, new JLabel(property.getValue()));
    } else {
      tfield = new TextFieldWidget(change, property.getValue(), 0);
      result.add(BorderLayout.NORTH, tfield);
      result.setFocus(tfield);
    }
    
    // Done
    return result;
  }

} //ProxyUnknown
