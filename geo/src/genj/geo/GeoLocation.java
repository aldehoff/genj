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
package genj.geo;



/**
 *  Information about a geographic location
 */
public class GeoLocation implements Comparable {
   
  /** state */
  private String place, zip, state, country;
  private float lat, lon;
  private int score;
  
  /*package*/ GeoLocation(String place, String zip, String state, String country, int score) {
    this.place = place;
    this.zip = zip;
    this.state = state;
    this.country = country;
    this.score = score;
  }
  
  public String getPlace() {
    return place;
  }
  
  public String getZip() {
    return zip;
  }
  
  public String getState() {
    return state;
  }
  
  public String getCountry() {
    return country;
  }
  
  public float getLatitude() {
    return lat;
  }
  
  public float getLongitude() {
    return lon;
  }
  
  public String toString() {
    return place;
  }
  
  public int compareTo(Object o) {
    GeoLocation that = (GeoLocation)o;
    return this.score-that.score;
  }
}
