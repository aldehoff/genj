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

import java.util.Collection;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends Property {

  public final static String
    JURISDICTION_SEPARATOR = ",";
  
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
    // swap new for old
    String old = getValue();
    place = value;
    propagateChange(old);
    // remember it
    remember(old, place);
  }
  
  /**
   * Remember a jurisdiction's vlaue
   */
  private void remember( String theOld, String theNew) {
    // got access to a reference set?
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return;
    // forget old jurisdictions
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(theOld, JURISDICTION_SEPARATOR, true);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i);
      if (jurisdiction==null) break;
      if (jurisdiction.length()>0) gedcom.getReferenceSet(TAG+"."+i).remove(jurisdiction, this);
    }
    // remember new jurisdictions
    jurisdictions = new DirectAccessTokenizer(theNew, JURISDICTION_SEPARATOR, true);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i);
      if (jurisdiction==null) break;
      if (jurisdiction.length()>0) gedcom.getReferenceSet(TAG+"."+i).add(jurisdiction, this);
    }
    // done
  }

  /**
   * @see genj.gedcom.Property#addNotify(genj.gedcom.Property)
   */
  /*package*/ void addNotify(Property parent) {
    // delegate
    super.addNotify(parent);
    // a remember wouldn't have worked until now
    remember(EMPTY_STRING, place);
    // done
  }

  /**
   * Removing us from the reference set (our value is not used anymore)
   * @see genj.gedcom.PropertyRelationship#delNotify()
   */
  /*package*/ void delNotify(Property old) {
    // forget value
    remember(place, EMPTY_STRING);
    // continue
    super.delNotify(old);
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
   * Accessor - all jurisdictions of given level in gedcom
   */
  public static String[] getJurisdictions(Gedcom gedcom, int hierarchyLevel, boolean sort) {
    Collection jurisdictions = gedcom.getReferenceSet(TAG+"."+hierarchyLevel).getKeys(sort ? gedcom.getCollator() : null);
    return (String[])jurisdictions.toArray(new String[jurisdictions.size()]);
  }
  
  /**
   * Accessor - jurisdiction of given level
   * @return jurisdiction of zero+ length or null if n/a
   */
  public String getJurisdiction(int hierarchyLevel) {
    return new DirectAccessTokenizer(place, JURISDICTION_SEPARATOR).get(hierarchyLevel);
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
