import genj.almanac.Almanac;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Iterator;
import java.util.List;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
 * @author NMeier
 */
public class ReportAlmanac extends Report {

  /**
   * this report works on gedcom file or individual
   */
  public String accepts(Object context) {
    if (context instanceof Gedcom)
      return i18n("context.ged" , ((Gedcom)context).getName());
    if (context instanceof Indi)
      return i18n("context.indi");
    return null;
  }

  /**
   * main method
   */
  public void start(Object context) {
    
    Iterator events = getEvents(context);
    if (events==null) {
      println(i18n("norange", context));
      return;
    }

    int num = 0;
    while (events.hasNext()) {
      println(events.next());
      num++;
    }
    println(i18n("found", new Integer(num)));
    
    // done
  }
  
  /**
   * Lookup events for context
   */
  private Iterator getEvents(Object context) {
    if (context instanceof Gedcom)
      return getEvents((Gedcom)context);
    if (context instanceof Indi)
      return getEvents((Indi)context);
    throw new IllegalArgumentException("context n/a");
  }
  
  /**
   * Lookup alamanac events range for gedcom
   */
  private Iterator getEvents(Gedcom ged) {

    PointInTime 
  	from = new PointInTime(), 
  	to   = new PointInTime();

    Iterator indis = ged.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      getLifespan((Indi)indis.next(), from, to);
    }

    if (!from.isValid()||!to.isValid())
      return null;
    
    println(i18n("header", new Object[]{ ged, from, to}));

    return getAlmanac().getEvents(from, to, null);
  }  
  
  /**
   * Lookup alamanac events range for individual
   */
  private Iterator getEvents(Indi indi) {
    
    PointInTime 
    	from = new PointInTime(), 
    	to   = new PointInTime();

    getLifespan(indi, from, to);
    
    if (!from.isValid()||!to.isValid())
      return null;

    println(i18n("header", new Object[]{ indi, from, to}));
    
    return getAlmanac().getEvents(from, to, null);
  }  
  
  /**
   * Get start end of indi
   */
  private void getLifespan(Indi indi, PointInTime from, PointInTime to) {
    
    // look at his events to find start and end
    List events = indi.getProperties(PropertyEvent.class);
    for (int e=0; e<events.size(); e++) {
      Property event = (Property)events.get(e);
      PropertyDate date = (PropertyDate)event.getProperty("DATE");
      if (!date.isValid())
        continue;
      try {
	      PointInTime 
	      	start = date.getStart().getPointInTime(PointInTime.GREGORIAN),
	      	end   = date.isRange() ? date.getEnd().getPointInTime(PointInTime.GREGORIAN) : start;
	      if (!from.isValid()||from.compareTo(start)>0)
	        from.set(start);
	      if (!to.isValid()||to  .compareTo(end  )<0)
	        to.set(end);
      } catch (GedcomException ge) {
        // ignored
      }
    }
    
    // done
  }
  
  /**
   * Get initialized almanac
   */
  private Almanac getAlmanac() {
    Almanac almanac = Almanac.getInstance();
    almanac.waitLoaded();
    return almanac;
  }
  
} //ReportAlmanac
