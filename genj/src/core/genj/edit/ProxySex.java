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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
class ProxySex extends Proxy implements ItemListener {

  /** members */
  private JRadioButton[] rbSex = new JRadioButton[3];
  
  private boolean changed=false;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    
    // Something changed ?
    if (! hasChanged() ) return;
    
    // Gather data change
    PropertySex sex = (PropertySex)property; 
    for (int i=0;i<rbSex.length;i++) {
      if (rbSex[i].isSelected()) {
        sex.setSex(i);
        break;
      }
    }
    
    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return changed;
  }

  /**
   * RadioButton has been selected
   */
  public void itemStateChanged(ItemEvent e) {

    // We're waiting for selection only
    if (e.getStateChange() != e.SELECTED)
      return;

    changed=true;

    // Gather data change
    PropertySex sex = (PropertySex)property;
     
    for (int i=0;i<rbSex.length;i++) {
      if (rbSex[i].isSelected()) {
        label.setIcon(sex.getImage(i));
        break;
      }
    }

    // Done
  }          

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    PropertySex p = (PropertySex) property;
    ButtonGroup bg = new ButtonGroup();

    for (int i=0;i<rbSex.length;i++) {
      rbSex[i] = new JRadioButton( p.getLabelForSex(i) );
      in.add(rbSex[i]);
      
      rbSex[i].getModel().setGroup(bg);
      rbSex[i].getModel().addItemListener(this);
    }
    
    rbSex[p.getSex()].setSelected(true);

    // Done
    return null;
  }

} //ProxySex

