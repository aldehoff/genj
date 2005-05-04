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

import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A service for geographical computations / data services
 */
public class GeoService {

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /**
   * Constructor
   */
  private GeoService() {
    
  }
  
  /**
   * Singleton acces
   */
  public static GeoService getInstance() {
    if (instance==null) {
      synchronized (GeoService.class) {
        if (instance==null) 
          instance = new GeoService();
      }
    }
    return instance;
  }
  
  /**
   * Return a suitable location for given property
   * @param placeOrAddr place or address
   */
  public GeoLocation[] getLocations(Property placeOrAddr) {
    
    // a place?
    if (placeOrAddr instanceof PropertyPlace)
      return getLocationsForPlace((PropertyPlace)placeOrAddr);
    
    // an address?
    if (placeOrAddr.getTag().equals("ADDR"))
      return getLocationsForAddress(placeOrAddr);
    
    // n/a
    throw new IllegalArgumentException("either place or address argument required");
  }
  
  /**
   * Return a suitable location for given property
   * @param place the place
   */
  public GeoLocation[] getLocationsForPlace(PropertyPlace place) {
    return new GeoLocation[0];
  }
  
  /**
   * Return a suitable location for given property
   * @param address the address
   */
  public GeoLocation[] getLocationsForAddress(Property address) {
    return new GeoLocation[0];
  }
  
  /**
   * Available Maps
   */
  public synchronized GeoMap[] getMaps() {
    // known?
    if (maps==null) {
      maps = new ArrayList();
      // look em up in file system
      File dir = new File(EnvironmentChecker.getProperty(this, "genj.geo.dir", "./geo", "Looking for map directory"));
      if (dir.exists()) {
        File[] files = dir.listFiles();
        for (int i=0;i<files.length;i++) 
          if (!files[i].getName().equals("CVS")) 
            maps.add(new GeoMap(files[i]));
      }
    }
    return (GeoMap[])maps.toArray(new GeoMap[maps.size()]);
  }
    
} //GeoService
