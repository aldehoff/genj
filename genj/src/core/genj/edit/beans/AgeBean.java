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
import genj.gedcom.PropertyAge;
import genj.gedcom.time.Delta;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : AGE
 */
public class AgeBean extends PropertyBean {
  
  private final static String TEMPLATE = "99y 9m 9d";

  /** age */
  private PropertyAge age;

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    property.setValue(tfield.getText());
  }

  /**
   * Start editing a property
   */
  public void init(Gedcom setGedcom, Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setMgr, setReg);

    // keep age
    age = (PropertyAge)property;
    
    // create input
    tfield = new TextFieldWidget(property.getValue(), TEMPLATE.length());
    tfield.addChangeListener(changeSupport);

    // layout
    GridBagHelper gh = new GridBagHelper(this);
    gh.add(tfield                                       ,0,0);
    gh.setParameter(GridBagHelper.GROWFILL_HORIZONTAL);
    gh.add(new JLabel(TEMPLATE)                         ,1,0);
    gh.setParameter(0);
    gh.add(new ButtonHelper().create(new ActionUpdate()),2,0);
    gh.addFiller(1,1);

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
      setImage(property.getImage(false));
      setTip(resources.getString("age.tip"));
      if (age.getEarlier()==null||age.getLater()==null)
        setEnabled(false);
    }
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      Delta delta = Delta.get(age.getEarlier(), age.getLater());
      if (delta==null)
        return;
      tfield.setText(delta.getValue());
    }
  } //ActionUpdate

} //ProxyAge
