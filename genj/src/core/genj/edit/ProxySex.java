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

import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.util.ImgIcon;
import genj.util.swing.ImgIconConverter;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
class ProxySex extends Proxy implements ItemListener {

  /** members */
  private JLabel label;
  private JRadioButton rbMale,rbFemale;
  private boolean changed=false;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {
    // Something changed ?
    if (! hasChanged() )
      return;
    // Gather data change
    if ( rbMale.getModel().isSelected() == true)
      ((PropertySex)prop).setSex(PropertySex.MALE);
    if ( rbFemale.getModel().isSelected() == true)
      ((PropertySex)prop).setSex(PropertySex.FEMALE);
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
    ImgIcon img;
    if ( rbMale.getModel().isSelected() == true)
      img=((PropertySex)prop).getDefaultImage(PropertySex.MALE);
    else
      img=((PropertySex)prop).getDefaultImage(PropertySex.FEMALE);

    // Image change
    label.setIcon(ImgIconConverter.get(img));

    // Done
  }          

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    label=setLabel;
    prop=setProp;
    PropertySex p = (PropertySex) prop;

    rbMale   = new JRadioButton( p.getLabelForSex(PropertySex.MALE)   );
    rbFemale = new JRadioButton( p.getLabelForSex(PropertySex.FEMALE) );

    rbMale.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    rbFemale.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));

    in.add(rbMale);
    in.add(rbFemale);

    ButtonGroup bg = new ButtonGroup();
    rbMale  .getModel().setGroup(bg);
    rbFemale.getModel().setGroup(bg);

    switch (p.getSex()) {
      case PropertySex.MALE:
        rbMale  .getModel().setSelected(true);
        rbFemale.requestFocus();
        break;
      case PropertySex.FEMALE:
        rbFemale.getModel().setSelected(true);
        rbMale  .requestFocus();
        break;
    }

    rbMale.getModel().addItemListener(this);
    rbFemale.getModel().addItemListener(this);

    // Done
  }

}

