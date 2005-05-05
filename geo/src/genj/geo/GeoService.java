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
import genj.util.Debug;
import genj.util.EnvironmentChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A service for geographical computations / data services
 */
public class GeoService {
  
  private static final String 
    GEO_DIR = "./geo",
    GAZETTEER_SUFFIX = ".gzt";

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /** databases */
  private List gazetteers;
  
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
   * Calculate geo directory
   */
  private File calcGeoDir() {
    return new File(EnvironmentChecker.getProperty(GeoService.class,
        "user.home/.genj/geo",
        GEO_DIR,
        "calculate dir for geometric database"
      ));
  }

  /**
   * Create a gazetteer for given country and state
   */
  public void createGazetteer(String country, String state) {
    
  }

  /**
   * Return all available gazetteers
   */
  public synchronized Gazetteer[] getGazetteers() {

    // don't have it yet?
    if (gazetteers==null) {
        
      gazetteers = new ArrayList();
      
      // check 'em files
      File[] files = calcGeoDir().listFiles();
      for (int i=0;i<files.length;i++) {
        File file = files[i];
        if (file.getName().endsWith(GAZETTEER_SUFFIX))
          gazetteers.add(new Gazetteer(file));
      }
    
      // got it now
    }
    
    // done
    return (Gazetteer[])gazetteers.toArray(new Gazetteer[gazetteers.size()]);
  }
  
  /**
   * Return a suitable geographic location for given property. Any
   * property that has a nested PLACe or ADDRess is a suitable
   * argument.
   * @param property property to analyze
   * @return array of suitable locations
   */
  public GeoLocation[] getLocations(Property property) {
    
    return new GeoLocation[0];
    
  }
  
  /**
   * Available Maps
   */
  public synchronized GeoMap[] getMaps() {
    // know all maps already?
    if (maps==null) {
      
      maps = new ArrayList();
      
      // look em up in file system
      File dir = new File(EnvironmentChecker.getProperty(this, "genj.geo.dir", GEO_DIR, "Looking for map directory"));
      if (dir.exists()) {
        // loop over files and directories in ./geo or ${genj.geo.dir}
        File[] files = dir.listFiles();
        for (int i=0;i<files.length;i++) {
          // only directories - later zip files as well
          if (!files[i].isDirectory())
            continue;
          // 20050504 don't consider directory 'CVS'
          if (files[i].getName().equals("CVS"))
            continue;
          // add it to available maps
          try {
            maps.add(new GeoMap(files[i]));
          } catch (Throwable t) {
            Debug.log(Debug.WARNING, this, "problem reading map from "+files[i], t);
          }
          // next
        }
      }
      
      // finished looking for maps
    }
    
    // done
    return (GeoMap[])maps.toArray(new GeoMap[maps.size()]);
  }
  
  /**
   * Geographical dictionary with places and coordinates
   */
  public class Gazetteer  implements Comparable {
    
    /** unique key, country and state id, name */
    private String key, country, state, name;
    
    /** constructor */
    /*package*/ Gazetteer(File file) {

      // analyze filename
      String filename = file.getName();
      assert filename.endsWith(GAZETTEER_SUFFIX) : "need gazetteer file";
      key = filename.substring(0, filename.length()-GAZETTEER_SUFFIX.length());
      
      // parse for country and state part
      int i = key.indexOf('_');
      assert i <0 || i>0 : "strange filename - should be country[_state].gzt";
      if (i>0) { 
        country = key.substring(0, i);
        state = key.substring(i+1);
      } else {
        country = key;
        state = null;
      }
      
      // init a display name
      name = new Locale("en", country).getDisplayCountry();
      
      // done
    }
    
    /** string representation */
    public String toString() {
      return name;
    }
    
    /** comparison */
    public int compareTo(Object o) {
      Gazetteer that = (Gazetteer)o;
      return this.name.compareTo(that.name);
    }
  } //Gazetteer
    
} //GeoService
