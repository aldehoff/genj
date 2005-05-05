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
import genj.util.DirectAccessTokenizer;
import genj.util.EnvironmentChecker;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipInputStream;


/**
 * A service for geographical computations / data services
 */
public class GeoService {
  
//  /*package*/ final static int
//  PLACE = 0,
//  ZIP = 1,
//  STATE = 2,
//  COUNTRY = 3,
//  LAT = 4,
//  LON = 5,
//  NUM_CRITERIAS = 4;
  
  private static final Charset UTF8 = Charset.forName("UTF-8");
  
  private static final String 
    GEO_DIR = "./geo",
    GAZETTEER_SUFFIX = ".gzt",
    FORMAT = "PLACE\tZIP\tSTATE\tCOUNTRY\tLAT\tLON";

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
  public void createGazetteer(String country, String state) throws IOException {
    
    // standardize to lower case
    country = country.trim().toLowerCase();
    if (state!=null) {
      state = state.trim().toLowerCase();
      if (state.length()==0) state=null;
    }
    
    // check at least country
    if (country.length()==0)
      throw new IllegalArgumentException("Country can't be empty");
    
    // find directory to store data in
    File dir = calcGeoDir();
    File file = new File(dir, (state!=null ? country+"_"+state : country) +GAZETTEER_SUFFIX);
    
    // use NGA for everything but US
    Import im;
    if ("us".equals(country))
      im = new USGSImport(state);
    else
      im = new NGAImport(country, state);
    
    // let importer write all it can find
    try {
      im.write(file);
    } catch (Throwable t) {
      throw new IOException("Geo Import failed ["+t.getMessage()+"]");
    }

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
    
  /**
   * Importer of geographic information
   */
  /*package*/ abstract static class Import {
    
    /** state */
    private BufferedWriter out;
    
    /** write to file */
    public final void write(File file) throws Throwable {
      // make sure the directory for file exists
      file.getParentFile().mkdirs();
      // open for output and continue with impl
      try {
        
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF8));
        write("place", "zip", "state", "country", "lat", "lon");
        write();
        
      } finally {
        if (out!=null) out.close();
      }
      
      // done
    }
    
    /** implementation dependent write */
    protected abstract void write() throws Throwable;
    
    /** write one line of information */
    protected void write(String place, String zip, String state, String country, float lat, float lon) throws IOException {
      write(place, zip, state, country, Float.toString(lat), Float.toString(lon));
    }
    protected void write(String place, String zip, String state, String country, String lat, String lon) throws IOException {

      assert FORMAT=="PLACE\tZIP\tSTATE\tCOUNTRY\tLAT\tLON" : "format problem";
      
      out.write(place); out.write('\t');
      out.write(zip); out.write('\t');
      out.write(state); out.write('\t');
      out.write(country); out.write('\t');
      out.write(lat); out.write('\t');
      out.write(lon); 
    }
  } //Import

  /**
   * An implementation to write location names/coordinates (us) grabbed from USGS
   * http://geonames.usgs.gov/gnishome.html
   * http://geonames.usgs.gov/geonames/stategaz/index.html
   */
  /*package*/ static class USGSImport extends Import {
    
    private final static String 
      URL = "http://geonames.usgs.gov/geonames/stategaz/XX_DECI.zip";
      
    /** state */
    private String state;
    
    /** constructor */
    public USGSImport(String state) throws IOException {
      if (state==null)
        throw new IOException("state is required for US import");
      this.state = state;
    }
    
    /** our implementation for writing info */
    protected void write() throws Throwable {
      
      Debug.log(Debug.INFO, GeoService.class, "Importing USGS state information for state "+state);
      
      // try to connect to provider URL 
      InputStream in = null;
      try {
        
        URL url = new URL(URL.replaceFirst("XX", state.toUpperCase()));
        in = url.openConnection().getInputStream();
        
        // read content
        ZipInputStream zin = new ZipInputStream(in);
        zin.getNextEntry();
        parse(new BufferedReader(new InputStreamReader(zin, UTF8)));
        
      } finally {
        if (in!=null) in.close();
      }
      
      // done
    }
    
    /** parse USGS lines */
    private void parse(BufferedReader in) throws IOException {
      
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
        if (!"ppl".equals(values.get(3)))
          continue;
        
        // grab lat lon
        try {
          String s = values.get(9); 
          if (s.length()==0||"UNKNOWN".equals(s)) continue;
          lat = Float.parseFloat(s); // LAT
          if (s.length()==0||"UNKNOWN".equals(s)) continue;
          lon = Float.parseFloat(s); // LON
        } catch (NumberFormatException e) {
          throw new IOException("Format problem - expected to find lat/lon");
        }
  
        // keep it
        write(name, null, state, "us", lat, lon);
      }
  
      // done
    }
  }

  /**
   * An implementation to write location names/coordinates (non-us) grabbed from NGA
   * http://gnswww.nga.mil/geonames/GNS/index.jsp
   * http://www.nga.mil/gns/html/cntry_files.html
   */
  /*package*/ static class NGAImport extends Import {
    
    private final static Resources ISO2FIPS = new Resources(NGAImport.class.getResourceAsStream("iso2fips.properties"));
    
    private final static String 
      URL = "http://earth-info.nga.mil/gns/html/cntyfile/COUNTRY.zip";
    
    private String fipsCountry, isoCountry, state;
    
    /** constructor */
    public NGAImport(String country, String state) {
     fipsCountry  = ISO2FIPS.getString(country);
     isoCountry = country;
     this.state = state;
    }
  
    protected void write() throws Throwable {
      
      Debug.log(Debug.INFO, GeoService.class, "Importing NGA country information for "+isoCountry+" (fips "+fipsCountry+")");
      
      // try to connect to provider URL 
      InputStream in = null;
      try {
        
        URL url = new URL(URL.replaceFirst("COUNTRY", fipsCountry));
        in = url.openConnection().getInputStream();
        
        // read content
        ZipInputStream zin = new ZipInputStream(in);
        zin.getNextEntry();
        parse(new BufferedReader(new InputStreamReader(zin, UTF8)));
        
      } finally {
        if (in!=null) in.close();
      }
      
      // done
    }
    
    /** parse NGA lines */
    private void parse(BufferedReader in) throws IOException {
      
      // skip header
      in.readLine();
      
      String name;
      float lat, lon;
      
      while (true) {
        // next line
        String line = in.readLine();
        if (line==null) 
          return;
  
        // RC UFI UNI (LAT) (LON) DMSLAT DMSLON UTM JOG (FC) DSG PC (CC1) ADM1 ADM2 DIM CC2 NT LC SHORT_FORM GENERIC SORT_NAME (NAME) FULL MOD
        DirectAccessTokenizer values = new DirectAccessTokenizer(line, "\t");
        
        try {
          lat = Float.parseFloat(values.get(3)); // LAT
          lon = Float.parseFloat(values.get(4)); // LON
        } catch (NumberFormatException e) {
          throw new IOException("Format problem - expected to find lat/lon");
        }
  
        // look for 'populated areas' only
        if ('P'!=values.get(9).charAt(0))
          continue;
        
        // and check country
        if (!fipsCountry.equalsIgnoreCase(values.get(12)))
          continue;
        
        // grab name
        name = values.get(22); // FULL_NAME
        
        // keep it
        write(name, null, null, isoCountry, lat, lon);
      }
  
      // done
    }
  
  } //NGAImport

} //GeoService
