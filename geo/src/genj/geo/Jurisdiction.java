/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;

/**
 * We support the concept of a top-level jurisdiction
 */
public class Jurisdiction {
  
  /** a weak cache of displayNames to reusable instances */
  private static Map displayName2jurisdiction = new WeakHashMap(); 
  
  /** a cache of key to jurisdiction    (xx.yy where xx=iso country yy=fips jurisdiction code) */
  private static Map key2jurisdiction = new HashMap();

  /** all known jurisdictions */
  private static List JURISDICTIONS;
  
  /** state */
  private Country country;
  private String code, displayName;
  
  /**
   * Constructor
   */
  private Jurisdiction(Country country, String code, String displayName) {
    
    // remember 20050525 don't lowercase code since we're comparing case sensitive with database values
    this.country = country;
    this.code = code;
    this.displayName = displayName;
    
  }
  
  /**
   * Accessor
   */
  public String getDisplayName() {
    return displayName;
  }
  
  /**
   * Accessor
   */
  public String getCode() {
    return code;
  }
  
  /**
   * Equal if country and code are the same 
   */
  public boolean equals(Object obj) {
    if (obj==null) return false;
    Jurisdiction that = (Jurisdiction)obj;
    return this.country.equals(that.country) && this.code.equals(that.code);
  }
  
  /**
   * String representation
   */
  public String toString() {
    return displayName;
  }
  
  /**
   * Access
   */
  public static Jurisdiction get(Country country, String code) {
    
    // make sure the jurisdictions are initialized
    getJurisdictions();
    
    // lookup & create if necessary but don't cache unknown jurisdictions
    Jurisdiction result = (Jurisdiction)key2jurisdiction.get(country.getCode()+"."+code);
    if (result==null) 
      result = new Jurisdiction(country, code, code);
      
    return result;
  }
  
  /**
   * Access
   */
  public static Jurisdiction get(Collator collator, String displayName, Country country) {
    
    // do a quick lookup for cached info
    if (displayName2jurisdiction.containsKey(displayName))
      return (Jurisdiction)displayName2jurisdiction.get(displayName);
      
    // look it up
    Jurisdiction result = null;
    
    List jurisdictions = getJurisdictions();
    for (int i = 0; i < jurisdictions.size(); i++) {
      Jurisdiction jurisdiction = (Jurisdiction)jurisdictions.get(i);
      // null or good country?
      if (country!=null&&!country.equals(jurisdiction.country))
        continue;
      // good display name?
      if (collator.compare(jurisdiction.displayName, displayName)==0)  {
        // stop if not deterministic (2nd match)
        if (result!=null) {
          result = null;
          break;
        }
        // keep it
        result = jurisdiction;
      }
      // try next
    }

    // something found? create a new instance just for this displayName!
    if (result!=null) 
      result = new Jurisdiction(result.country, result.code, displayName);
    
    // cache it for next time
    displayName2jurisdiction.put(displayName, result);

    // done
    return result;
  }

  private static List getJurisdictions() {
    
    // already known?
    if (JURISDICTIONS!=null) 
      return JURISDICTIONS;
    
    // initialize!
    JURISDICTIONS =  new ArrayList();
      
    File[] files = GeoService.getInstance().getGeoFiles();
    for (int i = 0; i < files.length; i++) {
      // the right file either local or global?
      if (!files[i].getName().equals("jurisdictions.properties"))
        continue;
      // read it
      try {
        Resources meta = new Resources(new FileInputStream(files[i]));
        // look for all 'xx.yy = aaa,bbb,ccc'
        // where 
        //  xx = country
        //  yy = state code
        //  aaa,bbb,ccc = state names or abbreviations
        for (Iterator keys = meta.getKeys(); keys.hasNext(); ) {
          String key = keys.next().toString();
          if (key.length()!=5) continue;
          String country = key.substring(0,2);
          String code = key.substring(3,5);
          StringTokenizer names = new StringTokenizer(meta.getString(key), ",");
          for (int n=0; names.hasMoreTokens(); n++) {
            Jurisdiction jurisdiction = new Jurisdiction(Country.get(country), code, names.nextToken().trim());
            if (n==0) key2jurisdiction.put(key, jurisdiction);
            JURISDICTIONS.add(jurisdiction);
          }
        }
      } catch (Throwable t) {
        GeoView.LOG.log(Level.WARNING, "unexpected throwable parsing jurisdictions ", t);
      }

      // next file
    }

    // done
    return JURISDICTIONS;
  }
  
}
