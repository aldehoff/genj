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

import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.time.PointInTime;
import genj.util.Registry;
import genj.util.swing.DateWidget;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : DATE
 */
public class DateBean extends PropertyBean implements ItemListener {

  /** members */
  private int currentDate;
  private JComboBox combo;
  private DateWidget deOne, deTwo;

  private static final boolean drange[] = {
    false,
    true,
    false,
    false,
    true,
    false,
    false,
    false,
    false,
    false
  };
  
  /**
   * Finish proxying edit for property Date
   */
  public void commit() {

    PropertyDate p = (PropertyDate)property;

    // Remember format
    p.setFormat(combo.getSelectedIndex());

    // Remember One
    PointInTime start = deOne.getValue();
    if (start!=null)
      p.getStart().set(start);
  
    // Remember Two
    if ( p.isRange() ) {
      PointInTime end = deTwo.getValue();
      if (end!=null)
        p.getEnd().set(deTwo.getValue());
    }
    
    // Done
  }

  /**
   * Trigger for changes in editing components
   */
  public void itemStateChanged(ItemEvent e) {

    if (PropertyDate.isRange(combo.getSelectedIndex()))
      deOne.getParent().add(deTwo);
    else
      deOne.getParent().remove(deTwo);

    deOne.getParent().invalidate();
    deOne.getParent().validate();

    // notify of change
    changeSupport.fireChangeEvent();

    // Done
  }          

  /**
   * Initialize
   */
  public void init(Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setProp, setMgr, setReg);

    // we know it's a date
    PropertyDate p = (PropertyDate)property;

    // Components
    combo = new JComboBox();
    combo.setAlignmentX(0);
    combo.setEditable(false);
    combo.setMaximumSize(new Dimension(Integer.MAX_VALUE,combo.getPreferredSize().height));

    for (int i = 0; i <= PropertyDate.LAST_ATTRIB; i++) {
      combo.addItem(PropertyDate.getLabelForFormat(i));
    }
    add(combo);
    combo.addItemListener(this);

    WindowManager mgr = viewManager.getWindowManager();

    deOne = new DateWidget(p.getStart(), mgr);
    deOne.addChangeListener(changeSupport);
    deOne.setAlignmentX(0);
    add(deOne);

    deTwo = new DateWidget(p.getEnd(), mgr);
    deTwo.addChangeListener(changeSupport);
    deTwo.setAlignmentX(0);

    defaultFocus = deOne;

    combo.setSelectedIndex( p.getFormat() );

    // Done
  }

} //ProxyDate
