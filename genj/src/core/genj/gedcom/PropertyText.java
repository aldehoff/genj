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
 * Gedcom Property : TEXT 
 * A property that consists of text (used in SOURCE)
 */
public class PropertyText extends Property {
  
  /** the text */
  private String text = "";

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyText() {
    this(null,"");
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyText(String tag, String value) {
    setValue(value);
  }

  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of this address
   */
  public Enumeration getLineIterator() {
    // iterate
    return new StringTokenizer(text, "\n");
  }

  /**
   * Returns this property's value cut to a first line in
   * case someone actually asks us
   */
  public String getValue() {
    String result = text;
    int pos = result.indexOf('\n');
    if (pos>=0) result = result.substring(0,pos)+"...";
    return result;
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public boolean setValue(String set) {
    text = set;
    if (text==null) text="";
    return false;
  }


  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    // multiline
    return "MLE";    
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    // Could be XRef or MLE
    return "MLE";
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {
    return "TEXT";
  }

  /**
   * This property incorporates several lines with newlines
   */
  public int isMultiLine() {
    // sure bring it on!
    return MULTI_NEWLINE;
  }

  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always
    return true;
  }

  
} //PropertyNote

