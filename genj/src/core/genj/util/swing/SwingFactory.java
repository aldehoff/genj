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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Stack;

import javax.swing.Box;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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
  private void wrap(Component result) {
    
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
    JComboBox result = new JComboBox(values, selection);
    wrap(result);
    return result;
  }
  
  /**
   * creates a list
   */
  public JList JList(ListModel model) {
    
    JList result = model!=null ? new JList(model) : new JList();
    
    result.setCellRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof ActionDelegate) {
          ActionDelegate action = (ActionDelegate)value; 
          setText(action.txt);
          setIcon(action.img);
        }
        return this;
      }
    });
    // done
    return result;
  }

  /**
   * requestFocus - since jdk 1.4 there's a method on 
   * JComponent which requests the focus in the window
   * the component is contained in. I'd like to use this
   * but don't want to require jdk 1.4. So we're trying
   * to use that method via introspection and use requestFocus()
   * on pre 1.4 impls otherwise
   */
  public static void requestFocusFor(final JComponent c) {
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          c.getClass().getMethod("requestFocusInWindow", new Class[]{} )
            .invoke(c, new Object[]{});
        } catch (Throwable t) {
          c.requestFocus();
        }
      }
    });
    
  }
  
  /**
   * Our own JComboBox
   */
  public static class JComboBox extends javax.swing.JComboBox {

    /** change flag */  
    private boolean changed = false;
    
    /** our editor */
    private Editor editor = new Editor();
    
    /**
     * Constructor
     */     
    private JComboBox(Object[] values, Object selection) {
      super(values);
      setSelectedItem(selection);
      setAlignmentX(LEFT_ALIGNMENT);
      setEditor(editor);
      changed = false;
    }
    
    /**
     * @see javax.swing.JComponent#getMaximumSize()
     */
    public Dimension getMaximumSize() {
      return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
    }
    
    /**
     * Changed?
     */
    public boolean hasChanged() {
      return changed;
    }
    
    /**
     * @see javax.swing.JComboBox#setEditable(boolean)
     */
    public void setEditable(boolean set) {
      super.setEditable(set);
      // mark unchanged if start editable
      if (set) changed = false;
    }
    
    /**
     * Current text value
     */
    public String getText() {
      if (isEditable()) return editor.getText();
      return super.getSelectedItem().toString();
    }
      
    /**
     * our own editor
     */
    private class Editor extends JTextField implements ComboBoxEditor, DocumentListener, PopupMenuListener, FocusListener {
    
      /**
       * Constructor
       */
      private Editor() {
        getDocument().addDocumentListener(this);
        JComboBox.this.addPopupMenuListener(this);
        JComboBox.this.addFocusListener(this);
      }
    
      /**
       * @see javax.swing.ComboBoxEditor#getEditorComponent()
       */
      public Component getEditorComponent() {
        return this;
      }

      /**
       * @see javax.swing.ComboBoxEditor#getItem()
       */
      public Object getItem() {
        return this.getText();
      }

      /**
       * @see javax.swing.ComboBoxEditor#setItem(java.lang.Object)
       */
      public void setItem(Object set) {
        super.setText(set!=null ? set.toString() : "");
        changed = true;
      }
    
      /**
       * @see genj.edit.ProxyChoice.Editor#setText(java.lang.String)
       */
      public void setText(String t) {
        super.setText(t);
        changed = false;
      }

      /**
       * Change notification
       */
      public void changedUpdate(DocumentEvent e) {
        changed = true;
      }

      /**
       * Document event - insert
       */
      public void insertUpdate(DocumentEvent e) {
        changed = true;
      }

      /**
       * Document event - remove
       */
      public void removeUpdate(DocumentEvent e) {
        changed = true;
      }
    
      /**
       * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
       */
      public void popupMenuCanceled(PopupMenuEvent e) {
        // ignored
      }

      /**
       * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
       */
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // ignored
      }

      /**
       * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
       */
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // try to find prefix in combo
        String pre = this.getText();
        for (int i=0; i<getItemCount(); i++) {
          String item = (String) getItemAt(i);
          if (item.regionMatches(true, 0, pre, 0, pre.length())) {
            setSelectedIndex(i);
            break;
          }
        }
        // done
      }

      /**
       * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
       */
      public void focusGained(FocusEvent e) {
        requestFocusFor(this);
      }

      /**
       * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
       */
      public void focusLost(FocusEvent e) {
        // ignored
      }

    } //Editor

  } //JComboBox
  
} //WidgetFactory
