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
import genj.gedcom.PropertySex;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.view.ViewManager;

import javax.swing.AbstractButton;
import javax.swing.JRadioButton;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
public class SexBean extends PropertyBean {

  /** members */
  private AbstractButton[] buttons = new AbstractButton[3];
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    PropertySex sex = (PropertySex)property; 
    sex.setSex(getSex());
  }
  
  /**
   * Get current sex
   */
  private int getSex() {
    
    // Gather data change
    for (int i=0;i<buttons.length;i++) {
      if (buttons[i].isSelected()) 
        return i;
    }
        
    // unknown
    return PropertySex.UNKNOWN;
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setMgr, setReg);
  
    // FIXME use custom layout to arrange gender choices hori or vert as fits best
  
    // we know it's PropertySex
    PropertySex p = (PropertySex) property;

    // create buttons    
    ButtonHelper bh = new ButtonHelper()
      .setButtonType(JRadioButton.class)
      .setContainer(this);
    bh.createGroup();
    for (int i=0;i<buttons.length;i++)
      buttons[i] = bh.create( new Gender(i) );
    buttons[p.getSex()].setSelected(true);
    
    defaultFocus = buttons[p.getSex()];

    // Done
  }
  
  private class Gender extends ActionDelegate {
    int sex;
    private Gender(int sex) {
      this.sex = sex;
      setText(PropertySex.getLabelForSex(sex));
    }
    protected void execute() {
      changeSupport.fireChangeEvent();
    }

  } //Gender

} //ProxySex

