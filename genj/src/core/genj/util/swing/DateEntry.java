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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Generic component for editing dates
 * @author Tomas Dahlqvist fix for US, European and ISO handling of Date
 */
public class DateEntry extends JPanel {

  /** field for displaying (static) validity status */
  private JLabel lStatus;

  /** text-fields for day,month,year */
  private JTextField tfDay,tfMonth,tfYear;
  
  /** fields in order */
  private JTextField[] tfs = new JTextField[3];

  /** state change flag */
  private boolean changed;

  /** images */
  private ImageIcon
    imgGood = new ImageIcon(this,"DateGood.gif"),
    imgBad  = new ImageIcon(this,"DateBad.gif" );
    
  /**
   * Constructor
   */
  public DateEntry(int setDay, int setMonth, int setYear) {
    // Sub-components
    lStatus = new JLabel();
    tfDay   = new JTextField( (setDay  <0) ? "" : ""+setDay      ,2+1);
    tfMonth = new JTextField( (setMonth<0) ? "" : ""+(setMonth+1),2+1);
    tfYear  = new JTextField( (setYear <0) ? "" : ""+setYear     ,4+1);
    // order fields
    String format;
    switch (new SimpleDateFormat().toPattern().charAt(0)) {
      case 'm': case 'M':
        tfs[0] = tfMonth; tfs[1] = tfDay  ; tfs[2] = tfYear; format = "mm/dd/yyyy"; break;
      case 'd': case 'D':
        tfs[0] = tfDay  ; tfs[1] = tfMonth; tfs[2] = tfYear; format = "dd.mm.yyyy"; break;
      default: 
        tfs[0] = tfYear ; tfs[1] = tfMonth; tfs[2] = tfDay ; format = "yyyy-mm-dd"; break;
    }
    // Listeners
    Events e = new Events();
    tfDay  .getDocument().addDocumentListener(e);
    tfMonth.getDocument().addDocumentListener(e);
    tfYear .getDocument().addDocumentListener(e);
    tfDay  .addFocusListener(e);
    tfMonth.addFocusListener(e);
    tfYear .addFocusListener(e);
    addFocusListener(e);
    // Layout
    setLayout(new FlowLayout(FlowLayout.LEFT));
    add(lStatus);
    for (int i=0;i<tfs.length;i++) add(tfs[i]);
    add(new JLabel(format));
    // Status
    changed=false;
    checkStatus();
    // Done
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
   * Returns Day
   */
  public int getDay() {
    try {
      return Integer.parseInt(tfDay.getText());
    } catch (NumberFormatException e) {
      return -1;
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
  public int getMonth() {
    try {
      return Integer.parseInt(tfMonth.getText())-1;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Returns Year
   */
  public int getYear() {
    try {
      return Integer.parseInt(tfYear.getText());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Returns true when user has changed day/month/year
   */
  public boolean hasChanged() {
    return changed;
  }

//  /**
//   * Sets the date
//   */
//  public void setDate(Integer setYear, Integer setMonth, Integer setDay) {
//    tfDay  .setText( setDay  != null ? setDay  .toString() : "" );
//    tfMonth.setText( setMonth!= null ? setMonth.toString() : "" );
//    tfYear .setText( setYear != null ? setYear .toString() : "" );
//    changed=false;
//  }

  /**
   * Dis-/Enables the control
   */
  public void setEnabled(boolean set) {
    for (int i=0;i<tfs.length;i++) tfs[i].setEnabled(set);
  }
  
  /**
   * Glue to events
   */
  private class Events implements DocumentListener, FocusListener {
    /** note change */
    private void changed() {
      changed=true;
      checkStatus();
    }
    /** callback - text updated */
    public void changedUpdate(DocumentEvent e) {
      changed();
    }
    /** callback - text inserted */
    public void insertUpdate(DocumentEvent e) {
      changed();
      // jump to next?
      Object doc = e.getDocument();
      for (int i=0;i<tfs.length-1;i++) {
        JTextField tf = tfs[i]; 
        if (tf.getDocument()==doc && tf.getText().length()==tf.getColumns()-1) {
          tfs[i+1].requestFocus();
          break;
        }
      }
      // done
    }
    /** callback - text removed */
    public void removeUpdate(DocumentEvent e) {
      changed();
    }
    /** callback - focus gained */
    public void focusGained(FocusEvent e) {
      // me?
      if (e.getSource()==DateEntry.this) {
        tfs[0].requestFocus();
        return;
      }
      // one of the textfields!
      JTextField tf = (JTextField) e.getSource();
      tf.selectAll();
    }
    /** callback - focus lost */
    public void focusLost(FocusEvent e) {
    }
  } //Events
  
} //DateEntry
