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

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyBirth;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : BIRT
 * This Proxy was written by Dan Kionka, and only exists to display the age.
 */
class ProxyBirth extends Proxy implements ItemListener {

  /**
   * Finish proxying edit for property Birth
   */
  protected void finish() {
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return false;
  }

  /**
   * Trigger for changes in editing components
   */
  public void itemStateChanged(ItemEvent e) {
  }          

  /**
   * Starts Proxying edit for property Date by filling a vector with
   * components to edit this property
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {
    JPanel ageLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
    ageLine.setAlignmentX(0);
    JTextField ageText = new JTextField(10);

    ageLine.add(new JLabel("Age:"));
    ageLine.add(ageText);
    ageText.setEditable(false);
    in.add(ageLine);

    prop=setProp;
    PropertyBirth p = (PropertyBirth)prop;

    Indi indi = (Indi) p.getParent();
    String ageStr = indi.getAge();
    ageText.setText((ageStr.length() == 0) ? "(unknown)" : ageStr);
  }

}
