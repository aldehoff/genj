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

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import genj.util.*;

/**
 * Generic component for editing dates
 */
public class DateEntry extends javax.swing.JPanel implements DocumentListener, FocusListener {

  /** field for displaying (static) validity status */
  private JLabel lStatus;

  /** text-fields for day,month,year */
  private JTextField tfDay,tfMonth,tfYear;

  /** state change flag */
  private boolean changed;

  /** images */
  private ImageIcon
    imgGood = ImgIconConverter.get(new ImgIcon(this,"DateGood.gif")),
    imgBad  = ImgIconConverter.get(new ImgIcon(this,"DateBad.gif" ));

  /**
   * Constructor
   */
  public DateEntry(Integer setDay, Integer setMonth, Integer setYear) {
    // Sub-components
    lStatus = new JLabel();
    tfDay   = new JTextField( (setDay  != null) ? setDay  .toString() : "",2+1);
    tfMonth = new JTextField( (setMonth!= null) ? setMonth.toString() : "",2+1);
    tfYear  = new JTextField( (setYear != null) ? setYear .toString() : "",4+1);
    // Listeners
    tfDay  .getDocument().addDocumentListener(this);
    tfMonth.getDocument().addDocumentListener(this);
    tfYear .getDocument().addDocumentListener(this);
    tfDay  .addFocusListener(this);
    tfMonth.addFocusListener(this);
    tfYear .addFocusListener(this);
    // Layout
    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(lStatus);
    add(tfDay);
    add(tfMonth);
    add(tfYear);
    add(new JLabel("dd/mm/yyyy"));	// dkionka: I assumed mm/dd/yyyy
    // Status
    changed=false;
    checkStatus();
    // Done
  }

  /**
   * Day/Month/Year has changed
   */
  public void changedUpdate(DocumentEvent e) {
    changed=true;
    checkStatus();
  }

  /**
   * Checks if date is o.k and sets corresponding image
   * dkionka: added simple month range check
   */
  private void checkStatus() {

    Integer d=null,m=null,y=null;

    try {
      y = new Integer(tfYear .getText());
    } catch (NumberFormatException e) { }
    try {
      m = new Integer(tfMonth.getText());
      if ((m.intValue() < 1) || (m.intValue() > 12))
        m = null;			// month[] index in PropertyDate
    } catch (NumberFormatException e) { }
    try {
      d = new Integer(tfDay  .getText());
    } catch (NumberFormatException e) { }

    if (  ((d!=null)&&(m!=null)&&(y!=null))
       || ((d==null)&&(m!=null)&&(y!=null))
       || ((d==null)&&(m==null)&&(y!=null)) ) {
      lStatus.setIcon(imgGood);
    } else {
      lStatus.setIcon(imgBad);
    }

    // Done
  }

  /**
   * Day/Month/Year got focus
   */
  public void focusGained(FocusEvent e) {
    JTextField tf = (JTextField) e.getSource();
    tf.selectAll();
  }

  /**
   * Day/Month/Year lost focus
  */
  public void focusLost(FocusEvent e) {
  }

  /**
   * Returns Day
   */
  public Integer getDay() {
    try {
      return new Integer(tfDay.getText());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Return the maximum size this component should be sized to
   */
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  /**
   * Returns Month
   */
  public Integer getMonth() {
    try {
      return new Integer(tfMonth.getText());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Returns Year
   */
  public Integer getYear() {
    try {
      return new Integer(tfYear.getText());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Returns true when user has changed day/month/year
   */
  public boolean hasChanged() {
    return changed;
  }

  /**
   * Day/Month/Year has changed
   */
  public void insertUpdate(DocumentEvent e) {
    changed=true;
    checkStatus();

    if (e.getDocument() == tfDay.getDocument()) {
      if (tfDay.getText().length()==2)
      tfMonth.requestFocus();
      return;
    }
    if (e.getDocument() == tfMonth.getDocument()) {
      if (tfMonth.getText().length()==2)
      tfYear.requestFocus();
      return;
    }
  }

  /**
   * Day/Month/Year has changed
   */
  public void removeUpdate(DocumentEvent e) {
    changed=true;
    checkStatus();
  }

  /**
   * Sets the date
   */
  public void setDate(Integer setYear, Integer setMonth, Integer setDay) {
    tfDay  .setText( setDay  != null ? setDay  .toString() : "" );
    tfMonth.setText( setMonth!= null ? setMonth.toString() : "" );
    tfYear .setText( setYear != null ? setYear .toString() : "" );
    changed=false;
  }

  /**
   * Dis-/Enables the control
   */
  public void setEnabled(boolean set) {
    tfDay  .setEnabled(set);
    tfMonth.setEnabled(set);
    tfYear .setEnabled(set);
  }
}
