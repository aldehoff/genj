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
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Iterator;

import javax.swing.JTabbedPane;

/**
 * A report that shows pie charts with events by months
 */
public class ReportEventsByMonths extends Report {
  
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
    String[] months = CALENDARS[calendar].getMonths(true);
    
    // look for events we consider
    IndexedSeries[] series = {
      new IndexedSeries("BIRT", months.length),  
      new IndexedSeries("DEAT", months.length)
    };
    
    analyze(series, c, gedcom);
    
    // show it
    JTabbedPane charts = new JTabbedPane();
    for (int i=0;i<series.length;i++) {
      charts.addTab(Gedcom.getName(series[i].getName()), new Chart(null, series[i], months, false));
    }

    showComponentToUser(charts);
    
    // done
  }
  
  private void analyze(IndexedSeries[] series, Calendar c, Gedcom gedcom) {
    
    // loop over individuals
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      
      Indi indi = (Indi)indis.next();
      
      // loop over series
      for (int i=0;i<series.length;i++) {
        analyze(indi, series[i], c);
      }
      
    }
    
  }
  
  private void analyze(Indi indi, IndexedSeries series, Calendar c) {
    
    Property event = indi.getProperty(series.getName());
    if (!(event instanceof PropertyEvent))
      return;
    PropertyDate date = ((PropertyEvent)event).getDate();
    if (date==null) 
      return;

    // inc appropriate month
    try {
      series.inc(date.getStart().getPointInTime(c).getMonth());
    } catch (Throwable t) {
    }
    // done
  }
  
} //ReportBirthMonths
