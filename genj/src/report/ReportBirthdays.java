/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PointInTime;
import genj.gedcom.PropertyDate;
import genj.report.Report;
import genj.util.ReferenceSet;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Müssig carsten.muessig@gmx.net
 */
public class ReportBirthdays extends Report {
    
    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    public boolean reportBirth = true;
    public boolean reportDeath = true;
    public boolean reportMarriage = true;
    
    /** persons to report
     * year > 0 : persons with event in this year or later
     * year <= 0: all persons
     */
    public int year = 0;
    
    /** this report's version */
    public static final String VERSION = "0.4";
    
    private static final int DAY = 0;
    private static final int YEAR = 1;
    
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
        // a call to i18n will lookup a string with given key in ReportBirthdays.properties
        return i18n("name");
    }
    
    /**
     * Some information about this report
     */
    public String getInfo() {
        // a call to i18n will lookup a string with given key in ReportBirthdays.properties
        return i18n("info");
    }
    
    /**
     * Author
     */
    public String getAuthor() {
        return "Nils Meier <nils@meiers.net>, Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
    /**
     * Entry point into this report - by default reports are only run on a
     * context of type Gedcom. Depending on the logic in accepts either
     * an instance of Gedcom, Entity or Property can be passed in though.
     */
    public void start(Object context) {
        
        // assuming Gedcom
        Gedcom gedcom = (Gedcom)context;
        
        // Show months and check user's selection
        String[] months = PointInTime.getMonths(true, false);
        String selection = (String)getValueFromUser(i18n("select"),months,null);
        if (selection==null)
            return;
        
        // find out which month it was
        int month=0;
        while (month<months.length&&months[month]!=selection)
            month++;
        
        ArrayList indis = new ArrayList(gedcom.getEntities(gedcom.INDI));
        ReferenceSet births = new ReferenceSet(), marriages = new ReferenceSet(), deaths = new ReferenceSet();
        
        for(int i=0;i<indis.size();i++) {
            
            Indi indi = (Indi)indis.get(i);
            if (isSortDay) {
                if(reportBirth) {
                    if((indi.getBirthDate()!=null)&&(indi.getBirthDate().getStart()!=null))
                        addToReferenceSet(indi.getBirthDate(), indi, births, month, DAY);
                }
                if(reportMarriage) {
                    Fam[] fams = indi.getFamilies();
                    for(int j=0;j<fams.length;j++) {
                        if((fams[j].getMarriageDate()!=null)&&(fams[j].getMarriageDate().getStart()!=null))
                            addToReferenceSet(fams[j].getMarriageDate(), indi, marriages, month, DAY);
                    }
                }
                if(reportDeath) {
                    if((indi.getDeathDate()!=null)&&(indi.getDeathDate().getStart()!=null))
                        addToReferenceSet(indi.getDeathDate(), indi, deaths, month, DAY);
                }
            }
            else {
                if(reportBirth) {
                    if((indi.getBirthDate()!=null)&&(indi.getBirthDate().getStart()!=null))
                        addToReferenceSet(indi.getBirthDate(), indi, births, month, YEAR);
                }
                if(reportMarriage) {
                    Fam[] fams = indi.getFamilies();
                    for(int j=0;j<fams.length;j++) {
                        if((fams[j].getMarriageDate()!=null)&&(fams[j].getMarriageDate().getStart()!=null))
                            addToReferenceSet(fams[j].getMarriageDate(), indi, marriages, month, YEAR);
                    }
                }
                if(reportDeath) {
                    if((indi.getDeathDate()!=null)&&(indi.getDeathDate().getStart()!=null))
                        addToReferenceSet(indi.getDeathDate(), indi, deaths, month, YEAR);
                }
            }
        }
        
        if(year>0) {
            String[] msgargs = {Integer.toString(year),selection};
            println(i18n("resultYear", msgargs));
        }
        else
            println(i18n("resultAll", selection));
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
        if(reportDeath) {
            println("   "+i18n("death"));
            report(deaths);
        }
    }
    
    private void addToReferenceSet(PropertyDate date, Indi indi, ReferenceSet set, int month, int sortOrder) {
        if((date.getStart().getYear()>=year)&&(date.getStart().getMonth()==month)) {
            if(sortOrder==DAY) {
                int day = date.getStart().getDay();
                if(set.getReferences(new Integer(day)).size()==0)
                    set.add(new Integer(day), new ReferenceSet());
                ReferenceSet r = (ReferenceSet)set.getReferences(new Integer(day)).iterator().next();
                r.add(date.getStart(), indi);
            }
            if(sortOrder==YEAR)
                set.add(date.getStart(), indi);
        }
    }
    
    private void report(ReferenceSet indis) {
        Iterator it = indis.getKeys(true).iterator();
        while(it.hasNext()) {
            Object key = it.next();
            if(key instanceof PointInTime)
                output(indis, (PointInTime)key);
            if(key instanceof Integer) {
                ReferenceSet[] dates = (ReferenceSet[])indis.getReferences((Integer)key).toArray(new ReferenceSet[indis.getSize((Integer)key)]);
                for(int i=0;i<dates.length;i++) {
                    key = dates[i].getKeys().iterator().next();
                    output(dates[i], (PointInTime)key);
                }
            }
        }
    }
    
    private void output(ReferenceSet indis, PointInTime date) {
        ArrayList list = new ArrayList(indis.getReferences(date));
        for(int i=0;i<list.size();i++) {
            Indi indi = (Indi)list.get(i);
            println("      "+date+" @"+indi.getId()+"@ "+indi.getName());
        }
    }
}