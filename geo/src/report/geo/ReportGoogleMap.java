/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package geo;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.geo.GeoLocation;
import genj.geo.GeoService;
import genj.io.FileAssociation;
import genj.report.Report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

/**
 * This GenJ Report exports places for viewing online in Google Maps
 */
public class ReportGoogleMap extends Report {
  
  private final static DecimalFormat FORMAT = new DecimalFormat("##0.###", new DecimalFormatSymbols(Locale.US));

  /**
   * we're not a stdout report
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * Our main method for a gedcom file
   */
  public void start(Gedcom ged) {
    operate(ged, ged.getEntities());
  }
  
  /**
   * Our main method for one individual
   */
  public void start(Indi indi) {
    operate(indi.getGedcom(), Collections.singletonList(indi));
  }
  
  /**
   * Our main method for a set of individuals
   */
  public void start(Indi[] indis) {
    operate(indis[0].getGedcom(), Arrays.asList(indis));
  }
  
  /**
   * Working on a collection of individiuals
   */
  private void operate(Gedcom ged, Collection indis) {
    
    // match locations
    Collection locations = GeoService.getInstance().matchEntities(ged, indis, true);
    if (locations.isEmpty()) {
      getOptionFromUser(i18n("none_mapable"), OPTION_OK);
      return;
    }
    
    // ask for google key
    String key = getValueFromUser("google-key", i18n("enter_key"));
    if (key==null)
      return;
      
    // ask the user for file(s)
    File html = getFileFromUser(i18n("which_html_file"), i18n("generate"));
    if (html==null)
      return;
    if (!"html".equals(FileAssociation.getSuffix(html)))
      html = new File(html.getAbsolutePath()+".html");
    File xml = new File(html.getAbsolutePath().replaceAll(".html", ".xml"));
    
    // write the html file
    if (!writeHTML(ged, (GeoLocation)locations.iterator().next(), html, xml, key))
      return;
    
    // write the xml file
    if (!writeXML(locations, xml))
      return;
    
    // let the user know
    getOptionFromUser(i18n("done", new String[] { html.getName(), xml.getName(), html.getParent() }), OPTION_OK );

    // done
  }
  
  /**
   * write the html file
   */
  private boolean writeHTML(Gedcom ged, GeoLocation center, File html, File xml, String key) {
    
    String[] match = { "MAPGED", "MAPLAT", "MAPLON", "MAPXML", "MAPKEY" };
    String[] replace = { ged.getName(), FORMAT.format(center.getY()), FORMAT.format(center.getX()), xml.getName(), key };
    
    // copy template
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(html), Charset.forName("UTF8")));
      BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("gmap-template.html"), Charset.forName("UTF8")));
      while (true) {
        // read a line
        String line = in.readLine();
        if (line==null) break;
        // match stuff
        for (int m=0;m<match.length;m++)  {
          int i = line.indexOf(match[m]);
          if (i>=0) {
            line = line.substring(0,i) + replace[m] + line.substring(i+match[m].length());
            break;
          }
        }
        // write the line
        out.write(line);
        out.write("\n");
      }
      in.close();
      out.close();
    } catch (IOException e) {
      getOptionFromUser(i18n("ioerror", html), OPTION_OK);
      return false;
    }
    // done
    return true;
  }
  
  /**
   * write the xml file
   */
  private boolean writeXML(Collection locations, File xml) {
    
    // <ls>
    //   <l x="37.441" y="-122.141">foo</l>
    //   <l x="37.322" y="-121.213"/>
    // </ls>      
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xml), Charset.forName("UTF8")));
      out.write("<ls>");
      for (Iterator it=locations.iterator(); it.hasNext(); ) {
        // start with coordinates
        GeoLocation location = (GeoLocation)it.next();
        out.write("<l x=\"" + FORMAT.format(location.getX()) + "\" y=\"" + FORMAT.format(location.getY()) + "\">");
        // place
        out.write(location.toString());
        // add first property's info
        for (int p=0;p<3&&p<location.getNumProperties();p++) {
          out.write(";");
          Property prop = location.getProperty(p);
          Property date = prop.getProperty("DATE", true);
          if (date!=null) {
            out.write(date.toString());
            out.write(" ");
          }
          out.write(Gedcom.getName(prop.getTag()));
          out.write(" ");
          out.write(prop.getEntity().toString());
        }
        // next
        out.write("</l>");
      }
      out.write("</ls>");
      out.close();
    } catch (IOException e) {
      getOptionFromUser(i18n("ioerror", xml), OPTION_OK);
      return false;
    }
    
    // done
    return true;
  }
  
} //ReportGoogleMap
