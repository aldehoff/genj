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
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;

/**
 * Our own JComboBox
 */
public class ChoiceWidget extends JComboBox {
  
  /** our own model */
  private Model model = new Model();
  
  /** wether we match ignoring case */
  private boolean isIgnoreCase = false;
  
  /**
   * Constructor
   */
  public ChoiceWidget() {
    this(new Object[0], "");
  }
  
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

    // set the values now
    model.setValues(values);
       
    // alignment fix
    setAlignmentX(LEFT_ALIGNMENT);
    
    // init editor
    super.setEditor(new Editor());
    
    // try to set selection - not in values is ignored
    setSelectedItem(selection);
    
    // default is editable
    setEditable(true);

    // done
  }
  
  /**
   * Add change listener
   */
  public void addChangeListener(ChangeListener l) {
    ((Editor)getEditor()).addChangeListener(l);
  }
  
  /**
   * Remove change listener
   */
  public void removeChangeListener(ChangeListener l) {
    ((Editor)getEditor()).removeChangeListener(l);
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
   * Changes the currently selected item
   * @see javax.swing.JComboBox#setSelectedItem(java.lang.Object)
   */
  public void setSelectedItem(Object anObject) {
    // we're changed
    // FIXME is this necessary?
//    changeSupport.f();
    // continue
    super.setSelectedItem(anObject);
  }
  
  /**
   * Accessor - whether a selectAll() should occur on focus gained
   */
  public void setSelectAllOnFocus(boolean set) {
    ((Editor)getEditor()).setSelectAllOnFocus(set);
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
    ((Editor)getEditor()).setText(text);
  }
  
  /**
   * Access to editor
   */
  public TextFieldWidget getTextWidget() {
    return ((Editor)getEditor());
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
    ((Editor)editor).requestFocus();
  }
  
  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    return ((Editor)editor).requestFocusInWindow();
  }
  
  /**
   * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
   */
  public void setEditor(ComboBoxEditor anEditor) {
    // ignore
  }
  
  /**
   * @see javax.swing.JComboBox#addActionListener(java.awt.event.ActionListener)
   */
  public void addActionListener(ActionListener l) {
    getEditor().addActionListener(l);
  }
      
  /**
   * @see javax.swing.JComboBox#removeActionListener(java.awt.event.ActionListener)
   */
  public void removeActionListener(ActionListener l) {
    getEditor().removeActionListener(l);
  }
  
  /**
   * our own editor
   */
  private class Editor extends TextFieldWidget implements ComboBoxEditor, DocumentListener, Runnable {
    
    private boolean ignoreInsertUpdate = false;
    
    /**
     * Constructor
     */
    private Editor() {
      super("", 12);
      getDocument().addDocumentListener(this);
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
      ignoreInsertUpdate = true;
      super.setText(t);
      ignoreInsertUpdate = false;
    }

    /**
     * DocumentListener - callback
     */
    public void removeUpdate(DocumentEvent e) {
      // ignored
    }
    
    /**
     * DocumentListener - callback
     */
    public void changedUpdate(DocumentEvent e) {
      // ignored
    }
    
    /**
     * When something is typed in the editor's document we 
     * invoke a (delayed) auto complete on run()
     * @see genj.util.swing.TextFieldWidget#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      // add a auto-complete callback
      if (!ignoreInsertUpdate)
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
      getEditor().setItem(seLection);
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
