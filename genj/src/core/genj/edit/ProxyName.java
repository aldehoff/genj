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

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Vector;

import genj.gedcom.*;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
class ProxyName extends Proxy implements DocumentListener {

  /** our components */
  private JTextField tlast, tfirst, tsuff;

  /** dirty flag */
  private boolean changed=false;

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
    if ( !hasChanged() ) {
      return;
    }

    // ... calc texts
    String first = tfirst.getText().trim();
    String last  = tlast .getText().trim();
    String suff  = tsuff .getText().trim();

    // ... store changed value
    PropertyName p = (PropertyName) prop;
    p.setName( first, last, suff );

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

    prop=setProp;
    PropertyName pname = (PropertyName)prop;

    // Unknown property can be changed through a TextField
    tlast  = createTextField( pname.getLastName() ,"B" ,this, null );
    tfirst = createTextField( pname.getFirstName(),"FB",this, null  );
    tsuff  = createTextField( pname.getSuffix(),   "?", this, null );

    JLabel l = createLabel( pname.getLabelForFirstName() , "LFN");
    in.add(l);
    in.add(tfirst);

    l = createLabel( pname.getLabelForLastName(), "LLN" );
    in.add(l);
    in.add(tlast);

    l = createLabel( pname.getLabelForSuffix(), "?" );
    in.add(l);
    in.add(tsuff);

    in.add(Box.createVerticalGlue());

    tfirst.requestFocus();

    // Done
  }

}
