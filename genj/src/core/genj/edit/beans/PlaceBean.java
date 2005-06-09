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
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
public class PlaceBean extends PropertyBean {

  private GridBagHelper gh = new GridBagHelper(this);
  private int rows = 0;
  private JCheckBox global = new JCheckBox();


  /**
   * Initialization
   */
  protected void initializeImpl() {
    // nothing much we can do - hook up to change events and show changeAll on change 
    changeSupport.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String confirm = getGlobalConfirmMessage();
        global.setVisible(confirm!=null);
        global.setToolTipText(confirm);
      }
    });
    // listen to selection of global and ask for confirmation
    global.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (global.isSelected()) {
          int rc = viewManager.getWindowManager().openDialog(null, resources.getString("choice.global.enable"), WindowManager.IMG_QUESTION, getGlobalConfirmMessage(), CloseWindow.YESandNO(), PlaceBean.this);
          global.setSelected(rc==0);
        }        
      }
    });
    
  }
  
  /**
   * Compute commit value
   */
  private String getCommitValue() {
    
    boolean hierarchy = ((PropertyPlace)property).getFormat().length()>0;
    
    // collect the result by looking at all of the choices
    StringBuffer result = new StringBuffer();
    for (int c=0, n=getComponentCount(), j=0; c<n; c++) {
      
      // check each text field
      Component comp = getComponent(c);
      if (comp instanceof ChoiceWidget) {
        
        String jurisdiction = ((ChoiceWidget)comp).getText().trim();
        
        // make sure the user doesn't enter a comma ',' if there is a field per jurisdiction
        if (hierarchy) jurisdiction = jurisdiction.replaceAll(",",";"); 
          
        // always add separator for jurisdictions j>0 regardless of jurisdiction.length()
        if (j++>0) result.append(","); 
        result.append(jurisdiction);
        
      }
      // next
    }

    return result.toString();
  }
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    // propagate change
    ((PropertyPlace)property).setValue(getCommitValue(), global.isSelected());
    
  
  }

  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // check property's format
    PropertyPlace place = (PropertyPlace)prop;
    
    // remove all current fields and clear current default focus - this is all dynamic for each context
    removeAll();
    rows = 0;
    defaultFocus = null;
   
    // either a simple value or broken down into comma separated jurisdictions
    if (place.getDisplayFormat().length()==0) {
      createChoice(null, place.getValue(), place.getAllJurisdictions(-1,true));
    } else {
      DirectAccessTokenizer format = new DirectAccessTokenizer(place.getDisplayFormat(), ",");
      DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer( place.getValue(), ",");
      for (int i=0;;i++) {
        if (format.get(i)==null&&jurisdictions.get(i)==null) break;
        createChoice(format.get(i), jurisdictions.get(i), place.getAllJurisdictions(i, true));
      }
    }

    // add 'change all'
    global.setVisible(false);
    global.setSelected(false);
    gh.add(global, 2, rows);
    
    // add filler
    gh.addFiller(1,++rows);
    
    // Done
  }
  
  private void createChoice(String label, String value, String[] values) {
    // next row
    rows++;
    // add a label for the jurisdiction name?
    if (label!=null) 
      gh.add(new JLabel(label, SwingConstants.RIGHT), 0, rows, 1, 1, GridBagHelper.FILL_HORIZONTAL);
    // and a textfield
    ChoiceWidget choice = new ChoiceWidget();
    choice.setEditable(true);
    choice.setValues(values);
    choice.setText(value);
    choice.addChangeListener(changeSupport);
    gh.add(choice, 1, rows, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
    // set default focus if not done yet
    if (defaultFocus==null) defaultFocus = choice;
    // done
  }

  /**
   * Create confirm message for global
   */
  private String getGlobalConfirmMessage() {
    int others = ((PropertyPlace)property).getSameChoices().length;
    if (others<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return resources.getString("choice.global.confirm", new String[]{ ""+others, property.getDisplayValue(), getCommitValue() });
  }
  
} //PlaceBean
