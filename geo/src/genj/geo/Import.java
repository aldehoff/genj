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

import genj.util.DirectAccessTokenizer;
import genj.util.Trackable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Importer of geographic information
 */
public abstract class Import implements Trackable {
  
  /** number of lines expected/written */
  protected long linesExpected = -1;
  private long linesWritten = 0;
  
  /** worker thread */
  private Thread worker;
  
  /** key country[_state] */
  private String key;
  
  /** a prepared statement for inserts */
  private PreparedStatement insert;
  
  /** operated country/state */
  private Country country;
  protected String state;
  
  /** constructor */
  protected Import(Country country, String state) throws SQLException {
    this.key = (state!=null ? country.getCode()+"_"+state : country.getCode());
    this.country = country;
  }
  
  /**
   * lookup
   */
  /*package*/ static Import get(Country country, String state) throws SQLException {
    
    // standardize to lower case
    if (state!=null) {
      state = state.trim().toLowerCase();
      if (state.length()==0) state=null;
    }
    
    // use NGA for everything but US
    return country.getCode().equals("us") ? (Import)new USGSImport(state) : new NGAImport(country);
    
  }
  
  /** trackable callback - cancel */
  public synchronized void cancel() {
    if (worker!=null) {
      worker.interrupt();
      worker = null;
    }
  }
  
  /** trackable callback - state */
  public String getState() {
    return "Importing";
  }
  
  /** trackable callback - current progress */
  public int getProgress() {
    if (linesExpected<=0)
      return 0;
    return (int)Math.min(100, linesWritten*100/linesExpected);
  }
  
  /** perform the import  */
  public final void run() throws IOException {
    
    // keep current thread as worker
    worker = Thread.currentThread();
    
    // do the io
    InputStream in = null;
    try {

      // remove old locations for country
      Connection connection = GeoService.getInstance().getConnection();
      PreparedStatement delete;
      if (state!=null) {
        delete = connection.prepareStatement(GeoService.DELETE_LOCATIONS2);
        delete.setString(GeoService.DELETE_LOCATIONS_STATE, state);
      } else {
        delete = connection.prepareStatement(GeoService.DELETE_LOCATIONS);
      }
      delete.setString(GeoService.DELETE_LOCATIONS_COUNTRY, country.getCode());
      delete.executeUpdate();
      delete.close();
      
      // prepare insert
      insert = connection.prepareStatement(GeoService.INSERT_LOCATION);
      
      // open input
      URL url = getURL();
      GeoView.LOG.info( "Importing "+key+" from "+url);
      URLConnection con = url.openConnection();
      in = con.getInputStream();
      
      // always zipped - first entry is content
      ZipInputStream zin = new ZipInputStream(in);
      ZipEntry entry =  zin.getNextEntry();
      linesExpected = entry.getSize() / 160;

      // let implementation do its thing
      parse(new BufferedReader(new InputStreamReader(zin, "UTF-8")));
      
      // cleanup
      insert.close();
      
      // add the country (ignore error)
      try {
        PreparedStatement newcountry = connection.prepareStatement(GeoService.INSERT_COUNTRY);
        newcountry.setString(GeoService.INSERT_COUNTRY_COUNTRY, country.getCode());
        newcountry.executeUpdate();
        newcountry.close();
      } catch (SQLException s) {
      }
      
    } catch (SQLException e) {
      throw new IOException("error committing imported data ["+e.getMessage()+"]");
    } finally {
      if (in!=null) in.close();
      
      GeoService.getInstance().fireGeoDataChanged();
    }
    
    // done
  }
  
  /** implementation dependent url */
  protected abstract URL getURL() throws IOException;
  
  /** implementation dependent parse */
  protected abstract void parse(BufferedReader in) throws IOException, SQLException;
  
  /** declare one skipped line of information */
  protected void skip() {
    if (linesExpected>2) linesExpected--;
  }
  
  /** write one line of information */
  protected void write(String city, String state, String country, float lat, float lon) throws IOException, SQLException {
    
    // interrupted?
    if (worker==null)
      throw new IOException("Import Cancelled");

    // insert
    insert.setString(GeoService.INSERT_LOCATION_CITY, city);
    insert.setString(GeoService.INSERT_LOCATION_STATE, state);
    insert.setString(GeoService.INSERT_LOCATION_COUNTRY, country);
    insert.setFloat(GeoService.INSERT_LOCATION_LAT, lat);
    insert.setFloat(GeoService.INSERT_LOCATION_LON, lon);
    insert.executeUpdate();
    
    // done
    linesWritten++;
  }
  
  /**
   * An implementation to write location names/coordinates (us) grabbed from USGS
   * http://geonames.usgs.gov/gnishome.html
   * http://geonames.usgs.gov/geonames/stategaz/index.html
   */
  private static class USGSImport extends Import {
    
    private final static String 
      URL = "http://geonames.usgs.gov/geonames/stategaz/XX_DECI.zip";
      
    /** constructor */
    private USGSImport(String state) throws SQLException {
      super(Country.get("us"), state);
      if (state==null||state.length()==0)
        throw new IllegalArgumentException("US State is required for USGS import");
      this.state = state;
    }
    
    /** our URL */
    protected URL getURL() throws IOException {
        return new URL(URL.replaceFirst("XX", state.toUpperCase()));
    }
    
    /** parse USGS lines */
    protected void parse(BufferedReader in) throws IOException, SQLException {
      
      String name;
      float lat, lon;
      
      while (true) {
        // next line
        String line = in.readLine();
        if (line==null) 
          return;
        
        // FID state (name) (type) county state# county# (lat) (lon) lat lon dmslat dmslon dmslat dmslon elev poption fedstat cell
        DirectAccessTokenizer values = new DirectAccessTokenizer(line, "|");
  
        // grab name
        name = values.get(2);
        
        // look for 'populated areas' only
        if (!"ppl".equals(values.get(3))) {
          skip();
          continue;
        }
        
        // grab lat lon
        try {
          String 
            sLat = values.get(9),
            sLon = values.get(10); 
          if (sLat.length()==0||"UNKNOWN".equals(sLat)||sLon.length()==0||"UNKNOWN".equals(sLon)) {
            skip();
            continue;
          }
          lat = Float.parseFloat(sLat); // LAT
          lon = Float.parseFloat(sLon); // LON
        } catch (NumberFormatException e) {
          throw new IOException("Format problem - expected to find lat/lon");
        }
  
        // keep it
        write(name, state, "us", lat, lon);
      }
  
      // done
    }
  }
  
  /**
   * An implementation to write location names/coordinates (non-us) grabbed from NGA
   * http://gnswww.nga.mil/geonames/GNS/index.jsp
   * http://www.nga.mil/gns/html/cntry_files.html
   */
  private static class NGAImport extends Import {
    
    private final static String 
      URL = "http://earth-info.nga.mil/gns/html/cntyfile/COUNTRY.zip";
    
    private Country country;
    
    /** constructor */
    private NGAImport(Country country) throws SQLException {
      super(country, null);
      this.country = country;
    }
  
    protected URL getURL() throws IOException {
        return new URL(URL.replaceFirst("COUNTRY", country.getFips().toLowerCase()));
    }
    
    /** parse NGA lines */
    protected void parse(BufferedReader in) throws IOException, SQLException  {
      
      // skip header
      in.readLine();
      
      String city, state;
      float lat, lon;
      
      while (true) {
        // next line
        String line = in.readLine();
        if (line==null) 
          return;
  
        // RC UFI UNI (LAT) (LON) DMSLAT DMSLON UTM JOG (FC) DSG PC (CC1) (ADM1) ADM2 DIM CC2 NT LC SHORT_FORM GENERIC SORT_NAME (NAME) FULL MOD
        DirectAccessTokenizer values = new DirectAccessTokenizer(line, "\t");
        
        try {
          lat = Float.parseFloat(values.get(3)); // LAT
          lon = Float.parseFloat(values.get(4)); // LON
        } catch (NumberFormatException e) {
          throw new IOException("Format problem - expected to find lat/lon");
        }
  
        // look for 'populated areas' only
        if ('P'!=values.get(9).charAt(0)) {
            skip();
          continue;
        }
        
        // and check country
        if (!country.getFips().equalsIgnoreCase(values.get(12))) {
          skip();
          continue;
        }
        
        // grab state
        state = values.get(13);
        if (state.length()>2) {
          skip();
          continue;
        }        
        
        // grab name
        city = values.get(22); // FULL_NAME
        
        // keep it
        write(city, state, country.getCode(), lat, lon);
      }
  
      // done
    }
  
  } //NGAImport
  
} //Import

