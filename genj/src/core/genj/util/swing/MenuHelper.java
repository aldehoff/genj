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
package genj.util.swing;

import genj.util.MnemonicAndText;
import genj.view.ActionProvider.SeparatorAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Class which provides some static helpers for menu-handling
 */
public class MenuHelper  {

  private List<Action2> actions = new ArrayList<Action2>(16);
  private Stack<JComponent> menus = new Stack<JComponent>();  // JMenu or JPopupMenu or JMenuBar
  
  /** Setters */    
  public MenuHelper popMenu() { 
    menus.pop(); 
    return this; 
  }
  
  public MenuHelper pushMenu(JComponent menu) { 
    if (!menus.isEmpty())
      menus.peek().add(menu);
    menus.push(menu);
    
    return this;
  }
  
  public JMenu createMenu(Action2.Group action) {
    JMenu result = new JMenu(action);
    pushMenu(result);
    for (Action2 sub : action)
      createItem(sub);
    return result;
  }
  
  /**
   * Creates a menu
   */
  public JMenu createMenu(String text) {
    return createMenu(text, null);
  }

  /**
   * Creates a menu
   */
  public JMenu createMenu(String text, Icon img) {
    
    JMenu result = new JMenu();
    // text support for mnemonic
    if (text!=null&&text.length()>0) {
      MnemonicAndText mat = new MnemonicAndText(text);
      result.setText(mat.getText());
      result.setMnemonic(mat.getMnemonic());
    }
    if (img!=null) 
      result.setIcon(img);
    pushMenu(result);
    return result;
  }
  
  /**
   * Creates a PopupMenu
   */
  public JPopupMenu createPopup() {
    // create one
    JPopupMenu result = new JPopupMenu();
    // that's the menu now
    pushMenu(result);
    // done
    return result;
  }

  /**
   * Creates items from list of ActionDelegates
   * @param actions either ActionDelegates or lists of ActionDelegates that
   * will be separated visually by createSeparator
   */
  public void createItems(Iterable<Action2> actions) {
    // historically - supported null
    if (actions==null)
      return;
    // Loop through list
    boolean first = true;
    for (Action2 action : actions) {
      if (first) {
        createSeparator();
        first = false;
      }
      createItem(action);
    }
    // done
  }
  
  public List<Action2> getActions() {
    return actions;
  }
  
  public JMenuItem createItem(Action2 action) {
    
    // an action group?
    if (action instanceof Action2.Group) {
      Action2.Group group = (Action2.Group)action;
      if (group.size()==0)
        return null;
      JMenu sub = new JMenu(action);
      sub.setMnemonic(action.getMnemonic());
      pushMenu(sub);
      createItems(group);
      popMenu();
      return sub;
    }
    
    // a NOOP results in separator
    // TODO this should not refer to something from genj.view
    if (action instanceof SeparatorAction) {
      createSeparator();
      return null;
    }
    
    // create a menu item
    JMenuItem result = new JMenuItem();
    result.setAction(action);
    result.setMnemonic(action.getMnemonic());
    
    actions.add(action);
    
    // add it to current menu on stack  
    menus.peek().add(result);
      
    // done
    return result;
  }

  /**
   * Creates an separator
   */
  public MenuHelper createSeparator() {
    // try to create one
    JComponent menu = menus.peek();
    if (menu instanceof JMenu) {
      JMenu jmenu = (JMenu)menu;
      int count = jmenu.getMenuComponentCount();
      if (count>0 && jmenu.getMenuComponent(count-1).getClass() != JPopupMenu.Separator.class)
        jmenu.addSeparator();
    }
    if (menu instanceof JPopupMenu) {
      JPopupMenu pmenu = (JPopupMenu)menu;
      int count = pmenu.getComponentCount();
      if (count>0 && pmenu.getComponent(count-1).getClass() != JPopupMenu.Separator.class)
        pmenu.addSeparator();
    }
    // done      
    return this;
  }
  
} //MenuHelper

