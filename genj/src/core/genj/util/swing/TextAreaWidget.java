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

import javax.swing.JTextArea;

/**
 * Our own JTextArea
 */
public class TextAreaWidget extends JTextArea {

  /** wrapped observable */  
  private ObservableBoolean change;

  /**
   * Constructor
   */
  public TextAreaWidget(String text, int rows, int cols) {
    this(null, text, rows, cols);
  }

  /**
   * Constructor
   */
  public TextAreaWidget(ObservableBoolean observable, String text, int rows, int cols) {
    super(text, rows, cols);
    
    setAlignmentX(0);
    
    change = observable!=null ? observable : new ObservableBoolean();
    getDocument().addDocumentListener(observable);
  }

  /**
   * Accessor - observable
   */
  public ObservableBoolean getChangeState() {
    return change;
  }
    
  /**
   * Overridden to try 1.4's requestFocusInWindow
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
