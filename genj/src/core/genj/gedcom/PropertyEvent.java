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

import genj.util.*;

/**
 * Gedcom Property : EVENT
 */
public class PropertyEvent extends Property {
  
  /** our Tag */
  private String tag;

  /** known EVENT tags */
  private static String[] tags;

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyEvent(String tag) {

    // Remember
    this.tag = tag;

    // Done
  }

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyEvent(String tag, String value) {

    // Remember
    this.tag = tag;

    // Done
  }

  /**
   * Adds all default properties to this property
   */
  public void addDefaultProperties() {
    addProperty(new PropertyDate());
    addProperty(new PropertyPlace(""));
  }

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
  //  Property prop = getProperty(new TagPath(getTag()+":DATE"),valid);
    Property prop = getProperty(new TagPath("DATE"),valid);
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

    // look for PropertyDate in children
    if (children==null) return "";
    
    for (int i=0;i<children.getSize();i++) {
      Property prop = children.get(i);
      if ( prop instanceof PropertyDate ) {
        return ((PropertyDate)prop).toString();
      }
    }

    // No information
    return "";
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Empty";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "Empty";
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
    return "";
  }

  /**
   * Sets the value of this property
   */
  public boolean setValue(String value) {
    return false;
  }

  /**
   * Returns the list of tags which identify PropertyEvents
   */
  public static String[] getTags() {

    // Already calculated?
    if (tags!=null) {
      return tags;
    }

    Vector v = new Vector(100);
    for (int i=0;i<metaDefs.length;i++) {
      MetaDefinition def = metaDefs[i];
      if (def.isEvent()) {
        v.addElement(def.getTag());
      }
    }

    String[] result = new String[v.size()];
    Enumeration e = v.elements();
    for (int i=0;e.hasMoreElements();i++) {
      result[i] = e.nextElement().toString();
    }

    tags = result;
    return result;
  }


}
