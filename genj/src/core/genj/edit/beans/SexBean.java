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
import genj.gedcom.PropertySex;
import genj.util.ActionDelegate;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.view.ViewManager;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : SEX
 */
public class SexBean extends PropertyBean {

  /** members */
  private AbstractButton[] buttons = new AbstractButton[3];
  
  /**
   * Finish editing a property through proxy
   */
  public void commit() {
    
    PropertySex sex = (PropertySex)property; 
    sex.setSex(getSex());
  }
  
  /**
   * Get current sex
   */
  private int getSex() {
    
    // Gather data change
    for (int i=0;i<buttons.length;i++) {
      if (buttons[i].isSelected()) 
        return i;
    }
        
    // unknown
    return PropertySex.UNKNOWN;
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, ViewManager setMgr, Registry setReg) {

    super.init(setGedcom, setProp, setMgr, setReg);
  
    // use our layout
    setLayout(new HVLayout());
      
    // we know it's PropertySex
    PropertySex p = (PropertySex) property;

    // create buttons    
    ButtonHelper bh = new ButtonHelper()
      .setButtonType(JRadioButton.class)
      .setContainer(this);
    bh.createGroup();
    for (int i=0;i<buttons.length;i++)
      buttons[i] = bh.create( new Gender(i) );
    buttons[p.getSex()].setSelected(true);
    
    defaultFocus = buttons[p.getSex()];

    // Done
  }
  
  /**
   * HVLayout - layout for components either in one horizontal row or one vertical column as fits best
   * for the surrounding container
   */
  public static class HVLayout implements LayoutManager {

    /** the current layout */
    private boolean horizontal = true;
    
    /**
     * callback - ignored
     */
    public void addLayoutComponent(String name, Component comp) {
      // ignored
    }

    /**
     * callback - ignored
     */
    public void removeLayoutComponent(Component comp) {
      // ignored
    }
    
    /**
     * minimum == preferred
     */
    public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
    }
    
    /**
     * depends on horizontal or not
     */
    public Dimension preferredLayoutSize(Container parent) {
      
      // loop children and check widths/heights according to hori vs. vert    
      int 
        w = 0,
        h = 0;
      for (int i=0,j=parent.getComponentCount();i<j;i++) {
        Component c = parent.getComponent(i);
        Dimension d = c.getPreferredSize();
        if (horizontal) {
          w = w + d.width;
          h = Math.max(h, d.height);
        } else {
          w = Math.max(w, d.width);
          h = h + d.height;
        }
      }
      
      // done
      return new Dimension(w,h);
    }
    
    boolean shuffle = true;
    
    /**
     * our layout logic
     */
    public void layoutContainer(final Container parent) {
      
      // needs to be JComponent
      if (!(parent instanceof JComponent))
        throw new IllegalArgumentException("!parent instanceof JComponent");

      // synchronized layout
      synchronized (parent.getTreeLock()) {

        // check orientation and potentially revalidate
        if (checkOrientation(parent)) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              shuffle = false;
              ((JComponent)parent).revalidate();
            }
          });
          return;
        }
  
        // layout vertical or horizontal
        for (int i=0,j=parent.getComponentCount(),x=0,y=0;i<j;i++) {
          Component c = parent.getComponent(i);
          Dimension p = c.getPreferredSize();
          
          if (horizontal) {
            c.setBounds(x, 0, p.width, p.height);
            x += p.width;
          } else {
            c.setBounds(0, y, p.width, p.height);
            y += p.height;
          }
        }

        shuffle = true;

        // done
      }
    }

    /**
     * resolve a root container for given parent (either topmost parent or JViewport)
     */
    private Container getRoot(Container parent) {

      // go up the hierarchy looking for topmost parent or JViewport
      for (parent=parent.getParent(); parent.getParent()!=null && !(parent instanceof JViewport); parent=parent.getParent());
      
      // done
      return parent;
    }
    
    /**
     * check orientation for better fit
     * @return true if orientation was changed
     */
    private boolean checkOrientation(Container parent) {
      
      // look for root container
      Container root = getRoot(parent);
        
      // check for our impact on viewport
      Dimension 
        preferred = root.getPreferredSize(),
        actual    = root.getSize();
            
      // horizontal : check if vertical would be better
      if (horizontal) {

        horizontal = false;
        Dimension couldbe = root.getPreferredSize();
        
        // stick with it if width an issue and height ok
        if (actual.width<preferred.width && actual.height>=couldbe.height)
          return true;

        // also if better use of space
        if (shuffle && actual.height>actual.width && actual.height>=couldbe.height)
          return true;
          
        // fallback to horizontal
        horizontal = true;
        // need to call preferred again to rollback any change of state
        root.getPreferredSize();

        // no change
        return false;
        
      } 

      // vertical : check if horizontal would be better 
      horizontal=true;
      Dimension couldbe = root.getPreferredSize();
      
      // stick with it if height an issue and width ok
      if (actual.height<preferred.height && actual.width>=couldbe.width)
        return true;

      // also if better use of space
      if (shuffle && actual.width>actual.height && actual.width>=couldbe.width)
        return true;

      // fallback to vertical
      horizontal = false;
      // need to call preferred again to rollback any change of state
      root.getPreferredSize();
        
      // no change
      return false; 
    }
    
  } //HVLayout
  
  /**
   * Gender change action
   */
  private class Gender extends ActionDelegate {
    int sex;
    private Gender(int sex) {
      this.sex = sex;
      setText(PropertySex.getLabelForSex(sex));
    }
    protected void execute() {
      changeSupport.fireChangeEvent();
    }

  } //Gender

} //ProxySex

