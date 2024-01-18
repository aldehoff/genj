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
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.DirectAccessTokenizer;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

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
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {
    
    StringBuffer result = new StringBuffer();
    int commas = 0;
    
    for (int i=0, j=getComponentCount(); i<j; i++) {
      // check each text field
      Component comp = getComponent(i);
      if (comp instanceof TextFieldWidget) {
        
        String jurisdiction = ((TextFieldWidget)comp).getText().trim();
        if (jurisdiction.length()>0) {
          while (commas>0) { result.append(","); commas--; }
          result.append(jurisdiction);
        }
        commas++;
      }
      // next
    }
    property.setValue(result.toString());
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);

    // check property's format
    PropertyPlace place = (PropertyPlace)setProp;
   
    // either a simple value or broken down into comma separated jurisdictions
    String placeFormat = place.getHierarchy();
    if (placeFormat.length()==0) {
      createText(null, place.getValue());
    } else {
      DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(placeFormat, ",", true);
      DirectAccessTokenizer values = new DirectAccessTokenizer( place.getValue(), ",", true);
      for (int i=0;;i++) {
        String jurisdiction = jurisdictions.get(i);
        String value = values.get(i);
        if (jurisdiction==null&&value==null) break;
        createText(jurisdiction!=null ? jurisdiction : null, value!=null ? value : "");
      }
    }
    
    // add filler
    gh.addFiller(1,rows);
    
    // Done
  }
  
  private TextFieldWidget createText(String label, String txt) {
    // add a label?
    if (label!=null) 
      gh.add(new JLabel(label, SwingConstants.RIGHT), 0, rows, 1, 1, gh.FILL_HORIZONTAL);
    // and a textfield
    TextFieldWidget result = new TextFieldWidget(txt, 20);
    result.addChangeListener(changeSupport);
    gh.add(result, 1, rows, 1, 1, gh.GROWFILL_HORIZONTAL);
    // set default focus if not done yet
    if (defaultFocus==null) defaultFocus = result;
    // increase rows
    rows++;
    // done
    return result;
  }

} //ProxyUnknown
