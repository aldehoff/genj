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

import java.awt.Image;
import java.util.Vector;

import genj.util.*;

/**
 * Gedcom Property : AGE
 */
public class PropertyAge extends Property {

  /** the age */
  private int age;

  /** as string */
  String ageAsString;

  /**
   * Constructor
   */
  public PropertyAge(int setAge) {
    age=setAge;
    ageAsString=null;
  }

  /**
   * Constructor
   */
  public PropertyAge(String tag, String value) {
    setValue(value);
  }

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    return ageAsString!=null;
  }


  /**
   * Accessor Tag
   */
  public String getTag() {
    return "AGE";
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    if (ageAsString!=null)
      return ageAsString;
    return String.valueOf(age);
  }

  /**
   * Accessor Value
   */
  public boolean setValue(String newValue) {
    // Transformation to int
    Integer i;
    try {
      i = Integer.valueOf(newValue);
    } catch (NumberFormatException e) {
      noteModifiedProperty();
      ageAsString=newValue;
      age=0;
      return false;
    }
    // OK
    noteModifiedProperty();
    ageAsString=null;
    age = i.intValue();
    // Done
    return true;
  }
}
