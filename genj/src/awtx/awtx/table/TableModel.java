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

/**
 * Interface for encapsulating Table's Data
 */
public interface TableModel {

  /**
   * Adds a listener to this model
   */
  public void addTableModelListener(TableModelListener listener);

  /**
   * Compares two rows
   * @param first row of first object
   * @param second row of second object
   * @param column to be used for comparison
   * @return -1 when first smaller than second
   *          0 when first equals second
   *          1 when first greater than second
   */
  public int compareRows(int first, int second, int column);

  /**
   * Returns the header object of given column of this model
   */
  public Object getHeaderAt(int column);

  /**
   * Returns the number of columns of this model
   */
  public int getNumColumns();

  /**
   * Returns the number of rows of this model
   */
  public int getNumRows();

  /**
   * Returns the object at given position in this model
   */
  public Object getObjectAt(int row, int column);

  /**
   * Removes a listener from this model
   */
  public void removeTableModelListener(TableModelListener listener);

}
