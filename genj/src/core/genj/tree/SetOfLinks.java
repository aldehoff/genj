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
package genj.tree;

import java.util.*;

/**
 * Class for encapsulating a set of Links to Persons
 */
class SetOfLinks {

  private Vector vector;

  /**
   * Constructor
   */
  public SetOfLinks() {
    vector = new Vector();
  }

  /**
   * Constructor with pre-occupied number of links to persons
   */
  public SetOfLinks(int initialCapacity) {
    vector = new Vector(initialCapacity);
  }

  /**
   * Adds a new last link to this set
   */
  public SetOfLinks addLink(Link link) {
    vector.addElement(link);
    return this;
  }

  /**
   * Adds a new link to this set at the given position
   */
  public SetOfLinks addLinkAt(int where,Link link) {
    vector.insertElementAt(link,where-1);
    return this;
  }

  /**
   * Adds new last links to this set
   */
  public SetOfLinks addLinksFrom(SetOfLinks links) {
    for (int i=1;i<=links.getSize();i++) {
      addLink(links.getLink(i));
    }
    return this;
  }

  /**
   * Changes a link in the set of links to persons
   */
  public SetOfLinks changeLink(int which,Link link) {
    if (which<=getSize()) {
      vector.setElementAt(link,which-1);
      return this;
    }
    if (which==getSize()+1) {
      vector.addElement(link);
      return this;
    }
    throw new RuntimeException("LinksToPersons.Tried to change non-existing link !");
  }

  /**
   * Removes one of the links to persons from this set
   */
  private void delLinkAt(int pos) {
    vector.removeElementAt(pos-1);
  }

  /**
   * Returns an enumeration of all links in this set
   */
  public Enumeration elements() {
    return this.vector.elements();
  }

  /**
   * Returns one of the links to persons from this set
   */
  public Link getLink(int which) {
    return ((Link)vector.elementAt(which-1));
  }

  /**
   * Size of this set
   */
  public int getSize() {
    return vector.size();
  }
}
