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
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends Property {
  
  /** our Tag */
  private String tag;

  /**
   * Returns the date of the event
   */
  public PropertyDate getDate() {
    return getDate(true);
  }

  /**
   * Returns the date of the event
   * @param valid specifies wether data has to be valid to be found
   */
  public PropertyDate getDate(boolean valid) {

    // Try to get date-property which is valid
    Property prop = getProperty("DATE",valid);
    if (prop==null) {
      return null;
    }

    // Return as Date
    return (PropertyDate)prop;
  }

  /**
   * Calculate event's date
   */
  public String getDateAsString() {
    Property date = getProperty("DATE");
    return date!=null ? date.getValue() : EMPTY_STRING;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Event";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return tag;
  }

  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(String set, String value) throws GedcomException {
    tag = set;
    return super.init(tag,value);
  }

  /**
   * Returns the value of this property
   */
  public String getValue() {
    return EMPTY_STRING;
  }

  /**
   * Sets the value of this property
   */
  public void setValue(String value) {
  }

  /**
   * Text representation (e.g. BIRT 7 MAY 2002)
   */
  public String toString() {
    return getTag()+' '+getDateAsString();
  }

  /**
   * Returns the list of paths which identify PropertyEvents
   */
  public static TagPath[] getTagPaths() {
    return MetaProperty.getPaths(-1, PropertyEvent.class);  
  }

} //PropertyEvent
