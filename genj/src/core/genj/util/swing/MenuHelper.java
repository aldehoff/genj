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
import genj.util.Debug;
import genj.util.ImgIcon;
import genj.util.Resources;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

/**
 * Class which provides some static helpers for menu-handling
 */
public class MenuHelper  {
  
  private String text             = null;
  private String action           = null;
  private ImgIcon image           = null;
  private Vector menus            = new Vector();  // JMenu or JPopupMenu or JMenuBar
  private ActionListener listener = null;
  private Vector collection       = null;
  private Resources resources     = null;
  private boolean enabled         = true;

  /** Setters */    
  public MenuHelper popMenu() { menus.removeElement(menus.lastElement()); return this; }
  public MenuHelper pushMenu(Object set) { menus.addElement(set); return this; }
  public MenuHelper setCollection(Vector set) { collection=set; return this; }
  public MenuHelper setResources(Resources set) { resources=set; return this; }
  public MenuHelper setEnabled(boolean set) { enabled=set; return this; }

  /**
   * Creates a menubar
   */
  public JMenuBar createBar() {
    JMenuBar result = new JMenuBar();
    pushMenu(result);
    return result;
  }

  /**
   * Creates a menu
   */
  public JMenu createMenu(String text) {
    JMenu result = new JMenu(string(text));

    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).add(result);
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).add(result);
    if (menu instanceof JMenuBar)
      ((JMenuBar)menu).add(result);

    pushMenu(result);
    return result;
  }

  /**
   * Creates a PopupMenu
   */
  public JPopupMenu createPopup(String label, Component component) {
    
    // create one
    final JPopupMenu result = new JPopupMenu(string(label));
    
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
    
    // that's the menu now
    pushMenu(result);
    
    // done
    return result;
  }

  /**
   * Creates an item
   */
  public JMenuItem createItem(ActionDelegate action) {
    
    JMenuItem result = new JMenuItem();
    result.addActionListener((ActionListener)action.as(ActionListener.class));
    if (action.txt!=null) result.setText(string(action.txt));
    if (action.img!=null) result.setIcon(ImgIconConverter.get(action.img));
    result.setEnabled(enabled);
    
    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).add(result);
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).add(result);
    if (menu instanceof JMenuBar)
      ((JMenuBar)menu).add(result);
      
    if (collection!=null) collection.addElement(result);
    
    return result;
  }
  
  /**
   * Creates an separator
   */
  public MenuHelper createSeparator() {

    Object menu = peekMenu();
    if (menu instanceof JMenu)
      ((JMenu)menu).addSeparator();
    if (menu instanceof JPopupMenu)
      ((JPopupMenu)menu).addSeparator();
      
    return this;
  }

  /**
   * Helper resolving a text
   */
  private String string(String txt) {
    if (txt==null) return "";
    if (resources==null) return txt;
    return resources.getString(txt);
  }

  /**
   * Helper getting the top Menu from the stack
   */  
  private Object peekMenu() {
    if (menus.size()==0) return null;
    return menus.lastElement();
  }
}
