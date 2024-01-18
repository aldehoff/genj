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

import genj.gedcom.Gedcom;
import genj.util.EnvironmentChecker;
import genj.util.Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;


/**
 * A service for geographical computations / data services. 
 */
public class GeoService {
  
  final static Charset UTF8 = Charset.forName("UTF8");
  final static Logger LOG = Logger.getLogger("genj.geo");
  final static URL GEOQ = createQueryURL();
  
  /** our work directory */
  private static final String GEO_DIR = "./geo";

  /** singleton */
  private static GeoService instance;
  
  /** maps */
  private List maps;
  
  /** our query url */
  private static URL createQueryURL() {
    try {
      return new URL("http://genj.sourceforge.net/php/geoq.php");    
    } catch (MalformedURLException e) {
      throw new Error("init");
    }
  }
  
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
  /*package*/ File[] getGeoFiles() {
    
    List result = new ArrayList();
    
    String[] dirs  = {
        EnvironmentChecker.getProperty(this, "user.home.genj/geo", "", "looking for user's geo files"),
        EnvironmentChecker.getProperty(this, "all.home.genj/geo", "", "looking for shared geo files"),
        EnvironmentChecker.getProperty(this, "genj.geo.dir", GEO_DIR, "looking for installed geo files")
    };
    
    // loop directories
    for (int i=0;i<dirs.length;i++) {
      File dir = new File(dirs[i]);
      if (dir.isDirectory()) {
        result.addAll(Arrays.asList(dir.listFiles()));
      }
    }

    // done
    return (File[])result.toArray(new File[result.size()]);
  }
  
  /**
   * Find a registry for gedcom file (geo.properties) 
   */
  private Registry getRegistry(Gedcom gedcom) {
    String name = gedcom.getName();
    if (name.endsWith(".ged")) name = name.substring(0, name.length()-".ged".length());
    name = name + ".geo";
    return Registry.lookup(name, gedcom.getOrigin());
  }
  
  /**
   * Encode a location into what our webservice understands. 
   * <code>
   *   location -> city[,jurisdiction]+,country
   * </code>
   */
  private String encode(GeoLocation location) {
    
    // city [,jurisdiction]+ , country
    StringBuffer query = new StringBuffer();
    query.append(location.getCity());
    query.append(",");
    if (location.getJurisdictions().isEmpty()) {
      query.append(",");
    } else for (Iterator it = location.getJurisdictions().iterator(); it.hasNext(); ) {
      query.append(it.next().toString());
      query.append(",");
    }
    Country c = location.getCountry();
    if (c!=null) query.append(c.getCode());

    // done
    return query.toString();
  }

  /**
   * Decode a location from what our service returned
   * <code>
   *   city,jurisdiction,country,lat,lon -> jurisdiction
   * </code>
   */
  private GeoLocation decode(String location) {
    
    // city, jurisdiction, iso country, lat, lon
    StringTokenizer tokens = new StringTokenizer(location, ",");
    if (tokens.countTokens()!=5)
      return null;
    
    GeoLocation result = new GeoLocation(tokens.nextToken(), tokens.nextToken(), Country.get(tokens.nextToken())); 
    result.setCoordinate(Float.parseFloat(tokens.nextToken()), Float.parseFloat(tokens.nextToken()));
    
    return result;
  }
  
  /**
   * do a service call
   * @param list list of locations to query
   * @return list of list of locations
   */
  List webservice(List locations) throws IOException {

    long start = System.currentTimeMillis();
    int rowCount = 0, hitCount = 0;
    try {
      // open connection
      HttpURLConnection con = (HttpURLConnection)GEOQ.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      con.setDoInput(true);
  
      // write our query
      Writer out = new OutputStreamWriter(con.getOutputStream(), UTF8);
      out.write("GEOQ\n");
      for (int i=0;i<locations.size();i++) {
        if (i>0) out.write("\n");
        out.write(encode((GeoLocation)locations.get(i)));
      }
      out.close();
      
      // read input
      List rows  = new ArrayList();
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), UTF8));
      for (int l=0;l<locations.size();l++) {
        String line = in.readLine();
        if (line==null) break;
        rowCount++;
        List row = new ArrayList();
        StringTokenizer hits = new StringTokenizer(line, ";");
        while (hits.hasMoreTokens()) {
          hitCount++;
          GeoLocation hit = decode(hits.nextToken());
          if (hit!=null) row.add(hit);
        }
        rows.add(row);
      }
      in.close();
      
      // done
      return rows;
      
    } finally {
      long secs  = (System.currentTimeMillis()-start)/1000;
      LOG.info("query for "+locations.size()+" locations in "+secs+"s resulted in "+rowCount+" rows and "+hitCount+" total hits");
    }
    
  }

  /**
   * Find all matching locations for given location
   * @return list of matching locations
   */
  public List query(GeoLocation location) throws IOException {
    // run query and grab first result list
    List rows = webservice(Collections.singletonList(location ));
    return rows.isEmpty() ?  new ArrayList() : (List)rows.get(0);
  }
  
  /**
   * Find best matches for given locations
   * @param location list of locations
   */
  public Collection match(Gedcom gedcom, Collection locations) throws IOException {

    // grab registry
    Registry registry = gedcom!=null ? getRegistry(gedcom) : new Registry();
    
    // loop over locations try to use registry for matching
    List todos = new ArrayList(locations.size());
    for (Iterator it=locations.iterator(); it.hasNext(); ) {
      GeoLocation location = (GeoLocation)it.next();
      // something we can map through the registry or have to add to todo-list?
      String restored  = registry.get(location.getJurisdictionsAsString(), (String)null);
      if (restored!=null) try {
        StringTokenizer tokens = new StringTokenizer(restored, ",");
        location.setCoordinate( Double.parseDouble(tokens.nextToken()), Double.parseDouble(tokens.nextToken()));
        if (tokens.hasMoreTokens())
          location.setMatches(Integer.parseInt(tokens.nextToken()));
      } catch (Throwable t) {
      }
      // still todo?
      if (!location.isValid())
        todos.add(location);
    }
    
    // still a webserive query to do?
    if (!todos.isEmpty()) {
      List rows = webservice(todos);
      if (rows.size()<todos.size()) {
        LOG.warning("got "+rows.size()+" rows for "+todos.size()+" locations");
      } else {    
        for (int i=0;i<todos.size();i++) {
          GeoLocation todo  = (GeoLocation)todos.get(i);
          List hits = (List)rows.get(i);
          if (!hits.isEmpty()) {
            GeoLocation hit = (GeoLocation)hits.get(0);
            todo.setCoordinate(hit.getCoordinate());
            todo.setMatches(hits.size());
            remember(gedcom, todo);
          }
        }
      }
    }
    
    // done
    return locations;
  }

  /**
   * Remember a specific location's lat and lon
   */
  public void remember(Gedcom gedcom, GeoLocation location) {
    if (gedcom==null)
      return;
    Coordinate coord = location.getCoordinate();
    getRegistry(gedcom).put(location.getJurisdictionsAsString(), coord.y + "," + coord.x + "," + location.getMatches());
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
          LOG.log(Level.SEVERE, "problem reading map from "+files[i], t);
        }
        // next
      }
      
      // finished looking for maps
    }
    
    // done
    return (GeoMap[])maps.toArray(new GeoMap[maps.size()]);
  }
    
} //GeoService
