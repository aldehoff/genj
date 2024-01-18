/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Chart names and their usage in a gedcom file
 */
public class ReportNameHistory extends Report {
  
  /** lifespan we assume if there's no death */
  private int lifespanWithoutDEAT = 80;
  
  /** minimum percentage of name to be considered */
  private int minUseOfName = 2;
  
  /** Accessor - minimum percentage of name to be considered */
  public int getMinUseOfName() {
    return minUseOfName;
  }
  
  /** Accessor - minimum percentage of name to be considered */
  public void setMinUseOfName(int set) {
    minUseOfName = Math.max(0, Math.min(set, 50));
  }
  
  /** Accessor - lifespan we assume when there's no DEAT */
  public int getLifespanWithoutDEAT() {
    return lifespanWithoutDEAT;
  }
  
  /** Accessor - lifespan we assume when there's no DEAT */
  public void setLifespanWithoutDEAT(int set) {
    lifespanWithoutDEAT = Math.max(20, Math.min(120, set));
  }

  /**
   * no stdout necessary for this report
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * Main
   */
  public void start(Object context) {
    
    // assuming gedcom
    Gedcom gedcom = (Gedcom)context;
    Collection indis = gedcom.getEntities(Gedcom.INDI);
    
    // determine range
    int 
      yearStart = findStart(indis),
      yearEnd   = PointInTime.getNow().getYear();
    
    // loop over individuals
    Map name2series = new HashMap();
    Iterator iterator = indis.iterator();
    while (iterator.hasNext()) {
      Indi indi = (Indi)iterator.next();
      analyze(gedcom, indis, indi, yearStart, yearEnd, name2series);
    }
    
    // check if got something
    if (name2series.isEmpty()) 
      return;
    
    // show it
    showChartToUser(new Chart(getName(), null, i18n("yaxis"), IndexedSeries.toArray(name2series.values()), yearStart, yearEnd, new DecimalFormat("#"), true));

    // done
  }
  
  /**
   * Find earliest year
   */
  private int findStart(Collection indis) {
    // start with year now-100
    int result = PointInTime.getNow().getYear()-100;
    // loop over indis
    Iterator it = indis.iterator();
    while (it.hasNext()) {
      Indi indi = (Indi)it.next();
      PropertyDate birth = indi.getBirthDate();
      if (birth!=null) {
        PointInTime start = birth.getStart();
        if (start.isValid()) try {
          // try to change by birth's year
          result = Math.min(result, start.getPointInTime(PointInTime.GREGORIAN).getYear());
        } catch (GedcomException e) {
        }
      }
    }
    // done
    return result;
  }

  /**
   * Analyze one individual
   */
  private void analyze(Gedcom gedcom, Collection indis, Indi indi, int yearStart, int yearEnd, Map name2series) {
    
    // check name
	  PropertyName name = (PropertyName)indi.getProperty("NAME");
	  if (name==null||!name.isValid())
	    return;
	  String last = name.getLastName();
	  if (last.length()==0)
	    return;
    
	  // check minimum percentage of name
	  if (PropertyName.getPropertyNames(gedcom, last).size()<indis.size()*minUseOfName/100)
	    return;
	  
	  // calculate start
	  int start;
	  try {
	    start = indi.getBirthDate().getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	  } catch (Throwable t) {
	    return;
	  }
	  
	  // calculate end
	  int end;
	  try {
		  end = indi.getDeathDate().getStart().getPointInTime(PointInTime.GREGORIAN).getYear();
	  } catch (Throwable t) {
	    end = start+lifespanWithoutDEAT;
	  }
	  
	  // check range
	  if (end<start||end<yearStart||start>yearEnd)
	    return;
	  
	  // convert to indexed start/end 0<index<yearEnd-yearStart
	  start = Math.max(0                , start-yearStart);
	  end   = Math.min(yearEnd-yearStart,   end-yearStart);
	  
	  // increase indexedseries for last-name throughout lifespan (start to end)
	  IndexedSeries series = (IndexedSeries)name2series.get(last);
	  if (series==null) {
	    series = new IndexedSeries(last, yearEnd-yearStart+1);
	    name2series.put(last, series);
	  }
	  for (;start<=end;start++) 
	    series.inc(start);
	  
	  // done
	}
  
} //ReportNameUsage