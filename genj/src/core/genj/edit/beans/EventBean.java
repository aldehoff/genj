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
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.util.Registry;
import genj.util.swing.NestedBlockLayout;
import genj.view.ViewManager;

import java.awt.geom.Point2D;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : *Events*
 * This Proxy was written by Dan Kionka, and only exists to display the age.
 */
public class EventBean extends PropertyBean {

  /** known to have happened */
  private JCheckBox known;

  /**
   * Finish proxying edit for property Birth
   */
  public void commit(Transaction tx) {
    // known might be null!
    if (known!=null) {
      ((PropertyEvent)property).setKnownToHaveHappened(known.isSelected());
    }
  }

  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return known!=null;
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);
    
    // showing age/event-has-happened if indeed an event
    if (!(property instanceof PropertyEvent))
      return;
    
    NestedBlockLayout layout = new NestedBlockLayout(false, 2);
    setLayout(layout);
    
    PropertyEvent event = (PropertyEvent)property;
    PropertyDate date = event.getDate(true);
    
    // show age of individual?
    if (property.getEntity() instanceof Indi) {
    
      Indi indi = (Indi)event.getEntity();
      
      // Calculate label & age
      String ageat = "even.age";
      String age = "";
      if ("BIRT".equals(event.getTag())) {
        ageat = "even.age.today";
        if (date!=null) {
          Delta delta = Delta.get(date.getStart(), PointInTime.getNow());
          if (delta!=null)
            age = delta.toString();
        }
      } else {
        age = date!=null ? indi.getAgeString(date.getStart()) : resources.getString("even.age.?");
      }

      JTextField txt = new JTextField(age, 16); 
      txt.setEditable(false);
      txt.setFocusable(false);
      
      add(new JLabel(resources.getString(ageat)));
      add(txt, new Point2D.Double(1,0));
      
      layout.createBlock(0);
    }

    // show event-has-happened?
    if (!"EVEN".equals(property.getTag())) {
      Boolean happened = event.isKnownToHaveHappened();
      if (happened!=null) {
        known = new JCheckBox(resources.getString("even.known"));
        known.setSelected(happened.booleanValue());
        known.addActionListener(changeSupport);
        add(known);
        defaultFocus = known;
      }
    }
    
    // done
  }

} //EventBean
