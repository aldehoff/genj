/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.Calendar;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;
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
   * getupdated date
   */
  public PointInTime getUpdatedDate(){
	  String updated = "$Date: 2006-11-13 07:57:00 $";
	    try {
	    	return new PointInTime(updated.substring(7, 11)+
	    			updated.substring(12, 14)+
	    			updated.substring(15, 17));
	      } catch (Exception e) {
	        return super.getUpdatedDate();
	      }
  }

  /**
   * Report's main
   */
  public void start(Gedcom gedcom) {

    // look for events we consider
    IndexedSeries[] series = {
      analyze(gedcom.getEntities("INDI"), "BIRT"),
      analyze(gedcom.getEntities("INDI"), "DEAT"),
      analyze(gedcom.getEntities("FAM" ), "MARR")
    };

    // show it in a chart per series
    String[] categories = CALENDARS[calendar].getMonths(true);

    JTabbedPane charts = new JTabbedPane();
    for (int i=0;i<series.length;i++) {
      String label = Gedcom.getName(series[i].getName());
      Chart chart = new Chart(null, series[i], categories, false);
      charts.addTab(label, chart);
    }
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, charts);

    showComponentToUser(panel);

    // done
  }

  private IndexedSeries analyze(Collection entities, String tag) {

    int months = CALENDARS[calendar].getMonths(true).length;

    IndexedSeries series = new IndexedSeries(tag, months);

    // loop over entities
    Iterator it = entities.iterator();
    while (it.hasNext()) {

      Entity e = (Entity)it.next();

      // check it out
      Property event = e.getProperty(series.getName());
      if (!(event instanceof PropertyEvent))
        continue;
      PropertyDate date = ((PropertyEvent)event).getDate();
      if (date==null)
        continue;

      // inc appropriate month
      try {
        series.inc(date.getStart().getPointInTime(CALENDARS[calendar]).getMonth());
      } catch (Throwable t) {
      }

      // next
    }

    // done
    return series;
  }

} //ReportBirthMonths
