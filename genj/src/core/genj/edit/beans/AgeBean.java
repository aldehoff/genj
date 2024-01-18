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
import genj.gedcom.PropertyAge;
import genj.gedcom.time.Delta;
import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;

import javax.swing.JLabel;

/**
 * A bean that lets the user edit AGE
 */
public class AgeBean extends PropertyBean {
  
  private final static String TEMPLATE = "99y 9m 9d";

  /** members */
  private TextFieldWidget tfield;
  private ActionUpdate update;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    property.setValue(tfield.getText());
  }
  
  /** 
   * Initialize once
   */
  protected void initializeImpl() {

    tfield = new TextFieldWidget("", TEMPLATE.length());
    tfield.addChangeListener(changeSupport);
    
    setLayout(new NestedBlockLayout("<col><row><value/><template/></row><row><action/></row></col>"));
    add(tfield);
    add(new JLabel(TEMPLATE));
    
    update =  new ActionUpdate();
    add(new ButtonHelper().create(update));
    
  }
  
  /**
   * Set context to edit
   */
  protected void setContextImpl(Property prop) {

    // update components
    PropertyAge age = (PropertyAge)property;
    
    tfield.setText(property.getValue());
    update.setEnabled(age.getEarlier()!=null&&age.getLater()!=null);

    // Done
  }
  
  /**
   * Action Update age
   */
  private class ActionUpdate extends ActionDelegate {
    /**
     * Constructor
     */
    private ActionUpdate() {
      setImage(PropertyAge.IMG);
      setTip(resources.getString("age.tip"));
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      PropertyAge age = (PropertyAge)property;
      Delta delta = Delta.get(age.getEarlier(), age.getLater());
      if (delta==null)
        return;
      tfield.setText(delta.getValue());
    }
  } //ActionUpdate

} //ProxyAge
