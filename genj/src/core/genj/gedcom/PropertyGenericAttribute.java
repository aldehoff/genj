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

import java.util.Vector;

import genj.util.*;

/**
 * Gedcom Property : XYZ
 */
public class PropertyGenericAttribute extends Property {

  /** A generic Attribute's tag */
  private String tag;

  /** the value */
  private String value;

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyGenericAttribute(String initTag) {
    tag = initTag;
  }

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyGenericAttribute(String tag, String value) {

    // Remember
    this.tag = tag;
    setValue(value);
    // Done
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return tag;
  }

  /**
   * Returns the value of this property
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of this property
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    this.value=value;
    return true;
  }
}
