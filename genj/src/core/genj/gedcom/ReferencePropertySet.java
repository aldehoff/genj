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
package genj.gedcom;

import java.util.*;

/**
 * Class for encapsulating a set of Properties that get notified
 * about participation (add/removal)
 */
public class ReferencePropertySet {

  /** the contained properties */
  private Vector vector = new Vector();

  /**
   * Adds another property to this set of properties
   * @prop the property to add to this list
   * @parent the parent of this list
   */
  ReferencePropertySet add(Property prop, Property parent) {
    vector.addElement(prop);
    prop.addNotify(parent);
    return this;
  }

  /**
   * Property in list?
   */
  public boolean contains(Property property) {
    return vector.contains(property);
  }

  /**
   * Removes a property
   */
  ReferencePropertySet delete(Property which) {
    vector.removeElement(which);
    which.delNotify();
    return this;
  }

  /**
   * Removes all properties
   */
  ReferencePropertySet deleteAll() {

    Enumeration e = ((Vector)vector.clone()).elements();

    vector.removeAllElements();

    while (e.hasMoreElements())
      ((Property)e.nextElement()).delNotify();

    return this;
  }

  /**
   * Returns one of the properties in this set by index
   */
  public Property get(int which) {
    return ((Property)vector.elementAt(which));
  }

  /**
   * Returns one of the properties in this set by tag
   */
  public Property get(String tag) {

    Property p;
    for (int i=0;i<getSize();i++) {
      p = get(i);
      if (p.getTag().equals(tag)) return p;
    }
    return null;
  }

  /**
   * Returns the number of properties in this set
   */
  public int getSize() {
    return vector.size();
  }

  /**
   * Swaps place of properties given by index
   */
  public void swap(int i, int j) {

    Object o = vector.elementAt(i);
    vector.setElementAt(vector.elementAt(j),i);
    vector.setElementAt(o,j);
  }          
}
