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

import genj.util.ChangeSupport;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.Timer;
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
  
  /** change support */
  private ChangeSupport changeSupport = new ChangeSupport(this);
  
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

    // default is editable
    setEditable(true);

    // do our model
    setModel(model);

    // set the values now
    model.setValues(values);
       
    // alignment fix
    setAlignmentX(LEFT_ALIGNMENT);
    
    // try to set selection - not in values is ignored
    setSelectedItem(selection);
    
    // done
  }
  
  /**
   * Add change listener
   */
  public void addChangeListener(ChangeListener l) {
    changeSupport.addChangeListener(l);
  }
  
  /**
   * Remove change listener
   */
  public void removeChangeListener(ChangeListener l) {
    changeSupport.removeChangeListener(l);
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
  }
    
  /**
   * Accessor - whether a selectAll() should occur on focus gained
   */
  public void setSelectAllOnFocus(boolean set) {
    // ignored - currently LnF dependant
  }
    
  /**
   * Current text value
   */
  public String getText() {
    if (isEditable()) 
      return getEditor().getItem().toString();
    return super.getSelectedItem().toString();
  }
  
  /**
   * Set text value
   */
  public void setText(String text) {
    if (!isEditable) 
      throw new IllegalArgumentException("setText && !isEditable n/a");
    getTextEditor().setText(text);
  }
  
  /**
   * Access to editor
   */
  public JTextField getTextEditor() {
    return (JTextField)getEditor().getEditorComponent();
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
    getTextEditor().requestFocus();
  }
  
  /**
   * @see javax.swing.JComponent#requestFocusInWindow()
   */
  public boolean requestFocusInWindow() {
    try {
      return getTextEditor().requestFocusInWindow();
    } catch (Throwable t) {
      requestFocus();
      return true;
    }
  }
  
  /**
   * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
   */
  public void setEditor(ComboBoxEditor set) {
    
    // continue
    super.setEditor(set);
    
    // enable autocomplete support
    new AutoCompleteSupport();
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
   * Auto complete support
   */
  private class AutoCompleteSupport implements DocumentListener, ActionListener {

    private boolean ignoreInsertUpdate = false;
    
    private Timer timer = new Timer(500, this);
    
    /**
     * Constructor
     */
    private AutoCompleteSupport() {
      getTextEditor().getDocument().addDocumentListener(this);
      timer.setRepeats(false);
    }
    
    /**
     * DocumentListener - callback
     */
    public void removeUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
    }
      
    /**
     * DocumentListener - callback
     */
    public void changedUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
    }
      
    /**
     * When something is typed in the editor's document we 
     * invoke a (delayed) auto complete on run()
     * @see genj.util.swing.TextFieldWidget#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
      changeSupport.fireChangeEvent();
      // add a auto-complete callback
      if (!ignoreInsertUpdate)
        timer.start();
    }
      
    /**
     * Our auto-complete callback
     */
    public void actionPerformed(ActionEvent e) {
        
      // do the auto-complete for txt
      String txt = getTextEditor().getText();
        
      // try to select an item
      ignoreInsertUpdate = true;
      String match = model.setSelectedPrefix(txt);
      ignoreInsertUpdate = false;
        
      // text exactly matches - done
      if (match.length()<=txt.length())
        return;
  
      // and select the text that has been added
      // ie from the current edit position to the end of the text
      Caret c = getTextEditor().getCaret();
      c.setDot(match.length());
      c.moveDot(txt.length());
  
      // done      
    }
  } //AutoCompleteSupport
  
//  /**
//   * our own editor
//   * remark: only by implementing UIResource can I convince the UI to try
//   * to set the editor on lnf changes - which gives us a chance to adopt
//   * in change in look of the combobox
//   */
//  private class AutoCompleteEditor extends TextFieldWidget implements ComboBoxEditor, DocumentListener, ActionListener, UIResource {
//    
//    private boolean ignoreInsertUpdate = false;
//    
//    private Timer timer = new Timer(500, this);
//    
//    /**
//     * Constructor
//     */
//    private AutoCompleteEditor() {
//      super("", 12);
//      getDocument().addDocumentListener(this);
//      timer.setRepeats(false);
//    }
//    
//    /**
//     * @see javax.swing.ComboBoxEditor#getEditorComponent()
//     */
//    public Component getEditorComponent() {
//      return this;
//    }
//
//    /**
//     * @see javax.swing.ComboBoxEditor#getItem()
//     */
//    public Object getItem() {
//      return this.getText();
//    }
//
//    /**
//     * @see javax.swing.ComboBoxEditor#setItem(java.lang.Object)
//     */
//    public void setItem(Object set) {
//      setText(set==null ? "" : set.toString());
//    }
//    
//    /**
//     * @see genj.util.swing.TextFieldWidget#setText(java.lang.String)
//     */
//    public void setText(String t) {
//      ignoreInsertUpdate = true;
//      super.setText(t);
//      ignoreInsertUpdate = false;
//    }
//
//    /**
//     * DocumentListener - callback
//     */
//    public void removeUpdate(DocumentEvent e) {
//      // ignored
//    }
//    
//    /**
//     * DocumentListener - callback
//     */
//    public void changedUpdate(DocumentEvent e) {
//      // ignored
//    }
//    
//    /**
//     * When something is typed in the editor's document we 
//     * invoke a (delayed) auto complete on run()
//     * @see genj.util.swing.TextFieldWidget#insertUpdate(javax.swing.event.DocumentEvent)
//     */
//    public void insertUpdate(DocumentEvent e) {
//      // add a auto-complete callback
//      if (!ignoreInsertUpdate)
//        timer.start();
//    }
//    
//    /**
//     * Our auto-complete callback
//     */
//    public void actionPerformed(ActionEvent e) {
//      
//      // do the auto-complete for txt
//      String txt = super.getText();
//      
//      // try to select an item
//      String match = model.setSelectedPrefix(txt);
//      
//      // text exactly matches - done
//      if (match.length()<=txt.length())
//        return;
//
//      // and select the text that has been added
//      // ie from the current edit position to the end of the text
//      Caret c = getCaret();
//      c.setDot(match.length());
//      c.moveDot(txt.length());
//
//      // done      
//    }
//    
//  } //Editor

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

} //ChoiceWidget
