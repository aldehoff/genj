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
import genj.gedcom.PropertyName;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class NameBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col><row><l/><v wx=\"1\"/></row><row><l/><v wx=\"1\"/></row><row><l/><v wx=\"1\"/></row></col>");
  
  /** our components */
  private ChoiceWidget cLast;
  private TextFieldWidget tFirst, tSuff;

  /**
   * Initialization
   */
  protected void initializeImpl() {
    
    setLayout(LAYOUT.copy());

    cLast  = new ChoiceWidget();
    cLast.addChangeListener(changeSupport);
    tFirst = new TextFieldWidget("", 10); 
    tFirst.addChangeListener(changeSupport);
    tSuff  = new TextFieldWidget("", 10); 
    tSuff.addChangeListener(changeSupport);

    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(tFirst);

    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast);

    add(new JLabel(PropertyName.getLabelForSuffix()));
    add(tSuff);


    defaultFocus = tFirst;

  }

  /**
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {

    // ... calc texts
    String first = tFirst.getText().trim();
    String last  = cLast .getText().trim();
    String suff  = tSuff .getText().trim();

    // ... store changed value
    PropertyName p = (PropertyName) property;
    p.setName( first, last, suff );

    // Done
  }

  /**
   * Set context to edit
   */
  protected void setContextImpl(Gedcom ged, Property prop, TagPath path, Registry reg) {

    // first, last, suff
    PropertyName pname = (PropertyName)property;
    
    cLast.setValues(PropertyName.getLastNames(gedcom, true));
    cLast.setText(pname.getLastName());
    tFirst.setText(pname.getFirstName()); 
    tSuff.setText(pname.getSuffix()); 

    // done
  }

} //ProxyName
