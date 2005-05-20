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

import genj.util.Debug;
import genj.util.Resources;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A country - why isn't that in java.util
 */
public class Country implements Comparable {
  
  private final static Resources ISO2FIPS = new Resources(Country.class.getResourceAsStream("iso2fips.properties"));
  
  private static Country DEFAULT_COUNTRY = null;
  
  private final static Map iso2country = new HashMap();
  
  /** states to state-codes */
  private static Map state2code;
  
  /** state */
  private String iso;
  private String fips;
  private String name;
  
  /** lookup */
  public static Country get(String iso) {
    iso = iso.toLowerCase();
    Country result = (Country) iso2country.get(iso);
    if (result==null) {
      result = new Country(iso);
      iso2country.put(iso, result);
    }
    return result;
  }
  
  /** lookup best match for location */
  public static Country get(GeoLocation loc) {

    // information there at all?
    String country = loc.getCountry();
    if (country==null||country.length()==0)
      return getDefaultCountry();

    // look for country
    String lang = Locale.getDefault().getLanguage();
    
    String[] countries = Locale.getISOCountries();
    for (int c=0; c<countries.length; c++) {
      if (new Locale(lang, countries[c]).getDisplayCountry().equalsIgnoreCase(country))
        return get(countries[c]);
    }
    
    // use default then
    return getDefaultCountry();
  }
  
  /** constructor */
  private Country(String code) {
    iso = code;
    name =  new Locale("en", code).getDisplayCountry();
  }
  
  /** name */
  public String getName() {
    return name;
  }
  
  /** string representation - it's name */
  public String toString() {
    return name;
  }
  
  /** iso code */
  public String getCode() {
    return iso;
  }
  
  /** fips code */
  public String getFips() {
    if (fips==null)
      fips = ISO2FIPS.getString(iso);
    return fips;
  }
  
  /** comparison - by name */
  public int compareTo(Object o) {
    return toString().compareTo(o.toString());
  }
  
  /** equals - same country */
  public boolean equals(Object obj) {
    return this==obj;
  }
  
  /**
   * Get default country
   */
  public static Country getDefaultCountry() {
    getAllCountries();
    return DEFAULT_COUNTRY;
  }
  
  /**
   * Get all countries
   */
  public static Country[] getAllCountries() {
    
    // checked that once already?
    if (DEFAULT_COUNTRY!=null) {
      Collection countries = iso2country.values();
      Country[] result = (Country[])countries.toArray(new Country[countries.size()]);
      Arrays.sort(result);
      return result;
    }

    DEFAULT_COUNTRY = get(Locale.getDefault().getCountry());
    
    // grab all country codes
    String[] codes = Locale.getISOCountries(); 
    for (int i=0;i<codes.length;i++) 
      get(codes[i]);
    
    // try again
    return getAllCountries();
  }
  
  /**
   * Lookup a state code 
   */
  public static String getState(GeoLocation loc) {
    
    // information there?
    String state = loc.getState();
    if (state==null||state.length()==0)
      return null;
    
    // initialized?
    if (state2code==null) { synchronized (Country.class) { if (state2code==null) {
      
      state2code =  new HashMap();
      
      // try to find states.properties
      File[] files = GeoService.getInstance().getGeoFiles();
      for (int i = 0; i < files.length; i++) {
        // the right file either local or global?
        if (!files[i].getName().equals("states.properties"))
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
            String code = key.substring(3,5);
            for (StringTokenizer names = new StringTokenizer(meta.getString(key), ","); names.hasMoreTokens(); )
              state2code.put(mangleState(names.nextToken()), code);
          }
        } catch (Throwable t) {
          Debug.log(Debug.WARNING, Country.class, t);
        }
        
        // next
      }
      
      // initialized
    }}}

    // look it up
    return (String)state2code.get(mangleState(state));
  }
  
  private static String mangleState(String state) {
    return state.trim().toLowerCase().replaceAll(" ", "").replaceAll("-", "");
  }

} //Country