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

import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.swing.SwingFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
class ProxyName extends Proxy implements DocumentListener {

  /** our components */
  private SwingFactory.JComboBox cLast;
  private JTextField tFirst, tSuff;

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
    String first = tFirst.getText().trim();
    String last  = cLast .getText().trim();
    String suff  = tSuff .getText().trim();

    // ... store changed value
    PropertyName p = (PropertyName) prop;
    p.setName( first, last, suff );

    // Done
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return changed || cLast.hasChanged();
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

    // keep prop and setup name components
    prop=setProp;
    
    // first, last, suff
    PropertyName pname = (PropertyName)prop;
    
    cLast  = factory.JComboBox(pname.getLastNames().toArray(), pname.getLastName());
    cLast.setEditable(true);
    tFirst = createTextField( pname.getFirstName(),"FB",this, null  );
    tSuff  = createTextField( pname.getSuffix(),   "?", this, null );

    in.add(factory.JLabel(pname.getLabelForFirstName()));
    in.add(tFirst);

    in.add(factory.JLabel(pname.getLabelForLastName()));
    in.add(cLast);

    in.add(factory.JLabel(pname.getLabelForSuffix()));
    in.add(tSuff);

    // focus to first
    SwingFactory.requestFocusFor(tFirst);

    // Done
  }

} //ProxyName
