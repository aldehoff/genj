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
import genj.gedcom.PointInTime;
import genj.report.Report;
import genj.util.ReferenceSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Müssig carsten.muessig@gmx.net
 */
public class ReportEvents extends Report {

    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    /** whether births should be reported */
    public boolean reportBirth = true;
    /** wether dead persons should be shown */
    public boolean showDead = true;
    /** whether deaths should be reported */
    public boolean reportDeath = true;
    /** whether marriages should be reported */
    public boolean reportMarriage = true;
    /** whether divorces should be reported */
    public boolean reportDivorce = true;
    /** day of the date limit */
    public int day = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
    public String[] days = { "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31" };
    /** month of the date limit */
    public int month = new GregorianCalendar().get(Calendar.MONTH)+1;
    public String[] months = { i18n("month.jan"), i18n("month.feb"),  i18n("month.mar"),  i18n("month.apr"),  i18n("month.may"),  i18n("month.jun"),  i18n("month.jul"),  i18n("month.aug"),  i18n("month.sep"),  i18n("month.oct"),  i18n("month.nov"),  i18n("month.dec") };
    /** year of the date limit */
    public int year = new GregorianCalendar().get(Calendar.YEAR);
    /** how the day should be handled */
    public int handleDay = 2;
    public String[] handleDays = { i18n("date.min"), i18n("date.max"), i18n("date.fix"), i18n("date.ignore")};
    /** how the day should be handled */
    public int handleMonth = 2;
    public String[] handleMonths = { i18n("date.min"), i18n("date.max"), i18n("date.fix"), i18n("date.ignore")};

    /** how the day should be handled */
    public int handleYear = 2;
    public String[] handleYears = { i18n("date.min"), i18n("date.max"), i18n("date.fix"), i18n("date.ignore")};

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
        if((reportBirth)||(reportDeath)||(reportMarriage)||(reportDivorce)) {
            // assuming Gedcom
            Gedcom gedcom = (Gedcom)context;

            ArrayList indis = new ArrayList(gedcom.getEntities(gedcom.INDI));
            ReferenceSet births = new ReferenceSet(), marriages = new ReferenceSet(), divorces = new ReferenceSet(), deaths = new ReferenceSet();

            for(int i=0;i<indis.size();i++) {

                Indi indi = (Indi)indis.get(i);

                if(reportBirth) {
                    if((indi.getBirthDate()!=null)&&(indi.getBirthDate().getStart()!=null)&&(checkDate(indi.getBirthDate().getStart())))
                      if (showDead)
                          addToReferenceSet(indi.getBirthDate().getStart(), indi, births);
                        else
                          if ((indi.getDeathDate()==null)||(indi.getDeathDate().getStart()==null))
                            addToReferenceSet(indi.getBirthDate().getStart(), indi, births);
                }
                if(reportMarriage) {
                    Fam[] fams = indi.getFamilies();
                    for(int j=0;j<fams.length;j++) {
                        if((fams[j].getMarriageDate()!=null)&&(fams[j].getMarriageDate().getStart()!=null)&&(checkDate(fams[j].getMarriageDate().getStart())))
                            addToReferenceSet(fams[j].getMarriageDate().getStart(), fams[j], marriages);
                    }
                }
                if(reportDivorce) {
                    Fam[] fams = indi.getFamilies();
                    for(int j=0;j<fams.length;j++) {
                        if((fams[j].getDivorceDate()!=null)&&(fams[j].getDivorceDate().getStart()!=null)&&(checkDate(fams[j].getDivorceDate().getStart())))
                            addToReferenceSet(fams[j].getDivorceDate().getStart(), fams[j], divorces);
                    }
                }
                if(reportDeath) {
                    if((indi.getDeathDate()!=null)&&(indi.getDeathDate().getStart()!=null)&&(checkDate(indi.getDeathDate().getStart())))
                        addToReferenceSet(indi.getDeathDate().getStart(), indi, deaths);
                }
            }

            println(i18n("day")+": "+(day+1)+" ("+getHandleDate(handleDay)+")");
            println(i18n("month")+": "+(month+1)+" ("+getHandleDate(handleMonth)+")");
            println(i18n("year")+": "+year+" ("+getHandleDate(handleYear)+")");
            println();

            if(reportBirth) {
                println("   "+i18n("birth"));
                report(births);
                println();
            }
            if(reportMarriage) {
                println("   "+i18n("marriage"));
                report(marriages);
                println();
            }
            if(reportDivorce) {
                println("   "+i18n("divorce"));
                report(divorces);
                println();
            }
            if(reportDeath) {
                println("   "+i18n("death"));
                report(deaths);
            }
        }
    }

    /** get an i18n() string with info about date handling
     * @param int value for string lookup */
    private String getHandleDate(int what) {
        switch(what) {
            case 0: return i18n("date.min");
            case 1: return i18n("date.max");
            case 2: return i18n("date.fix");
            case 3: return i18n("date.ignore");
            default: return "";
        }
    }

    /** adds an entry to a RefernceSet
     * @param date of an event
     * @param entity involved in the event
     * @param set to store date and entity
     */
    private void addToReferenceSet(PointInTime date, Entity entity, ReferenceSet set) {
        if(isSortDay) {
            int month = date.getMonth();
            if(set.getReferences(new Integer(month)).size()==0)
                set.add(new Integer(month), new ReferenceSet());
            ReferenceSet r = (ReferenceSet)set.getReferences(new Integer(month)).iterator().next();
            r.add(date, entity);
        }
        else
            set.add(date, entity);
    }

    /** prepares the data for output / printing which is done via output() calls
     * @param indis data for output */
    private void report(ReferenceSet indis) {
        Iterator i = indis.getKeys(true).iterator();
        while(i.hasNext()) {
            Object key = i.next();
            if(key instanceof PointInTime)
                output(indis, (PointInTime)key);
            if(key instanceof Integer) {
                Iterator j = indis.getReferences(key).iterator();
                while(j.hasNext()) {
                    ReferenceSet r = (ReferenceSet)j.next();
                    Iterator k = r.getKeys(true).iterator();
                    ArrayList keys = new ArrayList();
                    while(k.hasNext())
                        keys.add((PointInTime)k.next());
                    Collections.sort(keys, new Comparator() {
                        public int compare(Object o1, Object o2) {
                            return ((PointInTime)o1).getDay()-((PointInTime)o2).getDay();
                        }
                    });
                    for(int l=0;l<keys.size();l++)
                        output(r, (PointInTime)keys.get(l));
                }
            }
        }
    }

    /** prints the report output
     * @param indis data to print
     * @param date of an event */
    private void output(ReferenceSet indis, PointInTime date) {
        if(date.getDay()!=-1) {
            ArrayList list = new ArrayList(indis.getReferences(date));
            for(int j=0;j<list.size();j++) {
                Object obj = list.get(j);
                if(obj instanceof Indi) {
                    Indi indi = (Indi)obj;
                    println("      "+date+" @"+indi.getId()+"@ "+indi.getName());
                }
                if(obj instanceof Fam) {
                    Fam fam = (Fam)obj;
                    println("      "+date+" @"+fam.getId()+"@ "+fam.toString()+" (@"+fam.getHusband().getId()+"@ + @"+fam.getWife().getId()+"@)");
                }
            }
        }
    }

    /* checks if the date of an event matches the given values of the user (fix, max, min, ...)
     * @return boolean to indicate the result */
    private boolean checkDate(PointInTime date) {
        boolean d = false, m = false, y = false;

        if((handleDay==0)&&(day<=date.getDay())) // day = minimum
            d = true;
        else if((handleDay==1)&&(day>=date.getDay())) // day = maximum
            d = true;
        else if((handleDay==2)&&(day==date.getDay())) // day = fix
            d = true;
        else if(handleDay==3) // day = ignore
            d = true;

        if((handleMonth==0)&&(month<=date.getMonth())) // month = minimum
            m = true;
        else if((handleMonth==1)&&(month>=date.getMonth())) // month = maximum
            m = true;
        else if((handleMonth==2)&&(month==date.getMonth())) // month = fix
            m = true;
        else if(handleMonth==3) // month = ignore
            m = true;

        if((handleYear==0)&&(year<=date.getYear())) // year = minimum
            y = true;
        else if((handleYear==1)&&(year>=date.getYear())) // year = maximum
            y = true;
        else if((handleYear==2)&&(year==date.getYear())) // year = fix
            y = true;
        else if(handleYear==3) // year = ignore
            y = true;

        if((d)&&(m)&&(y))
            return true;
        return false;
    }
    
} //ReportEvents
