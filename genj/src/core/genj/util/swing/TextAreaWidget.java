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

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Our own JTextArea
 */
public class TextAreaWidget extends JTextArea implements DocumentListener {

  /** change flag */  
  private boolean isChanged = false;

  /**
   * Constructor
   */
  public TextAreaWidget(String text, int rows, int cols) {
    super(text, rows, cols);
    getDocument().addDocumentListener(this);
    setAlignmentX(0);
  }

  /**
   * @see javax.swing.text.JTextComponent#setText(java.lang.String)
   */
  public void setText(String t) {
    super.setText(t);
    setChanged(false);
  }


  /**
   * Test for change
   */
  public boolean hasChanged() {
    return isChanged;
  }
    
  /**
   * Set change
   */
  public void setChanged(boolean set) {
    isChanged = set;
  }

  /**
   * Change notification
   */
  public void changedUpdate(DocumentEvent e) {
    isChanged = true;
  }

  /**
   * Document event - insert
   */
  public void insertUpdate(DocumentEvent e) {
    isChanged = true;
  }

  /**
   * Document event - remove
   */
  public void removeUpdate(DocumentEvent e) {
    isChanged = true;
  }

  /**
   * Overridden to try 1.4's requestFocusInWindow
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
   * Overriden to try 1.4's super.setFocusable()
   * @see java.awt.Component#setFocusable(boolean)
   */
  public void setFocusable(boolean focusable) {
    try {
      super.setFocusable(focusable);
    } catch (Throwable t) {
      // try pre 1.4 instead
      super.setRequestFocusEnabled(false);
    }
  }
  
} //TextAreaWidget
