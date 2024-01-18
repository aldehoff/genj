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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;

/**
 * Helper for button creation etc.
 */
public class ButtonHelper {
  
  private Insets insets           = null;
  private boolean isEnabled       = true;
  private boolean isFocusable     = true;
  private boolean isBorder        = true;
  private Resources resources     = null;
  private Container container     = null;
  private Dimension minSize       = null;
  private int horizontalAlignment = -1;
  private Vector collection       = null;
  private boolean isTextAllowed   = true;
  private boolean isImageAllowed  = true;

  /** Setters */    
  public ButtonHelper setInsets(Insets set) { insets=set; return this; }
  public ButtonHelper setInsets(int val) { insets=new Insets(val,val,val,val); return this; }
  public ButtonHelper setEnabled(boolean set) { isEnabled=set; return this; }
  public ButtonHelper setFocusable(boolean set) { isFocusable=set; return this; }
  public ButtonHelper setBorder(boolean set) { isBorder=set; return this; }
  public ButtonHelper setResources(Resources set) { resources=set; return this; }
  public ButtonHelper setContainer(Container set) { container=set; return this; }
  public ButtonHelper setMinimumSize(Dimension set) { minSize=set; return this; }
  public ButtonHelper setHorizontalAlignment(int set) { horizontalAlignment=set; return this; }
  public ButtonHelper setCollection(Vector set) { collection=set; return this; }
  public ButtonHelper setImageAllowed(boolean set) { isImageAllowed=set; return this; }
  public ButtonHelper setTextAllowed(boolean set) { isTextAllowed=set; return this; }
  
  
  /**
   * Creates the button
   */
  public JButton create(ActionDelegate action) {
    
    JButton result = new JButton();
    
    if (isTextAllowed&&action.txt!=null) 
      result.setText(string(action.txt));
    if (isImageAllowed&&action.img!=null) 
      result.setIcon(ImgIconConverter.get(action.img));
    if (isImageAllowed&&action.roll!=null)
      result.setRolloverIcon(ImgIconConverter.get(action.roll));
    if (action.tip!=null) 
      result.setToolTipText(string(action.tip));
    if (insets!=null)
      result.setMargin(insets);
    if (minSize!=null)
      result.setMinimumSize(minSize);
    if (horizontalAlignment>=0)
      result.setHorizontalAlignment(horizontalAlignment);
    
    result.setBorderPainted(isBorder);
    result.setRequestFocusEnabled(isFocusable); // This should be setFocusable which comes with JDK1.4
    result.setEnabled(isEnabled);
    result.addActionListener(action);

    if (container!=null)
      container.add(result);
    if (collection!=null)
      collection.addElement(result);

    return result;
  }

  /**
   * Helper that takes given text and tries to use resources
   */
  private String string(String string) {
    if (resources!=null) 
      string = resources.getString(string);
    return string;    
  }
}
