/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.almanac.Almanac;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author NMeier
 */
public class ReportAlmanac extends Report {

  /**
   * main for Gedcom
   */
  public void start(Gedcom gedcom) {
    report(gedcom, gedcom.getEntities(Gedcom.INDI));
  }
  
  /**
   * main for Indi
   */
  public void start(Indi indi) {
    report(indi.getGedcom(), Collections.singletonList(indi));
  }
  
  /**
   * main for Indis
   */
  public void start(Indi[] indis) {
    report(indis[0].getGedcom(), Arrays.asList(indis));
  }
  
  /**
   * main for dates
   */
  public void start(PropertyDate[] dates) 
  {
    // collect 'lifespan'
    PointInTime 
      from = new PointInTime(), 
      to   = new PointInTime();

    for (int i=0;i<dates.length;i++) 
      getTimespan(dates[i], from, to);

    if (!from.isValid()||!to.isValid())
      return;

    // report it
    report(getAlmanac().getEvents(from, to, null));
    
  }
  
  /**
   * Report events for list of individuals
   */
  private void report(Gedcom ged, Collection indis) {
    
    Iterator events = getEvents(ged, indis);
    if (events==null) {
      println(translate("norange", indis.size()));
      return;
    }
    
    report(events);
    
  }
  
  private void report(Iterator events) {

    int num = 0;
    while (events.hasNext()) {
      println(" + "+events.next());
      num++;
    }
    println(translate("found", new Integer(num)));
    
    // done
  }
  
  /**
   * Lookup alamanac events for the given individuals
   */
  private Iterator getEvents(Gedcom gedcom, Collection indis) {

    // collect 'lifespan'
    PointInTime 
      from = new PointInTime(), 
      to   = new PointInTime();

    for (Iterator it=indis.iterator(); it.hasNext(); ) 
      getLifespan((Indi)it.next(), from, to);

    // got something?
    if (!from.isValid()||!to.isValid())
      return null;
    
    println(translate("header", new Object[]{ gedcom, from, to}));

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
      getTimespan(date, from, to);
    }
    
    // done
  }
  
  private void getTimespan(PropertyDate date, PointInTime from, PointInTime to) {
    if (date==null||!date.isValid())
      return;
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
  
  /**
   * Get initialized almanac
   */
  private Almanac getAlmanac() {
    Almanac almanac = Almanac.getInstance();
    almanac.waitLoaded();
    return almanac;
  }
  
} //ReportAlmanac
