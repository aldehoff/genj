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
import genj.util.EnvironmentChecker;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A service for geographical computations / data services. It keeps well known
 * locations in files in ./geo with format
 * <pre>
 *   PLACE \t STATE \t COUNTRY \t LAT \t LON
 * </pre>
 */
public class GeoService {
  
  /** our work directory */
  private static final String GEO_DIR = "./geo";
  
  /** our sqls */
  /*package*/ static final String 
    CREATE_TABLES =
      "CREATE CACHED TABLE countries (country CHAR(2) PRIMARY KEY); " +
      "CREATE CACHED TABLE locations (city VARCHAR(32), state CHAR(2), country CHAR(2) NOT NULL, lat FLOAT, lon FLOAT); "+
      "CREATE INDEX cities ON locations (city)",
    DELETE_LOCATIONS = "DELETE FROM locations WHERE country = ?",
    INSERT_COUNTRY = "INSERT INTO countries (country) VALUES (?)",
    INSERT_LOCATION = "INSERT INTO locations (city, state, country, lat, lon) VALUES (?, ?, ?, ?, ?)",
    SELECT_COUNTRIES = "SELECT country FROM countries",
    SELECT_LOCATIONS = "SELECT state, country, lat, lon FROM locations WHERE city = ?";

  /*package*/ static final int
    DELETE_LOCATIONS_IN_COUNTRY = 1,
    
    INSERT_COUNTRY_IN_COUNTRY = 1,
    
    INSERT_LOCATION_IN_CITY = 1,
    INSERT_LOCATION_IN_STATE = 2,
    INSERT_LOCATION_IN_COUNTRY = 3,
    INSERT_LOCATION_IN_LAT = 4,
    INSERT_LOCATION_IN_LON = 5,
    
    SELECT_COUNTRIES_OUT_COUNTRY = 1,
    
    SELECT_LOCATIONS_IN_CITY = 1,
    SELECT_LOCATIONS_OUT_STATE = 1,
    SELECT_LOCATIONS_OUT_COUNTRY = 2,
    SELECT_LOCATIONS_OUT_LAT = 3,
    SELECT_LOCATIONS_OUT_LON = 4;

  /** cached matches */
  private Map pattern2match = new HashMap();
  
  /** directories */
  private File localDir, globalDir;

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /** database ready */
  private Connection connection;
  private PreparedStatement selectLocations, selectCountries;
  
  /**
   * Constructor
   */
  private GeoService() {

    File geo =  new File(EnvironmentChecker.getProperty(this,"user.home/.genj/geo/database", "", "looking for user's geo directory"));
    geo.getParentFile().mkdir();

    try {
      // initialize database
      Class.forName("org.hsqldb.jdbcDriver");
  
      // connect to the database.   
      connection = DriverManager.getConnection("jdbc:hsqldb:file:"+geo.getAbsolutePath(), "sa",""); 
      connection.setAutoCommit(true);

      Statement statement = connection.createStatement();
      statement.execute("SET PROPERTY \"hsqldb.cache_scale\" 8");  // less rows 3*2^x
      statement.execute("SET PROPERTY \"hsqldb.cache_size_scale\" 7"); // less size per row 2^x
            
      // create tables
      try {
        connection.createStatement().executeUpdate(CREATE_TABLES);
      } catch (SQLException e) {
        // ignored
      }
      
      // prepare prepared statements
      selectLocations = connection.prepareStatement(SELECT_LOCATIONS);
      selectCountries = connection.prepareStatement(SELECT_COUNTRIES);
      
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this, "Couldn't initialize database", t);
    }
    
    // done
  }
  
  /**
   * Test
   */
  public static void main(String[] args) {
    
//    Country[] countries = getInstance().getCountries();
//    for (int i=0;i<countries.length;i++)
//      System.out.println(countries[i]);
    
//    Indi indi = new Indi();
//    Property birt = indi.addProperty("BIRT", "");
//    birt.addProperty("PLAC", "Nantes");
//    
//    GeoLocation loc  = new GeoLocation(birt);
//    getInstance().match(loc);
//    System.out.println(loc);
    
    String city = "Nantes";
    GeoService gs = getInstance();
    try {
      gs.selectLocations.setString(SELECT_LOCATIONS_IN_CITY, city);
      ResultSet result = gs.selectLocations.executeQuery();
      while (result.next()) {
        System.out.println( result.getString(SELECT_LOCATIONS_OUT_STATE) 
            +","+ result.getString(SELECT_LOCATIONS_OUT_COUNTRY)
            +","+ result.getFloat(SELECT_LOCATIONS_OUT_LAT)
            +","+ result.getFloat(SELECT_LOCATIONS_OUT_LON)
          );
      }
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, gs, t);
    }
    
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
   * Return the database connection
   */
  /*package*/ Connection getConnection() {
    return connection;
  }
  
  /**
   * Lookup Files in Geo directories
   */
  private File[] getGeoFiles() {
    
    List result = new ArrayList();
    
    // find directories
    if (localDir==null) {
      localDir =  new File(EnvironmentChecker.getProperty(this,"user.home/.genj/geo", "", "looking for locale geo files"));
      globalDir = new File(EnvironmentChecker.getProperty(this, "genj.geo.dir", GEO_DIR, "Looking for map directory"));
    }

    // local files
    if (localDir.exists())
      result.addAll(Arrays.asList(localDir.listFiles()));

    // global files
    if (globalDir.exists()) 
      result.addAll(Arrays.asList(globalDir.listFiles()));

    // done
    return (File[])result.toArray(new File[result.size()]);
  }
  
  /**
   * Prepare an import 
   */
  public Import getImport(Country country, String state) throws IOException {
    // look it up
    try {
      return Import.get(country, state);
    } catch (SQLException e) {
      throw new IOException("error preparing import ["+e.getMessage()+"]");
    }
  }

  /**
   * Checks for country coverage
   */
  public synchronized boolean covers(Country country) {
    // FIXME check
    return true;
  }
  
  /**
   * Return all available countries
   */
  public synchronized Country[] getCountries() {

    List countries = new ArrayList();
    
    try {
      ResultSet rows = selectCountries.executeQuery();
      while (rows.next()) 
        countries.add(Country.get(rows.getString(SELECT_COUNTRIES_OUT_COUNTRY)));
      
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this, t);
    }
    
    // done
    return (Country[])countries.toArray(new Country[countries.size()]);
  }
  
  /**
   * Find all suitable locations for given location 
   * @param location
   * @return array of suitable locations
   */
  public GeoLocation[] findAll(GeoLocation location) {
    return new GeoLocation[0];
  }
  
  /**
   * Locate given location
   */
  public synchronized boolean match(GeoLocation location) {

    float lat = Float.NaN, lon = Float.NaN;
    
    // try to find 
    try {
      selectLocations.setString(SELECT_LOCATIONS_IN_CITY, location.getCity());
      ResultSet result = selectLocations.executeQuery();
      if (result.next()) {
        lat = result.getFloat(SELECT_LOCATIONS_OUT_LAT);
        lon = result.getFloat(SELECT_LOCATIONS_OUT_LON);
      }
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this, "throwable on looking for "+location, t);
    }
    
    // set it
    location.set(lat, lon);

    // not found
    return false;
  }
  
  /**
   * Available Maps
   */
  public synchronized GeoMap[] getMaps() {
    // know all maps already?
    if (maps==null) {
      
      maps = new ArrayList();
      
      // loop over files 
      File[] files = getGeoFiles();
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
      
      // finished looking for maps
    }
    
    // done
    return (GeoMap[])maps.toArray(new GeoMap[maps.size()]);
  }
    
  
} //GeoService
