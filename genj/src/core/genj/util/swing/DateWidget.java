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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Generic component for editing dates
 * @author Tomas Dahlqvist fix for US, European and ISO handling of Date
 */
public class DateWidget extends JPanel {

  /** field for displaying (static) validity status */
  private JLabel lStatus;

  /** text-fields for day,month,year */
  private TextFieldWidget tfDay,tfYear;
  private ChoiceWidget cMonth;
  
  /** images */
  private ImageIcon
    imgGood = new ImageIcon(this,"DateGood.gif"),
    imgBad  = new ImageIcon(this,"DateBad.gif" );
    
  /** localized month names */
  private final static String[] MONTHS = PointInTime.getMonths(true, false); 
    
  /**
   * Constructor
   */
  public DateWidget(PointInTime pit) {
    
    // Sub-components
    lStatus = new JLabel();
    tfDay   = new TextFieldWidget( int2string(pit.getDay  (), true ),2+1);
    cMonth  = new ChoiceWidget(MONTHS, "");
    cMonth.setIgnoreCase(true);
    try {
      cMonth.setSelectedItem(MONTHS[pit.getMonth()]);
      cMonth.setChanged(false);
    } catch (ArrayIndexOutOfBoundsException e) {
    }
    tfYear  = new TextFieldWidget( int2string(pit.getYear (), false),4+1);
    
    // Layout
    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(lStatus);
    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
      case 'm': case 'M':
        add(cMonth); add(tfDay) ; add(tfYear); format = "mmm/dd/yyyy"; break;
      case 'd': case 'D':
        add(tfDay) ; add(cMonth); add(tfYear); format = "dd.mmm.yyyy"; break;
      default: 
        add(tfYear); add(cMonth); add(tfDay ); format = "yyyy-mmm-dd"; break;
    }
    super.setToolTipText(format);
    
    // Listeners
    Events e = new Events();
    tfDay  .addFocusListener(e);
    tfYear .addFocusListener(e);
    cMonth.getEditor().getEditorComponent().addFocusListener(e);
    addFocusListener(e);
    
    // Status
    updateIcon();
    
    // Done
  }
  
  /**
   * Get current value
   */
  public PointInTime getValue() {
    int d = string2int(tfDay.getText(), true);
    int y = string2int(tfYear.getText(), false);
    int m = string2int(cMonth.getText(), true);
    if (m<0&&cMonth.getText().equals(cMonth.getSelectedItem())) 
      m = cMonth.getSelectedIndex();    
    return PointInTime.getPointInTime(d, m, y);
  }

  /**
   * Update the status icon
   */
  private void updateIcon() {
    lStatus.setIcon(getValue().isValid() ? imgGood : imgBad);
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
    return tfDay.hasChanged() || tfYear.hasChanged() || cMonth.hasChanged();
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
      getClass().getMethod("requestFocusInWindow", new Class[]{})
        .invoke(this, new Object[]{});
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
      updateIcon();
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
  
} //DateEntry
