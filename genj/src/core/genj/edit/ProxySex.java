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

import genj.gedcom.PropertySex;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;

import javax.swing.AbstractButton;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
class ProxySex extends Proxy {

  /** members */
  private AbstractButton[] buttons = new AbstractButton[3];
  
  /**
   * Finish editing a property through proxy
   */
  protected void commit() {
    
    // Gather data change
    PropertySex sex = (PropertySex)property; 
    for (int i=0;i<buttons.length;i++) {
      if (buttons[i].isSelected()) {
        sex.setSex(i);
        break;
      }
    }
    
    // Done
  }

  /**
   * Start editing a property through proxy
   */
  protected Editor getEditor() {
  
    // we know it's PropertySex
    PropertySex p = (PropertySex) property;

    // prepare result
    Editor result = new Editor();
    result.setBoxLayout();

    // create buttons    
    ButtonHelper bh = new ButtonHelper()
      .setButtonType(JRadioButton.class)
      .setContainer(result);
    bh.createGroup();
    for (int i=0;i<buttons.length;i++)
      buttons[i] = bh.create( new Gender(i) );
    buttons[p.getSex()].setSelected(true);
    
    result.setFocus(buttons[p.getSex()]);

    // Done
    return result;
  }
  
  private class Gender extends ActionDelegate {
    int sex;
    private Gender(int sex) {
      this.sex = sex;
      setText(PropertySex.getLabelForSex(sex));
    }
    protected void execute() {
      label.setIcon(PropertySex.getImage(sex));
      // notify of change
      stateChanged(new ChangeEvent(ProxySex.this));
    }

  } //Gender

} //ProxySex

