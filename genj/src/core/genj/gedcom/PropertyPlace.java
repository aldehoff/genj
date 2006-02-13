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
import java.util.Set;

/**
 * PLAC a choice value with brains for understanding sub-property FORM
 */
public class PropertyPlace extends PropertyChoiceValue {
  
  private final static boolean USE_SPACES = Options.getInstance().isUseSpacedPlaces;

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
    
    /*
     20051212 at some point we switched to trimming values on places
     here, making sure that the separator only is between jurisdictions.
     Peter asked me to add spaces as well for readability:
       2 PLAC Hamburg, Schleswig Holstein, Deutschland
     instead of 
       2 PLAC Hamburg,Schleswig Holstein,Deutschland

     But Francois reminded me that we didn't want to have spaces in
     the Gedcom file - the spec doesn't explicitly disallow it but especially
     in Francois' way of keeping place information 
       2 PLAC ,Allanche,,Cantal,Auvergne,
     adding spaces doesn't look good
       2 PLAC , Allanche, , Cantal, Auvergne, 

     We played with the idea of using space-comma in getDisplayValue()
     and comma-only in getValue()/trim() - problem is that it takes mem
     to cache or runtime performance to calculate that. It's also problematic
     that the display value would be different from the choices remembered
     (one with space the other without)
     
     So finally we decided to put in a global option that lets the user
     make the choice - internally getValue()-wize we handle this uniformly then
    */
    
    // trim each jurisdiction separately
    StringBuffer buf = new StringBuffer(value.length());
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(value, JURISDICTION_SEPARATOR);
    for (int i=0; ; i++) {
      String jurisdiction = jurisdictions.get(i, true);
      if (jurisdiction==null) break;
      if (i>0) {
        buf.append(JURISDICTION_SEPARATOR);
        if (USE_SPACES) buf.append(' ');
      }
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
  public String getHierarchy() {
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
  public void setHierarchy(boolean global, String format) {
    if (!global)
      throw new IllegalArgumentException("non-global n/a");
    // propagate
    getGedcom().setPlaceFormat(format);
    // mark changed
    propagateChange(getValue());
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
   * Accessor - first non-empty jurisdiction from skip starting point 
   * @return jurisdiction of zero+ length
   */
  public String getFirstAvailableJurisdiction(int skip) {
      if (skip<0) throw new IllegalArgumentException("negative skip value");
    DirectAccessTokenizer jurisdictions = new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR);
    String result = jurisdictions.get(skip);
    if (result==null)
      return "";
    for (int i=skip+1; result.length()==0 && jurisdictions.get(i)!=null ;i++) 
      result = jurisdictions.get(i);
    return result;
  }

  /**
   * Accessor - first non-empty jurisdiction
   * @return jurisdiction of zero+ length
   */
  public String getFirstAvailableJurisdiction() {
    return getFirstAvailableJurisdiction(0);
  }
  
  /**
   * Accessor - jurisdiction of given level
   * @return jurisdiction of zero+ length or null if n/a
   */
  public String getJurisdiction(int hierarchyLevel) {
    return new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(hierarchyLevel);
  }
  
  /**
   * Accessor - jurisdictions that is the city
   */
  public String getCity() {
    return new DirectAccessTokenizer(getValue(), JURISDICTION_SEPARATOR).get(getCityIndex());
  }
  
  /**
   * Accessor - all jurisdictions starting with city
   */
  public String getValueStartingWithCity() {
    String result = getValue();
    int city = getCityIndex();
    if (city==0)
      return result;
    return new DirectAccessTokenizer(result, JURISDICTION_SEPARATOR).getSubstring(city);
  }
  
  private int getCityIndex() {
    
    // calculate the city index in place format
    String hierarchy = getHierarchy();
    if (hierarchy.length()>0) {
      // look for a city key in the hierarchy
      Set cityKeys = Options.getInstance().placeHierarchyCityKeys;
      DirectAccessTokenizer hs = new DirectAccessTokenizer(hierarchy, ",");
      for (int index=0; hs.get(index)!=null ;index++) {
        if (cityKeys.contains(hs.get(index).toLowerCase())) 
          return index;
      }
    }
    
    // assuming first
    return 0;
  }
  
} //PropertyPlace
