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
import genj.util.ActionDelegate;
import genj.util.swing.DateWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;

import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A bean for editing DATEs
 */
public class DateBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col><row><a/><b/></row><row><c/><d/></row></col>");

  private final static ImageIcon PIT = new ImageIcon(PropertyBean.class, "/genj/gedcom/images/Time.gif");
  
  /** members */
  private PropertyDate.Format format; 
  private DateWidget date1, date2;
  private PopupWidget choose;
  private JLabel label2;

  /**
   * Initializer
   */
  protected void initializeImpl() {
    
    // setup Laout
    setLayout(LAYOUT.copy());
    
    // prepare format change actions
    ArrayList actions = new ArrayList(10);
    for (int i=0;i<PropertyDate.FORMATS.length;i++)
      actions.add(new ChangeFormat(PropertyDate.FORMATS[i]));

    // .. the chooser (making sure the preferred size is pre-computed to fit-it-all)
    choose = new PopupWidget(null, null, actions);
    add(choose);
    
    // .. first date
    date1 = new DateWidget(viewManager.getWindowManager());
    date1.addChangeListener(changeSupport);
    add(date1);

    // .. second date
    label2 = new JLabel();
    add(label2);
    
    date2 = new DateWidget(viewManager.getWindowManager());
    date2.addChangeListener(changeSupport);
    add(date2);
    
    // setup default focus
    defaultFocus = date1;

    // Done
  }
  
  /**
   * Finish proxying edit for property Date
   */
  public void commit() {

    PropertyDate p = (PropertyDate)property;
    
    // check if valid
    PointInTime start = date1.getValue();
    if (start==null)
      return;
    PointInTime end = null;
    if (format.isRange()) {
      end = date2.getValue();
      if (end==null)
        return;
    }
    
    // Remember
    p.setValue(format, start, end);

    // Done
  }

  /**
   * Setup format
   */
  private void setFormat(PropertyDate.Format set) {

    PropertyDate p = (PropertyDate)property;
    
    // already?
    if (format==set)
      return;
    
    changeSupport.fireChangeEvent();

    // remember
    format = set;

    // set text of chooser 
    choose.setText(format.getLabel1());
    
    // check label2/date2 visibility
    if (format.isRange()) {
      date2.setVisible(true);
      label2.setVisible(true);
      label2.setText(format.getLabel2());
    } else {
      date2.setVisible(false);
      label2.setVisible(false);
    }

    // set image and tooltip of chooser
    choose.setIcon(format==PropertyDate.DATE ? PIT : null);
    choose.setToolTipText(format.getLabel());
    
    // show
    revalidate();
    repaint();
  }          
  

  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // we know it's a date
    PropertyDate p = (PropertyDate)property;

    // connect
    date1.setValue(p.getStart());
    date2.setValue(p.getEnd());
    setFormat(p.getFormat());
    
    // done
  }
  
  /**
   * Action for format change
   */
  private class ChangeFormat extends ActionDelegate {
    
    private PropertyDate.Format format;
    
    private ChangeFormat(PropertyDate.Format set) {
      format = set;
      super.setText(set.getLabel());
    }
    
    protected void execute() {
      setFormat(format);
    }
    
  } //ChangeFormat 

} //ProxyDate
