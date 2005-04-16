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
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.gedcom.time.PointInTime;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.DateWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.PopupWidget;
import genj.view.ViewManager;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : DATE
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
   * Finish proxying edit for property Date
   */
  public void commit(Transaction tx) {

    PropertyDate p = (PropertyDate)property;

    // Remember format
    p.setFormat(format);

    // Remember One
    PointInTime start = date1.getValue();
    if (start!=null)
      p.getStart().set(start);
  
    // Remember Two
    if ( p.isRange() ) {
      PointInTime end = date2.getValue();
      if (end!=null)
        p.getEnd().set(date2.getValue());
    }
    
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
      if (date2==null) {
        label2 = new JLabel();
        add(label2);
        date2 = new DateWidget(p.getEnd(), viewManager.getWindowManager());
        date2.addChangeListener(changeSupport);
        add(date2);
      }
      date2.setVisible(true);
      label2.setVisible(true);
      label2.setText(format.getLabel2());
    } else {
      if (date2!=null) date2.setVisible(false);
      if (label2!=null) label2.setVisible(false);
    }

    // set image and tooltip of chooser
    choose.setIcon(format==PropertyDate.DATE ? PIT : null);
    choose.setToolTipText(format.getLabel());
    
    // show
    revalidate();
    repaint();
  }          
  
  private static Dimension preferredPopupSize;
  
  /**
   * set cached calculated preferred size for popup
   */
  private static void setPreferredSize(PopupWidget choose) {
    
    // unknown?
    if (preferredPopupSize==null) {

      // calculate image alone
      choose.setIcon(PIT);
      preferredPopupSize = choose.getPreferredSize();
      choose.setIcon(null);
      
      // loop over date format texts and patch preferred
      for (int i=0,j=PropertyDate.FORMATS.length;i<j;i++) {
        choose.setText(PropertyDate.FORMATS[i].getLabel1());
        Dimension pref = choose.getPreferredSize();
        preferredPopupSize.width = Math.max(preferredPopupSize.width , pref.width );
        preferredPopupSize.height= Math.max(preferredPopupSize.height, pref.height);
      }
      
    }
    
    // set it
    choose.setPreferredSize(preferredPopupSize);

  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);
    
    // setup Laout
    setLayout(LAYOUT.copy());
    
    // we know it's a date
    PropertyDate p = (PropertyDate)property;

    // prepare format change actions
    ArrayList actions = new ArrayList(10);
    for (int i=0;i<PropertyDate.FORMATS.length;i++)
      actions.add(new ChangeFormat(PropertyDate.FORMATS[i]));

    // .. the chooser (making sure the preferred size is pre-computed to fit-it-all)
    choose = new PopupWidget(null, null, actions);
    add(choose);
    
    // .. first date
    date1 = new DateWidget(p.getStart(), viewManager.getWindowManager());
    date1.addChangeListener(changeSupport);
    add(date1);

    // set format
    setFormat(p.getFormat());
    
    // done
    defaultFocus = date1;

    // Done
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
