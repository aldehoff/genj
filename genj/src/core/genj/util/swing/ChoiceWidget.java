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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ComboBoxEditor;

/**
 * Our own JComboBox
 */
public class ChoiceWidget extends javax.swing.JComboBox {
  
  /** did we change */
  private boolean isChanged = false;
  

  /**
   * Constructor
   */     
  public ChoiceWidget(Object[] values, Object selection) {
    
    super(values);
    
    // apparently the default model preselects a value
    // which isn't overridden by selection if selection
    // isn't in values
    getModel().setSelectedItem(null);
    
    // try to set selection - not in values is ignored
    setSelectedItem(selection);
    
    // alignment fix
    setAlignmentX(LEFT_ALIGNMENT);
    
    // our editor
    setEditor(editor);
    
    // initially unchanged
    isChanged = false;
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
    // check editor
    Object edit = getEditor();
    if (edit instanceof Editor && ((Editor)edit).hasChanged() )
      return true;
    // check us
    return isChanged;
  }

  /**
   * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
   */
  public void setSelectedItem(Object anObject) {
    // mark changed
    isChanged = true;
    // continue
    super.setSelectedItem(anObject);
  }

  
  /**
   * @see javax.swing.JComboBox#setEditable(boolean)
   */
  public void setEditable(boolean set) {
    super.setEditable(set);
    // install our special editor for now
    if (set) {
      Editor edit = new Editor();
      setEditor(edit);
      edit.setChanged(false);
    }
    // done 
  }
    
  /**
   * Current text value
   */
  public String getText() {
    if (isEditable()) return getEditor().getItem().toString();
    return super.getSelectedItem().toString();
  }
  
  /**
   * @see javax.swing.JComboBox#setPopupVisible(boolean)
   */
  public void setPopupVisible(boolean v) {
    // show it
    super.setPopupVisible(v);
    // try to find prefix in combo
    if (v) { 
      // not via this.addPopupMenuListener() in 1.4 only 
      String pre = getText();
      for (int i=0; i<getItemCount(); i++) {
        String item = (String) getItemAt(i);
        if (item.regionMatches(true, 0, pre, 0, pre.length())) {
          setSelectedIndex(i);
          break;
        }
      }
    }
    // done
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
  public void requestFocus() {
    // try JDK 1.4's requestFocusInWindow instead
    try {
      getClass().getMethod("requestFocusInWindow", new Class[]{})
        .invoke(this, new Object[]{});
    } catch (Throwable t) {
      requestFocus();
    }
  }
      
  /**
   * our own editor
   */
  private class Editor extends TextFieldWidget implements ComboBoxEditor, FocusListener {
    
    /**
     * Constructor
     */
    private Editor() {
      ChoiceWidget.this.addFocusListener(this);
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
      setChanged(true);
    }
    
    /**
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e) {
      this.requestFocus();
    }

    /**
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    public void focusLost(FocusEvent e) {
      // ignored
    }

  } //Editor

} //JComboBox
