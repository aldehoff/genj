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

import genj.util.ImgIcon;
import genj.util.Resources;

import java.awt.Container;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * Helper for button creation etc.
 */
public class ButtonHelper {
  
  private String text             = null;
  private ImgIcon image           = null;
  private ImgIcon rollover        = null;
  private Insets insets           = null;
  private String tip              = null;
  private boolean isEnabled       = true;
  private boolean isFocusable     = true;
  private boolean isBorder        = true;
  private Resources resources     = null;
  private Container container     = null;
  private ActionListener listener = null;
  private String action           = null;

  /** Setters */    
  public ButtonHelper setText(String set) { text=set; return this; }
  public ButtonHelper setImage(ImgIcon set) { image=set; return this; }
  public ButtonHelper setRollover(ImgIcon set) { rollover=set; return this; }
  public ButtonHelper setInsets(Insets set) { insets=set; return this; }
  public ButtonHelper setInsets(int val) { insets=new Insets(val,val,val,val); return this; }
  public ButtonHelper setTip(String set) { tip=set; return this; }
  public ButtonHelper setEnabled(boolean set) { isEnabled=set; return this; }
  public ButtonHelper setFocusable(boolean set) { isFocusable=set; return this; }
  public ButtonHelper setBorder(boolean set) { isBorder=set; return this; }
  public ButtonHelper setResources(Resources set) { resources=set; return this; }
  public ButtonHelper setContainer(Container set) { container=set; return this; }
  public ButtonHelper setListener(ActionListener set) { listener=set; return this; }
  public ButtonHelper setAction(String set) { action=set; return this; }
    
  /**
   * Creates the button
   */
  public JButton create() {
    
    JButton result = new JButton();
    
    if (text!=null) 
      result.setText(string(text));
    if (image!=null) 
      result.setIcon(ImgIconConverter.get(image));
    if (rollover!=null) {
      result.setRolloverIcon(ImgIconConverter.get(rollover));
    }
    if (tip!=null) 
      result.setToolTipText(string(tip));
    if (insets!=null)
      result.setMargin(insets);
    
    result.setBorderPainted(isBorder);
    result.setFocusable(isFocusable);
    result.setEnabled(isEnabled);

    if (action!=null)
      result.setActionCommand(action);
    if (listener!=null)
      result.addActionListener(listener);

    if (container!=null)
      container.add(result);

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
