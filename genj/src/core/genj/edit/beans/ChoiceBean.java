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
import genj.gedcom.PropertyChoiceValue;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.view.ViewManager;

import java.awt.BorderLayout;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : Choice (e.g. RELA)
 * @author nils@meiers.net
 * @author Tomas Dahlqvist fix for prefix lookup
 */
public class ChoiceBean extends PropertyBean {

  /** members */
  private ChoiceWidget choice;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {

    // Store changed value
    Object result = choice.getText();
    property.setValue(result!=null?result.toString():"");
  
    // Done
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setMgr, setReg);
    
    // setup choices
    Object[] items = new Object[0];
    if (property instanceof PropertyChoiceValue)
      items = ((PropertyChoiceValue)property).getChoices(setGedcom).toArray();

    // Setup controls
    choice = new ChoiceWidget(items, property.getValue());
    choice.addChangeListener(changeSupport);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, choice);
    
    // focus
    defaultFocus = choice;
    
  }
  
} //ProxyChoice
