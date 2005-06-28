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

import genj.util.ActionDelegate;
import genj.util.MnemonicAndText;
import genj.util.Resources;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Class which provides some static helpers for menu-handling
 */
public class MenuHelper  {
  
  private String text             = null;
  private String action           = null;
  private ImageIcon image         = null;
  private Stack menus            = new Stack();  // JMenu or JPopupMenu or JMenuBar
  private ActionListener listener = null;
  private Vector collection       = null;
  private Resources resources     = null;
  private boolean enabled         = true;
  private JComponent target       = null;

  /** Setters */    
  public MenuHelper popMenu() { 
    // pop it of the stack
    JMenu menu = (JMenu)menus.pop(); 
    // remove it if empty
    if (menu.getMenuComponentCount()==0)
      menu.getParent().remove(menu);
    // done
    return this; 
  }
  public MenuHelper pushMenu(JPopupMenu popup) { menus.push(popup); return this; }
  public MenuHelper setCollection(Vector set) { collection=set; return this; }
  public MenuHelper setResources(Resources set) { resources=set; return this; }
  public MenuHelper setEnabled(boolean set) { enabled=set; return this; }
  public MenuHelper setTarget(JComponent set) { target=set; return this; }

  /**
   * Creates a menubar
   */
  public JMenuBar createBar() {
    JMenuBar result = new JMenuBar();
    menus.push(result);
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
    set(text, result);
    if (img!=null) 
      result.setIcon(img);
    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).add(result);
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).add(result);
    if (menu instanceof JMenuBar)
      ((JMenuBar)menu).add(result);

    menus.push(result);
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
   * Creates a PopupMenu
   */
  public JPopupMenu createPopup(Component component) {
    
    // create one
    final JPopupMenu result = createPopup();
    
    // start listening for it
    component.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        // 20020829 on some OSes isPopupTrigger() will
        // be true on mousePressed
        mouseReleased(e);
      }
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          result.show(e.getComponent(),e.getX(), e.getY());
        }
      }
    });
    
    // done
    return result;
  }
  
  /**
   * Creates a simple text items
   */
  public JLabel createItem(String txt, ImageIcon img, boolean emphasized) {
    JLabel item = new JLabel(txt, img, JLabel.CENTER);
    if (emphasized) {
      item.setFont(item.getFont().deriveFont(Font.BOLD));
    }
    createItem(item);
    return item;
  }
  
  private void createItem(Component item) {

    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).add(item);
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).add(item);
    if (menu instanceof JMenuBar)
      ((JMenuBar)menu).add(item);
    
  }

  /**
   * Creates items from list of ActionDelegates
   * @param actions either ActionDelegates or lists of ActionDelegates that
   * will be separated visually by createSeparator
   */
  public void createItems(List actions, boolean addLeadingSeparator) {
    // nothing to do?
    if (actions==null||actions.isEmpty())
      return;
    // add separator
    if (addLeadingSeparator)
      createSeparator(false);      
    // Loop through list
    Iterator it = actions.iterator();
    while (it.hasNext()) {
      Object o = it.next();
      // a nested list ?
      if (o instanceof List) {
        createSeparator(false);
        createItems((List)o, false);
        continue;
      }
      // a component?
      if (o instanceof Component) {
        createItem((Component)o);
        continue;
      }
      // an action?
      if (o instanceof ActionDelegate) {
        createItem((ActionDelegate)o);
        continue;
      }
      // n/a
      throw new IllegalArgumentException("type "+o.getClass()+" n/a");
    }
    // done
  }

  /**
   * Creates an item
   */
  public JMenuItem createItem(ActionDelegate action) {
    
    // a NOOP results in separator
    if (action == ActionDelegate.NOOP) {
      createSeparator(false);
      return null;
    }
    
    // create a menu item
    JMenuItem result = new JMenuItem();
    result.addActionListener(action);
    if (action.getText()!=null) 
      set(action.getText(), result);
    if (action.getImage()!=null) 
      result.setIcon(action.getImage());
    result.setEnabled(enabled&&action.isEnabled());
  
    // add it to current menu on stack  
    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).add(result);
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).add(result);
    if (menu instanceof JMenuBar)
      ((JMenuBar)menu).add(result);
      
    // put it in collection if applicable
    if (collection!=null) collection.addElement(result);

    // propagate target
    if (target!=null) action.setTarget(target);
    
    // done
    return result;
  }

  /**
   * Creates an separator
   */
  public MenuHelper createSeparator() {
    return createSeparator(true);
  }
  
  /**
   * Creates an separator
   * @param force whether to force the separator even though the current
   * menu is empty
   */
  public MenuHelper createSeparator(boolean force) {
    // try to create one
    Object menu = peekMenu();
    if (menu instanceof JMenu) {
      JMenu jmenu = (JMenu)menu;
      if (force||jmenu.getItemCount()>0)
        jmenu.addSeparator();
    }
    if (menu instanceof JPopupMenu) {
      JPopupMenu pmenu = (JPopupMenu)menu;
      if (force||pmenu.getComponentCount()>0)
        pmenu.addSeparator();
    }
    // done      
    return this;
  }

  /**
   * Helper getting the top Menu from the stack
   */  
  private Object peekMenu() {
    if (menus.size()==0) return null;
    return menus.peek();
  }
  
  /**
   * Set text taking mnemonic into account
   */
  private void set(String txt, JMenuItem item) {

    // no text?
    if (txt==null) 
      return;
    // localize?
    if (resources!=null) {
      txt = resources.getString(txt);  
    }
    // safety check on ""
    if (txt.length()==0)
      return;
    // calc mnemonic 
    MnemonicAndText mat = new MnemonicAndText(txt);
    item.setText(mat.getText());
    item.setMnemonic(mat.getMnemonic());
  }

} //MenuHelper

