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
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Müssig carsten.muessig@gmx.net
 */
public class ReportEvents extends Report {
    
    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    
    /** wether dead persons' events should be considered */
    public boolean isShowDead = true;
    
    /** whether births should be reported */
    public boolean reportBirth = true;
    
    /** whether deaths should be reported */
    public boolean reportDeath = true;
    
    /** whether marriages should be reported */
    public boolean reportMarriage = true;
    
    /** whether divorces should be reported */
    public boolean reportDivorce = true;
    
    public int sex = 3;
    public String[] sexs = {i18n("sex.male"), i18n("sex.female"), i18n("sex.unknown"), i18n("sex.ignore")};
    
    /** day of the date limit */
    public int day = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
    public String[] days = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" };
    
    /** month of the date limit */
    public int month = new GregorianCalendar().get(Calendar.MONTH) + 1;
    public String[] months =
    { i18n("month.jan"), i18n("month.feb"), i18n("month.mar"), i18n("month.apr"), i18n("month.may"), i18n("month.jun"), i18n("month.jul"), i18n("month.aug"), i18n("month.sep"), i18n("month.oct"), i18n("month.nov"), i18n("month.dec")};
    
    /** year of the date limit */
    public int year = new GregorianCalendar().get(Calendar.YEAR);
    
    /** how the day should be handled */
    public int handleDay = 2;
    public String[] handleDays = { i18n("date.min"), i18n("date.max"), i18n("date.fix"), i18n("date.ignore")};
    
    /** how the day should be handled */
    public int handleMonth = 2;
    public String[] handleMonths = handleDays;
    
    /** how the day should be handled */
    public int handleYear = 2;
    public String[] handleYears = handleDays;
    
    /** this report's version */
    public static final String VERSION = "1.0";
    
    /**
     * Returns the version of this script
     */
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Returns the name of this report
     */
    public String getName() {
        return i18n("name");
    }
    
    /**
     * Some information about this report
     */
    public String getInfo() {
        return i18n("info");
    }
    
    public String getAuthor() {
        return "Nils Meier <nils@meiers.net>, Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
    /**
     * @see genj.report.Report#accepts(java.lang.Object)
     */
    public String accepts(Object context) {
        // we accept only GEDCOM
        return context instanceof Gedcom ? getName() : null;
    }
    
    /**
     * Entry point into this report - by default reports are only run on a
     * context of type Gedcom. Depending on the logic in accepts either
     * an instance of Gedcom, Entity or Property can be passed in though.
     */
    public void start(Object context) {
        
        // check that something is selected
        if ((!reportBirth) && (!reportDeath) && (!reportMarriage) && (!reportDivorce))
            return;
        
        // assuming Gedcom
        Gedcom gedcom = (Gedcom) context;
        
        // collect all individuals/families per event
        HashMap
        births = new HashMap(),
        marriages = new HashMap(),
        divorces = new HashMap(),
        deaths = new HashMap();
        
        
        // loop individuals
        for (Iterator indis = gedcom.getEntities(gedcom.INDI).iterator(); indis.hasNext(); ) {
            analyze((Indi)indis.next(), births, marriages, divorces, deaths);
        }
        
        // output results
        println(i18n("sex") + ": " + sexs[sex]);
        println(i18n("day") + ": " + (day + 1) + " (" + handleDays[handleDay] + ")");
        println(i18n("month") + ": " + (month + 1) + " (" + handleMonths[handleMonth] + ")");
        println(i18n("year") + ": " + year + " (" + handleYears[handleYear] + ")");
        println();
        
        if (reportBirth) {
            println("   " + i18n("birth"));
            report(births);
            println();
        }
        if (reportMarriage) {
            println("   " + i18n("marriage"));
            report(marriages);
            println();
        }
        if (reportDivorce) {
            println("   " + i18n("divorce"));
            report(divorces);
            println();
        }
        if (reportDeath) {
            println("   " + i18n("death"));
            report(deaths);
        }
        
        // done
    }
    
    /**
     * Analyze one individual
     */
    private void analyze(Indi indi, HashMap births, HashMap marriages, HashMap divorces, HashMap deaths) {
        
        // consider dead?
        if (!isShowDead && indi.getDeathDate() != null && indi.getDeathDate().isValid())
            return;
        
        if(checkSex(indi)==false)
            return;
        
        // look for births?
        if (reportBirth) {
            if (checkDate(indi.getBirthDate()))
                births.put(indi, new Hit(indi.getBirthDate(), indi));
        }
        
        // look for marriages?
        if (reportMarriage) {
            Fam[] fams = indi.getFamilies();
            for (int j = 0; j < fams.length; j++) {
                Fam fam = fams[j];
                if (checkDate(fam.getMarriageDate()))
                    marriages.put(fam, new Hit(fam.getMarriageDate(), fam));
            }
        }
        
        // look for divorces?
        if (reportDivorce) {
            Fam[] fams = indi.getFamilies();
            for (int j = 0; j < fams.length; j++) {
                Fam fam = fams[j];
                if (checkDate(fam.getDivorceDate()))
                    divorces.put(fam, new Hit(fam.getDivorceDate(), fam));
            }
        }
        
        // look for deaths?
        if (reportDeath) {
            if (checkDate(indi.getDeathDate()))
                deaths.put(indi, new Hit(indi.getDeathDate(), indi));
        }
        
        // done
    }
    
    /**
     * Output a list of hits
     */
    private void report(HashMap ent2hits) {
        
        ArrayList hits = new ArrayList(ent2hits.values());
        
        // sort the hits either by
        //  year/month/day or
        //  month/day
        Collections.sort(hits);
        
        // print 'em
        for (Iterator it=hits.iterator();it.hasNext();) {
            report((Hit)it.next());
        }
        
    }
    
    /**
     * Print a hit
     */
    private void report(Hit hit) {
        if (hit.who instanceof Indi) {
            Indi indi = (Indi) hit.who;
            println("      " + hit.when + " @" + indi.getId() + "@ " + indi.getName());
        }
        if (hit.who instanceof Fam) {
            Fam fam = (Fam) hit.who;
            println("      " + hit.when + " @" + fam.getId() + "@ " + fam.toString() + " (@" + fam.getHusband().getId() + "@ + @" + fam.getWife().getId() + "@)");
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
        
        // check criteria
        boolean d = false, m = false, y = false;
        
        if ((handleDay == 0) && (day <= start.getDay())) // day = minimum
            d = true;
        else if ((handleDay == 1) && (day >= start.getDay())) // day = maximum
            d = true;
        else if ((handleDay == 2) && (day == start.getDay())) // day = fix
            d = true;
        else if (handleDay == 3) // day = ignore
            d = true;
        
        if ((handleMonth == 0) && (month <= start.getMonth())) // month = minimum
            m = true;
        else if ((handleMonth == 1) && (month >= start.getMonth())) // month = maximum
            m = true;
        else if ((handleMonth == 2) && (month == start.getMonth())) // month = fix
            m = true;
        else if (handleMonth == 3) // month = ignore
            m = true;
        
        if ((handleYear == 0) && (year <= start.getYear())) // year = minimum
            y = true;
        else if ((handleYear == 1) && (year >= start.getYear())) // year = maximum
            y = true;
        else if ((handleYear == 2) && (year == start.getYear())) // year = fix
            y = true;
        else if (handleYear == 3) // year = ignore
            y = true;
        
        if ((d) && (m) && (y))
            return true;
        return false;
    }
    
    /**
     * Wrapping an Event hit
     */
    private class Hit implements Comparable {
        PointInTime when;
        Entity who;
        PointInTime compare;
        // Constructor
        Hit(PropertyDate date, Entity ent) {
            when = date.getStart();
            if (isSortDay)
                compare = new PointInTime(when.getDay(), when.getMonth(), 1, when.getCalendar());
            else
                compare = when;
            who = ent;
        }
        // comparison
        public int compareTo(Object object) {
            return compare.compareTo(((Hit)object).compare);
        }
    } //Hit
    
} //ReportEvents
