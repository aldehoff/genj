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
 * Gedcom Property : PLAC
 */
public class PropertyPlace extends Property {

  /** the place */
  private String place;

  /**
   * Constructor of place Gedcom-line
   */
  public PropertyPlace() {
  }

  /**
   * Constructor of place Gedcom-line
   */
  public PropertyPlace(String value) {
    setValue(value);
  }

  /**
   * Constructor of place Gedcom-line
   */
  public PropertyPlace(String tag, String value) {
    setValue(value);
  }

  /**
   * the tag
   */
  public String getTag() {
    return "PLAC";
  }

  /**
   * the gedcom value
   */
  public String getValue() {
    return place;
  }

  /**
   * set the gedcom value
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    place=value;
    // Done
    return true;
  }
}
