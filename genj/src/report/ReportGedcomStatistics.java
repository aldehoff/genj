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
import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;
import genj.gedcom.PointInTime;
import genj.util.ReferenceSet;

import java.util.Iterator;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 * GenJ - Report
 * Note: this report requires Java2
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.31 2003-09-30 17:03:22 nmeier Exp $
 * @author Francois Massonneau <fmas@celtes.com>
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 2.0
 */
public class ReportGedcomStatistics extends Report {
    
    /** number of digits allowed in the fraction portion of a number */
    public int fractionDigits = 2;        
    /** if individuals should be analyzed */
    public boolean analyzeIndividuals = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportIndiAge = true;    
    /** if families should be analyzed */
    public boolean analyzeFamilies = true;
    /** whether families with min. / max. children should be reported */
    public boolean reportFamChildren = true;
    /** if birth places should be analyzed */
    public boolean analyzeBirthPlaces = true;
    /** if marriage places should be analyzed */
    public boolean analyzeMarriagePlaces = true;
    /** if death places should be analyzed */
    public boolean analyzeDeathPlaces = true;    
    /** whether we sort places by name or freqeuncy */
    public boolean sortByName = true;
    
    /** to store data about individuals
     * (all, males, females, unknown gender, husbands, wifes)
     */    
    private static class StatisticsIndividuals {
        /** number of individuals
         */        
        int number = 0;
        /** number of individuals with age
         */        
        int withAge = 0;
        /** individuals sorted by age
         */        
        ReferenceSet age = new ReferenceSet();
        /** min. age of individuals
         */        
        int minAge = Integer.MAX_VALUE;
        /** min. age of individuals
         */        
        int maxAge = Integer.MIN_VALUE;
        /** age of individuals added up
         */        
        int sumAge = 0;
    }
    
    /** to store data about families
     */    
    private static class StatisticsFamilies {
        /** statistics of husbands
         */        
        StatisticsIndividuals husbands = new StatisticsIndividuals();
        /** statistics of wifes
         */        
        StatisticsIndividuals wifes = new StatisticsIndividuals();
        /** number of families
         */        
        int number = 0;
        /** number of families with children
         */        
        int withChildren = 0;
        /** families sorted by number of children
         */        
        ReferenceSet children = new ReferenceSet();
        /** min. number of children
         */        
        int minChildren = 999;
        /** max. number of children
         */        
        int maxChildren = 0;
        /** number of children added up
         */        
        int sumChildren = 0;
    }
    
    /** to store data about places
     */    
    private static class StatisticsPlaces {
        /** which places the statistic is about (BIRTH||DEATh||MARRIAGE)
         */        
        int which = -1;
        /** number of known places
         */        
        int knownPlaces = 0;
        /** places sorted by name
         */        
        ReferenceSet places = new ReferenceSet();
    }
    
    /** constant for indidcating birth
     */    
    public static final int BIRTH = 1;
    /** constant for indidcating marriage
     */    
    public static final int MARRIAGE = 2;
    /** constant for indidcating death
     */    
    public static final int DEATH = 3;
    
    /** this report's version */
    public static final String VERSION = "2.0";
    
    /** Returns the version of the report
     */
    public String getVersion() {
        return VERSION;
    }
    
    /** Returns the name of this report - should be localized.
     */
    public String getName() {
        return i18n("name");
    }
    
    /**
     * Some information about this report
     * @return Information as String
     */
    public String getInfo() {
        return i18n("info");
    }
    
    /**
     * Author
     */
    public String getAuthor() {
        return "Francois Massonneau <fmas@celtes.com>, Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
    public String accepts(Object context) {
        if (context instanceof Gedcom)
            return getName();
        return null;
    }
    
    /**
     * This method actually starts this report
     */
    public void start(Object context) {
        
        // stop report when no output categories choosen
        if((analyzeIndividuals==false)&&(analyzeFamilies==false)&&(analyzeBirthPlaces==false)&&(analyzeMarriagePlaces==false)&&(analyzeDeathPlaces==false))
            return;
        
        Gedcom gedcom = (Gedcom)context;
        
        // what to analyze
        Entity[] indis = gedcom.getEntities(gedcom.INDI, "");
        Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");
        
        // where to write the statistic data
        StatisticsIndividuals all=null, males=null, females=null, unknown=null;
        StatisticsFamilies families=null;
        StatisticsPlaces births=null, marriages=null, deaths=null;
        
        if(analyzeIndividuals) {
            all = new StatisticsIndividuals();
            all.number = indis.length;
            males = new StatisticsIndividuals();
            females = new StatisticsIndividuals();
            unknown = new StatisticsIndividuals();
            analyzeIndividuals(indis, all, males, females, unknown);
        }
        
        if(analyzeFamilies) {
            families = new StatisticsFamilies();
            families.number = fams.length;
            analyzeFamilies(fams, families);
        }
        if(analyzeBirthPlaces) {
            births = new StatisticsPlaces();
            births.which = BIRTH;
            analyzePlaces(indis, births);
        }
        if(analyzeMarriagePlaces) {
            marriages = new StatisticsPlaces();
            marriages.which = MARRIAGE;
            analyzePlaces(fams, marriages);
        }
        if(analyzeDeathPlaces) {
            deaths = new StatisticsPlaces();
            deaths.which = DEATH;
            analyzePlaces(indis, deaths);
        }
        
        // generate output
        println(i18n("header",gedcom.getName()));
        println();
        
        if(analyzeIndividuals)
            reportIndividuals(all, males, females, unknown);
        
        
        if(analyzeFamilies)
            reportFamilies(families);
        
        if(analyzeBirthPlaces) {
            println("   "+i18n("birthPlaces")+": "+new Integer(births.knownPlaces));
            reportPlaces(births);
        }
        if(analyzeMarriagePlaces) {
            println("   "+i18n("marriagePlaces")+": "+new Integer(marriages.knownPlaces));
            reportPlaces(marriages);
        }
        if(analyzeDeathPlaces) {
            println("   "+i18n("deathPlaces")+": "+new Integer(deaths.knownPlaces));
            reportPlaces(deaths);
        }
    }
    
    /** Calculates the age of a individual. Ranges are taken into
     * consideration by getDelta(begin, end)/2.
     * @param indi individual for the calculation
     * @param end end date for age calculation
     * @return int[] : [day, month, year] or null if <CODE>d</CODE> < <CODE>birth</CODE>
     */    
    private int[] getAge(Indi indi, PropertyDate end) {
        
        double[] age = null;
        int[] zero = {0,0,0};
        String[] months = PointInTime.getMonths(false, true);
        PropertyDate birth = indi.getBirthDate();
        
        // end date < birth date
        if(end.compareTo(birth)<0)
            return null;
        // end date == birth date
        if(end.compareTo(birth)==0)
            return zero;
        
        PointInTime newBirth = null;
        PointInTime newEnd = null;
        
        if(birth.isRange()) {
            PointInTime a = birth.getStart(), b = birth.getEnd();
            // calculateAverageAge return int[] = {year, month, day}
            age = calculateAverageAge(a.getDay()+b.getDay()+a.getMonth()*30+b.getMonth()*30+a.getYear()*360+b.getYear()*360, 2);
            newBirth = PointInTime.getPointInTime((int)age[2]+" "+months[(int)age[1]]+" "+(int)age[0]);
        }
        else
            newBirth = birth.getStart();
        
        if(end.isRange()) {
            PointInTime a = end.getStart(), b = end.getEnd();
            // calculateAverageAge return int[] = {year, month, day}
            age = calculateAverageAge(a.getDay()+b.getDay()+a.getMonth()*30+b.getMonth()*30+a.getYear()*360+b.getYear()*360, 2);
            newEnd = PointInTime.getPointInTime((int)age[2]+" "+months[(int)age[1]]+" "+(int)age[0]);
        }
        else
            newEnd = end.getStart();
        
        return PointInTime.getDelta(newBirth, newEnd);
    }
    
    /** Rounds a number to a specified number digits in the fraction portion
     * @param number number to round
     * @param digits number of digits allowed in the fraction portion of <CODE>number</CODE>
     * @return the rounded number
     */
    private double roundNumber(double number, int digits) {
        
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(digits);
        nf.setMaximumFractionDigits(digits);
        nf.setGroupingUsed(false);
        return Double.parseDouble(nf.format(number).replace(',','.'));
    }
    
    /**
     * @param e entities to analyze
     * @param places to store results
     */    
    private void analyzePlaces(Entity[] e, StatisticsPlaces places) {
        
        for(int i=0;i<e.length;i++) {
            Object o=null;
            Property prop=null;
            
            switch(places.which) {
                
                case BIRTH:
                    prop = e[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
                    break;
                    
                case DEATH:
                    o = e[i].getProperty("DEAT");
                    if (o!=null)
                        prop = e[i].getProperty(new TagPath("INDI:DEAT:PLAC"));
                    break;
                    
                case MARRIAGE:
                    o = e[i].getProperty("MARR");
                    if (o!=null)
                        prop = e[i].getProperty(new TagPath("FAM:MARR:PLAC"));
                    break;
            }
            
            if (prop!=null) {
                String place = prop.getValue();
                if (place.length()>0) {
                    //places.places.add(place, prop);
                    places.places.add(place, e[i]);
                    places.knownPlaces++;
                }
            }
        }
    }
    
    /**
     * @param indi individual to analyze
     * @param age age of indi
     * @param gender to store results of the indi
     * @param all to store results of all indis
     */    
    private void analyzeAge(Indi indi, int[] age, StatisticsIndividuals gender, StatisticsIndividuals all) {
        
        int a = age[0]*360+age[1]*30+age[2];
        if(all!=null) {
            all.withAge++;
            all.age.add(new Integer(a),indi);
            all.sumAge=all.sumAge+a;
            if(a>all.maxAge)
                all.maxAge=a;
            
            if(a<all.minAge)
                all.minAge=a;
        }
        
        gender.withAge++;
        gender.age.add(new Integer(a),indi);
        gender.sumAge=gender.sumAge+a;
        
        if(a>gender.maxAge)
            gender.maxAge=a;
        
        if(a<gender.minAge)
            gender.minAge=a;
    }
    
    /** Analyzes the individuals
     * @param all to store results for all
     * @param males to store results for males
     * @param females to store results for females
     * @param unknown to store results for unknown
     * @param e array with individuals
     */
    private void analyzeIndividuals(Entity[] e,StatisticsIndividuals all,StatisticsIndividuals males,StatisticsIndividuals females,StatisticsIndividuals unknown) {
        
        for(int i=0;i<e.length;i++) {
            
            Indi indi = (Indi)e[i];
            int[] age = null;
            
            // get age when birth and death date are known and aren't ranges
            if((indi.getBirthDate()!=null)&&(indi.getDeathDate()!=null))
                age = getAge(indi, indi.getDeathDate());
            
            switch (indi.getSex()) {
                
                case PropertySex.MALE:
                    
                    males.number++;
                    if(age!=null)
                        analyzeAge(indi, age, males, all);
                    break;
                    
                case PropertySex.FEMALE:
                    
                    females.number++;
                    if(age!=null)
                        analyzeAge(indi, age, females, all);
                    break;
                    
                default:
                    
                    unknown.number++;
                    if(age!=null)
                        analyzeAge(indi, age, unknown, all);
                    break;
            }
        }
    }
    
    /**
     * @param families to store the result
     * @param e array with families
     */
    private void analyzeFamilies(Entity[] e, StatisticsFamilies families) {
        
        for(int i=0;i<e.length;i++) {
            Fam fam = (Fam)e[i];
            
            int children = fam.getNoOfChildren();
            if(children > 0) {
                families.withChildren++;
                families.children.add(Integer.toString(children), fam);
                
                //update number of max. children
                if(children>families.maxChildren)
                    families.maxChildren=children;
                
                //update families with min. children
                if(children<families.minChildren)
                    families.minChildren=children;
                
                // analyze marriage age of husband and wife
                Indi husband=fam.getHusband();
                Indi wife=fam.getWife();
                
                // birth date of husband and wife as well as marriage date must be known and fixed (no ranges)
                if((husband.getBirthDate()!=null)&&(wife.getBirthDate()!=null)&&(fam.getMarriageDate()!=null)) {
                    
                    int[] marriageAgeHusband = getAge(husband, fam.getMarriageDate());
                    int[] marriageAgeWife = getAge(wife, fam.getMarriageDate());
                    
                    analyzeAge(husband, marriageAgeHusband, families.husbands, null);
                    analyzeAge(wife, marriageAgeWife, families.wifes, null);
                }
            }
        }
    }
    
    /**
     * @param ages all ages added up (unit: days)
     * @param numAges number of persons added up
     * @return double[] with average age
     */    
    private double[] calculateAverageAge(double ages, double numAges) {
        
        double[] age = {0.0, 0.0, 0.0};
        
        // only calculate if paramaters != default or unvalid values
        if((numAges>0)&&(ages!=Integer.MAX_VALUE)&&(ages!=Integer.MIN_VALUE)) {
            age[0] = Math.floor(ages/360/numAges);
            ages = ages%(360*numAges);
            age[1] = Math.floor(ages/30/numAges);
            ages = ages%(30*numAges);
            age[2] = roundNumber(ages/numAges, fractionDigits);
        }
        return age;
    }
    
    /** Prints min., average, and max. age
     * @param stats to get the values from
     * @param preMin prefix for min. age, e. g. "min. age:"
     * @param preAvg prefix for average age, e. g. "avg. age:"
     * @param preMax prefix for max. age, e. g. "max. age:"
     */
    private void printAges(String preMin, String preAvg, String preMax, StatisticsIndividuals stats) {
        
        // print min. age
        printMinMaxAge(preMin, stats.minAge, new ArrayList(stats.age.getReferences(new Integer(stats.minAge))));
        
        // print average age
        double[] age = calculateAverageAge(stats.sumAge,stats.withAge);
        String[] str = {Integer.toString((int)age[0]), Integer.toString((int)age[1]), Double.toString(age[2])};
        println("            "+i18n(preAvg)+" "+i18n("ageDisplay",str));
        
        // print max. age
        printMinMaxAge(preMax, stats.maxAge, new ArrayList(stats.age.getReferences(new Integer(stats.maxAge))));
    }
    
    /**
     * @param prefix e. g. "min. age:"
     * @param age to print
     * @param ages individuals with this age
     */    
    private void printMinMaxAge(String prefix, int age, ArrayList ages) {
        
        double[] a = calculateAverageAge(age,1);
        String[] str = {Integer.toString((int)a[0]), Integer.toString((int)a[1]), Integer.toString((int)a[2])};
        println("            "+i18n(prefix)+" "+i18n("ageDisplay",str));
        if(reportIndiAge) {
            for(int i=0;i<ages.size();i++) {
                Indi indi = (Indi)ages.get(i);
                Object[] output = {indi.getId(), indi.getName()};
                println("                 "+i18n("entity", output));
            }
        }
    }
    
    /**
     * @param stats data about individuals with this gender
     */    
    private void printGender(StatisticsIndividuals stats) {
        
        String[] output = { Integer.toString(stats.withAge), Double.toString(roundNumber((double)stats.withAge/(double)stats.number*100,fractionDigits)) };
        println("         "+i18n("birthAndDeath",output));
        printAges("minAge", "avgAge", "maxAge", stats);
    }
    
    
    /**
     * @param all data about all
     * @param males data about males
     * @param females data about females
     * @param unknown data about individuals with unknown gender
     */
    private void reportIndividuals(StatisticsIndividuals all, StatisticsIndividuals males, StatisticsIndividuals females, StatisticsIndividuals unknown) {
        
        String[] str = new String[2];
        println("   "+i18n("people"));
        
        println("      "+i18n("number",all.number));
        printGender(all);
        
        str[0] = Integer.toString(males.number);
        str[1] = Double.toString(roundNumber((double)males.number/(double)all.number*100, fractionDigits));
        println("      "+i18n("males",str));
        printGender(males);
        
        str[0] = Integer.toString(females.number);
        str[1] = Double.toString(roundNumber((double)females.number/(double)all.number*100, fractionDigits));
        println("      "+i18n("females",str));
        printGender(females);
        
        str[0] = Integer.toString(unknown.number);
        str[1] = Double.toString(roundNumber((double)unknown.number/(double)all.number*100, fractionDigits));
        println("      "+i18n("unknown",str));
        printGender(unknown);
        
        println();
    }
    
    /**
     * @param fams data about families
     */
    private void reportFamilies(StatisticsFamilies fams) {
        
        ArrayList minChildren = new ArrayList(fams.children.getReferences(Integer.toString(fams.minChildren)));
        ArrayList maxChildren = new ArrayList(fams.children.getReferences(Integer.toString(fams.maxChildren)));
        
        println("   "+i18n("families"));
        
        // all
        println("      "+i18n("number",fams.number));
        println("         "+i18n("minChild",fams.minChildren));
        
        if(reportFamChildren) {
            for(int i=0;i<minChildren.size();i++) {
                Fam fam = (Fam)minChildren.get(i);

                String[] output = {fam.getId(), fam.toString()};
                println("              "+i18n("entity", output));
            }
        }
        
        println("         "+i18n("avgChild",Double.toString(roundNumber((double)fams.withChildren/(double)fams.number,fractionDigits))));
        println("         "+i18n("maxChild",fams.maxChildren));
        
        if(reportFamChildren) {
            for(int i=0;i<maxChildren.size();i++) {
                Fam fam = (Fam)maxChildren.get(i);
                Object[] output = {fam.getId(), fam.toString()};
                println("              "+i18n("entity", output));
            }
        }
        
        //ages at marriage
        String[] output = {Integer.toString(fams.husbands.withAge),Double.toString(roundNumber((double)fams.husbands.withAge/(double)fams.number*100,fractionDigits))};
        println("      "+i18n("birthAndMarriage",output));
        
        //husbands
        println("         "+i18n("husbands"));
        printAges("minMarrAge", "avgMarrAge", "maxMarrAge", fams.husbands);
        
        // wifes
        println("         "+i18n("wifes"));
        printAges("minMarrAge", "avgMarrAge", "maxMarrAge", fams.wifes);
        
        println();
    }
    
    /**
     * @param places  */
    private void reportPlaces(StatisticsPlaces places) {
        
        int number = 0;
        Iterator p = places.places.getKeys(sortByName).iterator();
        while (p.hasNext()) {
            String place = (String)p.next();
            number = places.places.getSize(place);
            println("      + "+place+": "+number+" ("+roundNumber((double)number/(double)places.knownPlaces*100, fractionDigits)+"%)");
        }
        println();
    }
} //ReportGedcomStatistics