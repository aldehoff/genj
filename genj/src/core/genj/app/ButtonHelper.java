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
package genj.app;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class which provides some static helpers for JButtons
 */
public class ButtonHelper  {

  /**
   * Helper that creates a button
   */
  static public JButton createButton(String label, ImageIcon img, String action, ActionListener listener) {
  return createButton(label,img,action,listener,true,true);
  }          
  /**
   * Helper that creates a button
   */
  static public JButton createButton(String label, ImageIcon img, String action, ActionListener listener, boolean enabled) {
  return createButton(label,img,action,listener,enabled,true);
  }          
  /**
   * Helper that creates a button
   */
  static public JButton createButton(String label, ImageIcon img, String action, ActionListener listener, boolean enabled, boolean margin) {

  JButton result = new JButton();
  if (label!=null)
    result.setText(label);
  if (img!=null)
    result.setIcon(img);
  if (action!=null)
    result.setActionCommand(action);
  if (listener!=null)
    result.addActionListener(listener);
  if (!margin)
    result.setMargin(new Insets(0,0,0,0));

  result.setEnabled(enabled);

  return result;
  }          
}
