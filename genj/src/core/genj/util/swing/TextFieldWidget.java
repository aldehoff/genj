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

import genj.util.ObservableBoolean;

import java.awt.Dimension;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;

/**
 * Our own JTextField
 */
public class TextFieldWidget extends JTextField {

  /** whether we're a template */
  private boolean isTemplate = false;
  
  /** whether we do a selectAll() on focus */
  private boolean isSelectAllOnFocus = false;
  
  /** an observable we offer */
  private ObservableBoolean change;
  
  /**
   * Constructor
   */
  public TextFieldWidget() {
    this("", 0);
  }
  
  /**
   * Constructor
   */
  public TextFieldWidget(String text, int cols) {
    this(null, text, cols);
  }

  /** 
   * Constructor
   */
  public TextFieldWidget(ObservableBoolean chan, String text, int cols) {
    super(text, cols);
    setAlignmentX(0);
    change = chan!=null ? chan : new ObservableBoolean();
    getDocument().addDocumentListener(change);
  }
  
  /**
   * Accessor - observable
   */
  public ObservableBoolean getChangeState() {
    return change;
  }
    
  /**
   * Make this a template - the field is set to unchanged, any
   * current value in the text-field is not returned but empty
   * string until the user changes focus to this component (which
   * clears the content)
   */
  public TextFieldWidget setTemplate(boolean set) {
    isTemplate = set;
    return this;
  }
  
  /**
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    return new Dimension(super.getMaximumSize().width, super.getPreferredSize().height);
  }
  
  /**
   * Accessor isSelectAllOnFocus
   */
  public void setSelectAllOnFocus(boolean set) {
    isSelectAllOnFocus = set;
  }
  
  /**
   * @see java.awt.Component#processFocusEvent(java.awt.event.FocusEvent)
   */
  protected void processFocusEvent(FocusEvent e) {
    if (e.getID()==FocusEvent.FOCUS_GAINED) {
      if (isTemplate) {
        setText("");
        isTemplate = false;
      }
      if (isSelectAllOnFocus) {
        // 20040307 wrote my own selectAll() so that the
        // caret is at position 0 after selection - this
        // makes sure the beginning of the text is visible
        if (getDocument() != null) {
          setCaretPosition(getDocument().getLength());
          moveCaretPosition(0);
        }
      }
    }
    super.processFocusEvent(e);
  }
    
  /**
   * Returns the current content unless isTemplate  is
   * still true (the component didn't receive focus) in
   * which case empty string is returned
   * @see javax.swing.text.JTextComponent#getText()
   */
  public String getText() {
    if (isTemplate) 
      return "";
    return super.getText();
  }
  
  /**
   * Sets the content 
   * @see javax.swing.text.JTextComponent#setText(java.lang.String)
   */
  public void setText(String txt) {
    super.setText(txt);
    
    // 20040307 reset caret to 0 - this makes sure the
    // first part of the string is visible
    setCaretPosition(0);
  }

  /**
   * @see javax.swing.JComponent#requestFocus()
   */
  public void requestFocus() {
    // try JDK 1.4's requestFocusInWindow instead
    try {
      super.requestFocusInWindow();
    } catch (Throwable t) {
      super.requestFocus();
    }
  }
  
} //JTextField
