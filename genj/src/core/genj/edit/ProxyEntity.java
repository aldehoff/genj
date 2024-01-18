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

import genj.gedcom.Entity;
import genj.gedcom.PropertyChange;

import java.awt.BorderLayout;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
class ProxyEntity extends Proxy {

  /** members */
  private JTextField tfield;
  private AbstractButton bchange;

  /**
   * Finish editing a property through proxy (no changes here unless
   * hasChanged()==true since this will be called in all cases)
   */
  protected void commit() {
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Nothing to edit
   */  
  protected boolean isEditable() {
    return false;
  }

  /**
   * Start editing a property through proxy
   */
  protected Editor getEditor() {

    Editor result = new Editor();
    result.setLayout(new BorderLayout());

    // Look for entity
    if (property instanceof Entity) {

      Entity e = (Entity)property;

      // add a preview
      result.add(BorderLayout.CENTER, new Preview(e));

      // add change date/time
      PropertyChange change = e.getLastChange();
      if (change!=null) {
        result.add(BorderLayout.SOUTH, new JLabel(resources.getString("proxy.entity.change", new String[] {change.getDateAsString(), change.getTimeAsString()} )));      
      }

    }
    
    // Done
    return result;
  }
  
} //ProxyEntity
