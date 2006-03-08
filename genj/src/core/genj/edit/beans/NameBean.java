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

import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class NameBean extends PropertyBean {

  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout("<col><row><l/><v wx=\"1\"/></row><row><l/><v wx=\"1\"/></row><row><l/><v wx=\"1\"/></row></col>");
  
  /** our components */
  private ChoiceWidget cLast, cFirst;
  private TextFieldWidget tSuff;

  void initialize(ViewManager setViewManager, Registry setRegistry) {
    super.initialize(setViewManager, setRegistry);
    
    setLayout(LAYOUT.copy());

    cLast  = new ChoiceWidget();
    cLast.addChangeListener(changeSupport);
    cLast.setIgnoreCase(true);
    cFirst = new ChoiceWidget();
    cFirst.addChangeListener(changeSupport);
    cFirst.setIgnoreCase(true);
    tSuff  = new TextFieldWidget("", 10); 
    tSuff.addChangeListener(changeSupport);

    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(cFirst);

    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast);

    add(new JLabel(PropertyName.getLabelForSuffix()));
    add(tSuff);


    defaultFocus = cFirst;

  }

  /**
   * Finish editing a property through proxy
   */
  public void commit(Property property) {

    // ... calc texts
    String first = cFirst.getText().trim();
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
  public void setProperty(PropertyName name) {

    // remember property
    property = name;
    
    // first, last, suff
    
    cLast.setValues(name.getLastNames(true));
    cLast.setText(name.getLastName());
    cFirst.setValues(name.getFirstNames(true));
    cFirst.setText(name.getFirstName()); 
    tSuff.setText(name.getSuffix()); 

    // done
  }

} //ProxyName
