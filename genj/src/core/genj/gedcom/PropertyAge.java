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

/**
 * Gedcom Property : AGE
 */
public class PropertyAge extends Property {

  /** the age */
  private PointInTime age = null;

  /** as string */
  private String ageAsString;

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    return getAge().isValid();
  }


  /**
   * Accessor Tag
   */
  public String getTag() {
    return "AGE";
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  public void setTag(String tag) throws GedcomException {
    if (!"AGE".equals(tag)) throw new GedcomException("Unsupported Tag");
  }

  /**
   * Accessor Value
   */
  public String getValue() {
    return ageAsString;
  }

  /**
   * Accessor Value
   */
  public void setValue(String newValue) {
    ageAsString = newValue;
    age = null;
    noteModifiedProperty();
    // Done
  }

  /**
   * Accessor age
   */
  public PointInTime getAge() {
    // calc if need be
    if (age==null)
      age = PointInTime.getDelta(ageAsString);
    // done
    return age;
  }
  
  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object other) {
    // no age?
    if (!(other instanceof PropertyAge))
      return super.compareTo(other);
    // compare ages
    return getAge().compareTo(((PropertyAge)other).getAge());
  }

  
} //PropertyAge
