/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PointInTime;
import genj.gedcom.PropertyDate;
import genj.report.Report;

import java.util.Comparator;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @author Carsten Müssig carsten.muessig@gmx.net
 */
public class ReportBirthdays extends Report {
    
    /** whether we sort by day-of-month or date */
    public boolean isSortDay = true;
    
    /** persons to report
     * year > 0 : persons born in year or later
     * year <= 0: all persons
     */
    public int startYear = 0;
    
    /** this report's version */
    public static final String VERSION = "0.3";
    
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
        
        // Loop through individuals - folks with birthdays in given month
        Entity[] indis;
        if (isSortDay) {
            
            // by day of month
            Comparator comparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    // O.K. here are the birthdays (might be null!)
                    PropertyDate b1 = ((Indi)o1).getBirthDate();
                    PropertyDate b2 = ((Indi)o2).getBirthDate();
                    
                    // So we check whether we can get the day information
                    int
                    d1 = b1!=null ? b1.getStart().getDay() : 0,
                    d2 = b2!=null ? b2.getStart().getDay() : 0;
                    
                    // Comparison at last
                    return d1-d2;
                }
            }; //Comparator
            
            indis = gedcom.getEntities(gedcom.INDI, comparator);
            
        } else {
            
            // by date
            indis = gedcom.getEntities(gedcom.INDI, "INDI:BIRT:DATE");
            
        }
        
        if(startYear>0) {
            String[] msgargs = {Integer.toString(startYear),selection};
            println(i18n("resultYear", msgargs));
        }
        else
            println(i18n("resultAll", selection));
        println();
        
        for (int i=0;i<indis.length;i++) {
            
            Indi indi = (Indi)indis[i];
            
            PropertyDate birth = indi.getBirthDate();
            if (birth==null)
                continue;
            
            if (birth.getStart().getMonth() != month)
                continue;
            
            if ((startYear!=0)&&(birth.getStart().getYear() < startYear))
                continue;
            
            String[] msgargs = {indi.getId(),indi.getName(),indi.getBirthDate()+""};
            println(i18n("format",msgargs));
        }
        
        // Done
    }    
} //ReportBirthdays