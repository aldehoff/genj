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
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.40 2003-11-09 21:27:58 cmuessig Exp $
 * @author Francois Massonneau <fmas@celtes.com>
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 2.2
 */
public class ReportGedcomStatistics extends Report {
    
    /** number of digits allowed in the fraction portion of a number */
    public int fractionDigits = 2;
    /** if individuals should be analyzed */
    public boolean analyzeIndividuals = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportAgeToIndis = true;
    /** if families should be analyzed */
    public boolean analyzeFamilies = true;
    /** whether individuals with min. / max. marriage age should be reported */
    public boolean reportIndisToMarriageAge = true;
    
    /** whether indis with min/max age at child birth should be reported */
    public int reportFamsToChildren = 1;
    public String[] reportFamsToChildrens = { i18n("choice.all"), i18n("choice.minmax"), i18n("choice.none")};
    /** whether individuals with min. / max. age at child birth should be reported */
    public boolean reportIndisToChildBirth = true;
    /** whether the surnames should be analyzed */
    public boolean analyzeLastNames = true;
    /** whether individuals with min. / max. age should be reported */
    public boolean reportAgeToLastNames = true;
    /** whether indis with min./max. marriage should be reported */
    public boolean reportLastNamesToMarriageAge = true;
    /** whether indis with min./max. children should be reported */
    public int reportLastNamesToChildren = 2;
    public String[] reportLastNamesToChildrens = { i18n("choice.all"), i18n("choice.minmax"), i18n("choice.none")};
    /** whether indis with min./max. ages at child births should be reported */
    public boolean reportLastNamesToChildBirths = true;
    /** whether we sort last names by name or frequency */
    public boolean sortLastNamesByName = true;
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
        /** which places the statistic is about (ALL|MALE|FEMALE|UNKNOWN) */
        int which = -1;
        /** number of individuals */
        int number = 0;
        /** individuals sorted by age */
        ReferenceSet age = new ReferenceSet();
        /** individuals sorted by age at child births */
        ReferenceSet childBirthAge = new ReferenceSet();
        /** min. age of individuals at child birth */
        int minChildBirthAge = Integer.MAX_VALUE;
        /** max. age of individuals at child birth */
        int maxChildBirthAge = Integer.MIN_VALUE;
        /** number of childbirths */
        int childBirthNumber = 0;
        /** age of individuals at child birth added up */
        int sumChildBirthAge = 0;
        /** min. age of individuals */
        int minAge = Integer.MAX_VALUE;
        /** min. age of individuals */
        int maxAge = Integer.MIN_VALUE;
        /** age of individuals added up */
        int sumAge = 0;
    }
    
    /** to store data about last names */
    private static class StatisticsLastNames {
        
        /** individiuals sorted by last names */
        ReferenceSet lastNamesIndis = new ReferenceSet();
        /** statistics of all, males, females, unknown gender sorted by last names */
        ReferenceSet lastNamesStatistic = new ReferenceSet();
    }
    
    /** to store data about occupations */
    private static class StatisticsOccupations {
        
        /** number of all individuals */
        int numberIndis = 0;
        /** individiuals sorted by occupations */
        ReferenceSet occupations = new ReferenceSet();
    }
    
    /** to store data about families */
    private static class StatisticsFamilies {
        /** statistics of husbands */
        StatisticsIndividuals husbands = new StatisticsIndividuals();
        /** statistics of wifes */
        StatisticsIndividuals wifes = new StatisticsIndividuals();
        /** number of families */
        int number = 0;
        /** number of families with children */
        int withChildren = 0;
        /** families sorted by number of children */
        ReferenceSet children = new ReferenceSet();
        /** min. number of children */
        int minChildren = 999;
        /** max. number of children */
        int maxChildren = 0;
        /** number of children added up */
        int sumChildren = 0;
    }
    
    /** to store data about places */
    private static class StatisticsPlaces {
        /** which places the statistic is about (BIRTH||DEATh||MARRIAGE) */
        int which = -1;
        /** number of known places */
        int knownPlaces = 0;
        /** places sorted by name */
        ReferenceSet places = new ReferenceSet();
    }
    
    private static class Constants {
        /** all individuals */
        static final int ALL = 1;
        /** males */
        static final int MALES = 2;
        /** females */
        static final int FEMALES = 3;
        /** unknown gender */
        static final int UNKNOWN = 4;
        /** indis */
        static final int INDIS = 5;
        /** childbirth */
        static final int CHILDBIRTH = 6;
        /** marriage*/
        static final int MARRIAGE = 7;
        /** birth  */
        static final int BIRTH = 8;
        /** death */
        static final int DEATH = 9;
    }
    
    /** this report's version */
    public static final String VERSION = "2.2";
    
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
            all.which = Constants.ALL;
            males = new StatisticsIndividuals();
            males.which=Constants.MALES;
            females = new StatisticsIndividuals();
            females.which=Constants.FEMALES;
            unknown = new StatisticsIndividuals();
            unknown.which=Constants.UNKNOWN;
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
            analyzeFamilies(fams, null, families);
        }
        
        if(analyzeBirthPlaces) {
            births = new StatisticsPlaces();
            births.which = Constants.BIRTH;
            analyzePlaces(indis, births);
        }
        
        if(analyzeMarriagePlaces) {
            marriages = new StatisticsPlaces();
            marriages.which = Constants.MARRIAGE;
            analyzePlaces(fams, marriages);
        }
        
        if(analyzeDeathPlaces) {
            deaths = new StatisticsPlaces();
            deaths.which = Constants.DEATH;
            analyzePlaces(indis, deaths);
        }
        
        // generate output
        println(i18n("header",gedcom.getName()));
        println();
        
        if(analyzeIndividuals) {
            int i;
            if(reportAgeToIndis)
                i=1;
            else
                i=3;
            reportIndividuals(i, null, 0, all, males, females, unknown);
        }
        
        if(analyzeFamilies)
            reportFamilies(families, reportIndisToMarriageAge, reportFamsToChildren, reportIndisToChildBirth, false);
        
        if(analyzeLastNames)
            reportLastNames(lastNames, indis.length);
        
        if(analyzeOccupations)
            reportOccupations(occupations);
        
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
            int[] age = calculateAverageAge(a.getDay()+b.getDay()+a.getMonth()*30+b.getMonth()*30+a.getYear()*360+b.getYear()*360, 2);
            // calculateAverageAge returns int[] = {year, month, day}
            return PointInTime.getPointInTime(age[2]+" "+months[age[1]]+" "+age[0]);
        }
        
        return p.getStart();
    }
    
    /** Calculates the age of a individual. Ranges are taken into
     * consideration by PointInTime.getDelta(begin, end)/2.
     * @param indi individual for the calculation
     * @param end end date for age calculation
     * @return int[] : [day, month, year] or null if <CODE>d</CODE> < <CODE>birth</CODE>
     */
    private int[] getAge(Indi indi, PropertyDate end, int which) {
        String message = null;
        int[] zero = {0,0,0};
        PropertyDate birth = indi.getBirthDate();
        
        // end date < birth date
        if(end.compareTo(birth)<0) {
            switch(which) {
                case Constants.MARRIAGE:
                    message = i18n("warningMarriage");
                    break;
                case Constants.CHILDBIRTH:
                    message = i18n("warningChildBirth");
                    break;
                case Constants.DEATH:
                    message = i18n("warningDeath");
                    break;
            }
            println(message+": @"+indi.getId()+"@ "+indi.getName()+" "+end+" <= "+birth);
            return null;
        }
        
        // end date == birth date
        if(end.compareTo(birth)==0) {
            switch(which) {
                case Constants.MARRIAGE:
                    message = i18n("warningMarriage");
                    break;
                case Constants.CHILDBIRTH:
                    message = i18n("warningChildBirth");
                    break;
            }
            if(which!=Constants.DEATH)
                println(message+": @"+indi.getId()+"@ "+indi.getName()+" "+end+" <= "+birth);
            return zero;
        }
        
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
                
                case Constants.BIRTH:
                    prop = e[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
                    break;
                    
                case Constants.DEATH:
                    prop = e[i].getProperty("DEAT");
                    if (prop!=null)
                        prop = e[i].getProperty(new TagPath("INDI:DEAT:PLAC"));
                    break;
                    
                case Constants.MARRIAGE:
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
    private void analyzeAge(Indi indi, int[] age, StatisticsIndividuals single, StatisticsIndividuals all, int which) {
        
        int a = age[0]*360+age[1]*30+age[2];
        switch(which) {
            case Constants.INDIS:
            case Constants.MARRIAGE:
                
                if(all!=null) {
                    all.age.add(new Integer(a),indi);
                    all.sumAge=all.sumAge+a;
                    if(a>all.maxAge)
                        all.maxAge=a;
                    
                    if(a<all.minAge)
                        all.minAge=a;
                }
                
                single.age.add(new Integer(a),indi);
                single.sumAge=single.sumAge+a;
                
                if(a>single.maxAge)
                    single.maxAge=a;
                
                if(a<single.minAge)
                    single.minAge=a;
                break;
                
            case Constants.CHILDBIRTH:
                if(a < single.minChildBirthAge)
                    single.minChildBirthAge = a;
                if(a > single.maxChildBirthAge)
                    single.maxChildBirthAge = a;
                
                single.childBirthNumber++;
                single.sumChildBirthAge = single.sumChildBirthAge + a;
                single.childBirthAge.add(new Integer(a), indi);
                break;
        }
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
                age = getAge(indi, indi.getDeathDate(), Constants.DEATH);
            
            switch (indi.getSex()) {
                
                case PropertySex.MALE:
                    males.number++;
                    if(age!=null)
                        analyzeAge(indi, age, males, all, Constants.INDIS);
                    break;
                    
                case PropertySex.FEMALE:
                    females.number++;
                    if(age!=null)
                        analyzeAge(indi, age, females, all, Constants.INDIS);
                    break;
                    
                default:
                    unknown.number++;
                    if(age!=null)
                        analyzeAge(indi, age, unknown, all, Constants.INDIS);
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
            lastNames.lastNamesIndis.add(indi.getLastName(), indi);//name, indi);
        }
        
        // analyze all individuals with same name and store the result
        Iterator it = lastNames.lastNamesIndis.getKeys(true).iterator();
        ArrayList familiesToLastName = new ArrayList();
        while(it.hasNext()) {
            familiesToLastName.clear();
            name = (String)it.next();
            // indis with the same last name
            Entity[] entities = (Entity[])lastNames.lastNamesIndis.getReferences(name).toArray(new Entity[lastNames.lastNamesIndis.getSize(name)]);
            // find families in which indis with the same last name are involved
            for(int i=0;i<entities.length;i++) {
                Indi indi = (Indi)entities[i];
                if(indi.getNoOfFams() > 0) {
                    Fam[] fams = indi.getFamilies();
                    for(int j=0;j<fams.length;j++)
                        familiesToLastName.add(fams[j]);
                }
            }
            // create statistics for individuals with the same last name
            StatisticsFamilies families = new StatisticsFamilies();
            families.number = familiesToLastName.size();
            StatisticsIndividuals all = new StatisticsIndividuals();
            all.which=Constants.ALL;
            StatisticsIndividuals males = new StatisticsIndividuals();
            males.which=Constants.MALES;
            StatisticsIndividuals females = new StatisticsIndividuals();
            females.which=Constants.FEMALES;
            StatisticsIndividuals unknown = new StatisticsIndividuals();
            unknown.which=Constants.UNKNOWN;
            // fill the statistics
            analyzeIndividuals(entities, all, males, females, unknown);
            analyzeFamilies((Entity[])familiesToLastName.toArray(new Entity[familiesToLastName.size()]), name, families);
            // store the statistics
            lastNames.lastNamesStatistic.add(name, all);
            lastNames.lastNamesStatistic.add(name, males);
            lastNames.lastNamesStatistic.add(name, females);
            lastNames.lastNamesStatistic.add(name, unknown);
            lastNames.lastNamesStatistic.add(name, families);
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
    private void analyzeFamilies(Entity[] e, String lastName, StatisticsFamilies families) {
        
        int age[] = null;
        for(int i=0;i<e.length;i++) {
            Fam fam = (Fam)e[i];
            
            // analyze marriage age of husband and wife
            Indi husband=fam.getHusband();
            Indi wife=fam.getWife();
            
            if(fam.getMarriageDate()!=null) {
                if((husband!=null)&&(husband.getBirthDate()!=null)&&((lastName==null)||husband.getLastName().equals(lastName))){
                    age = getAge(husband, fam.getMarriageDate(), Constants.MARRIAGE);
                    if(age!=null)
                        analyzeAge(husband, age, families.husbands, null, Constants.MARRIAGE);
                }
                if((wife!=null)&&(wife.getBirthDate()!=null)&&((lastName==null)||wife.getLastName().equals(lastName)))
                    age= getAge(wife, fam.getMarriageDate(), Constants.MARRIAGE);
                if(age!=null)
                    analyzeAge(wife, age, families.wifes, null, Constants.MARRIAGE);
            }
            
            // analyze ages at child births
            Indi[] children = fam.getChildren();
            
            for(int j=0;j<children.length;j++) {
                if((children[j].getBirthDate()!=null)) {
                    if ((husband!=null)&&(husband.getBirthDate()!=null)&&((lastName==null)||(husband.getLastName().equals(lastName)))) {
                        age = getAge(husband, children[j].getBirthDate(), Constants.CHILDBIRTH);
                        if(age!=null)
                            analyzeAge(husband, age, families.husbands, null, Constants.CHILDBIRTH);
                    }
                    if ((wife!=null)&&(wife.getBirthDate()!=null)&&((lastName==null)||(wife.getLastName().equals(lastName)))) {
                        age = getAge(wife, children[j].getBirthDate(), Constants.CHILDBIRTH);
                        if(age!=null)
                            analyzeAge(wife, age, families.wifes, null, Constants.CHILDBIRTH);
                    }
                }
            }
            
            // analyze number of children
            families.children.add(new Integer(children.length), fam);
            
            if(children.length > 0)
                families.withChildren++;
            
            if(children.length>families.maxChildren)
                families.maxChildren=children.length;
            
            if(children.length<families.minChildren)
                families.minChildren=children.length;
        }
    }
    
    /**
     * @param ages all ages added up (unit: days)
     * @param numAges number of persons added up
     * @return double[] with average age
     */
    private int[] calculateAverageAge(double ages, double numAges) {
        
        int[] age = {0, 0, 0};
        
        // only calculate if paramaters != default or unvalid values
        if((numAges>0)&&(ages!=Integer.MAX_VALUE)&&(ages!=Integer.MIN_VALUE)) {
            age[0] = (int)roundNumber(Math.floor(ages/360/numAges),0);
            ages = ages%(360*numAges);
            age[1] = (int)roundNumber(Math.floor(ages/30/numAges),0);
            ages = ages%(30*numAges);
            age[2] = (int)roundNumber(ages/numAges, 0);
        }
        return age;
    }
    
    /** Prints min., average, and max. age
     * @param stats to get the values from
     * @param preMin prefix for min. age, e. g. "min. age:"
     * @param preAvg prefix for average age, e. g. "avg. age:"
     * @param preMax prefix for max. age, e. g. "max. age:"
     * @param printIndis 1=all, 2=min./max. age, 3=none
     * @param indent how much indent has to be printed
     */
    private void printAges(int printIndis, int indent, StatisticsIndividuals stats, int which) {
        
        int[] age;
        
        switch(which) {
            case Constants.INDIS:
            case Constants.MARRIAGE:
                if(stats.age.getKeys().size()>0) {
                    // min. age
                    printMinMaxAge(printIndis, indent, "minAge", stats.minAge, new ArrayList(stats.age.getReferences(new Integer(stats.minAge))));
                    // average age
                    age = calculateAverageAge(stats.sumAge,stats.age.getSize());
                    println(getIndent(indent)+i18n("avgAge")+" "+PropertyAge.getAgeString(age[0], age[1], age[2], true));
                    // max. age
                    printMinMaxAge(printIndis, indent, "maxAge", stats.maxAge, new ArrayList(stats.age.getReferences(new Integer(stats.maxAge))));
                }
                else
                    println(getIndent(indent)+i18n("missingData"));
                break;
            case Constants.CHILDBIRTH:
                if(stats.childBirthAge.getKeys().size()>0) {
                    // min. age
                    printMinMaxAge(printIndis, indent, "minAge", stats.minChildBirthAge, new ArrayList(stats.childBirthAge.getReferences(new Integer(stats.minChildBirthAge))));
                    // avg age
                    age = calculateAverageAge(stats.sumChildBirthAge,stats.childBirthNumber);
                    println(getIndent(indent)+i18n("avgAge")+" "+PropertyAge.getAgeString(age[0], age[1], age[2], true));
                    // max. age
                    printMinMaxAge(printIndis, indent, "maxAge", stats.maxChildBirthAge, new ArrayList(stats.childBirthAge.getReferences(new Integer(stats.maxChildBirthAge))));
                }
                else
                    println(getIndent(indent)+i18n("missingData"));
                break;
        }
    }
    
    /**
     * @param prefix e. g. "min. age:"
     * @param age to print
     * @param ages individuals with this age
     * @param indent how much indent has to be printed
     * @param printIndis 1=all, 2=min./max. age, 3=none
     */
    private void printMinMaxAge(int reportIndis, int indent, String prefix, int age, ArrayList ages) {
        
        int[] avg = calculateAverageAge(age,1);
        println(getIndent(indent)+i18n(prefix)+" "+PropertyAge.getAgeString(avg[0], avg[1], avg[2], true));
        if(reportIndis<3) {
            for(int i=0;i<ages.size();i++) {
                Indi indi = (Indi)ages.get(i);
                String[] output = {indi.getId(), indi.getName()};
                println(getIndent(indent+1)+i18n("entity", output));
            }
        }
    }
    
    /**
     * prints individuals (all, males, females, unknown gender, wifes, husbands,...
     * @param printIndis whether individuals with min. / max. ages should be displayed
     * @param lastName last name of the individuals, only needed if last names are reported, else <code>null</code>
     * @param numberAllIndis number of all inidividuals (needed for calculations when last names are reported)
     */
    private void reportIndividuals(int printIndis, String lastName, double numberAllIndis, StatisticsIndividuals all, StatisticsIndividuals males, StatisticsIndividuals females, StatisticsIndividuals unknown) {
        
        String[] str = new String[2];
        int indent;
        if(lastName==null) {
            println(getIndent(1)+i18n("people"));
            println(getIndent(2)+i18n("people",all.number));
            indent=3;
        }
        else {
            println(getIndent(2)+lastName);
            println(getIndent(3)+i18n("ages"));
            println(getIndent(4)+i18n("number",all.number)+" ("+roundNumber((double)all.number/(double)numberAllIndis*100, fractionDigits)+"%)");
            indent=5;
        }
        
        printAges(printIndis, indent, all, Constants.INDIS);
        
        str[0] = Integer.toString(males.number);
        str[1] = Double.toString(roundNumber((double)males.number/(double)all.number*100, fractionDigits));
        println(getIndent(indent-1)+i18n("males",str));
        printAges(printIndis, indent, males, Constants.INDIS);
        
        str[0] = Integer.toString(females.number);
        str[1] = Double.toString(roundNumber((double)females.number/(double)all.number*100, fractionDigits));
        println(getIndent(indent-1)+i18n("females",str));
        printAges(printIndis, indent, females, Constants.INDIS);
        
        str[0] = Integer.toString(unknown.number);
        str[1] = Double.toString(roundNumber((double)unknown.number/(double)all.number*100, fractionDigits));
        println(getIndent(indent-1)+i18n("unknown",str));
        printAges(printIndis, indent, unknown, Constants.INDIS);
        
        if(lastName==null)
            println();
    }
    
    /** print children of families
     * @param families data source for printing
     * @param which print only families with this number of children
     **/
    private void printChildren(StatisticsFamilies families, int which, int indent) {
        ArrayList children = new ArrayList(families.children.getReferences(new Integer(which)));
        for(int i=0;i<children.size();i++) {
            Fam fam = (Fam)children.get(i);
            String[] output = {fam.getId(), fam.toString()};
            println(getIndent(indent+2)+i18n("entity", output));
        }
    }
    
    private void reportFamilies(StatisticsFamilies families, boolean reportIndisToMarriageAge, int reportFamsToChildren, boolean reportIndisToChildBirths, boolean lastName) {
        
        int i = -1, j = -1, indent = -1;
        if(reportIndisToMarriageAge)
            i=1;
        else
            i=3;
        
        if(reportIndisToChildBirth)
            j=1;
        else
            j=3;
        
        if(lastName==false) {
            println(getIndent(1)+i18n("families")+": "+families.number);
            indent = 2;
        }
        else
            indent = 3;
        
        if(families.number>0) {
            //ages at marriage
            println(getIndent(indent)+i18n("ageAtMarriage"));
            //husbands
            println(getIndent(indent+1)+i18n("husbands"));
            printAges(i, indent+2, families.husbands, Constants.MARRIAGE);
            // wifes
            println(getIndent(indent+1)+i18n("wifes"));
            printAges(i, indent+2, families.wifes, Constants.MARRIAGE);
            
            //children
            String[] output = { Integer.toString(families.withChildren), Double.toString(roundNumber((double)families.withChildren/(double)families.number*100,fractionDigits)) };
            println(getIndent(indent)+i18n("withChildren", output));
            
            switch(reportFamsToChildren) {
                case 0:
                    println(getIndent(indent+1)+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    Iterator f = families.children.getKeys().iterator();
                    while(f.hasNext()) {
                        int children = ((Integer)f.next()).intValue();
                        println(getIndent(indent+1)+i18n("children")+": "+children);
                        printChildren(families, children, indent);
                    }
                    break;
                case 1:
                    println(getIndent(indent+1)+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    println(getIndent(indent+1)+i18n("minChildren",families.minChildren));
                    printChildren(families, families.minChildren, indent);
                    println(getIndent(indent+1)+i18n("maxChildren",families.maxChildren));
                    printChildren(families, families.maxChildren, indent);
                    break;
                case 2:
                    println(getIndent(indent+1)+i18n("minChildren",families.minChildren));
                    println(getIndent(indent+1)+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    println(getIndent(indent+1)+i18n("maxChildren",families.maxChildren));
                    break;
            }
            
            //ages at child birth
            println(getIndent(indent)+i18n("agesAtChildBirths"));
            //husbands
            println(getIndent(indent+1)+i18n("husbands"));
            printAges(j, indent+2, families.husbands, Constants.CHILDBIRTH);
            //wifes
            println(getIndent(indent+1)+i18n("wifes"));
            printAges(j, indent+2, families.wifes, Constants.CHILDBIRTH);
        }
        
        if(lastName==false)
            println();
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
                    
                    if(places.which==Constants.MARRIAGE) {
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
    
    private void reportLastNames(StatisticsLastNames lastNames, int numberAllIndis) {
        
        String[] output = { Integer.toString(lastNames.lastNamesIndis.getKeys().size()), Integer.toString(numberAllIndis) };
        println(getIndent(1)+i18n("lastNames", output));
        Iterator it = lastNames.lastNamesIndis.getKeys(sortLastNamesByName).iterator();
        while(it.hasNext()) {
            String name = (String)it.next();
            ArrayList stats = new ArrayList(lastNames.lastNamesStatistic.getReferences(name));
            StatisticsIndividuals all=null, males=null, females=null, unknown=null;
            StatisticsFamilies families = null;
            for(int i=0;i<stats.size();i++) {
                Object stat = stats.get(i);
                if(stat instanceof StatisticsIndividuals) {
                    switch(((StatisticsIndividuals)stat).which) {
                        case Constants.ALL:
                            all=(StatisticsIndividuals)stat;
                            break;
                        case Constants.MALES:
                            males=(StatisticsIndividuals)stat;
                            break;
                        case Constants.FEMALES:
                            females=(StatisticsIndividuals)stat;
                            break;
                        case Constants.UNKNOWN:
                            unknown=(StatisticsIndividuals)stat;
                            break;
                    }
                }
                else
                    families = (StatisticsFamilies)stat;
            }
            int i;
            if(reportAgeToLastNames)
                i=1;
            else
                i=3;
            reportIndividuals(i, name, numberAllIndis, all, males, females, unknown);
            reportFamilies(families, reportLastNamesToMarriageAge, reportLastNamesToChildren, reportLastNamesToChildBirths, true);
        }
    }
    
    private void reportOccupations(StatisticsOccupations occupations) {
        
        String[] output = new String[3];
        println(getIndent(1)+i18n("occupations"));
        println(getIndent(2)+i18n("number", occupations.occupations.getKeys().size()));
        Iterator it = occupations.occupations.getKeys(sortOccupationsByName).iterator();
        while(it.hasNext()) {
            String occupation = (String)it.next();
            output[0] = occupation;
            output[1] = Integer.toString(occupations.occupations.getSize(occupation));
            output[2] = Double.toString(roundNumber((double)occupations.occupations.getSize(occupation)/(double)occupations.occupations.getSize()*100, fractionDigits));
            println(getIndent(3)+i18n("occupation", output));
            if(reportIndisToOccupations) {
                ArrayList indis = new ArrayList(occupations.occupations.getReferences(occupation));
                for(int i=0;i<indis.size();i++) {
                    Indi indi = (Indi)indis.get(i);
                    output[0] = indi.getId();
                    output[1] = indi.getName();
                    println(getIndent(4)+i18n("entity", output));
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
            default:
                buffer.append("   "); break;
        }
        return buffer.toString();
    }
} //ReportGedcomStatistics