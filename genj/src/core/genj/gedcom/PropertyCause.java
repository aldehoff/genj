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
 * Gedcom Property : CAUSE
 */
public class PropertyCause extends Property {

  /** the cause description */
  private String cause;

  /**
   * Member class for iterating through cause's lines
   */
  private class CauseLineIterator implements Property.LineIterator {

    /** tokens */
    private StringTokenizer tokens;

    /** constructor */
    CauseLineIterator() {
      tokens = new StringTokenizer(cause,"\n");
    }

    /** more? */
    public boolean hasMoreValues() {
      return tokens.hasMoreTokens();
    }

    /** Returns the next line of this iterator */
    public String getNextValue() throws NoSuchElementException {
      return tokens.nextToken();
    }

    // EOC
  }

  /**
   * Constructor of cause Gedcom-line
   */
  public PropertyCause(String cause) {
    this.cause=cause;
  }

  /**
   * Constructor of cause Gedcom-line
   */
  public PropertyCause(String tag, String value) {
    setValue(value);
  }

  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of this cause
   */
  public LineIterator getLineIterator() {
    return new CauseLineIterator();
  }

  /**
   * Which Proxy to use for this property
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
   * Accessor Tag
   */
  public String getTag() {
    return "CAUS";
  }

  /**
   * Accessor Value
   */
  public String getValue() {

    // More than one line ?
    int pos = cause.indexOf('\n');
    if (pos<0) {
      return cause;
    }

    // Value
    return cause.substring(0,pos)+"...";
  }

  /**
   * This property incorporates several lines
   */
  public int isMultiLine() {
    return MULTI_NEWLINE;
  }

  /**
   * Accessor Value
   */
  public boolean setValue(String value) {
    noteModifiedProperty();
    cause=value;
    return true;
  }
}
