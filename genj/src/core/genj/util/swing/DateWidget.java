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
package genj.util.swing;

import genj.gedcom.PointInTime;
import genj.util.ActionDelegate;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Generic component for editing dates
 * @author Tomas Dahlqvist fix for US, European and ISO handling of Date
 */
public class DateWidget extends JPanel {
  
  /** calendar */
  private PointInTime.Calendar calendar = PointInTime.CALENDARS[0];

  /** components */
  private PopupWidget widgetCalendar; 
  private TextFieldWidget widgetDay,widgetYear;
  private ChoiceWidget widgetMonth;
  
  /** localized month names */
  private final static String[] MONTHS = PointInTime.getMonths(true, false);
  
  /** calendar switches */
  private ArrayList switches; 
    
  /**
   * Constructor
   */
  public DateWidget(PointInTime pit) {
    
    // create calendar switches
    switches = new ArrayList(PointInTime.CALENDARS.length);
    for (int s=0;s<PointInTime.CALENDARS.length;s++)
      switches.add(new SwitchCalendar(PointInTime.CALENDARS[s]));
    
    // initialize Sub-components
    widgetCalendar = new PopupWidget(pit.getCalendar().getImage()); 
    widgetCalendar.setMargin(new Insets(1,1,1,1));
    widgetCalendar.setActions(switches);
    
    //  FIXME reflect pit's calendar
    
    widgetDay   = new TextFieldWidget( int2string(pit.getDay  (), true ),2+1);
    
    widgetMonth = new ChoiceWidget(MONTHS, "");
    widgetMonth.setIgnoreCase(true);
    try {
      widgetMonth.setSelectedItem(MONTHS[pit.getMonth()]);
      widgetMonth.setChanged(false);
    } catch (ArrayIndexOutOfBoundsException e) {
    }

    widgetYear  = new TextFieldWidget( int2string(pit.getYear (), false),4+1);
    
    // Layout
    setLayout(new FlowLayout(FlowLayout.LEFT));
    
    add(widgetCalendar);
    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
      case 'm': case 'M':
        add(widgetMonth); add(widgetDay) ; add(widgetYear); format = "mmm/dd/yyyy"; break;
      case 'd': case 'D':
        add(widgetDay) ; add(widgetMonth); add(widgetYear); format = "dd.mmm.yyyy"; break;
      default: 
        add(widgetYear); add(widgetMonth); add(widgetDay ); format = "yyyy-mmm-dd"; break;
    }
    super.setToolTipText(format);
    
    // Listeners
    Events e = new Events();
    widgetDay  .addFocusListener(e);
    widgetYear .addFocusListener(e);
    widgetMonth.getEditor().getEditorComponent().addFocusListener(e);
    addFocusListener(e);
    
    // Status
    checkValidDate();
    
    // Done
  }
  
  /**
   * Get current value
   */
  public PointInTime getValue() {
    int d = string2int(widgetDay.getText(), true);
    int y = string2int(widgetYear.getText(), false);
    int m = string2int(widgetMonth.getText(), true);
    if (m<0&&widgetMonth.getText().equals(widgetMonth.getSelectedItem())) 
      m = widgetMonth.getSelectedIndex();    
    return PointInTime.getPointInTime(d, m, y);
  }

  /**
   * Update the status icon
   */
  private void checkValidDate() {
    widgetCalendar.setEnabled(getValue().isValid());
  }

  /**
   * Return the maximum size this component should be sized to
   */
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }

  /**
   * Returns true when user has changed day/month/year
   */
  public boolean hasChanged() {
    return widgetDay.hasChanged() || widgetYear.hasChanged() || widgetMonth.hasChanged();
  }

  /**
   * transfer a date int into String
   */
  private String int2string(int i, boolean zeroBased) {
    if (i<0)
      return "";
    if (zeroBased)
      i++;    
    return Integer.toString(i);
  }
  
  /**
   * transfer a data string into int
   */
  private int string2int(String s, boolean zeroBased) {
    try {
      return Integer.parseInt(s) - (zeroBased?1:0);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
  public void requestFocus() {
    // try JDK 1.4's requestFocusInWindow instead
    try {
      super.requestFocusInWindow();
    } catch (Throwable t) {
      super.requestFocus();
    }
  }
  
  /**
   * Glue to events
   */
  private class Events implements FocusListener {
    /** callback - focus gained */
    public void focusGained(FocusEvent e) {
      checkValidDate();
      // me?
      if (e.getSource()==DateWidget.this) {
        getComponent(1).requestFocus();
        return;
      }
      // one of the textfields!
      if (e.getSource() instanceof JTextField)
        ((JTextField) e.getSource()).selectAll();
      // done
    }
    /** callback - focus lost */
    public void focusLost(FocusEvent e) {
    }
  } //Events
  
  /**
   * Action to switch calendar
   */
  private class SwitchCalendar extends ActionDelegate {
    /**
     * Constructor
     */
    private SwitchCalendar(PointInTime.Calendar calendar) {
      setImage(calendar.getImage());
      setText(calendar.getName());
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // FIXME switch calendar
    }
  } // SwitchCalendar
  
} //DateEntry
