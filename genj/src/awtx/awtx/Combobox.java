/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.awt.*;
import java.awt.event.*;

/**
 * A abstract Combobox class
 * 2000-02-02 removed doLayout hoping that a heavyweight
 *            Combobox will now show up in Stan's browser
 */
public abstract class Combobox extends Container {

  /*package*/ Component _dependant;

  /**
   * Constructor
   * Since Stan has a problem seeing a ComboBox in a lower-right
   * corner of a Scrollpane, I've trid
   *   GridLayout()
   * and
   *   BorderLayout()
   * I hope that the ComboBox then actually shows up
   */
  /*package*/ Combobox() {
    _dependant = getDependant();
    setLayout(new GridLayout(1,1));
    add(_dependant);
  }

  /**
   * Adds an action listener - the concret implementation has to handle this
   */
  public abstract void addActionListener(ActionListener listener);

  /**
   * Returns the mode dependant component  - the concret implementation has to handle this
   */
  protected abstract Component getDependant();

  /**
   * Sizes are delegated to our dependant
   */
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  /**
   * Sizes are delegated to our dependant
   */
  public Dimension getPreferredSize() {
    return _dependant.getPreferredSize();
  }

  /**
   * Returns the selected item  - the concret implementation has to handle this
   */
  public abstract int getSelectedIndex();

  /**
   * Removes an action listener - the concret implementation has to handle this
   */
  public abstract void removeActionListener(ActionListener listener);

  /**
   * Sets the action command - the concret implementation has to handle this
   */
  public abstract void setActionCommand(String acommand);

  /**
   * Puts elements in  - the concret implementation has to handle this
   */
  public abstract void setElements(Object[] elements);

  /**
   * Selects an item  - the concret implementation has to handle this
   */
  public abstract void setSelectedIndex(int which);

}
