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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * A button that opens a context-menu on press
 */
public class PopupWidget extends JButton {
  
  /** popup */
  private JPopupMenu popup = null;
  
  /** list of actions */
  private List items = new ArrayList();

  /** whether we fire the first of the available actions on popup click */
  private boolean isFireOnClick = false;
    
  /**
   * Constructor  
   */
  public PopupWidget() {
    this((Icon)null);
  }

  /**
   * Constructor  
   */
  public PopupWidget(Icon icon) {
    this(null, icon);
  }

  /**
   * Constructor  
   */
  public PopupWidget(String text) {
    this(text, null);
  }

  /**
   * Constructor  
   */
  public PopupWidget(String text, Icon icon) {
    this(text, icon, null);
  }

  /**
   * Constructor
   */
  public PopupWidget(String text, Icon icon, List actions) {
    // delegate
    super(text, icon);
    // our own model
    setModel(new Model());
    // keep actions
    if (actions!=null) setActions(actions);
    // make non-focusable
    setFocusable(false);
    // done
  }
  
  /**
   * Our special model
   */
  private class Model extends DefaultButtonModel implements Runnable {
    /** our menu trigger */
    public void setPressed(boolean b) {
      // continue
      super.setPressed(b);
      // show menue (delayed)
      if (b) SwingUtilities.invokeLater(this);
    }
    /** EDT callback */
    public void run() { 
      showPopup(); 
    }
    /**
     * action performed
     */
    protected void fireActionPerformed(ActionEvent e) {
      // fire action on popup button press?
      if (isFireOnClick) { 
        List as = getActions();
        if (!as.isEmpty())
          ((ActionDelegate)as.get(0)).trigger();
      }
    }
  } //Model
  
  /**
   * Gets the toolbar we're in (might be null)
   */
  protected JToolBar getToolBar() {
    if (!(getParent() instanceof JToolBar)) return null;
    return (JToolBar)getParent();
  }
  
  /**
   * Change popup's visibility
   */
  public void showPopup() {
    
    // cancel popup?
    if (popup!=null) { 
      popup.setVisible(false);
      popup = null;
    }
    
    // show popup?
    List as = getActions(); //give chance to override
    if (!as.isEmpty()) {

      // .. create an populate        
      popup = new JPopupMenu();
      popup.setBackground(Color.white);
      MenuHelper mh = new MenuHelper();
      mh.pushMenu(popup);
      mh.createItems(as);

      // .. calc position
      int x=0, y=0;
      JToolBar bar = getToolBar();
      if (bar==null) {
        x += getWidth();
      } else {
        if (JToolBar.VERTICAL==bar.getOrientation()) {
          x += bar.getLocation().x==0 ? getWidth() : -popup.getPreferredSize().width;
        } else {
          y += bar.getLocation().y==0 ? getHeight() : -popup.getPreferredSize().height;
        }
      }

      // .. show        
      popup.show(PopupWidget.this, x, y);
    }
    
    // done
  }
  
  /**
   * Accessor - the actions in the popup
   */
  public List getActions() {
    return items;
  }
  
  /**
   * Accessor - the actions in the popup
   */
  public void setActions(List actions) {
    items = actions;
  }

  /**
   * Part of 1.4 we override for usage under 1.3
   */
  public void setFocusable(boolean focusable) {
    try {
      super.setFocusable(focusable);
    } catch (Throwable t) {
    }
  }
  
  /**
   * Setting this to true will fire first available action
   * on popup button click (default off) 
   */
  public void setFireOnClick(boolean set) {
    isFireOnClick = set;
  }

} //PopupButton
