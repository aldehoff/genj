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
import genj.util.swing.ImageIcon;

import java.util.Collection;
import java.util.Iterator;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends Property {

  public final static ImageIcon
    IMAGE = Grammar.getInstance().getMeta(new TagPath("INDI:BIRT:PLAC")).getImage();
  
  public final static String
    JURISDICTION_SEPARATOR = ",";
  
  private final static String 
    JURISDICTION_RESOURCE_PREFIX = "prop.plac.jurisdiction.";
  
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
    return getHierarchy(getGedcom());
  }
  
  /**
   * Format
   */
  public String getHierarchy(Gedcom gedcom) {
    // look it up
    String result = EMPTY_STRING;
    Property pformat = getProperty(FORM);
    if (pformat!=null) 
      result = pformat.getValue();
    else {
      if (gedcom!=null)
        result = gedcom.getPlaceHierarchy();
    }
    // done
    return gedcom2local(result);
  }
  
  /**
   * localize hierarchy
   */
  public static String gedcom2local(String hierarchy) {
    
    // loop over (assumed) english names
    StringBuffer buf = new StringBuffer();
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(hierarchy, JURISDICTION_SEPARATOR, true);
    for (int i=0;;i++) {
      String token = tokens.get(i);
      if (token==null) break;
      if (buf.length()>0) buf.append(", ");
      buf.append(_gedcom2local(token));
    }
    
    // done
    return buf.toString();
  }
  
  private static String _gedcom2local(String token) {
    String local = resources.getString(JURISDICTION_RESOURCE_PREFIX+token.toLowerCase().replaceAll(" ",""), false);
    // no translation available - no luck - fall back to original gedcom token
    if (local==null)
      return token;
    // choose one of multiple local translations
    int or = local.indexOf('|');
    if (or>0) local = local.substring(0,or);
    // done
    return local;
  }
  
  public static String local2gedcom(String hierarchy) {
    
    // loop over (assumed) local names
    StringBuffer buf = new StringBuffer();
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(hierarchy, JURISDICTION_SEPARATOR, true);
    for (int i=0;;i++) {
      String token = tokens.get(i);
      if (token==null) break;
      if (buf.length()>0) buf.append(", ");
      buf.append(_local2gedcom(token));
    }
    
    // done
    return buf.toString();
  }
  
  private static String _local2gedcom(String token) {
    Iterator keys = resources.getKeys();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      if (key.startsWith(JURISDICTION_RESOURCE_PREFIX)) {
        DirectAccessTokenizer locals = new DirectAccessTokenizer(resources.getString(key), "|", true);
        for (int i=0;;i++) {
          // check each local translation as a possibility
          String local = locals.get(i);
          if (local==null) break;
          // return key without prefix as gedcom value
          if (token.equals(local))
            return key.substring(JURISDICTION_RESOURCE_PREFIX.length());
        }
      }
    }
    return token;
  }
  
  /**
   * Accessor - all jurisdictions of given level in same gedcom file
   */
  public String[] getJurisdictions(int hierarchyLevel, boolean sort) {
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new String[0];
    return getJurisdictions(gedcom, hierarchyLevel, sort);
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
    getGedcom().setPlaceHierarchy(local2gedcom(hierarchy));
    // mark changed
    propagateChange(getValue());
  }
  
} //PropertyPlace
