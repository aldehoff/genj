/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.shell.swing;

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A Helper for simple Swing choirs
 */
public class SwingHelper {
  
  public static final int
    DLG_YES_NO = JOptionPane.YES_NO_OPTION,
    DLG_OK_CANCEL = JOptionPane.OK_CANCEL_OPTION,
    DLG_OK = -1;
    
  public static final int 
    OPTION_NO     = JOptionPane.NO_OPTION,
    OPTION_YES    = JOptionPane.YES_OPTION,
    OPTION_OK     = JOptionPane.OK_OPTION,
    OPTION_CANCEL = JOptionPane.CANCEL_OPTION;

  /**
   * Helper that creates a SplitPane
   */
  public static JSplitPane getSplitPane(boolean vertical, JComponent left, JComponent right) {
    JSplitPane result = new JSplitPane(
      vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT,
      left, right
    );
    result.setDividerSize(3);
    result.setDividerLocation(0.5D);
    return result;
  }
  
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
  public static JCheckBoxMenuItem getCheckBoxMenuItem(final UnifiedAction action) {
    
    JCheckBoxMenuItem result = new JCheckBoxMenuItem(action);
    result.setModel(new DefaultButtonModel() {
      public boolean isSelected() {
        return action.isSelected();
      }
    });
    
    return result;
  }
  
}
