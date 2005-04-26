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

import genj.util.DirectAccessTokenizer;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends Property {
  
  public final static String 
    TAG = "PLAC",
    FORM = "FORM";

  /** place */
  private String place = EMPTY_STRING;
  
  /**
   * The Place TAG
   */
  public String getTag() {
    return TAG;
  }
  
  /**
   * Value
   */
  public String getValue() {
    return place;
  }
  
  /**
   * Value
   */
  public void setValue(String value) {
    String old = getValue();
    place = value;
    propagateChange(old);
  }

  /**
   * Get Form
   */
  public String getProxy() {
    return "Place";
  }

  /**
   * Format
   */
  public String getHierarchy() {
    String result = EMPTY_STRING;
    Property pformat = getProperty(FORM);
    if (pformat!=null) 
      result = pformat.getValue();
    else {
      Gedcom ged = getGedcom();
      if (ged!=null)
        result = ged.getPlaceHierarchy();
    }
    return result.trim();
  }
  
  /**
   * Accessor - part of place by value index between commas
   */
  public String getJurisdiction(int formatIndex) {
    String result = new DirectAccessTokenizer(getHierarchy(), ",").get(formatIndex);
    return result!=null ? result : EMPTY_STRING;
  }
  
  /**
   * Setter of global place hierarchy
   */
  public void setHierarchy(boolean global, String hierarchy) {
    if (!global)
      throw new IllegalArgumentException("non-global n/a");
    // propagate
    getGedcom().setPlaceHierarchy(hierarchy);
    // mark changed
    propagateChange(getValue());
  }
  
} //PropertyPlace
