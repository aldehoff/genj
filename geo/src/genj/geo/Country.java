package genj.geo;

import genj.util.Resources;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A country - why isn't that in java.util
 */
public class Country implements Comparable {
  
  private final static Resources ISO2FIPS = new Resources(Country.class.getResourceAsStream("iso2fips.properties"));
  
  private static Country DEFAULT_COUNTRY = null;
  
  private final static Map iso2country = new HashMap();
  
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

} //Country