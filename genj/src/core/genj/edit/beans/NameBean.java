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

import java.awt.geom.Point2D;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyName;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : NAME
 */
public class NameBean extends PropertyBean {

  /** our components */
  private ChoiceWidget cLast;
  private TextFieldWidget tFirst, tSuff;

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
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    // first, last, suff
    PropertyName pname = (PropertyName)property;
    
    cLast  = new ChoiceWidget(PropertyName.getLastNames(setGedcom, true).toArray(), pname.getLastName());
    cLast.addChangeListener(changeSupport);
    tFirst = new TextFieldWidget(pname.getFirstName(), 10); 
    tFirst.addChangeListener(changeSupport);
    tSuff  = new TextFieldWidget(pname.getSuffix()   , 10); 
    tSuff.addChangeListener(changeSupport);

    add(new JLabel(PropertyName.getLabelForFirstName()));
    add(tFirst);

    add(new JLabel(PropertyName.getLabelForLastName()));
    add(cLast);

    add(new JLabel(PropertyName.getLabelForSuffix()));
    add(tSuff);


    defaultFocus = tFirst;

    // done
  }
  
  /**
   * Grow is ok
   */
  public Point2D getWeight() {
    return new Point2D.Double(0.1,0);
  }

} //ProxyName
