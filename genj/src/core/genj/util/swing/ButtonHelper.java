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
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.SwingConstants;

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
  private Vector collections      = new Vector();
  private boolean isTextAllowed   = true;
  private boolean isImageAllowed  = true;
  private boolean isImageOverText = false;
  private int fontSize            = -1;
  private boolean isShortTexts   = false;
  
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
  public ButtonHelper addCollection(Vector set) { collections.addElement(set); return this; }
  public ButtonHelper removeCollection(Vector set) { collections.removeElement(set); return this; }
  public ButtonHelper setImageAllowed(boolean set) { isImageAllowed=set; return this; }
  public ButtonHelper setTextAllowed(boolean set) { isTextAllowed=set; return this; }
  public ButtonHelper setImageOverText(boolean set) { isImageOverText=set; return this; }
  public ButtonHelper setFontSize(int set) { fontSize=set; return this; }
  public ButtonHelper setShortTexts(boolean set) { isShortTexts=set; return this; }
  
  
  /**
   * Creates the button
   */
  public JButton create(ActionDelegate action) {
    
    JButton result = new JButton();
    
    // its text
    String s = string((isShortTexts&&action.stxt!=null) ? action.stxt : action.txt);
    result.putClientProperty("save.text", s);
    if (isTextAllowed) result.setText(s);
    
    // its image
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
    if (isImageOverText) {
      result.setVerticalTextPosition(SwingConstants.BOTTOM);
      result.setHorizontalTextPosition(SwingConstants.CENTER);
    }
    if (horizontalAlignment>=0)
      result.setHorizontalAlignment(horizontalAlignment);
    if (fontSize>0) {
      Font f = result.getFont();
      result.setFont(new Font(f.getName(), f.getStyle(), fontSize));
    }
    
    result.setBorderPainted(isBorder);
    result.setRequestFocusEnabled(isFocusable); // This should be setFocusable which comes with JDK1.4
    result.setEnabled(isEnabled);
    result.addActionListener((ActionListener)action.as(ActionListener.class));

    if (container!=null)
      container.add(result);
    if (collections.size()>0) {
      Enumeration e = collections.elements();
      while (e.hasMoreElements()) {
        ((Vector)e.nextElement()).addElement(result);
      }
    }

    return result;
  }

  /**
   * Helper that takes given text and tries to use resources
   */
  private String string(String string) {
    if (string==null) return "";
    if (resources!=null) 
      string = resources.getString(string);
    return string;    
  }
  
  /**
   * Helper that en/disables a set of buttons
   */
  public static void setEnabled(Vector v, boolean set) {
    Enumeration e = v.elements();
    while (e.hasMoreElements()) {
      ((AbstractButton)e.nextElement()).setEnabled(set);
    }
  }
  
  /**
   * Helper that sets/unsets text for a set of buttons
   */
  public static void setTextAllowed(Vector v, boolean set) {
    Enumeration e = v.elements();
    while (e.hasMoreElements()) {
      AbstractButton b = (AbstractButton)e.nextElement();
      if (set) {
        Object s = b.getClientProperty("save.text");
        if (s!=null) b.setText(s.toString());
      } else {
        b.setText(null);
      }
    }
  }
  
}
