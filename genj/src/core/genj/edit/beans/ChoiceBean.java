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
import genj.gedcom.PropertyChoiceValue;
import genj.util.GridBagHelper;
import genj.util.swing.Action2;
import genj.util.swing.ChoiceWidget;
import genj.window.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A bean for editing choice properties(e.g. RELA)
 * @author nils@meiers.net
 * @author Tomas Dahlqvist fix for prefix lookup
 */
public class ChoiceBean extends PropertyBean {

  /** members */
  private ChoiceWidget choice;
  private JCheckBox global;
  
  /**
   * Initialization
   */
  protected void initializeImpl() {
    
    // prepare a choice for the user
    choice = new ChoiceWidget();
    choice.addChangeListener(changeSupport);
    choice.setIgnoreCase(true);

    // add a checkbox for global
    global = new JCheckBox();
    global.setBorder(new EmptyBorder(1,1,1,1));
    global.setVisible(false);
    global.setRequestFocusEnabled(false);
    
    // listen to changes in choice and show global checkbox if applicable
    choice.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        String confirm = getGlobalConfirmMessage();
        if (confirm!=null) {
          global.setVisible(true);
          global.setToolTipText(confirm);
        }
      }
    });
    
    // listen to selection of global and ask for confirmation
    global.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String confirm = getGlobalConfirmMessage();
        if (confirm!=null&&global.isSelected()) {
          int rc = viewManager.getWindowManager().openDialog(null, resources.getString("choice.global.enable"), WindowManager.QUESTION_MESSAGE, confirm, Action2.yesNo(), ChoiceBean.this);
          global.setSelected(rc==0);
        }        
      }
    });
    
    // layout
    GridBagHelper layout = new GridBagHelper(this);
    layout.add(choice, 0, 0, 1, 1, GridBagHelper.GROWFILL_HORIZONTAL);
    layout.add(global, 1, 0);
    layout.addFiller(0,1);
    
    // focus
    defaultFocus = choice;
  }
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    PropertyChoiceValue prop = (PropertyChoiceValue)property;

	    // change value
	    prop.setValue(choice.getText(), global.isSelected());
      
    // Done
  }

  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // setup choices    
    // Note: we're using getDisplayValue() here because like in PropertyRelationship's 
    // case there might be more in the gedcom value than what we want to display 
    // e.g. witness@INDI:BIRT
    choice.setValues(((PropertyChoiceValue)property).getChoices(true));
    choice.setText(property!=null&&!property.isSecret() ? property.getDisplayValue() : "");
    global.setSelected(false);
    global.setVisible(false);
    
    // done
  }

  /**
   * Create confirm message for global
   */
  private String getGlobalConfirmMessage() {
    int others = ((PropertyChoiceValue)property).getSameChoices().length;
    if (others<2)
      return null;
    // we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    return resources.getString("choice.global.confirm", new String[]{ ""+others, property.getDisplayValue(), choice.getText() });
  }
  
} //ProxyChoice
