/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000 Nils Meier <nils@meiers.net>
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
package awtx.table;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Abstract base class for TableModels
 */
public abstract class AbstractTableModel implements TableModel {

  protected Vector listeners = new Vector();

  /**
   * Adds a listener to this model
   */
  public void addTableModelListener(TableModelListener listener) {
    listeners.addElement(listener);
  }

  /**
   * Default comparer
   */
  public int compareRows(int a, int b, int col) {
    return 0;
  }

  /**
   * Fires a change in number of rows
   */
  public void fireNumRowsChanged() {
    Enumeration e = listeners.elements();
    while (e.hasMoreElements()) {
      ((TableModelListener)e.nextElement()).handleNumRowsChanged(getNumRows());
    }
  }

  /**
   * Fires a change in data of given rows
   */
  public void fireRowsChanged(int[] rows) {
    Enumeration e = listeners.elements();
    while (e.hasMoreElements()) {
      ((TableModelListener)e.nextElement()).handleRowsChanged(rows);
    }
  }

  /**
   * Returns the header object of given column of this model
   */
  public abstract Object getHeaderAt(int column);

  /**
   * Returns the number of columns of this model
   */
  public abstract int getNumColumns();

  /**
   * Returns the number of rows of this model
   */
  public abstract int getNumRows();

  /**
   * Returns the object at given position in this model
   */
  public abstract Object getObjectAt(int row, int column);

  /**
   * Removes a listener from this model
   */
  public void removeTableModelListener(TableModelListener listener) {
    listeners.removeElement(listener);
  }
}
