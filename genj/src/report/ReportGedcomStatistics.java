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
import genj.gedcom.PropertyAge;
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
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.33 2003-10-06 19:31:25 nmeier Exp $
 * @author Francois Massonneau <fmas@celtes.com>
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 2.1
 */
public class ReportGedcomStatistics extends Report {
    
    /** number of digits allowed in the fraction portion of a number */
    public int fractionDigits = 2;
    /** if individuals should be analyzed */
    public boolean analyzeIndividuals = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportIndisToAge = true;
    /** whether the surnames should be analyzed */
    public boolean analyzeLastNames = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportIndisToLastNames = true;
    /** whether we sort last names by name or freqeuncy */
    public boolean sortLastNamesByName = true;
    /** if families should be analyzed */
    public boolean analyzeFamilies = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportIndisToMarriageAge = true;
    /** whether families with min/avg/max children should be reported */
    public int reportFamsToChildren = 1;
    /** whether occupatoins should be analyzed */
    public boolean analyzeOccupations = true;
    /** whether the occupations should be sorted by name or frequency */
    public boolean sortOccupationsByName = true;
    /** whether individuals with occucaptions should be reported */
    public boolean reportIndisToOccupations = true;
    /** if birth places should be analyzed */
    public boolean analyzeBirthPlaces = true;
    /** whether indis to birthplaces should be reported */
    public boolean reportIndisToBirthPlaces = true;
    /** whether we sort birth places by name or freqeuncy */
    public boolean sortBirthPlacesByName = true;
    /** if marriage places should be analyzed */
    public boolean analyzeMarriagePlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToMarriagePlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortMarriagePlacesByName = true;
    /** if death places should be analyzed */
    public boolean analyzeDeathPlaces = true;
    /** whether indis to deathplaces should be reported */
    public boolean reportIndisToDeathPlaces = true;
    /** whether we sort death places by name or freqeuncy */
    public boolean sortDeathPlacesByName = true;
    
    /** to store data about individuals
     * (all, males, females, unknown gender, husbands, wifes)
     */
    private static class StatisticsIndividuals {
        /** constant for indidcating all individuals
         */
        static final int ALL = 1;
        /** constant for indidcating male individuals
         */
        static final int MALES = 2;
        /** constant for indidcating female individuals
         */
        static final int FEMALES = 3;
        /** constant for indidcating indidividuals of unknown gender
         */
        static final int UNKNOWN = 4;
        
        /** which places the statistic is about (ALL|MALE|FEMALE|UNKNOWN) */
        int which = -1;
        /** number of individuals
         */
        int number = 0;
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
    
    private static class StatisticsLastNames {
        
        /** individiuals sorted by last names */
        ReferenceSet lastNamesIndis = new ReferenceSet();
        /** statistics of all, males, females, unknown gender sorted by last names */
        ReferenceSet lastNamesStatistic = new ReferenceSet();
    }
    
    private static class StatisticsOccupations {
        
        /** number of all individuals */
        int numberIndis = 0;
        /** individiuals sorted by occupations */
        ReferenceSet occupations = new ReferenceSet();
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
        /** constant for indidcating birth
         */
        static final int BIRTH = 1;
        /** constant for indidcating marriage
         */
        static final int MARRIAGE = 2;
        /** constant for indidcating death
         */
        static final int DEATH = 3;
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
    
    /** this report's version */
    public static final String VERSION = "2.1";
    
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
        if((analyzeIndividuals==false)&&(analyzeLastNames==false)&&(analyzeOccupations==false)&&(analyzeFamilies==false)&&(analyzeBirthPlaces==false)&&(analyzeMarriagePlaces==false)&&(analyzeDeathPlaces==false))
            return;
        
        Gedcom gedcom = (Gedcom)context;
        
        // what to analyze
        Entity[] indis = gedcom.getEntities(gedcom.INDI, "");
        Entity[] fams = gedcom.getEntities(Gedcom.FAM,"");
        
        // where to write the statistic data
        StatisticsIndividuals all=null, males=null, females=null, unknown=null;
        StatisticsLastNames lastNames = null;
        StatisticsOccupations occupations = null;
        StatisticsFamilies families=null;
        StatisticsPlaces births=null, marriages=null, deaths=null;
        
        // now do the desired analyzes
        if(analyzeIndividuals) {
            all = new StatisticsIndividuals();
            all.which = StatisticsIndividuals.ALL;
            males = new StatisticsIndividuals();
            males.which=StatisticsIndividuals.MALES;
            females = new StatisticsIndividuals();
            females.which=StatisticsIndividuals.FEMALES;
            unknown = new StatisticsIndividuals();
            unknown.which=StatisticsIndividuals.UNKNOWN;
            analyzeIndividuals(indis, all, males, females, unknown);
        }
        
        if(analyzeOccupations) {
            occupations = new StatisticsOccupations();
            analyzeOccupations(indis, occupations);
        }
        
        if(analyzeLastNames) {
            lastNames = new StatisticsLastNames();
            analyzeLastNames(indis, lastNames);
        }
        
        if(analyzeFamilies) {
            families = new StatisticsFamilies();
            families.number = fams.length;
            analyzeFamilies(fams, families);
        }
        
        if(analyzeBirthPlaces) {
            births = new StatisticsPlaces();
            births.which = StatisticsPlaces.BIRTH;
            analyzePlaces(indis, births);
        }
        
        if(analyzeMarriagePlaces) {
            marriages = new StatisticsPlaces();
            marriages.which = StatisticsPlaces.MARRIAGE;
            analyzePlaces(fams, marriages);
        }
        
        if(analyzeDeathPlaces) {
            deaths = new StatisticsPlaces();
            deaths.which = StatisticsPlaces.DEATH;
            analyzePlaces(indis, deaths);
        }
        
        // generate output
        println(i18n("header",gedcom.getName()));
        println();
        
        if(analyzeIndividuals)
            reportIndividuals(reportIndisToAge, null, 0, all, males, females, unknown);
        
        if(analyzeOccupations)
            reportOccupations(occupations);
        
        if(analyzeLastNames)
            reportLastNames(lastNames);
        
        if(analyzeFamilies)
            reportFamilies(families);
        
        if(analyzeBirthPlaces) {
            println(getIndent(1)+i18n("birthPlaces")+": "+new Integer(births.knownPlaces));
            reportPlaces(reportIndisToBirthPlaces, sortBirthPlacesByName, births);
        }
        
        if(analyzeMarriagePlaces) {
            println(getIndent(1)+i18n("marriagePlaces")+": "+new Integer(marriages.knownPlaces));
            reportPlaces(reportIndisToMarriagePlaces, sortMarriagePlacesByName, marriages);
        }
        
        if(analyzeDeathPlaces) {
            println(getIndent(1)+i18n("deathPlaces")+": "+new Integer(deaths.knownPlaces));
            reportPlaces(reportIndisToDeathPlaces, sortDeathPlacesByName, deaths);
        }
    }
    
    /** Calculates the average PointInTime if parameter is a range.
     *  Otherwise the "normal" point in time is returned.
     * @param d date for calculation
     * @return PointInTime average */
    PointInTime calculateAveragePointInTime(PropertyDate p) {
        
        if(p.isRange()) {
            String[] months = PointInTime.getMonths(false, true);
            PointInTime a = p.getStart(), b = p.getEnd();
            double[] age = calculateAverageAge(a.getDay()+b.getDay()+a.getMonth()*30+b.getMonth()*30+a.getYear()*360+b.getYear()*360, 2);
            // calculateAverageAge returns int[] = {year, month, day}
            return PointInTime.getPointInTime((int)age[2]+" "+months[(int)age[1]]+" "+(int)age[0]);
        }
        
        return p.getStart();
    }
    
    /** Calculates the age of a individual. Ranges are taken into
     * consideration by PointInTime.getDelta(begin, end)/2.
     * @param indi individual for the calculation
     * @param end end date for age calculation
     * @return int[] : [day, month, year] or null if <CODE>d</CODE> < <CODE>birth</CODE>
     */
    private int[] getAge(Indi indi, PropertyDate end) {
        int[] zero = {0,0,0};
        PropertyDate birth = indi.getBirthDate();
        
        // end date < birth date
        if(end.compareTo(birth)<0)
            return null;
        // end date == birth date
        if(end.compareTo(birth)==0)
            return zero;
        
        PointInTime newBirth = calculateAveragePointInTime(birth);
        PointInTime newEnd = calculateAveragePointInTime(end);
        
        return PointInTime.getDelta(newBirth, newEnd);
    }
    
    /** Rounds a number to a specified number digits in the fraction portion
     * @param number number to round
     * @param digits number of digits allowed in the fraction portion of <CODE>number</CODE>
     * @return the rounded number
     */
    private double roundNumber(double number, int digits) {
        if((Double.isNaN(number))||(Double.isInfinite(number)))
            return 0.0;
        
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
        
        Property prop=null;
        for(int i=0;i<e.length;i++) {
            
            switch(places.which) {
                
                case StatisticsPlaces.BIRTH:
                    prop = e[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
                    break;
                    
                case StatisticsPlaces.DEATH:
                    prop = e[i].getProperty("DEAT");
                    if (prop!=null)
                        prop = e[i].getProperty(new TagPath("INDI:DEAT:PLAC"));
                    break;
                    
                case StatisticsPlaces.MARRIAGE:
                    prop = e[i].getProperty("MARR");
                    if (prop!=null)
                        prop = e[i].getProperty(new TagPath("FAM:MARR:PLAC"));
                    break;
            }
            
            if (prop!=null) {
                String place = prop.getValue();
                if (place.length()>0) {
                    places.places.add(place, e[i]);
                    places.knownPlaces++;
                }
            }
        }
    }
    
    /**
     * @param indi individual to analyze
     * @param age age of <CODE>indi</CODE>
     * @param gender to store the results of <CODE>indi</CODE>
     * @param all to store results of all individuals
     */
    private void analyzeAge(Indi indi, int[] age, StatisticsIndividuals gender, StatisticsIndividuals all) {
        
        int a = age[0]*360+age[1]*30+age[2];
        if(all!=null) {
            all.age.add(new Integer(a),indi);
            all.sumAge=all.sumAge+a;
            if(a>all.maxAge)
                all.maxAge=a;
            
            if(a<all.minAge)
                all.minAge=a;
        }
        
        gender.age.add(new Integer(a),indi);
        gender.sumAge=gender.sumAge+a;
        
        if(a>gender.maxAge)
            gender.maxAge=a;
        
        if(a<gender.minAge)
            gender.minAge=a;
    }
    
    /**
     * @param all to store results for all
     * @param males to store results for males
     * @param females to store results for females
     * @param unknown to store results for unknown
     * @param e array with individuals
     */
    private void analyzeIndividuals(Entity[] e,StatisticsIndividuals all,StatisticsIndividuals males,StatisticsIndividuals females,StatisticsIndividuals unknown) {
        
        for(int i=0;i<e.length;i++) {
            
            Indi indi = (Indi)e[i];
            all.number++;
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
    
    /** @param e array with individuals
     * @param surnames to store the results */
    private void analyzeLastNames(Entity[] e, StatisticsLastNames lastNames) {
        
        String name = null;
        // sort indis by name
        for(int i=0;i<e.length;i++) {
            Indi indi = (Indi)e[i];
            if(indi.getLastName().length()==0)
                name=" ";
            else
                name=indi.getLastName();
            lastNames.lastNamesIndis.add(name, indi);
        }
        
        // analyze all individuals with same name and store the result
        Iterator it = lastNames.lastNamesIndis.getKeys(true).iterator();
        while(it.hasNext()) {
            name = (String)it.next();
            Entity[] entities = (Entity[])lastNames.lastNamesIndis.getReferences(name).toArray(new Entity[lastNames.lastNamesIndis.getSize(name)]);
            // create statistics for individuals with the same name
            StatisticsIndividuals all = new StatisticsIndividuals();
            all.which=StatisticsIndividuals.ALL;
            StatisticsIndividuals males = new StatisticsIndividuals();
            males.which=StatisticsIndividuals.MALES;
            StatisticsIndividuals females = new StatisticsIndividuals();
            females.which=StatisticsIndividuals.FEMALES;
            StatisticsIndividuals unknown = new StatisticsIndividuals();
            unknown.which=StatisticsIndividuals.UNKNOWN;
            // fill the statistics
            analyzeIndividuals(entities, all, males, females, unknown);
            // store the statistics
            lastNames.lastNamesStatistic.add(name, all);
            lastNames.lastNamesStatistic.add(name, males);
            lastNames.lastNamesStatistic.add(name, females);
            lastNames.lastNamesStatistic.add(name, unknown);
        }
    }
    
    /** @param e array with individuals
     * @param occupations to store the results */
    private void analyzeOccupations(Entity[] e, StatisticsOccupations occupations) {
        
        Property[] prop = null;
        for(int i=0;i<e.length;i++) {
            Indi indi = (Indi)e[i];
            occupations.numberIndis++;
            prop = e[i].getProperties("OCCU");
            if (prop!=null) {
                // an individual might have more than one occupation
                for(int j=0;j<prop.length;j++)
                    occupations.occupations.add(prop[j].getValue(), e[i]);
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
            
            // analyze marriage age of husband and wife
            Indi husband=fam.getHusband();
            Indi wife=fam.getWife();
            
            // birth date of husband and wife as well as marriage date must be known and fixed (no ranges)
            if (husband!=null&&wife!=null) {
              if((husband.getBirthDate()!=null)&&(wife.getBirthDate()!=null)&&(fam.getMarriageDate()!=null)) {
                  
                  int[] marriageAgeHusband = getAge(husband, fam.getMarriageDate());
                  int[] marriageAgeWife = getAge(wife, fam.getMarriageDate());
                  
                  analyzeAge(husband, marriageAgeHusband, families.husbands, null);
                  analyzeAge(wife, marriageAgeWife, families.wifes, null);
              }
            }
            
            // analyze children
            int children = fam.getNoOfChildren();
            families.children.add(new Integer(children), fam);
            
            if(children > 0)
                families.withChildren++;
            
            if(children>families.maxChildren)
                families.maxChildren=children;
            
            if(children<families.minChildren)
                families.minChildren=children;
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
     * @param printIndis whether individuals with min. / max. ages should be displayed
     */
    private void printAges(boolean printIndis, String preMin, String preAvg, String preMax, StatisticsIndividuals stats) {
        
        // min. age
        printMinMaxAge(printIndis, preMin, stats.minAge, new ArrayList(stats.age.getReferences(new Integer(stats.minAge))));
        
        // average age
        double[] age = calculateAverageAge(stats.sumAge,stats.age.getSize());
        println(getIndent(4)+i18n(preAvg)+" "+PropertyAge.getAgeString((int)age[0], (int)age[1], (int)age[2], true));
        
        // max. age
        printMinMaxAge(printIndis, preMax, stats.maxAge, new ArrayList(stats.age.getReferences(new Integer(stats.maxAge))));
    }
    
    /**
     * @param prefix e. g. "min. age:"
     * @param age to print
     * @param ages individuals with this age
     * @param printIndis whether individuals with min. / max. ages should be displayed
     */
    private void printMinMaxAge(boolean reportIndis, String prefix, int age, ArrayList ages) {
        
        double[] avg = calculateAverageAge(age,1);
        println(getIndent(4)+i18n(prefix)+" "+PropertyAge.getAgeString((int)avg[0], (int)avg[1], (int)avg[2], true));
        if(reportIndis) {
            for(int i=0;i<ages.size();i++) {
                Indi indi = (Indi)ages.get(i);
                String[] output = {indi.getId(), indi.getName()};
                println(getIndent(5)+i18n("entity", output));
            }
        }
    }
    
    /**
     * @param stats data about individuals with the same gender (males, females, unknown)
     * @param printIndis whether individuals with min. / max. ages should be displayed
     */
    private void printGender(boolean printIndis, StatisticsIndividuals stats) {
        
        if(stats.number>0) {
            String[] output = { Integer.toString(stats.age.getSize()), Double.toString(roundNumber((double)stats.age.getSize()/(double)stats.number*100,fractionDigits)) };
            println(getIndent(3)+i18n("withBirthAndDeathDates",output));
            if(stats.age.getKeys().size()>0)
                printAges(printIndis, "minAge", "avgAge", "maxAge", stats);
        }
    }
    
    /**
     * prints individuals (all, males, females, unknown gender, wifes, husbands,...
     * @param printIndis whether individuals with min. / max. ages should be displayed
     * @param lastName last name of the individuals, only needed if last names are reported, else <code>null</code>
     * @param number number of all inidividuals
     */
    private void reportIndividuals(boolean printIndis, String lastName, double number, StatisticsIndividuals all, StatisticsIndividuals males, StatisticsIndividuals females, StatisticsIndividuals unknown) {
        
        String[] str = new String[2];
        if(lastName==null) {
            println(getIndent(1)+i18n("people"));
            println(getIndent(2)+i18n("number",all.number));
        }
        else
            println(getIndent(2)+lastName+": "+all.number+" ("+roundNumber((double)all.number/number,fractionDigits)+"%)");
        
        printGender(printIndis, all);
        
        str[0] = Integer.toString(males.number);
        str[1] = Double.toString(roundNumber((double)males.number/(double)all.number*100, fractionDigits));
        println(getIndent(2)+i18n("males",str));
        //        if(lastName==null)
        printGender(printIndis, males);
        
        str[0] = Integer.toString(females.number);
        str[1] = Double.toString(roundNumber((double)females.number/(double)all.number*100, fractionDigits));
        println(getIndent(2)+i18n("females",str));
        //        if(lastName==null)
        printGender(printIndis, females);
        
        str[0] = Integer.toString(unknown.number);
        str[1] = Double.toString(roundNumber((double)unknown.number/(double)all.number*100, fractionDigits));
        println(getIndent(2)+i18n("unknown",str));
        //        if(lastName==null)
        printGender(printIndis, unknown);
        
        println();
    }
    
    /** print children of families
     * @param families data source for printing
     * @param which print only families with this number of children
     **/
    private void printChildren(StatisticsFamilies families, int which) {
        ArrayList children = new ArrayList(families.children.getReferences(new Integer(which)));
        for(int i=0;i<children.size();i++) {
            Fam fam = (Fam)children.get(i);
            String[] output = {fam.getId(), fam.toString()};
            println(getIndent(3)+i18n("entity", output));
        }
    }
    
    private void reportFamilies(StatisticsFamilies families) {
        
        println(getIndent(1)+i18n("families"));
        println(getIndent(2)+i18n("number", families.number));
        
        if(families.number>0) {
            String[] output = new String[2];
            //ages at marriage
            output[0] = Integer.toString(families.husbands.age.getSize());
            output[1] = Double.toString(roundNumber((double)families.husbands.age.getSize()/(double)families.number*100,fractionDigits));
            println(getIndent(3)+i18n("withBirthAndMarriageDates",output));
            
            //husbands
            println(getIndent(2)+i18n("husbands"));
            printAges(reportIndisToMarriageAge, "minMarriageAge", "avgMarriageAge", "maxMarriageAge", families.husbands);
            
            // wifes
            println(getIndent(2)+i18n("wifes"));
            printAges(reportIndisToMarriageAge, "minMarriageAge", "avgMarriageAge", "maxMarriageAge", families.wifes);
            
            //children
            output[0] = Integer.toString(families.withChildren);
            output[1] = Double.toString(roundNumber((double)families.withChildren/(double)families.number*100,fractionDigits));
            println(getIndent(2)+i18n("withChildren", output));
            println(getIndent(2)+i18n("minChildren",families.minChildren));
            
            if(reportFamsToChildren==2)
                printChildren(families, families.minChildren);
            
            println(getIndent(2)+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
            println(getIndent(2)+i18n("maxChildren",families.maxChildren));
            
            if(reportFamsToChildren==2)
                printChildren(families, families.maxChildren);
            
            if(reportFamsToChildren==1) {
                Iterator f = families.children.getKeys().iterator();
                while(f.hasNext()) {
                    int children = ((Integer)f.next()).intValue();
                    println(getIndent(2)+i18n("children", children));
                    printChildren(families, children);
                }
            }
            
            println();
        }
    }
    
    private void reportPlaces(boolean reportIndisToPlaces, boolean sortPlacesByName, StatisticsPlaces places) {
        
        String place = null;
        Iterator p = places.places.getKeys(sortPlacesByName).iterator();
        while(p.hasNext()) {
            place = (String)p.next();
            int number = places.places.getSize(place);
            println(getIndent(2)+place+": "+number+" ("+roundNumber((double)number/(double)places.knownPlaces*100, fractionDigits)+"%)");
            if(reportIndisToPlaces) {
                ArrayList entities = new ArrayList(places.places.getReferences(place));
                String[] output = new String[2];
                for(int i=0;i<entities.size();i++){
                    
                    if(places.which==StatisticsPlaces.MARRIAGE) {
                        Fam fam = (Fam)entities.get(i);
                        output[0] = fam.getId();
                        output[1] = fam.toString();
                    }
                    else {
                        Indi indi = (Indi)entities.get(i);
                        output[0] = indi.getId();
                        output[1] = indi.getName();
                    }
                    println(getIndent(3)+i18n("entity", output));
                }
            }
        }
        println();
    }
    
    private void reportLastNames(StatisticsLastNames lastNames) {
        
        println(getIndent(1)+i18n("lastNames",lastNames.lastNamesIndis.getKeys().size()));
        Iterator it = lastNames.lastNamesIndis.getKeys(sortLastNamesByName).iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            ArrayList stats = new ArrayList(lastNames.lastNamesStatistic.getReferences(name));
            StatisticsIndividuals all=null, males=null, females=null, unknown=null;
            for(int i=0;i<stats.size();i++) {
                StatisticsIndividuals stat = (StatisticsIndividuals)stats.get(i);
                switch(stat.which) {
                    case StatisticsIndividuals.ALL:
                        all=stat;
                        break;
                    case StatisticsIndividuals.MALES:
                        males=stat;
                        break;
                    case StatisticsIndividuals.FEMALES:
                        females=stat;
                        break;
                    case StatisticsIndividuals.UNKNOWN:
                        unknown=stat;
                        break;
                }
            }
            reportIndividuals(reportIndisToLastNames, name, lastNames.lastNamesIndis.getKeys().size(), all, males, females, unknown);
        }
    }
    
    private void reportOccupations(StatisticsOccupations occupations) {
        
        String[] output = new String[3];
        println(getIndent(1)+i18n("occupations", occupations.occupations.getKeys().size()));
        Iterator it = occupations.occupations.getKeys(sortOccupationsByName).iterator();
        while(it.hasNext()) {
            String occupation = (String)it.next();
            output[0] = occupation;
            output[1] = Integer.toString(occupations.occupations.getSize(occupation));
            output[2] = Double.toString(roundNumber((double)occupations.occupations.getSize()/(double)occupations.occupations.getSize(occupation), fractionDigits)*100);
            println(getIndent(2)+i18n("occupation", output));
            if(reportIndisToOccupations) {
                ArrayList indis = new ArrayList(occupations.occupations.getReferences(occupation));
                for(int i=0;i<indis.size();i++) {
                    Indi indi = (Indi)indis.get(i);
                    output[0] = indi.getId();
                    output[1] = indi.getName();
                    println(getIndent(3)+i18n("entity", output));
                }
            }
        }
        println();
    }
    
    /**
     * Helper that indents to given level
     */
    private String getIndent(int level) {
        int l = level;
        StringBuffer buffer = new StringBuffer(256);
        while (--level>0) {
            buffer.append("     ");
        }
        switch(l) {
            case 1:
                buffer.append(" = "); break;
            case 2:
                buffer.append(" * "); break;
            case 3:
                buffer.append(" + "); break;
            case 4:
                buffer.append(" - "); break;
            case 5:
                buffer.append(" . "); break;
        }
        return buffer.toString();
    }
    
} //ReportGedcomStatistics