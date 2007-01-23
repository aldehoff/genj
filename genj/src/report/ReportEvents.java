/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;
import genj.util.WordBuffer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Muessig carsten.muessig@gmx.net
 */
public class ReportEvents extends Report {
  
    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    /** wether dead persons' events should be considered */
    public boolean isShowDead = true;
    /** whether births should be reported */
    public boolean reportBirth = true;
    /** whether baptisms should be reported */
    public boolean reportBaptism = true;
    /** whether marriages should be reported */
    public boolean reportMarriage = true;
    /** whether divorces should be reported */
    public boolean reportDivorce = true;
    /** whether emigration should be reported */
    public boolean reportEmigration = true;
    /** whether immigration should be reported */
    public boolean reportImmigration = true;
    /** whether naturalization should be reported */
    public boolean reportNaturalization = true;
    /** whether deaths should be reported */
    public boolean reportDeath = true;
    /** whether the output should be in icalendar format */
    public boolean isOutputICal = false;

    public int sex = 3;
    public String[] sexs = {PropertySex.TXT_MALE, PropertySex.TXT_FEMALE, PropertySex.TXT_UNKNOWN, ""};

    /** day of the date limit */
    public String day = "";

    /** month of the date limit */
    public String month = "";

    /** year of the date limit */
    public String year = "";

    /** the marriage symbol */
    private final static String TXT_MARR_SYMBOL = genj.gedcom.Options.getInstance().getTxtMarriageSymbol();
    
    /** time reporting variables */
    private String timestamp;
    private final static SimpleDateFormat 
      yyyymmddhhmmss = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'"),
      yyyymmdd = new SimpleDateFormat("yyyyMMdd");


    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) throws GedcomException {

        // check that something is selected
        if ((!reportBirth) && (!reportBaptism) && (!reportDeath) && (!reportMarriage) && (!reportDivorce) && (!reportEmigration) && (!reportImmigration) && (!reportNaturalization))
            return;
        
        // initialize timestamp
        timestamp = toISO(System.currentTimeMillis(), true);
        yyyymmdd.setTimeZone(TimeZone.getTimeZone("GMT"));


        // collect evens for all individuals/families
        Map tag2event = new HashMap();
        if (reportBirth) tag2event.put("BIRT", new ArrayList());
        if (reportBaptism) {
          List baptisms = new ArrayList();
          tag2event.put("BAPM", baptisms);
          tag2event.put("BAPL", baptisms);
          tag2event.put("CHR", baptisms);
          tag2event.put("CHRA", baptisms);
        }
        if (reportMarriage) tag2event.put("MARR", new ArrayList());
        if (reportDivorce) tag2event.put("DIV", new ArrayList());
        if (reportEmigration) tag2event.put("EMI", new ArrayList());
        if (reportImmigration) tag2event.put("IMMI", new ArrayList());
        if (reportNaturalization) tag2event.put("NATU", new ArrayList());
        if (reportDeath) tag2event.put("DEAT", new ArrayList());

        // loop individuals
        for (Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator(); indis.hasNext(); ) {
            analyze((Indi)indis.next(), tag2event);
        }
        
        // output header
        if (isOutputICal) {
          println("BEGIN:VCALENDAR");
          println("PRODID:-//Genealogy//ReportEvents/EN");
          println("VERSION:2.0");
          println("METHOD:PUBLISH");
        } else {
          println(PropertySex.TXT_SEX + ": " + (sex==3?"*":sexs[sex]));
          println(Delta.TXT_DAY + ": " + (day.length()==0?"*":day));
          println(Delta.TXT_MONTH + ": " + (month.length()==0?"*":month));
          println(Delta.TXT_YEAR + ": " + (year.length()==0?"*":year));
          println();
        }
        
        // ... output events type by type
        for (Iterator tags=tag2event.keySet().iterator(); tags.hasNext(); ) {
          String tag = (String)tags.next();
          List events = (List)tag2event.get(tag);
          if (!events.isEmpty()) {
            
            Collections.sort(events);
            
            if (!isOutputICal) 
              println(getIndent(2) + Gedcom.getName(tag));

            for (Iterator it=events.iterator();it.hasNext();) 
              println(it.next());
            
            if (!isOutputICal) 
              println();
          }
        }
        
        // output footer
        if (isOutputICal) {
          println("END:VCALENDAR");
        }

        // done
    }

    /**
     * Analyze one individual
     */
    private void analyze(Indi indi, Map tag2events) {

        // consider dead?
        if (!isShowDead && indi.getDeathDate() != null && indi.getDeathDate().isValid())
            return;

        if(checkSex(indi)==false)
            return;

        // look for events
        analyzeEvents(indi, tag2events);
        
        // check all marriages
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (int f = 0; f < fams.length; f++) {
          Fam fam = fams[f];
          analyzeEvents(fam, tag2events);
        }

        // done
    }
    
    private void analyzeEvents(Entity entity, Map tag2events) {
      
      for (Iterator tags = tag2events.keySet().iterator(); tags.hasNext(); ) {
        String tag = (String)tags.next();
        List events = (List)tag2events.get(tag);
        
        Property[] props = entity.getProperties(tag);
        for (int i = 0; i < props.length; i++) {
          Property event = props[i];
          if (event instanceof PropertyEvent) {
            Property date = event.getProperty("DATE");
            if (date instanceof PropertyDate) {
              Hit hit = new Hit(entity, (PropertyEvent)event, i, (PropertyDate)date);
              if(checkDate((PropertyDate)date)&&!events.contains(hit)) events.add(hit); 
            }
          }
        }
      }
      
    }

    /** checks if the sex of an indi matches the user choice
     * @param indi to check
     * @return boolean the result
     */
    private boolean checkSex(Indi indi) {

        if(sex==3)
            return true;

        switch(indi.getSex()) {

            case PropertySex.MALE:
                if(sex!=0)
                    return false;
                else
                    return true;

            case PropertySex.FEMALE:
                if(sex!=1)
                    return false;
                else
                    return true;

            case PropertySex.UNKNOWN:
                if(sex!=2)
                    return false;
                else
                    return true;

            default: return false;
        }
    }

    /**
     * checks if the date of an event matches the given values of the user (fix, max, min, ...)
     * @return boolean to indicate the result
     */
    private boolean checkDate(PropertyDate date) {

        // has to be valid
        if (date==null||!date.isValid())
            return false;

        PointInTime start = date.getStart();
        if (start.getCalendar()!=PointInTime.GREGORIAN)
            return false;
        
        if (checkValue(start.getDay()+1, day) && checkValue(start.getMonth()+1, month) && checkValue(start.getYear(), year))
          return true;

        return false; 
    }
    
    private boolean checkValue(int value, String filter) {
      // no filter - matched!
      if (filter.length()==0)
        return true;
      // parse filter
      try {
        // filter '>'
        if (filter.startsWith(">"))
          return Integer.parseInt(filter.substring(1)) < value;
        // filter '<'
        if (filter.charAt(0)=='<')
          return Integer.parseInt(filter.substring(1)) > value;
        // filter '='
        if (filter.charAt(0)=='=')
          return Integer.parseInt(filter.substring(1)) == value;
        return Integer.parseInt(filter) == value;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    
    private String toISO(long timeMillis, boolean time) {
      Date date = new Date(timeMillis);
      return time ? yyyymmddhhmmss.format(date) : yyyymmdd.format(date);
    }        

    /**
     * Wrapping an Event hit
     */
    private class Hit implements Comparable {
      
      Entity who;
      int num;
      Property event;
      PropertyDate date;
      PointInTime compare;
        
      /** Constructor*/
      Hit(Entity entity, PropertyEvent event, int num, PropertyDate date) {
        this.who = entity;
        this.event = event;
        this.num = num;
        this.date = date;
        PointInTime when = date.getStart();
        if (isSortDay)
          // blocking out year (to a Gregorian LEAP 4 - don't want to make it invalid) so that month and day count
          compare = new PointInTime(when.getDay(), when.getMonth(), 4, when.getCalendar());
        else
          compare = when;
      }
      // comparison
      public int compareTo(Object object) {
          return compare.compareTo(((Hit)object).compare);
      }
      // equals
      public boolean equals(Object that) {
        return event==((Hit)that).event;
      }
      // toString
      public String toString() {
        try {
          if (isOutputICal) {
            WordBuffer result = new WordBuffer("\n");
            result.append("BEGIN:VEVENT");
            result.append("DTSTART:"+toISO(date.getStart().getTimeMillis(), false));
            result.append("UID:"+who.getGedcom().getName()+"|"+who.getId()+"|"+event.getTag()+"|"+num);
            result.append("DTSTAMP:"+timestamp);
            result.append("SUMMARY:"+Gedcom.getName(event.getTag())+" "+icalescape(who.toString()));
            Property where  = event.getProperty("PLAC");
            if (where!=null)
              result.append("LOCATION:"+icalescape(where.getDisplayValue()));
            if (event.getTag().equals("BIRT"))
              result.append("RRULE:FREQ=YEARLY");
            result.append("DESCRIPTION:");
            result.append("END:VEVENT");
            return result.toString();
          } else {
            StringBuffer result = new StringBuffer();
            result.append(getIndent(3));
            result.append(date);
            result.append(" ");
            result.append(who);
            return result.toString();
          }
        } catch (Throwable t) {
          throw new RuntimeException();
        }
      }
      private String icalescape(String string) {
        return string.replaceAll(",", "\\\\,");
      }
    } //Hit

} //ReportEvents
