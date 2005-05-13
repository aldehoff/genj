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

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;

import java.util.ArrayList;
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
public class GeoLocation extends Point implements Feature {

  /** 
   * our schema - could be more complicated like
   * schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
   * schema.addAttribute("PLAC", AttributeType.STRING);
   */
  /*package*/ final static FeatureSchema SCHEMA = new FeatureSchema();
  
  private final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
  
  private Coordinate coordinate;

  private String city, state, country;
  
  /** properties at that location */
  private List properties = new ArrayList();
  
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
    
    // FIXME add support for ADDR, CITY, STAE, CNTY
    // FIXME add support for jurisdications
    // got a place?
    Property plac = prop.getProperty("PLAC");
    if (plac instanceof PropertyPlace)
       init((PropertyPlace)plac);
    else
      throw new IllegalArgumentException("can't locate "+prop.getTag()+" "+prop);
  }
  
  /**
   * Add poperties from another instance
   */
  public void add(GeoLocation other) {
    properties.addAll(other.properties);
  }
  
  /**
   * Init for PropertyPlace
   */
  private void init(PropertyPlace place) {
    
    // simple - first jurisdiction
    city = place.getJurisdiction(0);
    if (city==null)
      throw new IllegalArgumentException("can't determine location for "+place);
    
    // check for '(' and trim
    int i = city.indexOf('(');
    if (i>=0)
      city = city.substring(0,i);
    city = city.trim();
    
    // empty?
    if (city.length()==0)
      throw new IllegalArgumentException("can't determine location for "+place);

    // done
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
   * Country or null
   */
  public Country getCountry() {
    return null;
  }
  
  /**
   * Set location lat,lon
   */
  protected void set(double lat, double lon) {
    coordinate.x = lon;
    coordinate.y = lat;
  }
  
  /**
   * String representation
   */
  public String toString() {
     return city + "[" + coordinate.y + "," +  coordinate.x+ "]";
  }
  
  /** 
   * Check - valid or not?
   */
  public boolean isValid() {
    return !Double.isNaN(coordinate.x) && !Double.isNaN(coordinate.y);
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
    throw new IllegalArgumentException();
  }
  
}
