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

import genj.util.Resources;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

import org.geotools.shapefile.Shapefile;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;

/**
 * An available Map
 */
public class GeoMap {
  
  private final static String 
    SUFFIX_SHP = ".shp",
    PROPERTIES = "geo.properties";
  
  /** file or directory */
  private File fileOrDir;
  
  /** resources */
  private Resources resources;
  
  /** name */
  private String name;
  
  /** background color */
  private Color background = Color.WHITE;
  
  /** constructor */
  /*package*/ GeoMap(File fileOrDir) {
    
    // no file at this point
    if (!fileOrDir.isDirectory())
      throw new IllegalArgumentException("archive file not supported yet");
    
    this.fileOrDir = fileOrDir;

    // load properties
    loadProperties();
    
    // done
  }
  
  /** load properties */
  private void loadProperties() {
    
    // load properties
    try {
      File file = new File(fileOrDir, PROPERTIES);
      resources = new Resources(new FileInputStream(file));
    } catch (IOException e) {
    }
    
    // init name&color
    name = translate("name", fileOrDir.getName());
    try {
      background =  new Color(Integer.decode(translate("color.background", "#ffffff")).intValue());
    } catch (Throwable t) {
    }
    
    
  }
  
  /** a key */
  public String getKey() {
    return fileOrDir.getName();
  }
  
  /** resource access */
  private String translate(String key, String fallback) {
    // no resource?
    if (resources==null)
      return fallback;
    
    // try current language
    String result = resources.getString(key+"."+Locale.getDefault().getLanguage().toLowerCase(), false);
    if (result==null) 
      result = resources.getString(key, false);
    return result!=null ? result : fallback;
  }
  
  /** name */
  public String getName() {
    return name;
  }
  
  /** background color */
  public Color getBackground() {
    return background;
  }
  
  /** 
   * load all feature collections for this geo map into LayerManager  
   */
  void load(LayerManager manager) throws IOException {
    
    // reload properties
    loadProperties();

    // load shapes files
    File[] files = fileOrDir.listFiles();
    Arrays.sort(files);
    for (int i=0;i<files.length;i++) {
      
      // shape file?
      File file = files[i];
      if (!file.getName().endsWith(SUFFIX_SHP)) 
        continue;
      String name = file.getName().substring(0, file.getName().length()-SUFFIX_SHP.length());
      
      // load it
      FeatureCollection fc = load(file);
      // create layer
      Layer layer = manager.addLayer(getName(), name, fc);
      // check for parameters
      if (Character.isDigit(name.charAt(0))) name = name.substring(1);
      String color = translate("color."+name, null);
      if (color!=null) try {
        Color c = new Color(Integer.decode(color).intValue());
        BasicStyle style = layer.getBasicStyle();
        style.setFillColor(c);
        style.setAlpha(255);
        style.setLineColor(Layer.defaultLineColor(c));
      } catch (NumberFormatException nfe) {
        GeoView.LOG.warning( "Found undecodeable color "+color+" for map "+name);
      }

      // next
    }

    // done
  }
  
  /** load a feature collection for given shape file into layer manager */
  private FeatureCollection load(File shapefile) throws IOException {

    // read geometric shapes from file
    FileInputStream in = null;
    GeometryCollection gc;
    try {
      in = new FileInputStream(shapefile);
      gc = new Shapefile(in).read(new GeometryFactory());
    } catch (Throwable t) {
      GeoView.LOG.log(Level.WARNING, "Caught throwable reading "+shapefile, t);
      if (t instanceof IOException)
        throw (IOException)t;
      throw new IOException(t.getMessage());
    } finally {
      if (in!=null) in.close();
    }

    // pack into FeatureCollection
    FeatureSchema schema = new FeatureSchema();
    schema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    
    FeatureDataset result = new FeatureDataset(schema);
    
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      Feature feature = new BasicFeature(schema);
      Geometry geo = gc.getGeometryN(i);
      feature.setGeometry(geo);
      result.add(feature);
    }
    
    return result;
    
  }
  
}//GeoMap
