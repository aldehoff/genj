/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.TagPath;
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
    return true;
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
    Index primary = new Index();

    for (Iterator it = indis.iterator(); it.hasNext();) 
      analyze(  (Indi) it.next(), primary);
    
    // write it out
    for (Iterator ps = primary.keys(); ps.hasNext(); ) {
      String p = (String)ps.next();
      Index secondary = (Index)primary.get(p, null);
      for (Iterator ss = secondary.keys(); ss.hasNext(); ) {
        String s = (String)ss.next();
        Range range = (Range)secondary.get(s, null);
        
        println( p + " " + s + " " + range);
      }
    }
    
    println("*** Under Construction - this isn't done yet ***");
      
    // done
  }
  
  /**
   * Analyze an individual
   */
  private void analyze(Indi indi, Index primary) {
    
    // consider non-empty last names only
    String name = indi.getLastName();
    if (name.length()==0)
      return;
    
    // loop over all dates in indi
    for (Iterator dates = indi.getProperties(PropertyDate.class).iterator(); dates.hasNext(); ) {
      // consider valid dates only
      PropertyDate date = (PropertyDate)dates.next();
      if (!date.isValid()) continue;
      // compute first and last year (gotta be right)
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
  private void analyzeCities(String name, int start, int end, Property prop, Index primary) {
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
  private void analyzePlaces(String name, int start, int end, Property prop, Index primary) {
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
  
  private void keep(String name, int start, int end, String place, Index primary) {
    
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
    Index secondary = (Index)primary.get(pk, Index.class);
    Range range = (Range)secondary.get(ss, Range.class);
    range.add(start, end);
    // done
  }
  
  /**
   * our index
   */
  private static class Index {
    Map key2value = new TreeMap();
    
    Object get(String key, Class fallback) {
      Object result = key2value.get(key);
      if (result==null) {
        try {
          result = fallback.newInstance();
        } catch (Throwable t) {
          // can't happen
        }
        key2value.put(key, result);
      }
      return result;
    }
    
    Iterator keys() {
      return key2value.keySet().iterator();
    }
  }

  /**
   * our ranges
   */
  private static class Range {
    int firstYear = Integer.MAX_VALUE, lastYear = -Integer.MAX_VALUE;
    
    void add(int start, int end) {
      firstYear = Math.min(firstYear, start);
      lastYear = Math.max(lastYear, end);
    }
    
    public String toString() {
      return firstYear + " " + lastYear;
    }
  }

} //ReportHTMLSheets
