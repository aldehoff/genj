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
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.TagPath;
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.ChoiceWidget;
import genj.view.ViewManager;
import genj.window.CloseWindow;
import genj.window.WindowManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : Choice (e.g. RELA)
 * @author nils@meiers.net
 * @author Tomas Dahlqvist fix for prefix lookup
 */
public class ChoiceBean extends PropertyBean {

  /** members */
  private ChoiceWidget choice;
  private JCheckBox global;
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    PropertyChoiceValue prop = (PropertyChoiceValue)property;

    // change value
    prop.setValue(choice.getText(), global.isSelected());

    // refresh choices
    choice.setValues(prop.getChoices(gedcom).toArray());

    // hide global
    global.setSelected(false);
    global.setVisible(false);
    
    // Done
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setPath, setMgr, setReg);
    
    // setup choices
    Object[] items = ((PropertyChoiceValue)property).getChoices(setGedcom).toArray();

    // prepare a choice for the user - we're using getDisplayValue() here
    // because like in PropertyRelationship's case there might be more
    // in the gedcom value than what we want to display (witness@INDI:BIRT)
    choice = new ChoiceWidget(items, property.getDisplayValue());
    choice.addChangeListener(changeSupport);

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
          int rc = viewManager.getWindowManager().openDialog(null, resources.getString("choice.global.enable"), WindowManager.IMG_QUESTION, confirm, CloseWindow.YESandNO(), ChoiceBean.this);
          if (rc!=0)
            global.setSelected(false);
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
  
  /**
   * growth is good
   */
  public Point2D getWeight() {
    return new Point2D.Double(0.5,0);
  }
  
} //ProxyChoice
