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
 * Gedcom Property : abc
 */
public class PropertyIndividualAttribute extends Property {

  /** the attribute tag */
  private String tag;

  /** the value */
  private String value;

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyIndividualAttribute(String initTag) {
    tag = initTag;
  }

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyIndividualAttribute(String tag, String value) {
    this.tag = tag;
    setValue(value);
    // Done
  }

  /**
   * Adds all default properties to this property
   */
  public void addDefaultProperties() {

    // RESIdence ?
    if (getTag().equals("RESI")) {
      addProperty(new PropertyAddress());
      addProperty(new PropertyDate());
      return;
    }

    // Done
  }

  /**
   * Accessor for Tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Accessor for Value
   */
  public String getValue() {
    return value;
  }

  /**
   * Accessor for Value
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    this.value=value;
    return true;
  }          
}
