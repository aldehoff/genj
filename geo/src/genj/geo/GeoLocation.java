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

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.Debug;
import genj.util.DirectAccessTokenizer;
import genj.util.Resources;
import genj.util.WordBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 *  Information about a geographic location
 */
public class GeoLocation extends Point implements Feature, Comparable {

  /** states to state-codes */
  private static Map state2code, country2code;
  
  /** 
   * our schema - could be more complicated like
   * schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
   * schema.addAttribute("PLAC", AttributeType.STRING);
   */
  /*package*/ final static FeatureSchema SCHEMA = new FeatureSchema();
  
  /*package*/  final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /** the coordinate of this location */
  private Coordinate coordinate;

  /** city state and country */
  private String city, state, country;
  private String fipsState, isoCountry;
  private int hash;
  
  /** properties at that location */
  protected List properties = new ArrayList();
  
  /** match count - 0 = couldn't be matched - 1 = exact match - n = too many matches */
  private int matches = 0;
  
  /**
   * Constructor
   * @param prop property that nees location
   */
  public GeoLocation(Property prop) {
    super(GEOMETRY_FACTORY.getCoordinateSequenceFactory().create(new Coordinate[]{ new Coordinate() } ), GEOMETRY_FACTORY);
    
    // remember coordinate
    coordinate = super.getCoordinate();
    
    // remember property
    properties.add(prop);
    
    // init
    init(prop);

  }
  
  /**
   * Convert coord to lat/lon String
   */
  public static String getString(Coordinate coord) {
    double lat = coord.y, lon = coord.x;
    if (Double.isNaN(lat)||Double.isNaN(lon))
      return "n/a";
    char we = 'E', ns = 'N';
    if (lat<0) { lat = -lat; ns='S'; }
    if (lon<0) { lon = -lon; we='W'; }
    NumberFormat format = NumberFormat.getNumberInstance();
    //format.setMinimumIntegerDigits(3);
    format.setMaximumFractionDigits(1);
    format.setMinimumFractionDigits(1);
    return ns + format.format(lat) + " " + we + format.format(lon);
  }
  
  /** 
   * Our coordinate
   */
  public Coordinate getCoordinate() {
    return coordinate;
  }

  /**
   * Init
   */
  private void init(Property prop) {
    
    //  FIXME add support for jurisdictions
    
    Property plac = prop.getProperty("PLAC");
    Property addr = prop.getProperty("ADDR");
    
    // got a place?
    if (plac instanceof PropertyPlace) {
       initFromPlace((PropertyPlace)plac);
    } else if (addr!=null) {
        initFromAddress(addr);
    } else {
      throw new IllegalArgumentException("can't locate "+prop.getTag()+" "+prop);
    }
    
    // calculate hash code now
    if (city!=null) hash += city.toLowerCase().hashCode();
    if (state!=null) hash += state.toLowerCase().hashCode();
    if (country!=null) hash += country.toLowerCase().hashCode();
    
  }
  
  /**
   * Init for Address
   */
  private boolean initFromAddress(Property addr) {
    
    // got a city?
    Property pcity = addr.getProperty("CITY");
    if (pcity==null)
      throw new IllegalArgumentException("can't determine city from address");

    // Francois keeps department in brackets in city - so trim city at '(' and look for (state)
    city = pcity.getDisplayValue();
    int open = city.indexOf('(');
    if (open>=0) {
      int close = city.indexOf(')', open);
      if (close>=0) {
        String jurisdiction = city.substring(open+1, close).trim();
        String code = getFipsState(jurisdiction);
        if (code!=null) {
          fipsState = code;
          state = jurisdiction;
        }
      }
      city = city.substring(0, open).trim();
    }
    if (city.length()==0)
      throw new IllegalArgumentException("address without city value");
    
    // still need a a state?
    if (state==null) {
      Property pstate = addr.getProperty("STAE");
      if (pstate!=null) {
        String jurisdiction = pstate.getDisplayValue();
        String code = getFipsState(jurisdiction);
        if (code!=null) {
          state = jurisdiction;
          fipsState = code;
        }
      }
    }
    
    // how about a country?
    Property pcountry = addr.getProperty("CTRY");
    if (pcountry!=null)  {
      String value = pcountry.getDisplayValue();
      String iso = getIsoCountry(value);
      if (iso!=null) {
        country = value;
        isoCountry = iso;
      }
    }
    
    // good
    return true;
  }
  
  /**
   * Init for Place
   */
  private boolean initFromPlace(PropertyPlace place) {
    
    // city is simply the first jurisdiction and required
    city = place.getJurisdiction(0);
    if (city==null||city.length()==0)
      throw new IllegalArgumentException("can't determine jurisdiction city from place value "+place);

    // Francois keeps his Departement in brackets - so trim city at '('
    int open = city.indexOf('(');
    if (open>=0)  {
      city = city.substring(0, open).trim();
      if (city.length()==0)
        throw new IllegalArgumentException("can't determine jurisdiction city from place value "+place);
    }
    
    // loop over jurisdictions to find state
    DirectAccessTokenizer jurisdictions = place.getJurisdictions();
    int skip = 0;
    for (int i=0; ; i++) {
      String jurisdiction = jurisdictions.get(i);
      if (jurisdiction==null) break;
      
      // anything in brackets?
      open = jurisdiction.indexOf('(');
      if (open>=0) {
        int close = jurisdiction.indexOf(')', open);
        if (close>=0) {
          jurisdiction = jurisdiction.substring(open+1, close);
        }
      }
      
      // try to find matching state
      String code = getFipsState(jurisdiction);
      if (code!=null) {
        state = jurisdiction;
        fipsState = code;
        skip = i;
        break;
      }
    }

    // continue looking for country if available
    for (int i=skip; ; i++) {
      String jurisdiction = jurisdictions.get(i);
      if (jurisdiction==null) break;
      String iso = getIsoCountry(jurisdiction);
      if (iso!=null) {
        country = jurisdiction;
        isoCountry = iso;
        break;
      }
    }
    
    // done
    return true;
  }
  
  /**
   * Add poperties from another instance
   */
  public void add(GeoLocation other) {
    for (Iterator it = other.properties.iterator(); it.hasNext(); ) {
      Object prop = it.next();
      if (!properties.contains(prop))
        properties.add(prop);
    }
  }
  
  /**
   * Remove properties from this location
   */
  public boolean removeAll(Collection props) {
    return properties.removeAll(props);
  }
  
  /**
   * Check for containment
   */
  public boolean contains(Property[] properties) {
    if (matches==0)
      return false;
    for (int i=0; i<properties.length; i++) {
      if (this.properties.contains(properties[i]))
        return true;
    }
    return false;
  }
  
  /**
   * How many matches this location had
   */
  public int getMatches() {
    return matches;
  }
  
  /**
   * Validity test
   */
  public boolean isValid() {
    return matches>0 && !Double.isNaN(coordinate.x) && !Double.isNaN(coordinate.y);
  }
  
  /**
   * Gedcom this location is for
   */
  public Gedcom getGedcom() {
    return ((Property)properties.get(0)).getGedcom();
  }

  /**
   * identify is defined as city, state and country
   */
  public int hashCode() {
    return hash;
  }

  /**
   * identify is defined as city, state and country
   */
  public boolean equals(Object obj) {
    GeoLocation that = (GeoLocation)obj;
    return equals(this.city, that.city) && equals(this.state, that.state) && equals(this.country, that.country);
  }
  
  private static boolean equals(String a, String b) {
    if (a==null&&b==null)
      return true;
    if (a==null||b==null)
      return false;
    return a.equalsIgnoreCase(b);
  }

  /**
   * City (never null)
   */
  public String getCity() {
    return city;
  }

  /**
   * State Code
   */
  public String getFipsState() {
    return fipsState;
  }

  /**
   * Country or null
   */
  public String  getISOCountry() {
    return isoCountry;
  }
  
  /**
   * Contained properties
   */
  public Property[] getProperties() {
    return Property.toArray(properties);
  }
  
  /**
   * Set location lat,lon
   */
  protected void set(double lat, double lon, int matches) {
    coordinate.x = lon;
    coordinate.y = lat;
    this.matches = matches;
  }
  
  /**
   * String representation
   */
  public String toString() {
    WordBuffer result = new WordBuffer(", ");
    result.append(city);
    result.append(state);
    result.append(country);
    return result.toString();
  }
  
  /**
   * Feature - set attributes
   */
  public void setAttributes(Object[] arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - set schema
   */
  public void setSchema(FeatureSchema arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - id
   */
  public int getID() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute
   */
  public void setAttribute(int arg0, Object arg1) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute
   */
  public void setAttribute(String arg0, Object arg1) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - geometry
   */
  public void setGeometry(Geometry arg0) {
    throw new IllegalArgumentException();
  }
  
  /**
   * Feature - attribute
   */
  public Object getAttribute(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by name - we always return city
   */
  public Object getAttribute(String arg0) {
    return city;
  }

  /**
   * Feature - attribute by index
   */
  public String getString(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by index
   */
  public int getInteger(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by index
   */
  public double getDouble(int arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attribute by name
   */
  public String getString(String arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - geometry
   */
  public Geometry getGeometry() {
    return this;
  }

  /**
   * Feature - schema
   */
  public FeatureSchema getSchema() {
    return SCHEMA;
  }

  /**
   * Feature - clonig
   */
  public Object clone() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - cloing
   */
  public Feature clone(boolean arg0) {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - attributes
   */
  public Object[] getAttributes() {
    throw new IllegalArgumentException();
  }

  /**
   * Feature - comparison
   */
  public int compareTo(Object o) {
    GeoLocation that = (GeoLocation)o;
    return this.city.compareToIgnoreCase(that.city);
  }
  
  /**
   * Lookup a country code
   */
  private static String getIsoCountry(String name) {

    // information there at all?
    if (name==null||name.length()==0)
      return Country.getDefaultCountry().getCode();
    
    // initialized?
    if (country2code==null) { synchronized (GeoLocation.class) { if (country2code==null) {
      
      country2code = new HashMap();

      // init all well known countries
      String lang = Locale.getDefault().getLanguage();
      String[] countries = Locale.getISOCountries();
      for (int c=0; c<countries.length; c++) 
        country2code.put( new Locale(lang, countries[c]).getDisplayCountry(), countries[c]);
      
    }}}
    
    // look it up
    return (String)country2code.get(name);
  }
    
  /**
   * Lookup a state code 
   */
  private String getFipsState(String state) {
    
    // initialized?
    if (state2code==null) { synchronized (GeoLocation.class) { if (state2code==null) {
      
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
            for (StringTokenizer names = new StringTokenizer(meta.getString(key), ","); names.hasMoreTokens(); ) {
              String token = mangleState(names.nextToken());
              if (token.length()>0) state2code.put(token, code);
            }
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
  
  private String mangleState(String state) {
    StringBuffer result = new StringBuffer(state.length());
    for (int i=0;i<state.length();i++) {
      char c = state.charAt(i);
      if (Character.isLetter(c))
        result.append(Character.toLowerCase(c));
    }
    return result.toString();
  }
  
}
