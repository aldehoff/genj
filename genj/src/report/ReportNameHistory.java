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
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;

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
    
    // FIXME hardcoded - need to look into gedcom file
    int 
      rangeStart = 1500,
      rangeEnd   = 2000;
    
    // assuming gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // collect the indexed 
    IndexedSeries.Collector collector = new IndexedSeries.Collector(rangeEnd-rangeStart+1);
    
    // loop over individuals
    Collection indis = gedcom.getEntities(Gedcom.INDI);
    Iterator iterator = indis.iterator();
    while (iterator.hasNext()) {
      
      // look for individuals with a birth and name
      Indi indi = (Indi)iterator.next();
      PropertyDate birth = indi.getBirthDate();
      PropertyDate death = indi.getDeathDate();
      PropertyName name = (PropertyName)indi.getProperty("NAME");
      if (birth==null||!birth.isValid()||name==null||!name.isValid())
        continue;
      String last = name.getLastName();
      if (last.length()==0)
        continue;
      
      // check minimum percentage of name
      if (PropertyName.getPropertyNames(gedcom, last).size()<indis.size()*minUseOfName/100)
        continue;
      
      // calculate start
      int start = birth.getStart().getYear();
      
      // calculate end
      int end = death==null||!death.isValid() ? start+lifespanWithoutDEAT : death.getStart().getYear();
      end = Math.min( PointInTime.getNow().getYear(), Math.max(start, end));
      
      // check range
      if (end<rangeStart||start>rangeEnd)
        continue;
      start = Math.max(0, start-rangeStart);
      end   = Math.min(rangeEnd-rangeStart, end-rangeStart);
      
      // increase indexedseries for last-name throughout lifespan (start to end)
      IndexedSeries series = collector.get(last);
      for (;start<=end;start++) 
        series.inc(start);
    }
    
    // check if got something
    if (collector.isEmpty()) 
      return;
    
    // show it
    showChartToUser(new Chart(getName(), null, i18n("yaxis"), collector.toSeriesArray(), rangeStart, rangeEnd, new DecimalFormat("#"), true));

    // done
  }
  
} //ReportNameUsage