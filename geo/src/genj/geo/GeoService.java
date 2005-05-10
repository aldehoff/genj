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
import genj.util.DirectAccessTokenizer;
import genj.util.EnvironmentChecker;
import genj.util.Trackable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * A service for geographical computations / data services. It keeps well known
 * locations in files in ./geo with format
 * <pre>
 *   PLACE \t STATE \t COUNTRY \t LAT \t LON
 * </pre>
 */
public class GeoService {
  
  private final static Pattern MATCH_LAT_LON = Pattern.compile("^.*\t.*\t.*\t(.*)\t(.*)$");
  
  private static final Charset UTF8 = Charset.forName("UTF-8");
  
  private static final String 
    GEO_DIR = "./geo",
    TMP_SUFFIX = ".tmp",
    GAZETTEER_SUFFIX = ".gzt";

  /** cached matches */
  private Map pattern2match = new HashMap();
  
  /** directories */
  private File localDir, globalDir;

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /** databases */
  private Set gazetteers;
  
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
  public Import getImport(Country country, String state) {
    
    // standardize to lower case
    if (state!=null) {
      state = state.trim().toLowerCase();
      if (state.length()==0) state=null;
    }
    
    // use NGA for everything but US
    Import im;
    if ("us".equals(country))
      im = new USGSImport(state);
    else
      im = new NGAImport(country, state);
    
    // done
    return im;
  }

  /**
   * Returns gazetteers for given country
   */
  public synchronized boolean hasGazetteer(Country country) {
    Gazetteer[] gzts = getGazetteers();
    for (int i = 0; i < gzts.length; i++) {
      if (gzts[i].getCountry().equals(country))
        return true;
    }
    return false;
  }
  
  /**
   * Return all available gazetteers
   */
  public synchronized Gazetteer[] getGazetteers() {

    // don't have it yet?
    if (gazetteers==null) {
        
      gazetteers = new HashSet();
      
      // check 'em files
      File[] files = getGeoFiles();
      for (int i=0;files!=null&&i<files.length;i++) {
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
    
    Pattern pattern = location.getPattern();
    
    // check cached information
    String cached = (String)pattern2match.get(pattern.pattern());
    if (cached!=null)
      return found(location, null, cached);

    // loop over all gazetteers
    File[] files = getGeoFiles();
    for (int i=0;i<files.length;i++) {
      if (!files[i].getName().endsWith(GAZETTEER_SUFFIX))
        continue;
      // check each
      if (match(location, pattern, files[i]))
        return true;
      // next
    }

    // not found
    return false;
  }
  
  private boolean match(GeoLocation location, Pattern pattern, File gazetteer) {
    
    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(gazetteer), UTF8));   
      String line;
      while ( (line=in.readLine()) !=null) {
        if (pattern.matcher(line).find()) 
          return found(location, pattern, line);
      }
    } catch (Throwable t) {
      Debug.log(Debug.ERROR, this, t);
    } finally {
      try { in.close(); } catch (Throwable t) {}
    }

    return false;
  }
  
  private boolean found(GeoLocation location, Pattern pattern, String line) {
    if (pattern!=null)
      pattern2match.put(pattern.pattern(), line);
    Matcher latlon = MATCH_LAT_LON.matcher(line);
    latlon.matches();
    location.set(Float.parseFloat(latlon.group(1)), Float.parseFloat(latlon.group(2)));
    return true;
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
  
  /**
   * Geographical dictionary with places and coordinates
   */
  public class Gazetteer  implements Comparable {
    
    /** unique key, country and state id, name */
    private String key, state;
    private Country country;
    
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
        country = Country.get(key.substring(0, i));
        state = key.substring(i+1);
      } else {
        country = Country.get(key);
        state = null;
      }
      
      // done
    }
    
    /** string representation */
    public String toString() {
      return state==null ? country.getName() : country.getName()+ " (" + state.toUpperCase() + ")";
    }
    
    /** comparison */
    public int compareTo(Object o) {
      Gazetteer that = (Gazetteer)o;
      return this.country.compareTo(that.country);
    }
    
    /** country */
    public Country getCountry() {
      return country;
    }
    
    /** identity comparison */
    public boolean equals(Object obj) {
      Gazetteer that = (Gazetteer)obj;
      return this.key.equals(that.key);
    }
    
    /** identity */
    public int hashCode() {
      return key.hashCode();
    }
  } //Gazetteer
    
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
    
    /** state */
    private BufferedWriter out;
    
    /** constructor */
    protected Import(String isoCountry, String state) {
      this.key = (state!=null ? isoCountry+"_"+state : isoCountry);
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
    public final Gazetteer run() throws IOException {
      
      // keep current thread as worker
      worker = Thread.currentThread();
      
      // make sure the directory for file exists
      File dir =  new File(EnvironmentChecker.getProperty(this,"user.home/.genj/geo", "", "geo import destination"));
      File tmp = new File(dir, key +TMP_SUFFIX);
      File dst = new File(dir, key +GAZETTEER_SUFFIX);
      
      // make sure we have the directories 
      tmp.getParentFile().mkdirs();
      
      // do the io
      InputStream in = null;
      try {
        
        // open input
        URL url = getURL();
        Debug.log(Debug.INFO, getClass(), "Importing "+key+" from "+url);
        URLConnection con = url.openConnection();
        in = con.getInputStream();
        
        // always zipped - first entry is content
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry =  zin.getNextEntry();
        linesExpected = entry.getSize() / 160;

        // open output
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmp), UTF8));
        write("place", "state", "country", "lat", "lon");
        
        // let implementation do its thing
        parse(new BufferedReader(new InputStreamReader(zin, UTF8)));
        
        // close out
        out .close();
        out = null;
        
        // copy to final filename
        Gazetteer result = new Gazetteer(dst);
        dst.delete();
        if (!tmp.renameTo(dst))
          throw new IOException("Couldn't create output file "+dst);
        
        // keep gazetteer
        synchronized (GeoService.this) {
          gazetteers.add(result);
        }
        return result;

      } finally {
        if (out!=null) out.close();
        if (in!=null) in.close();
      }
      
      // done
    }
    
    /** implementation dependent url */
    protected abstract URL getURL() throws IOException;
    
    /** implementation dependent parse */
    protected abstract void parse(BufferedReader in) throws IOException;
    
    /** declare one skipped line of information */
    protected void skip() {
      if (linesExpected>2) linesExpected--;
    }
    
    /** write one line of information */
    protected void write(String city, String state, String country, float lat, float lon) throws IOException {
      write(city, state, country, Float.toString(lat), Float.toString(lon));
    }
    protected void write(String city, String state, String country, String lat, String lon) throws IOException {
      
      // interrupted?
      if (worker==null)
        throw new IOException("Import Cancelled");

      out.write(city); out.write('\t');
      out.write(state); out.write('\t');
      out.write(country); out.write('\t');
      out.write(lat); out.write('\t');
      out.write(lon); 
      out.newLine();
      
      linesWritten++;
    }
    
  } //Import

  /**
   * An implementation to write location names/coordinates (us) grabbed from USGS
   * http://geonames.usgs.gov/gnishome.html
   * http://geonames.usgs.gov/geonames/stategaz/index.html
   */
  private class USGSImport extends Import {
    
    private final static String 
      URL = "http://geonames.usgs.gov/geonames/stategaz/XX_DECI.zip";
      
    /** state */
    private String state;
    
    /** constructor */
    private USGSImport(String state) {
      super("us", state);
      if (state==null||state.length()==0)
        throw new IllegalArgumentException("US State is required for USGS import");
      this.state = state;
    }
    
    /** our URL */
    protected URL getURL() throws IOException {
        return new URL(URL.replaceFirst("XX", state.toUpperCase()));
    }
    
    /** parse USGS lines */
    protected void parse(BufferedReader in) throws IOException {
      
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
          String s = values.get(9); 
          if (s.length()==0||"UNKNOWN".equals(s)) {
            skip();
            continue;
          }
          lat = Float.parseFloat(s); // LAT
          if (s.length()==0||"UNKNOWN".equals(s)) {
            skip();
            continue;
          }
          lon = Float.parseFloat(s); // LON
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
  private class NGAImport extends Import {
    
    private final static String 
      URL = "http://earth-info.nga.mil/gns/html/cntyfile/COUNTRY.zip";
    
    private Country country;
    private String state;
    
    /** constructor */
    private NGAImport(Country country, String state) {
      super(country.getCode(), state);
      this.country = country;
      this.state = state;
    }
  
    protected URL getURL() throws IOException {
        return new URL(URL.replaceFirst("COUNTRY", country.getFips().toLowerCase()));
    }
    
    /** parse NGA lines */
    protected void parse(BufferedReader in) throws IOException {
      
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
        if ('P'!=values.get(9).charAt(0)) {
            skip();
          continue;
        }
        
        // and check country
        if (!country.getFips().equalsIgnoreCase(values.get(12))) {
          skip();
          continue;
        }
        
        // grab name
        name = values.get(22); // FULL_NAME
        
        // keep it
        write(name, "", country.getCode(), lat, lon);
      }
  
      // done
    }
  
  } //NGAImport

} //GeoService
