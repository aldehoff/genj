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

import genj.gedcom.PropertyAge;
import genj.gedcom.time.Delta;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextFieldWidget;

import javax.swing.JLabel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
class ProxyAge extends Proxy {
  
  private final static String TEMPLATE = "99y 9m 9d";

  /** age */
  private PropertyAge age;

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  protected void commit() {
    property.setValue(tfield.getText());
  }

  /**
   * Start editing a property through proxy
   */
  protected Editor getEditor() {

    age = (PropertyAge)property;
    
    // create input
    tfield = new TextFieldWidget(change, property.getValue(), TEMPLATE.length());

    // layout
    Editor result = new Editor();
    GridBagHelper gh = new GridBagHelper(result);
    gh.add(tfield                                       ,0,0);
    gh.setParameter(gh.GROWFILL_HORIZONTAL);
    gh.add(new JLabel(TEMPLATE)                         ,1,0);
    gh.setParameter(0);
    gh.add(new ButtonHelper().create(new ActionUpdate()),2,0);
    gh.addFiller(1,1);

    // Done
    return result;
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
      setTip(resources.getString("proxy.age.tip"));
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
