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

/**
 * Wrapper for a Property (could be read from XML file later)
 */
public class MetaDefinition {

  /** Members */
  private String    theTag;
  private Class     theClass;
  private Vector    vSubTags;
  private Hashtable hSubTags;
  private boolean   isEvent;

  /**
   * Constructor
   */
  public MetaDefinition(String t, String c) {
    this(t,c,new String[0]);
  }

  /**
   * Constructor
   */
  public MetaDefinition(String t, String c, String[] s) {

    // Remember data
    theTag = t;

    // Figure out the class
    try {
      theClass = Class.forName(Property.class.getName()+c);
    } catch (Throwable throwable) {
      // changed to throwable for IE throwing a security exception for unknown type :(
      theClass = new PropertyUnknown("","").getClass();
    }
    // Initialize subTags
    vSubTags = new Vector(s.length+4);
    hSubTags = new Hashtable(s.length+4);
    for (int i=0;i<s.length;i++) {
      vSubTags.addElement(s[i]);
      hSubTags.put(s[i],s[i]);
    }
    isEvent = PropertyEvent.class.isAssignableFrom(theClass);
  }

  /**
   * Adds a possible-sub-tag to this definition
   */
  public void addSubTag(String tag) {
    if (hSubTags.contains(tag)) {
      return;
    }
    vSubTags.addElement(tag);
    hSubTags.put(tag,tag);
  }

  /**
   * The property's type
   */
  public Class getPropertyClass() {
    return theClass;
  }

  /**
   * The property's sub-types
   */
  public Vector getSubTags() {
    return vSubTags;
  }

  /**
   * The property's tag
   */
  public String getTag() {
    return theTag;
  }

  /**
   * Returns whether this definition represents an event or not
   */
  public boolean isEvent() {
    return isEvent;
  }

}
