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
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextFieldWidget;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : UNKNOWN
 */
class ProxyAge extends Proxy {
  
  /** age */
  private PropertyAge age;

  /** members */
  private TextFieldWidget tfield;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    if (hasChanged())
      property.setValue(tfield.getText());

  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return tfield.hasChanged();
  }

  private final static String TEMPLATE = "99y 9m 9d";

  /**
   * Start editing a property through proxy
   */
  protected JComponent start(JPanel in) {

    age = (PropertyAge)property;
    
    // create input
    tfield = new TextFieldWidget(property.getValue(), TEMPLATE.length());

    // layout
    JPanel panel = new JPanel();
    panel.setAlignmentX(0F);

    GridBagHelper gh = new GridBagHelper(panel);
    gh.add(tfield                                       ,0,0);
    gh.setParameter(gh.GROWFILL_HORIZONTAL);
    gh.add(new JLabel(TEMPLATE)                         ,1,0);
    gh.setParameter(0);
    gh.add(new ButtonHelper().create(new ActionUpdate()),2,0);
    gh.addFiller(1,1);
    in.add(panel);
    
    // Done
    return tfield;
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
      String txt = age.getAgeString(age.getEarlier(), age.getLater(), true);
      tfield.setText(txt);
      tfield.setChanged(true);
    }
  } //ActionUpdate

} //ProxyAge
