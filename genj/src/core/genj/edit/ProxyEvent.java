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
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : *Events*
 * This Proxy was written by Dan Kionka, and only exists to display the age.
 */
class ProxyEvent extends Proxy implements ItemListener {

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
    
    // showing age@event only for individuals 
    if (!(setProp.getEntity() instanceof Indi)) return;
    PropertyEvent event = (PropertyEvent)setProp;
    PropertyDate date = event.getDate(true);
    Indi indi = (Indi)event.getEntity();
    
    // Calculate label & age
    String ageat = "Age";
    String age = date!=null ? indi.getAge(date) : "(unknown)";
    if ("BIRT".equals(event.getTag())) {
      ageat+=" (today)";
      age = indi.getAge(null);
    }
    
    // layout
    JLabel label = new JLabel(ageat); 
    JTextField txt = new JTextField(age, 10); txt.setEditable(false);
    
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setAlignmentX(0);
    panel.add(label);
    panel.add(txt);
    in.add(panel);

    // done
  }

}
