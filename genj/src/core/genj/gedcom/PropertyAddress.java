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

import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * Gedcom Property : ADDR
 */
public class PropertyAddress extends Property {

  /** the address */
  private String address;

  /**
   * Constructor of address Gedcom-line
   */
  public PropertyAddress() {
    this("");
  }

  /**
   * Constructor of address Gedcom-line
   * @param address initial value
   */
  public PropertyAddress(String address) {
    this.address=address;
  }

  /**
   * Constructor with Tag,Value,Level parameters
   * @param tag Tag
   * @param value Value
   * @param level Level
   */
  public PropertyAddress(String tag, String value) {
    setValue(value);
  }

  /**
   * Adds all known properties to this property
   */
  public Property addDefaultProperties() {
    noteModifiedProperty();
    addProperty(new PropertyCity(""));
    addProperty(new PropertyPostalCode(""));
    return this;
  }

  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of this address
   */
  public Enumeration getLineIterator() {
    return new StringTokenizer(address,"\n");
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   * @return proxy's logical name
   */
  public String getProxy() {
    return "MLE";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "MLE";
  }

  /**
   * Returns the Gedcom-Tag of this property
   * @return tag as <code>String</code>
   */
  public String getTag() {
    return "ADDR";
  }

  /**
   * Accessor Value
   */
  public String getValue() {

    // More than one line ?
    int pos = address.indexOf('\n');
    if (pos<0)
      return address;
    // Value
    return address.substring(0,pos)+"...";
  }

  /**
   * This property incorporates several lines with newlines
   */
  public int isMultiLine() {
    return MULTI_NEWLINE;
  }

  /**
   * Accessor Value
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    this.address=value;
    return true;
  }
}
