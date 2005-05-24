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
import genj.gedcom.PropertyPlace;
import genj.util.DirectAccessTokenizer;
import genj.util.GridBagHelper;
import genj.util.swing.ChoiceWidget;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
public class PlaceBean extends PropertyBean {

  private GridBagHelper gh = new GridBagHelper(this);
  int rows = 0;

  /**
   * Initialization
   */
  protected void initializeImpl() {
    // too dynamic to setup now
  }
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    StringBuffer result = new StringBuffer();

    boolean first = true;
    for (int i=0, j=getComponentCount(); i<j; i++) {
      // check each text field
      Component comp = getComponent(i);
      if (comp instanceof ChoiceWidget) {
        String jurisdiction = ((ChoiceWidget)comp).getText().trim();
        if (!first) result.append(","); // make sure to add a comma even if result.length()==0 since an empty jurisdiction was entered
        result.append(jurisdiction);
        first = false;
      }
      // next
    }
    property.setValue(result.toString());
  }

  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // check property's format
    PropertyPlace place = (PropertyPlace)prop;
    
    // remove all current fields - this is dynamic
    removeAll();
    
    // clear current default focus - will be decided on again
    defaultFocus = null;
   
    // either a simple value or broken down into comma separated jurisdictions
    if (place.getDisplayHierarchy().length()==0) {
      createField(null, place.getValue(), -1);
    } else {
      DirectAccessTokenizer hierarchyTokens = new DirectAccessTokenizer(place.getDisplayHierarchy(), ",", true);
      DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer( place.getValue(), ",", true);
      for (int i=0;;i++) {
        String hierarchyToken = hierarchyTokens.get(i);
        String jurisdiction = jurisdictions.get(i);
        if (hierarchyToken==null&&jurisdiction==null) break;
        createField(hierarchyToken!=null ? hierarchyToken : null, jurisdiction!=null ? jurisdiction : "", i);
      }
    }
    
    // add filler
    gh.addFiller(1,rows);
    
    // Done
  }
  
  private ChoiceWidget createField(String name, String jurisdiction, int hierarchyLevel) {
    // add a label for the jurisdiction name?
    if (name!=null) 
      gh.add(new JLabel(name, SwingConstants.RIGHT), 0, rows, 1, 1, GridBagHelper.FILL_HORIZONTAL);
    // and a textfield
    ChoiceWidget result = new ChoiceWidget();
    result.setEditable(true);
    result.setValues(((PropertyPlace)property).getJurisdictions(hierarchyLevel, true));
    result.setText(jurisdiction);
    result.addChangeListener(changeSupport);
    gh.add(result, 1, rows, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
    // set default focus if not done yet
    if (defaultFocus==null) defaultFocus = result;
    // increase rows
    rows++;
    // done
    return result;
  }

} //ProxyUnknown
