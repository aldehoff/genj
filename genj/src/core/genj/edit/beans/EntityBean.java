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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyChange;
import genj.gedcom.TagPath;
import genj.util.Registry;
import genj.view.ViewManager;

import java.awt.BorderLayout;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : ENTITY
 */
public class EntityBean extends PropertyBean {

  /** members */
  private JTextField tfield;
  private AbstractButton bchange;

  /**
   * Finish editing a property through proxy (no changes here unless
   * hasChanged()==true since this will be called in all cases)
   */
  public void commit() {
  }

  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    setLayout(new BorderLayout());

    // Look for entity
    if (property instanceof Entity) {

      Entity e = (Entity)property;

      // add a preview
      add(BorderLayout.CENTER, new Preview(e));

      // add change date/time
      PropertyChange change = e.getLastChange();
      if (change!=null)
        add(BorderLayout.SOUTH, new JLabel(resources.getString("entity.change", new String[] {change.getDateAsString(), change.getTimeAsString()} )));      

    }
    
    // Done
  }
  
} //ProxyEntity
