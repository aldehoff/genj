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

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.text.BreakIterator;

import genj.gedcom.*;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
class ProxyMLE extends Proxy implements DocumentListener {

  /** members */
  private JTextArea tarea;
  private boolean changed=false;
  private int rows;

  /**
   * Trigger for Document changes
   */
  public void changedUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    // Has something been edited ?
    if ( !hasChanged() )
      return;

    // Calc new value
    String value = tarea.getText();
    prop.setValue(value);

    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return changed;
  }

  /**
   * Trigger for Document changes
   */
  public void insertUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Trigger for Document changes
   */
  public void removeUpdate(DocumentEvent e) {
    changed = true;
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    // Remember property
    prop=setProp;

    // Calculate value to show
    String value="";

    Property.LineIterator iterator = prop.getLineIterator();
    if (iterator==null) {
      value = prop.getValue();
    } else {
      while (iterator.hasMoreValues())
      value += iterator.getNextValue() +"\n";
    }
    tarea = new JTextArea(value,6,20);
    tarea.getDocument().addDocumentListener(this);

    JScrollPane spane = new JScrollPane(tarea);
    spane.setBorder(BorderFactory.createMatteBorder(4,4,4,4,in.getBackground()));
    spane.setAlignmentX(0);
    in.add(spane);

    // Done
    tarea.requestFocus();
  }
}
