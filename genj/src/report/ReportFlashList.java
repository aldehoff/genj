/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.fo.Document;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;

/**
 * GenJ - Report
 * @author Nils Meier <nils@meiers.net>
 * @version 1.0
 */
public class ReportFlashList extends Report {
  
  private final static TagPath CITY = new TagPath(".:ADDR:CITY");
  
  /** option - whether to use last name as primary index */
  public boolean isIndexByLastName = true;

  /** option - whether to use a TOC or not */
  public boolean weAddaTOC = true;
  
  /**
   * Overriden image - we're using the provided FO image 
   */
  protected ImageIcon getImage() {
    return Report.IMG_FO;
  }

  /**
   * While we generate information on stdout it's not really
   * necessary because we're returning a Document
   * that is handled by the UI anyways
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * One of the report's entry point
   */
  public void start(Gedcom gedcom) {
    start(gedcom, gedcom.getEntities(Gedcom.INDI));
  }

  /**
   * One of the report's entry point
   */
  public void start(Indi[] indis) {
    start(indis[0].getGedcom(), Arrays.asList(indis));
  }

  /**
   * Our main logic
   */
  private void start(Gedcom gedcom, Collection indis) {

    // prepare our index
    Map primary = new TreeMap();

    for (Iterator it = indis.iterator(); it.hasNext();) 
      analyze(  (Indi) it.next(), primary);
    
    // write it out
    Document doc = new Document(getName());
    if (weAddaTOC) {
      doc.addTOC();
      if (primary.size()>10) doc.nextPage();
    }
    
        
    for (Iterator ps = primary.keySet().iterator(); ps.hasNext(); ) {
      String p = (String)ps.next();

      doc.startSection(p);
      
      doc.startTable("80%,10%,10%", false, true);
      
      Map secondary = (Map)lookup(primary, p, null);
      for (Iterator ss = secondary.keySet().iterator(); ss.hasNext(); ) {
        
        String s = (String)ss.next();
        Range range = (Range)lookup(secondary, s, null);

        doc.addText(s);
        doc.nextTableCell();
        doc.addText(range.getFirst());
        doc.nextTableCell();
        doc.addText(range.getLast());
        if (ss.hasNext()) doc.nextTableCell();
      }
      
      doc.endTable();

      // done
    }
    
    // done
    showDocumentToUser(doc);
  }
  
  /**
   * Analyze an individual
   */
  private void analyze(Indi indi, Map primary) {
    
    // consider non-empty last names only
    String name = indi.getLastName();
    if (name.length()==0)
      return;
    
    // loop over all dates in indi
    for (Iterator dates = indi.getProperties(PropertyDate.class).iterator(); dates.hasNext(); ) {
      // consider valid dates only
      PropertyDate date = (PropertyDate)dates.next();
      if (!date.isValid()) continue;
      // compute first and last year
      int start = date.getStart().getYear();
      int end = date.isRange() ? date.getEnd().getYear() : start;
      if (start>end) continue;
      // find all places for it
      analyzePlaces(name, start, end, date.getParent(), primary);
      // find all cities for it 
      analyzeCities(name, start, end, date.getParent(), primary);
      
      // next date
    }
    
    // done
  }

  /**
   * Analyze all cities for given indi, start, end & property
   */
  private void analyzeCities(String name, int start, int end, Property prop, Map primary) {
    Property[] cities = prop.getProperties(CITY);
    for (int c = 0; c < cities.length; c++) {
      // consider non-empty cities only
      String city = cities[c].getDisplayValue();
      if (city.length()==0) continue;
      // keep it
      keep(name, start, end, city, primary);
      // next city
    }
    // done
  }
  
  /**
   * Analyze all places for given indi, start, end & property
   */
  private void analyzePlaces(String name, int start, int end, Property prop, Map primary) {
    // loop over places
    for (Iterator places = prop.getProperties(PropertyPlace.class).iterator(); places.hasNext(); ) {
      // consider non-empty places only
      PropertyPlace place = (PropertyPlace)places.next();
      String jurisdiction = place.getFirstAvailableJurisdiction();
      if (jurisdiction.length()==0) continue;
      // keep it
      keep(name, start, end, jurisdiction, primary);
      // next place
    }
    // done
  }
  
  private void keep(String name, int start, int end, String place, Map primary) {
    
    // calculate primary and secondary key
    String pk, ss;
    if (isIndexByLastName) {
      pk = name;
      ss = place;
    } else { 
      pk= place;
      ss = name;
    }
    // remember
    Map secondary = (Map)lookup(primary, pk, TreeMap.class);
    Range range = (Range)lookup(secondary, ss, Range.class);
    range.add(start, end);
    // done
  }
  
  /**
   * Lookup an object in a map with a default class
   */
  private Object lookup(Map index, String key, Class fallback) {
    // look up and create lazily if necessary
    Object result = index.get(key);
    if (result==null) {
      try {
        result = fallback.newInstance();
      } catch (Throwable t) {
        t.printStackTrace();
        throw new IllegalArgumentException("can't instantiate fallback "+fallback);
      }
      index.put(key, result);
    }
    // done
    return result;
  }
  
  /**
   * our ranges
   */
  static class Range {
    int firstYear = Integer.MAX_VALUE, lastYear = -Integer.MAX_VALUE;
    
    void add(int start, int end) {
      // check for valid year - this might still be UNKNOWN even though a date was valid
      if (start!=PointInTime.UNKNOWN)
        firstYear = Math.min(firstYear, start);
      if (end!=PointInTime.UNKNOWN)
        lastYear = Math.max(lastYear, end);
    }
    
    public String toString() {
      // check for valid year - this might still be UNKNOWN even though a date was valid
      if (firstYear==Integer.MAX_VALUE|| lastYear==Integer.MAX_VALUE)
        return "";
      return firstYear + " " + lastYear;
    }
    
    String getFirst() {
      // check for valid year - this might still be UNKNOWN even though a date was valid
      if (firstYear==Integer.MAX_VALUE|| lastYear==Integer.MAX_VALUE)
        return "";
      return Integer.toString(firstYear);
    }
    
    String getLast() {
      return Integer.toString(lastYear);
    }
  }

} //ReportHTMLSheets
