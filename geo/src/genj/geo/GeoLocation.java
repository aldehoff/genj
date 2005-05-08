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

import java.util.regex.Pattern;

/**
 *  Information about a geographic location
 */
public class GeoLocation {

  /** state */
  private Property property;
  private float lat = Float.NaN, lon = Float.NaN;
  
  public GeoLocation(Property prop) {
    // remember
    property = prop;
    // test
    getPattern();
  }
  
  /**
   * Resolve pattern for place comparison
   */
  /*package*/ Pattern getPattern() {
    
    // FIXME add support for ADDR, CITY, STAE, CNTY
    // FIXME add support for jurisdications
    // got a place?
    Property plac = property.getProperty("PLAC");
    if (plac instanceof PropertyPlace)
      return getPattern((PropertyPlace)plac);
    
    throw new IllegalArgumentException("can't create matcher for "+property.getTag()+" "+property);
  }

  /**
   * Resolve pattern for place comparison
   */
  private Pattern getPattern(PropertyPlace place) {
    String city = place.getJurisdiction(0);
    if (city==null||city.length()==0)
      throw new IllegalArgumentException("can't determine location for "+place);
    return Pattern.compile("^"+city+"\t");
  }

  /**
   * Set location lat,lon
   */
  protected void set(float lat, float lon) {
    this.lat = lat;
    this.lon = lon;
  }
  
  /**
   * String representation
   */
  public String toString() {
     return getPattern().pattern() + "[" + lat + "," +  lon+ "]";
  }
  
} //GeoLocation
