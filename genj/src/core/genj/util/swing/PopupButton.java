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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * A button that opens a context-menu on press
 */
public class PopupButton extends JToggleButton {
  
  /** popup */
  private JPopupMenu popup = null;
  
  /** list of actions */
  private List items;
  
  /**
   * Constructor  
   */
  public PopupButton() {
  }

  /**
   * Constructor  
   */
  public PopupButton(Icon icon) {
    this(null, icon);
  }

  /**
   * Constructor  
   */
  public PopupButton(String text) {
    this(text, null);
  }

  /**
   * Constructor  
   */
  public PopupButton(String text, Icon icon) {
    this(text, icon, null);
  }

  /**
   * Constructor
   */
  public PopupButton(String text, Icon icon, List actions) {
    // delegate
    super(text, icon);
    // keep actions
    setActions(actions);
    // done
  }
  
  /**
   * @see javax.swing.JComponent#addNotify()
   */
  public void addNotify() {
    // delegate
    super.addNotify();
    // check if we're in a toolbar
    if (getParent() instanceof JToolBar) {
      // patch max size (helps buttons look)
      setMaximumSize(new Dimension(128,128));
      // check placement
      int o = ((JToolBar)getParent()).getOrientation();
    }
    // done
  }

  
  /**
   * Change popup's visibility
   */
  public void showPopup(boolean b) {
    
    // cancel popup?
    if (!b&&popup!=null) popup.setVisible(false);
    
    // show popup?
    if (b&&popup==null) {
      List l = getActions();
      if (l!=null) {
        popup = new Popup();
        MenuHelper mh = new MenuHelper();
        mh.pushMenu(popup);
        mh.createItems(getActions());
        popup.show(PopupButton.this, getWidth(), 0);
      }
    }
    
    // done
  }
  
  /**
   * @see javax.swing.AbstractButton#fireActionPerformed(java.awt.event.ActionEvent)
   */
  protected void fireActionPerformed(ActionEvent event) {
    // delegate
    super.fireActionPerformed(event);
    // show popup
    showPopup(getModel().isSelected());
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
   * Popup
   */
  private class Popup extends JPopupMenu {
    /**
     * @see javax.swing.JPopupMenu#setVisible(boolean)
     */
    public void setVisible(boolean b) {
      super.setVisible(b);
      if (!b) {
        popup=null;
        getModel().setSelected(false);
      } 
    }
  } //Popup
  
} //PopupButton
