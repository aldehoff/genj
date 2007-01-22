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
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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

    /**
     * Main for argument Gedcom
     */
    public void start(Gedcom gedcom) {

        // check that something is selected
        if ((!reportBirth) && (!reportBaptism) && (!reportDeath) && (!reportMarriage) && (!reportDivorce) && (!reportEmigration) && (!reportImmigration) && (!reportNaturalization))
            return;

        // collect all individuals/families per event
        ArrayList
        births = new ArrayList(),
        baptisms = new ArrayList(),
        marriages = new ArrayList(),
        divorces = new ArrayList(),
        emigrations = new ArrayList(),
        immigrations = new ArrayList(),
        naturalizations = new ArrayList(),
        deaths = new ArrayList();


        // loop individuals
        for (Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator(); indis.hasNext(); ) {
            analyze((Indi)indis.next(), births, baptisms, marriages, divorces, emigrations, immigrations, naturalizations, deaths);
        }


        // output results
        println(PropertySex.TXT_SEX + ": " + sexs[sex]);
        println(Delta.TXT_DAY + ": " + day);
        println(Delta.TXT_MONTH + ": " + month);
        println(Delta.TXT_YEAR + ": " +year);
        println();

        if (reportBirth&&!births.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("BIRT"));
            report(births);
            println();
        }
        if (reportBaptism&&!baptisms.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("BAPM"));
            report(baptisms);
            println();
        }
        if (reportMarriage&&!marriages.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("MARR"));
            report(marriages);
            println();
        }
        if (reportDivorce&&!divorces.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("DIV"));
            report(divorces);
            println();
        }
        if (reportEmigration&&!emigrations.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("EMIG"));
            report(emigrations);
            println();
        }
        if (reportImmigration&&!immigrations.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("IMMI"));
            report(immigrations);
            println();
        }
        if (reportNaturalization&&!naturalizations.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("NATU"));
            report(naturalizations);
            println();
        }
        if (reportDeath&&!deaths.isEmpty()) {
            println(getIndent(2) + Gedcom.getName("DEAT"));
            report(deaths);
        }

        // done
    }

    /**
     * Analyze one individual
     */
    private void analyze(Indi indi, ArrayList births, ArrayList baptisms, ArrayList marriages, ArrayList divorces, ArrayList emigrations, ArrayList immigrations, ArrayList naturalizations, ArrayList deaths) {

        // consider dead?
        if (!isShowDead && indi.getDeathDate() != null && indi.getDeathDate().isValid())
            return;

        if(checkSex(indi)==false)
            return;

        // look for births?
        if (reportBirth) {
            if (checkDate(indi.getBirthDate()))
                births.add(new Hit(indi.getBirthDate(), indi, ""));
        }

        if(reportBaptism) {
            analyzeTag(indi, "INDI:BAPM", baptisms, true);
            analyzeTag(indi, "INDI:BAPL", baptisms, true);
            analyzeTag(indi, "INDI:CHR", baptisms, true);
            analyzeTag(indi, "INDI:CHRA", baptisms, true);
        }

        // look for marriages?
        if (reportMarriage) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            for (int j = 0; j < fams.length; j++) {
                Fam fam = fams[j];
                if (checkDate(fam.getMarriageDate())) {
                  Hit hit = new Hit(fam.getMarriageDate(), fam, "");
                  if (!marriages.contains(hit)) marriages.add(hit);
                }
            }
        }

        // look for divorces?
        if (reportDivorce) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            for (int j = 0; j < fams.length; j++) {
                Fam fam = fams[j];
                if (checkDate(fam.getDivorceDate())) {
                  Hit hit = new Hit(fam.getDivorceDate(), fam, "");
                  if (!divorces.contains(hit)) divorces.add(hit);
                }
            }
        }

        if(reportEmigration)
            analyzeTag(indi, "INDI:EMIG", emigrations, false);

        if(reportImmigration)
            analyzeTag(indi, "INDI:IMMI", immigrations, false);

        if(reportNaturalization)
            analyzeTag(indi, "INDI:NATU", naturalizations, false);

        // look for deaths?
        if (reportDeath) {
            if (checkDate(indi.getDeathDate()))
                deaths.add(new Hit(indi.getDeathDate(), indi, ""));
        }

        // done
    }

    private void analyzeTag(Indi indi, String tag, ArrayList list, boolean saveTag) {
        if((indi.getProperty(new TagPath(tag))!=null) && (indi.getProperty(new TagPath(tag+":DATE"))!=null)) {
            PropertyDate prop = (PropertyDate)indi.getProperty(new TagPath(tag+":DATE"));
            if(checkDate(prop)) {
                if(saveTag)
                    list.add(new Hit(prop, indi, tag));
                else
                    list.add(new Hit(prop, indi, ""));
            }
        }
    }

    /**
     * Output a list of hits
     */
    private void report(ArrayList hits) {

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
            String tag = "";
            if(hit.tag.length() > 0)
                tag = hit.tag+": ";
            println(getIndent(3) + tag + hit.when + " " + indi.toString());
        }
        if (hit.who instanceof Fam) {
            Fam fam = (Fam) hit.who;
            println(getIndent(3) + hit.when + " " + fam.toString());
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

    /**
     * Wrapping an Event hit
     */
    private class Hit implements Comparable {
        String tag;
        PropertyDate date;
        PointInTime when;
        Entity who;
        PointInTime compare;
        // Constructor
        Hit(PropertyDate d, Entity e, String p) {
          date = d;
          tag = p;
          when = date.getStart();
          if (isSortDay)
            // blocking out year (to a Gregorian LEAP 4 - don't want to make it invalid) so that month and day count
            compare = new PointInTime(when.getDay(), when.getMonth(), 4, when.getCalendar());
          else
            compare = when;
          who = e;
        }
        // comparison
        public int compareTo(Object object) {
            return compare.compareTo(((Hit)object).compare);
        }
        // equals
        public boolean equals(Object that) {
          return date==((Hit)that).date;
        }
    } //Hit

} //ReportEvents
