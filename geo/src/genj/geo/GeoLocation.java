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
import genj.util.WordBuffer;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
  private boolean init(Property prop) {
    
    //  FIXME add support for jurisdictions
    
    // got a place?
    Property plac = prop.getProperty("PLAC");
    if (plac instanceof PropertyPlace)
       return initFromPlace((PropertyPlace)plac);
    
    // an address?
    Property addr = prop.getProperty("ADDR");
    if (addr!=null)
      return initFromAddress(addr);
    
    // hmm
    throw new IllegalArgumentException("can't locate "+prop.getTag()+" "+prop);
  }
  
  /**
   * Init for Address
   */
  private boolean initFromAddress(Property addr) {
    
    // got a city?
    Property pcity = addr.getProperty("CITY");
    if (pcity==null)
      throw new IllegalArgumentException("can't determine city from address");

    // trim it
    this.city = trim(pcity.getDisplayValue());
    
    // empty?
    if (this.city==null)
      throw new IllegalArgumentException("address without city value");
    
    // got a state?
    Property state = addr.getProperty("STAE");
    if (state!=null) 
      this.state = trim(state.getDisplayValue());
    
    // how about a country?
    Property pcountry = addr.getProperty("CTRY");
    if (pcountry!=null) 
      this.country = pcountry.getValue();
    
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
    
    // trying 2nd jurisdication as state
    state = place.getJurisdiction(1);
    
    // done
    return true;
  }
  
  /**
   * trim a value - some folks add stuff to (e.g.) a city that we don't want to use in locations 
   */
  private String trim(String value) {
    // null?
    if (value==null)
      return null;
    // check for '(' and trim
    int i = value.indexOf('(');
    if (i>=0)
      value = value.substring(0,i);
    value = value.trim();
    // done
    return value.length() == 0 ? null : value;
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
    int hash = 0;
    if (city!=null) hash += city.hashCode();
    if (state!=null) hash += state.hashCode();
    if (country!=null) hash += country.hashCode();
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
    return a.equals(b);
  }

  /**
   * City (never null)
   */
  public String getCity() {
    return city;
  }

  /**
   * State or null
   */
  public String getState() {
    return state;
  }

  /**
   * Country or null
   */
  public String getCountry() {
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
    return this.city.compareTo(that.city);
  }
  
}
