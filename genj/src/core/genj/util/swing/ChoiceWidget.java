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
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Caret;

/**
 * Our own JComboBox
 */
public class ChoiceWidget extends javax.swing.JComboBox {
  
  /** our own editor */
  private Editor editor = new Editor();
  
  /** our own change flag */
  private boolean hasChanged = false;
  
  /** our own model */
  private Model model = new Model();
  
  /** wether we match ignoring case */
  private boolean isIgnoreCase = false;
  
  /**
   * Constructor
   */
  public ChoiceWidget(List values) {
    this(values.toArray(), "");
  }
  
  /**
   * Constructor
   */     
  public ChoiceWidget(Object[] values, Object selection) {

    // do our model
    setModel(model);

    // always our own editor because 
    // (1) want to use our TextWidget here
    // (2) want to avoid actionPerformed on focusLost (see Editor)
    // (3) want to avoid double actionPerformed from JComboBox on 'enter'
    //     with a changed editor value
    // caveat: no action performed on select from choices
    super.setEditor(editor);
    
    // default is sorted
    setEditable(true);

    // set the values now
    model.setValues(values);
       
    // try to set selection - not in values is ignored
    setSelectedItem(selection);
    
    // we're not changed at this point
    editor.setChanged(false);
    hasChanged = false;
    
    // alignment fix
    setAlignmentX(LEFT_ALIGNMENT);
    
  }
  
  /**
   * set values
   */
  public void setValues(List values) {
    // set
    model.setValues(values.toArray());
    // done  
  }
    
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    // 20040223 seems like maximum width should be pretty big really
    return new Dimension(Integer.MAX_VALUE, super.getPreferredSize().height);
//    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }
    
  /**
   * Changed?
   */
  public boolean hasChanged() {
    return isEditable() ? editor.hasChanged() : hasChanged;
  }
  
  /**
   * Set change
   */
  public void setChanged(boolean set) {
    hasChanged = set;
    editor.setChanged(set);
  }

  /**
   * Changes the currently selected item
   * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
   */
  public void setSelectedItem(Object anObject) {
    // we're changed
    hasChanged = true;
    // 20030510 mark change in editor
    editor.setChanged(true);
    // continue
    super.setSelectedItem(anObject);
  }
  
  /**
   * Current text value
   */
  public String getText() {
    if (isEditable()) return getEditor().getItem().toString();
    return super.getSelectedItem().toString();
  }
  
  /**
   * Set text value
   */
  public void setText(String text) {
    if (!isEditable) 
      throw new IllegalArgumentException("setText && !isEditable n/a");
    editor.setText(text);
  }
  
  /**
   * Access to editor
   */
  public TextFieldWidget getTextWidget() {
    return editor;
  }
  
  /**
   * Enable case ignore matching with editor autocomplete
   */
  public void setIgnoreCase(boolean set) {
    isIgnoreCase = set;
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
        String item = getItemAt(i).toString();
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
      super.requestFocus();
    }
  }
  
  /**
   * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
   */
  public void setEditor(ComboBoxEditor anEditor) {
    // ignore
  }
  
  /**
   * @see javax.swing.JComboBox#setEditable(boolean)
   */
  public void setEditable(boolean aFlag) {
    // continnue
    super.setEditable(aFlag);
    // not a change
    editor.setChanged(false);
  }


  /**
   * @see javax.swing.JComboBox#addActionListener(java.awt.event.ActionListener)
   */
  public void addActionListener(ActionListener l) {
    editor.addActionListener(l);
  }
      
  /**
   * @see javax.swing.JComboBox#removeActionListener(java.awt.event.ActionListener)
   */
  public void removeActionListener(ActionListener l) {
    editor.removeActionListener(l);
  }
  
  /**
   * our own editor
   */
  private class Editor extends TextFieldWidget implements ComboBoxEditor, FocusListener, Runnable {
    
    /**
     * Constructor
     */
    private Editor() {
      super("", 8);
      ChoiceWidget.this.addFocusListener(this);
    }
    
    /**
     * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
     */
    public synchronized void addFocusListener(FocusListener l) {
      // BasicComboBoxUI has the annoying habit of listening
      // to focusLost to fire another actionperformed if 
      //   !editor.getItem().equals( comboBox.getSelectedItem())
      // so I'm filtering that listener here
      if (l.getClass().getName().indexOf("$EditorFocusListener")>0)
        return;
      // continue
      super.addFocusListener(l);
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
      String s = set==null ? "" : set.toString();
      if (!super.getText().equals(s)) setText(s);
      // done
    }
    
    /**
     * @see genj.util.swing.TextFieldWidget#setText(java.lang.String)
     */
    public void setText(String t) {
      super.setText(t);
      super.setChanged(true);
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

    /**
     * When something is typed in the editor's document we 
     * invoke a (delayed) auto complete on run()
     * @see genj.util.swing.TextFieldWidget#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      // let super do its thing
      super.insertUpdate(e);
      // add a auto-complete callback
      SwingUtilities.invokeLater(this);
    }
    
    /**
     * Our auto-complete callback
     */
    public void run() {
      
      // do the auto-complete for txt
      String txt = super.getText();
      
      // try to select an item
      String match = model.setSelectedPrefix(txt);
      
      // text exactly matches - done
      if (match.length()<=txt.length())
        return;

      // and select the text that has been added
      // ie from the current edit position to the end of the text
      Caret c = getCaret();
      c.setDot(match.length());
      c.moveDot(txt.length());

      // done      
    }
    
  } //Editor

  /**
   * our own model
   */
  private class Model extends AbstractListModel implements ComboBoxModel {
    
    /** list of values */
    private Object[] values = new Object[0];
    
    /** selection */
    private Object selection = null;

    /**
     * Setter - values
     */
    private void setValues(Object[] vaLues) {
      values = vaLues;
    }

    /**
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    public Object getSelectedItem() {
      return selection;
    }
    
    /**
     * selects an item by prefix
     * @return the matching item
     */
    private String setSelectedPrefix(String prefix) {
      
      if (isIgnoreCase)
        prefix = prefix.toLowerCase();
      
      // try to find a match
      for (int i=0;i<values.length;i++) {
        String value = values[i].toString();
        if (isIgnoreCase)
          value = value.toLowerCase();
           
        if (value.startsWith(prefix)) {
          setSelectedItem(values[i]);
          return values[i].toString();        
        }
      }
      
      // no match
      return "";
    }

    /**
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object seLection) {
      // remember
      selection = seLection;
      // propagate to editor
      editor.setItem(seLection);
      // notify about item state change
      fireItemStateChanged(new ItemEvent(ChoiceWidget.this, ItemEvent.ITEM_STATE_CHANGED, seLection, ItemEvent.SELECTED));
      // and notify of data change (combobox specialty)
      fireContentsChanged(this, -1, -1);
      // done
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
      return values[index];
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
      return values.length;
    }

  } //Model

} //JComboBox
