/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import java.util.Iterator;

import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

/**
 * A report that shows a chart with births by months
 */
public class ReportBirthMonths extends Report {
  
  /** calendar we use */
  private int calendar;

  /** calendars */
  public final static Calendar[] CALENDARS = {
    PointInTime.GREGORIAN,
    PointInTime.FRENCHR,
    PointInTime.JULIAN,
    PointInTime.HEBREW
  };

  /** accessor - calendar */
  public int getCalendar() {
    return calendar;
  }
  
  /** accessor - calendar */
  public void setCalendar(int set) {
    calendar = Math.max(0, Math.min(CALENDARS.length-1, set));
  }
  
  /** accessor - calendars */
  public Calendar[] getCalendars() {
    return CALENDARS;
  }
  
  /**
   * No STDOUT necessary
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * Report's main
   */
  public void start(Object context) {
    
    // cast to what we expect
    Gedcom gedcom = (Gedcom)context;
    
    // prepare calendar
    Calendar c = CALENDARS[calendar];

    // prepare months
    String[] months = CALENDARS[calendar].getMonths(true);
    IndexedSeries series = new IndexedSeries("", months.length);
    
    // loop over individuals
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      Indi indi = (Indi)indis.next();
      try {
        int m = indi.getBirthDate().getStart().getPointInTime(c).getMonth();
        series.inc(m);
      } catch (Throwable t) {
      }
    }
    
    // show it
    String title = i18n("title", gedcom.getName());
    Chart chart = new Chart(title, series, months, false);
    showChartToUser(chart);
    
    // done
  }
  
} //ReportBirthMonths
