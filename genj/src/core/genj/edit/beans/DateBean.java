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
import genj.gedcom.time.PointInTime;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.DateWidget;
import genj.util.swing.ImageIcon;
import genj.util.swing.PopupWidget;
import genj.view.ViewManager;
import genj.window.WindowManager;

import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : DATE
 */
public class DateBean extends PropertyBean {

  private ImageIcon PIT = new ImageIcon(this, "/genj/gedcom/images/Time.gif");
  
  /** members */
  private PropertyDate.Format format; 
  private DateWidget date1, date2;
  private PopupWidget choose;
  private JLabel label;

  /**
   * Finish proxying edit for property Date
   */
  public void commit() {

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

    changeSupport.fireChangeEvent();

    // remember
    format = set;

    // check date2 visibility
    date2.setVisible(format.isRange());
    choose.setText(format.getLabel1());
    label.setText(format.getLabel2());

    choose.setIcon(format==PropertyDate.DATE ? PIT : null);
    
    // show
    revalidate();
    repaint();
  }          

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setMgr, setReg);
    
    // we know it's a date
    PropertyDate p = (PropertyDate)property;

    // prepare format change actions
    ArrayList actions = new ArrayList(10);
    for (int i=0;i<PropertyDate.FORMATS.length;i++)
      actions.add(new ChangeFormat(PropertyDate.FORMATS[i]));

    // setup components
    WindowManager mgr = viewManager.getWindowManager();
    
    choose = new PopupWidget("from", PIT, actions);

    date1 = new DateWidget(p.getStart(), mgr);
    date1.addChangeListener(changeSupport);
    date1.setAlignmentX(0);

    label = new JLabel("to");
    
    date2 = new DateWidget(p.getEnd(), mgr);
    date2.addChangeListener(changeSupport);

    // setup Laout
    GridBagHelper gh = new GridBagHelper(this);

    gh.add(choose, 0, 0);
    gh.add(date1 , 1, 0);
    gh.add(label , 0, 1);
    gh.add(date2 , 1, 1);
    gh.add(new JLabel(), 2, 2, 1, 1, GridBagHelper.GROW_BOTH);
    
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
