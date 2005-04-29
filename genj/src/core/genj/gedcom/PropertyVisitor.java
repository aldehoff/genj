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

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor pattern for recursion over properties
 */
public abstract class PropertyVisitor {
  
  /** collector */
  private List result = new ArrayList();
  
  /**
   * Result access
   */
  public Property[] getProperties() {
    return Property.toArray(result);
  }
  
  /**
   * Result access - first in result
   */
  public Property getProperty() {
    return result.isEmpty() ? null : (Property)result.get(0);
  }
  
  /**
   * Add to result
   */
  protected boolean keep(Property property, boolean cont) {
    result.add(property);
    return cont;
  }
  
  /**
   * callback for reaching a leaf
   * @return whether to backtrack and continue the recursion or not
   */
  protected boolean leaf(Property leaf) {
    return true;
  }

  /**
   * callback for recursing from parent into child with given tag
   * @return whether to continue recursion
   */
  protected boolean recursion(Property parent, String child) {
    return true;
  }

} //TagPathVisitor
