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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Our own JComboBox
 */
public class ChoiceWidget extends javax.swing.JComboBox {

  /** our editor */
  private Editor editor = new Editor();
    
  /**
   * Constructor
   */     
  public ChoiceWidget(Object[] values, Object selection) {
    super(values);
    setSelectedItem(selection);
    setAlignmentX(LEFT_ALIGNMENT);
    setEditor(editor);
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
    return editor.hasChanged();
  }
    
  /**
   * @see javax.swing.JComboBox#setEditable(boolean)
   */
  public void setEditable(boolean set) {
    super.setEditable(set);
    // mark unchanged if start editable
    if (set) editor.setText(editor.getText());
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
  private class Editor extends TextFieldWidget implements ComboBoxEditor, PopupMenuListener, FocusListener {
    
    /**
     * Constructor
     */
    private Editor() {
      ChoiceWidget.this.addPopupMenuListener(this);
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
