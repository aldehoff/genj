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

import genj.gedcom.PropertyName;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.TextFieldWidget;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
class ProxyName extends Proxy {

  /** our components */
  private ChoiceWidget cLast;
  private TextFieldWidget tFirst, tSuff;

  /**
   * Finish editing a property through proxy
   */
  protected void commit() {

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
   * Start editing a property through proxy
   */
  protected Editor getEditor() {

    Editor result = new Editor();
    result.setBoxLayout();
    
    // first, last, suff
    PropertyName pname = (PropertyName)property;
    
    cLast  = new ChoiceWidget(pname.getLastNames().toArray(), pname.getLastName());
    cLast.addChangeListener(this);
    tFirst = new TextFieldWidget(pname.getFirstName(), 10); 
    tFirst.addChangeListener(this);
    tSuff  = new TextFieldWidget(pname.getSuffix()   , 10); 
    tSuff.addChangeListener(this);

    result.add(new JLabel(pname.getLabelForFirstName()));
    result.add(tFirst);

    result.add(new JLabel(pname.getLabelForLastName()));
    result.add(cLast);

    result.add(new JLabel(pname.getLabelForSuffix()));
    result.add(tSuff);

    result.setFocus(tFirst);

    // done
    return result;

  }

} //ProxyName
