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
import genj.util.Resources;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


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
    DELETE_COUNTRY = "DELETE FROM countries WHERE country = ?",
    INSERT_LOCATION = "INSERT INTO locations (city, state, country, lat, lon) VALUES (?, ?, ?, ?, ?)",
    SELECT_COUNTRIES = "SELECT country FROM countries",
    SELECT_LOCATIONS_WITH_CITY = "SELECT  lat, lon FROM locations WHERE city = ?",
    SELECT_LOCATIONS_WITH_CITYSTATE  = "SELECT lat, lon FROM locations WHERE city = ? AND state = ?";

  /*package*/ static final int
    DELETE_LOCATIONS_COUNTRY = 1,
    
    INSERT_COUNTRY_COUNTRY = 1,
    DELETE_COUNTRY_COUNTRY = 1,
    
    INSERT_LOCATION_CITY = 1,
    INSERT_LOCATION_STATE = 2,
    INSERT_LOCATION_COUNTRY = 3,
    INSERT_LOCATION_LAT = 4,
    INSERT_LOCATION_LON = 5,
    
    SELECT_COUNTRIES_OUT_COUNTRY = 1,
    
    SELECT_LOCATIONS_IN_CITY = 1,
    SELECT_LOCATIONS_IN_STATE = 2,
    SELECT_LOCATIONS_OUT_LAT = 1,
    SELECT_LOCATIONS_OUT_LON = 2;

  /** states to state-codes */
  private Map state2code = new HashMap();
  
  /** directories */
  private File localDir, globalDir;

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /** database ready */
  private Connection connection;
  
  /**
   * Constructor
   */
  private GeoService() {
    
    // startup now
    startup();
    
    // prepare database shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));

    // init gns information
    initStateCodes();
    
    // done
  }
  
  /**
   * our startup routine
   */
  private synchronized void startup() { 
    // startup database
    File geo =  new File(EnvironmentChecker.getProperty(this,"user.home/.genj/geo/database", "", "looking for user's geo directory"));
    geo.getParentFile().mkdir();

    try {
      
      Debug.log(Debug.INFO, GeoService.this, "GeoService Startup");
      
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
      
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this, "Couldn't initialize database", t);
    }
  } 
  
  /**
   * our shutdown
   */
  private class Shutdown implements Runnable {
      public void run() { synchronized(GeoService.this) {
          Debug.log(Debug.INFO, GeoService.this, "GeoService Shutdown");
          try {
            connection.createStatement().execute("SHUTDOWN");
          } catch (SQLException e) {
            // ignored
          }
      } }
  } //Shutdown
  
  /**
   * Initialize our state codes
   */
  private void initStateCodes() {

    // try to find states.properties
    File[] files = getGeoFiles();
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
          for (StringTokenizer names = new StringTokenizer(meta.getString(key), ","); names.hasMoreTokens(); )
            state2code.put(names.nextToken().trim(), code);
        }
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this, t);
      }
      
      // next
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
    
    String city = "Celle";
    GeoService gs = getInstance();
    try {
      
      PreparedStatement ps = gs.connection.prepareStatement("SELECT city, state, country, lat, lon FROM locations WHERE city LIKE  ?");
      ps.setString(1, city);
      ResultSet result = ps.executeQuery();
      while (result.next()) {
        System.out.println( result.getString(1)  +","+ result.getString(2)+","+ result.getString(3)+","+ result.getString(4)+","+ result.getString(5));
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

    // global files
    if (globalDir.exists()) 
      result.addAll(Arrays.asList(globalDir.listFiles()));

    // local files
    if (localDir.exists())
      result.addAll(Arrays.asList(localDir.listFiles()));

    // done
    return (File[])result.toArray(new File[result.size()]);
  }
  
  /**
   * Drop information for given country 
   */
  public synchronized void drop(Country country) throws IOException {
    
    try {
      PreparedStatement delete = connection.prepareStatement(DELETE_LOCATIONS);
      delete.setString(DELETE_LOCATIONS_COUNTRY, country.getCode());
      delete.executeUpdate();
      
      delete = connection.prepareStatement(DELETE_COUNTRY);
      delete.setString(DELETE_COUNTRY_COUNTRY, country.getCode());
      delete.executeUpdate();
      
    } catch (SQLException e) {
      throw new IOException(e.getMessage());
    }
    
    // done
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
   * Return all available countries
   */
  public synchronized Country[] getCountries() {

    List countries = new ArrayList();
    
    try {
      ResultSet rows = connection.prepareStatement(SELECT_COUNTRIES).executeQuery();
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
  public boolean match(GeoLocation location) {

    String city = location.getCity();
    String state = (String)state2code.get(location.getState());
   
    // try to find 
    int matches = 0;
    float lat = Float.NaN, lon = Float.NaN;
    synchronized (this) {
      try {
        
        // prepare appropriate select
        PreparedStatement select;
        if (state!=null)  {
          select = connection.prepareStatement(SELECT_LOCATIONS_WITH_CITYSTATE);
          select.setString(SELECT_LOCATIONS_IN_STATE, state);
        } else {
          select = connection.prepareStatement(SELECT_LOCATIONS_WITH_CITY);
        }
        select.setString(SELECT_LOCATIONS_IN_CITY, city);

        // loop over rows
        ResultSet rows = select.executeQuery();
        while (rows.next()) {
          // grab lat/lon
          if (Float.isNaN(lat)) {
            lat = rows.getFloat(SELECT_LOCATIONS_OUT_LAT);
            lon = rows.getFloat(SELECT_LOCATIONS_OUT_LON);
          }
          matches ++;
        }
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this, "throwable while trying to match "+location, t);
      }
    }
    
    // set it
    location.set(lat, lon, matches);

    // not found
    return false;
  }
  
  /**
   * Available Maps
   */
  public synchronized GeoMap[] getMaps() {
    
    
instance.initStateCodes();    

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
