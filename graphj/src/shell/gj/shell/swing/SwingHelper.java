/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2009 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.shell.swing;

import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * A Helper for simple Swing choirs
 */
public class SwingHelper {
  
  public static final int
    DLG_OK_CANCEL = JOptionPane.OK_CANCEL_OPTION,
    DLG_OK = -1;
    
  public static final int 
    OPTION_OK     = JOptionPane.OK_OPTION;

  /**
   * Helper that creates a simple input dialog
   */
  public static String showDialog(JComponent parent, String title, String message) {
    return JOptionPane.showInputDialog(parent,message,title,JOptionPane.QUESTION_MESSAGE );
  }
  
  /**
   * Helper that shows a dialog
   */
  public static int showDialog(JComponent parent, String title, Object content, int type) {
    return JOptionPane.showConfirmDialog(parent,content,title,type);
  }
  
  /** 
   * Helper that returns a JCheckBoxMenuItem which knows how
   * to handle our UnifiedAction's isSelected
   */
  public static JCheckBoxMenuItem getCheckBoxMenuItem(final Action2 action) {
    
    JCheckBoxMenuItem result = new JCheckBoxMenuItem(action);
    result.setModel(new DefaultButtonModel() {
      @Override
      public boolean isSelected() {
        return action.isSelected();
      }
    });
    
    return result;
  }
  
}
