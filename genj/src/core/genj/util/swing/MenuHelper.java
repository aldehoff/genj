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
  private JMenu menu              = null;  
  private ActionListener listener = null;
  private Vector collection       = null;
  private Resources resources     = null;
  private JMenuBar bar            = null;
  private boolean enabled         = true;

  /** Setters */    
  public MenuHelper setMenu(JMenu set) { menu=set; return this; }
  public MenuHelper setCollection(Vector set) { collection=set; return this; }
  public MenuHelper setResources(Resources set) { resources=set; return this; }
  public MenuHelper setBar(JMenuBar set) { bar=set; return this; }
  public MenuHelper setEnabled(boolean set) { enabled=set; return this; }

  /**
   * Creates a menubar
   */
  public JMenuBar createBar() {
    JMenuBar result = new JMenuBar();
    setBar(result);
    return result;
  }

  /**
   * Creates a menu
   */
  public JMenu createMenu(String text) {
    JMenu result = new JMenu(string(text));
    if ((menu==null)&&(bar!=null)) bar.add(result);
    setMenu(result);
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
    if (menu!=null) menu.add(result);
    if (collection!=null) collection.addElement(result);
    
    return result;
  }
  
  /**
   * Creates an separator
   */
  public MenuHelper createSeparator() {
    if (menu!=null) menu.addSeparator();
    return this;
  }

  /**
   * Helper resolving a text
   */
  private String string(String txt) {
    if (resources==null) return txt;
    return resources.getString(txt);
  }
}
