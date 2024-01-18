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
package genj.edit;

import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.util.swing.SwingFactory;

import java.awt.Component;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property : Choice (e.g. RELA)
 * @author nils@meiers.net
 * @author Tomas Dahlqvist fix for prefix lookup
 */
class ProxyChoice extends Proxy{

  /** members */
  private JComboBox combo;
  
  /** editor */
  private Editor editor;

  /**
   * Finish editing a property through proxy
   */
  protected void finish() {

    // Has something been edited ?
    if ( !hasChanged() )
      return;

    // Store changed value
    Object result = combo.getEditor().getItem();
    prop.setValue(result!=null?result.toString():"");

    // Done
    return;
  }

  /**
   * Returns change state of proxy
   */
  protected boolean hasChanged() {
    return editor.hasChanged();
  }

  /**
   * Start editing a property through proxy
   */
  protected void start(JPanel in, JLabel setLabel, Property setProp, EditView edit) {

    // remember prop
    prop=setProp;
    
    // setup choices
    Object[] items = new Object[0];
    if (prop instanceof PropertyChoiceValue) {
      items =  ((PropertyChoiceValue)prop).getChoices().toArray();
    }

    // Setup controls
    editor = new Editor();
    combo = new SwingFactory().JComboBox(items, null);
    combo.setEditable(true);
    combo.setEditor(editor);
    combo.addPopupMenuListener(editor);

    editor.setText(prop.getValue());
    
    // layout
    in.add(combo);
    
    SwingFactory.requestFocusFor(editor);
    
    // Done
  }
  
  /**
   * our own editor
   */
  private class Editor extends JTextField implements ComboBoxEditor, DocumentListener, PopupMenuListener {
    
    /** change flag */  
    private boolean changed = false;
    
    /**
     * Constructor
     */
    private Editor() {
      getDocument().addDocumentListener(this);
    }
    
    /**
     * Changed?
     */
    public boolean hasChanged() {
      return changed;
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
      return getText();
    }

    /**
     * @see javax.swing.ComboBoxEditor#setItem(java.lang.Object)
     */
    public void setItem(Object set) {
      if (set==null) set = "";
      setText(set.toString());
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
      String pre = getText();
      for (int i=0; i<combo.getItemCount(); i++) {
        String item = (String) combo.getItemAt(i);
        if (item.regionMatches(true, 0, pre, 0, pre.length())) {
          combo.setSelectedIndex(i);
          break;
        }
      }
      // done
    }

  } //Editor

} //ProxyChoice
