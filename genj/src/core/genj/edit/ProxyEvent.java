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
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.PointInTime;
import genj.util.GridBagHelper;
import genj.util.swing.ChoiceWidget;
import genj.window.WindowManager;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : *Events*
 * This Proxy was written by Dan Kionka, and only exists to display the age.
 */
class ProxyEvent extends Proxy {

  /** known to have happened */
  private ChoiceWidget known;

  /**
   * Finish proxying edit for property Birth
   */
  protected void commit() {
    // known might be null!
    if (known!=null) {
      ((PropertyEvent)property).setKnownToHaveHappened(known.getSelectedIndex()==0);
      known.getChangeState().set(false);
    }
  }

  /**
   * Nothing to edit
   */  
  protected boolean isEditable() {
    return known!=null;
  }

  /**
   * Starts Proxying edit for property Date by filling a vector with
   * components to edit this property
   */
  protected Editor getEditor() {

    Editor result = new Editor();
    
    // showing age@event only for individuals 
    if (!(property.getEntity() instanceof Indi&&property instanceof PropertyEvent)) 
      return result;
    
    PropertyEvent event = (PropertyEvent)property;
    PropertyDate date = event.getDate(true);
    Indi indi = (Indi)event.getEntity();
    
    // Calculate label & age
    String ageat = "proxy.even.age";
    String age;
    if ("BIRT".equals(event.getTag())) {
      ageat = "proxy.even.age.today";
      age = indi.getAgeString(PointInTime.getNow());
    } else {
      age = date!=null ? indi.getAgeString(date.getStart()) : resources.getString("proxy.even.age.?");
    }
    
    // layout
    JLabel 
      label1 = new JLabel(resources.getString(ageat)),
      label2 = new JLabel(resources.getString("proxy.even.known")); 
    
    // 20040321 increased from 10 to 16 to account for long age string
    JTextField txt = new JTextField(age, 16); txt.setEditable(false);
    String[] choices = WindowManager.OPTIONS_YES_NO;
    known = new ChoiceWidget(change, choices, event.isKnownToHaveHappened() ? choices[0] : choices[1]);
    known.setEditable(false);
    
    GridBagHelper gh = new GridBagHelper(result);
    gh.add(label1, 0, 0, 1, 1, gh.FILL_HORIZONTAL    );
    gh.add(txt   , 1, 0, 1, 1, gh.GROWFILL_HORIZONTAL);
    gh.add(label2, 0, 1, 1, 1, gh.FILL_HORIZONTAL    );
    gh.add(known , 1, 1, 1, 1, gh.GROWFILL_HORIZONTAL);
    gh.addFiller(0,2);

    // done
    return result;
  }

} //ProxyEvent
