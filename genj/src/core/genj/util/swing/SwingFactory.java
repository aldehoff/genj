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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * A factory for creating UI components
 */
public class SwingFactory {
  
  /** a stack of containers */
  private Stack containers = new Stack();
  
  /** 
   * push a container 
   */
  public SwingFactory push(Container set) {
    containers.push(set);
    return this; 
  }
  
  /**
   * pop a container
   */
  public SwingFactory pop() {
    if (!containers.isEmpty()) containers.pop();
    return this;
  }
  
  /**
   * Wrap a result into its context
   */
  private void wrap(JComponent result) {
    
    // setting container
    if (!containers.isEmpty()) {
      ((Container)containers.peek()).add(result);
    }    
    // done
  }
  
  /** 
   * Create a box
   */
  public Box Box(int axis) {
    Box result = new Box(axis);
    wrap(result);
    push(result);
    return result;
  }

  /**
   * Create a label
   */
  public JLabel JLabel(String txt) {
    JLabel result = new JLabel(txt);
    wrap(result);
    return result;
  }

  /**
   * Create a textfield
   */
  public JTextField JTextField(String txt, final boolean template, int cols) {
    
    JTextField result = new JTextField(txt, cols) {
      /**
       * @see javax.swing.JComponent#getMaximumSize()
       */
      public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
      }
      /**
       * @see java.awt.Component#processFocusEvent(java.awt.event.FocusEvent)
       */
      protected void processFocusEvent(FocusEvent e) {
        if (e.getID()==FocusEvent.FOCUS_GAINED && isTemp()) {
          setText("");
          setTemp(false);
        } 
        super.processFocusEvent(e);
      }
      
      /**
       * @see javax.swing.text.JTextComponent#getText()
       */
      public String getText() {
        if (isTemp()) return "";
        return super.getText();
      }
      /**
       * Check for template
       */
      private boolean isTemp() {
        return getClientProperty("TEMP").equals(Boolean.TRUE);
      }
      /**
       * Set template
       */
      private void setTemp(boolean set) {
        putClientProperty("TEMP", new Boolean(set));
      }
      /**
       * @see javax.swing.JComponent#addNotify()
       */
      public void addNotify() {
        setTemp(template);
        super.addNotify();
      }

    };
    
    // done    
    wrap(result);
    return result;
  }
  
  /**
   * Creates a checkbox
   */
  public JCheckBox JCheckBox(String txt, boolean checked) {
    JCheckBox result = new JCheckBox(txt, checked);
    wrap(result);
    return result; 
  }
  
  /**
   * Creates a combobox
   */
  public JComboBox JComboBox(Object[] values, Object selection) {
    JComboBox result = new JComboBox(values) {
      /**
       * @see javax.swing.JComponent#getMaximumSize()
       */
      public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
      }
    };
    result.setAlignmentX(result.LEFT_ALIGNMENT);
    result.setSelectedItem(selection);
    wrap(result);
    return result;
  }
  
} //WidgetFactory
