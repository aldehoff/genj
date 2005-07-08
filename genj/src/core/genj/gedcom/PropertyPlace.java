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
import genj.util.ReferenceSet;
import genj.util.swing.ImageIcon;

import java.util.Collection;
import java.util.Iterator;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends PropertyChoiceValue {

  public final static ImageIcon
    IMAGE = Grammar.getMeta(new TagPath("INDI:BIRT:PLAC")).getImage();
  
  public final static String
    JURISDICTION_SEPARATOR = ",";
  
  private final static String 
    JURISDICTION_RESOURCE_PREFIX = "prop.plac.jurisdiction.";
  
  public final static String 
    TAG = "PLAC",
    FORM = "FORM";
  
  /**
   * Overridden - special trim
   */
  protected String trim(String value) {
    // trim each jurisdiction separately
    StringBuffer buf = new StringBuffer(value.length());
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
    for (int i=0; ; i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      if (i>0) buf.append(JURISDICTION_SEPARATOR);
      buf.append(jurisdiction);
    }
    return buf.toString().intern();
  }
    
  /**
   * Remember a jurisdiction's vlaue
   */
  protected boolean remember( String theOld, String theNew) {
    
    // let super do its stuff
    if (!super.remember(theOld, theNew))
      return false;
    Gedcom gedcom = getGedcom();
    
    // forget old jurisdictions
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(theOld, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      // forget PLAC.n
      if (jurisdiction.length()>0)
        gedcom.getReferenceSet(TAG+"."+i).remove(jurisdiction, this);
      // next
    }
    
    // remember new jurisdictions
    jurisdictions = new DirectAccessTokenizer(theNew, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      // remember PLAC.n
      if (jurisdiction.length()>0) 
        gedcom.getReferenceSet(TAG+"."+i).add(jurisdiction.intern(), this);
      // next
    }
    
    // done
    return true;
  }

  /**
   * We expect our own proxy here
   */
  public String getProxy() {
    return "Place";
  }

  /**
   * Accessor - the format of this place's value (non localized)
   */
  public String getFormat() {
    // look it up
    String result = "";
    Property pformat = getProperty(FORM);
    if (pformat!=null) 
      result = pformat.getValue();
    else {
      Gedcom ged = getGedcom();
      if (ged!=null)
        result = ged.getPlaceFormat();
    }
    // done
    return result;
  }
  
  /**
   * Accessor - the hierarchy of this place's value (non localized)
   */
  public void setFormat(boolean global, String format) {
    if (!global)
      throw new IllegalArgumentException("non-global n/a");
    // propagate
    getGedcom().setPlaceFormat(format);
    // mark changed
    propagateChange(getValue());
  }
  
  /**
   * Accessor - the format  of this place's value ( localized)
   */
  public String getDisplayFormat() {
    
    String format = getFormat();
    
    // loop over (assumed) english names
    StringBuffer buf = new StringBuffer();
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(format, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String token = tokens.get(i, true);
      if (token==null) break;
      if (buf.length()>0) buf.append(JURISDICTION_SEPARATOR);
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
  
  /**
   * Accessor - the format of this place's value (localized)
   */
  public void setDisplayFormat(boolean global, String format) {
    
    // loop over (assumed) local names
    StringBuffer buf = new StringBuffer();
    DirectAccessTokenizer tokens = new DirectAccessTokenizer(format, JURISDICTION_SEPARATOR);
    for (int i=0;;i++) {
      String token = tokens.get(i, true);
      if (token==null) break;
      if (buf.length()>0) buf.append(JURISDICTION_SEPARATOR);
      buf.append(_local2gedcom(token));
    }
    
    // done
    setFormat(global, buf.toString());
  }
  
  private static String _local2gedcom(String token) {
    Iterator keys = resources.getKeys();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      if (key.startsWith(JURISDICTION_RESOURCE_PREFIX)) {
        DirectAccessTokenizer locals = new DirectAccessTokenizer(resources.getString(key), "|");
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
   * Accessor - all places with the same jurisdiction for given hierarchy level
   */
  public PropertyPlace[] getSameChoices(int hierarchyLevel) {
    String jurisdiction = getJurisdiction(hierarchyLevel);
    if (jurisdiction==null)
      return null;
    Collection places = getGedcom().getReferenceSet(TAG+"."+hierarchyLevel).getReferences(jurisdiction);
    return (PropertyPlace[])places.toArray(new PropertyPlace[places.size()]);
  }
  
  /**
   * Accessor - all jurisdictions of given level in same gedcom file
   */
  public String[] getAllJurisdictions(int hierarchyLevel, boolean sort) {
    Gedcom gedcom = getGedcom();
    if (gedcom==null)
      return new String[0];
    return getAllJurisdictions(gedcom, hierarchyLevel, sort);
  }
  
  /**
   * Accessor - all jurisdictions of given level in gedcom
   * @param hierarchyLevel either a zero-based level or -1 for whole place values
   */
  public static String[] getAllJurisdictions(Gedcom gedcom, int hierarchyLevel, boolean sort) {
    ReferenceSet refset = gedcom.getReferenceSet( hierarchyLevel<0 ? TAG : TAG+"."+hierarchyLevel);
    Collection jurisdictions = refset.getKeys(sort ? gedcom.getCollator() : null);
    return (String[])jurisdictions.toArray(new String[jurisdictions.size()]);
  }
  
  /**
   * Accessor - jurisdiction of given level
   * @return jurisdiction of zero+ length or null if n/a
   */
  public String getJurisdiction(int hierarchyLevel) {
    return getJurisdictions().get(hierarchyLevel);
  }
  
  /**
   * Accessor - jurisdiction iterator
   */
  public DirectAccessTokenizer getJurisdictions() {
    return new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR);
  }
  
} //PropertyPlace
