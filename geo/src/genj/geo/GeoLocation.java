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
import genj.util.DirectAccessTokenizer;
import genj.util.WordBuffer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

  /** "locale to displayCountries to country-codes"*/
  private static Map locale2displayCountry2code = new HashMap();
  
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
  private String city;
  private Country country;
  private Jurisdiction jurisdiction;
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
    if (jurisdiction!=null) hash += jurisdiction.getDisplayName().toLowerCase().hashCode();
    if (country!=null) hash += country.getDisplayName().toLowerCase().hashCode();
    
  }
  
  /**
   * Init for Address
   */
  private boolean initFromAddress(Property addr) {
    
    Gedcom ged =addr.getGedcom();
    
    // got a city?
    Property pcity = addr.getProperty("CITY");
    if (pcity==null)
      throw new IllegalArgumentException("can't determine city from address");

    city = pcity.getDisplayValue().trim();
    if (city.length()==0)
      throw new IllegalArgumentException("address without city value");
    
    // still need a a state?
    Property pstate = addr.getProperty("STAE");
    if (pstate!=null) 
      jurisdiction = Jurisdiction.get(ged.getCollator(), pstate.getDisplayValue());
    
    // how about a country?
    Locale locale = addr.getGedcom().getLocale();
    Property pcountry = addr.getProperty("CTRY");
    if (pcountry!=null)  
      country = Country.get(ged.getLocale(), pcountry.getDisplayValue());
    
    // good
    return true;
  }
  
  /**
   * Init for Place
   */
  private boolean initFromPlace(PropertyPlace place) {
    
    Gedcom ged = place.getGedcom();
    
    // city is simply the first jurisdiction and required
    city = place.getJurisdiction(0);
    if (city==null||city.length()==0)
      throw new IllegalArgumentException("can't determine jurisdiction city from place value "+place);

    // loop over jurisdictions to find state
    DirectAccessTokenizer js = place.getJurisdictions();
    int skip =1;
    for (int i= skip; ; i++) {
      String j = js.get(i);
      if (j==null) break;
      // try to find matching jurisdiction
      jurisdiction = Jurisdiction.get(ged.getCollator(), j);
      if (jurisdiction!=null)  {
        skip = i;
        break;
      }
    }

    // continue looking for country if available
    Locale locale = place.getGedcom().getLocale();
    for (int i=skip; ; i++) {
      String j = js.get(i);
      if (j==null) break;
      country = Country.get(ged.getLocale(), j);
      if (country!=null) 
        break;
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
    return equals(this.city, that.city) && equals(this.jurisdiction, that.jurisdiction) && equals(this.country, that.country);
  }
  
  private static boolean equals(Object o1, Object o2) {
    if (o1==null&&o2==null)
      return true;
    if (o1==null||o2==null)
      return false;
    return o1.equals(o2);
  }

  /**
   * City (never null)
   */
  public String getCity() {
    return city;
  }

  /**
   * Identified Top Level Jurisdiction
   */
  public Jurisdiction getJurisdiction() {
    return jurisdiction;
  }

  /**
   * Country or null
   */
  public Country  getCountry() {
    return country;
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
    if (jurisdiction!=null) result.append(jurisdiction.getDisplayName());
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
  
}
