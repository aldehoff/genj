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
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.report.Report;
import genj.util.ReferenceSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * GenJ - Report
 * Note: this report requires Java2
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.53 2004-03-14 22:17:42 cmuessig Exp $
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
    
    /** if baptism places should be analyzed */
    public boolean analyzeBaptismPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToBaptismPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortBaptismPlacesByName = true;
    
    /** if marriage places should be analyzed */
    public boolean analyzeMarriagePlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToMarriagePlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortMarriagePlacesByName = true;
    
    /** if emigration places should be analyzed */
    public boolean analyzeEmigrationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToEmigrationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortEmigrationPlacesByName = true;
    
    /** if immigration places should be analyzed */
    public boolean analyzeImmigrationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToImmigrationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortImmigrationPlacesByName = true;
    
    /** if naturalization places should be analyzed */
    public boolean analyzeNaturalizationPlaces = true;
    /** whether indis to marriageplaces should be reported */
    public boolean reportIndisToNaturalizationPlaces = true;
    /** whether we sort marriage places by name or freqeuncy */
    public boolean sortNaturalizationPlacesByName = true;
    
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
        /** which places the statistic is about (BIRTH|BAPTISM|MARRIAGE|EMIGRATION|IMMIGRATION|NATURALIZATION|DEATH) */
        int which = -1;
        /** entities with known places */
        int entitiesWithKnownPlaces = 0;
        /** places sorted by name */
        ReferenceSet places = new ReferenceSet();
    }
    
    // constants for statistics of individuals
    private static final int ALL = 1;
    private static final int MALES = 2;
    private static final int FEMALES = 3;
    private static final int UNKNOWN = 4;
    
    // constants for analyze, report and print methods
    private static final int INDIS = 5;
    private static final int CHILDBIRTH = 6;
    
    // constants for statistics of places
    private static final int BIRTH = 7;
    private static final int BAPTISM = 8;
    private static final int MARRIAGE = 9;
    private static final int EMIGRATION = 10;
    private static final int IMMIGRATION = 11;
    private static final int NATURALIZATION = 12;
    private static final int DEATH = 13;
    
    /** for indent calculation */
    private static final int SPACES_PER_LEVEL = 5;
    private static final String FRONT_FIRST_LEVEL = " = ";
    private static final String FRONT_SECOND_LEVEL = " * ";
    private static final String FRONT_THIRD_LEVEL = " + ";
    private static final String FRONT_FOURTH_LEVEL = " - ";
    private static final String FRONT_FIFTH_LEVEL = " . ";
    private static final String FRONT_SIXTH_LEVEL = "   ";
    
    /** this report's version */
    public static final String VERSION = "2.3";
    
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
        if((analyzeIndividuals==false)&&(analyzeLastNames==false)&&
        (analyzeOccupations==false)&&(analyzeFamilies==false)&&
        (analyzeBirthPlaces==false)&&(analyzeBaptismPlaces==false)&&
        (analyzeMarriagePlaces==false)&&(analyzeEmigrationPlaces==false)&&
        (analyzeImmigrationPlaces==false)&&(analyzeNaturalizationPlaces==false)&&
        (analyzeDeathPlaces==false))
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
        StatisticsPlaces births=null, baptisms=null, marriages=null, emigrations=null, immigrations=null, naturalizations=null, deaths=null;
        
        // now do the desired analyzes
        if(analyzeIndividuals) {
            all = new StatisticsIndividuals();
            all.which = ALL;
            males = new StatisticsIndividuals();
            males.which= MALES;
            females = new StatisticsIndividuals();
            females.which= FEMALES;
            unknown = new StatisticsIndividuals();
            unknown.which= UNKNOWN;
            analyzeIndividuals(indis, all, males, females, unknown);
        }
        
        if(analyzeFamilies) {
            families = new StatisticsFamilies();
            families.number = fams.length;
            analyzeFamilies(fams, null, families);
        }
        
        if(analyzeLastNames) {
            lastNames = new StatisticsLastNames();
            analyzeLastNames(indis, lastNames);
        }
        
        if(analyzeOccupations) {
            occupations = new StatisticsOccupations();
            analyzeOccupations(indis, occupations);
        }
        
        if(analyzeBirthPlaces) {
            births = new StatisticsPlaces();
            births.which = BIRTH;
            analyzePlaces(indis, births);
        }
        
        if(analyzeBaptismPlaces) {
            baptisms = new StatisticsPlaces();
            baptisms.which = BAPTISM;
            analyzePlaces(indis, baptisms);
        }
        
        if(analyzeMarriagePlaces) {
            marriages = new StatisticsPlaces();
            marriages.which = MARRIAGE;
            analyzePlaces(fams, marriages);
        }
        
        if(analyzeEmigrationPlaces) {
            emigrations = new StatisticsPlaces();
            emigrations.which = EMIGRATION;
            analyzePlaces(indis, emigrations);
        }
        
        if(analyzeImmigrationPlaces) {
            immigrations = new StatisticsPlaces();
            immigrations.which = IMMIGRATION;
            analyzePlaces(indis, immigrations);
        }
        
        if(analyzeNaturalizationPlaces) {
            naturalizations = new StatisticsPlaces();
            naturalizations.which = NATURALIZATION;
            analyzePlaces(indis, naturalizations);
        }
        
        if(analyzeDeathPlaces) {
            deaths = new StatisticsPlaces();
            deaths.which = DEATH;
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
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("birthPlaces")+": "+new Integer(births.places.getKeys().size()));
            reportPlaces(reportIndisToBirthPlaces, sortBirthPlacesByName, births);
        }
        
        if(analyzeBaptismPlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("baptismPlaces")+": "+new Integer(baptisms.places.getKeys().size()));
            reportPlaces(reportIndisToBaptismPlaces, sortBaptismPlacesByName, baptisms);
        }
        
        if(analyzeMarriagePlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("marriagePlaces")+": "+new Integer(marriages.places.getKeys().size()));
            reportPlaces(reportIndisToMarriagePlaces, sortMarriagePlacesByName, marriages);
        }
        
        if(analyzeEmigrationPlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("emigrationPlaces")+": "+new Integer(emigrations.places.getKeys().size()));
            reportPlaces(reportIndisToEmigrationPlaces, sortEmigrationPlacesByName, emigrations);
        }
        
        if(analyzeImmigrationPlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("immigrationPlaces")+": "+new Integer(immigrations.places.getKeys().size()));
            reportPlaces(reportIndisToImmigrationPlaces, sortImmigrationPlacesByName, immigrations);
        }
        
        if(analyzeNaturalizationPlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("naturalizationPlaces")+": "+new Integer(naturalizations.places.getKeys().size()));
            reportPlaces(reportIndisToNaturalizationPlaces, sortNaturalizationPlacesByName, naturalizations);
        }
        
        if(analyzeDeathPlaces) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("deathPlaces")+": "+new Integer(deaths.places.getKeys().size()));
            reportPlaces(reportIndisToDeathPlaces, sortDeathPlacesByName, deaths);
        }
    }
    
    /** Rounds a number to a specified number digits in the fraction portion
     * @param number number to round
     * @param digits number of digits allowed in the fraction portion
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
                    
        Property prop;
        Property[] props;
        
        for(int i=0;i<e.length;i++) {
            
            prop = null;
            props = null;
            
            switch(places.which) {
                
                case BIRTH:
                    props = new Property[1];
                    props[0] = e[i].getProperty(new TagPath("INDI:BIRT:PLAC"));
                    break;
                    
                case BAPTISM:
                    ArrayList baps = new ArrayList();
                    prop = e[i].getProperty("BAPM");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:BAPM:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("BAPL");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:BAPL:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("CHR");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:CHR:PLAC"));
                        baps.add(prop);
                    }
                    prop = e[i].getProperty("CHRA");
                    if (prop!=null) {
                        prop = e[i].getProperty(new TagPath("INDI:CHRA:PLAC"));
                        baps.add(prop);
                    }
                    props = (Property[])baps.toArray(new Property[baps.size()]);
                    break;
                    
                case EMIGRATION:
                    prop = e[i].getProperty("EMIG");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:EMIG:PLAC"), Property.QUERY_VALID_TRUE);
                    break;
                    
                case IMMIGRATION:
                    prop = e[i].getProperty("IMMI");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:IMMI:PLAC"), Property.QUERY_VALID_TRUE);
                    break;
                    
                case NATURALIZATION:
                    prop = e[i].getProperty("NATU");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("INDI:NATU:PLAC"), Property.QUERY_VALID_TRUE);
                    break;
                    
                case MARRIAGE:
                    prop = e[i].getProperty("MARR");
                    if (prop!=null)
                        props = e[i].getProperties(new TagPath("FAM:MARR:PLAC"), Property.QUERY_VALID_TRUE);
                    break;
                    
                case DEATH:
                    props = new Property[1];
                    prop = e[i].getProperty("DEAT");
                    if (prop!=null)
                        props[0] = e[i].getProperty(new TagPath("INDI:DEAT:PLAC"), Property.QUERY_VALID_TRUE);
                    break;
                    
            }
            
            if ((props!=null) && (props.length>0)) {
                for(int j=0;j<props.length;j++) {
                    if(props[j]!=null) {
                        String place = props[j].getValue();
                        if (place.length()>0) {
                            if(places.places.add(place, e[i]))                            
                                places.entitiesWithKnownPlaces++;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * @param indi individual to analyze
     * @param age of indi
     * @param single statistic for the single indi
     * @param all to store results of all individuals
     * @param which info to analyze
     */
    private void analyzeAge(Indi indi, Delta age, StatisticsIndividuals single, StatisticsIndividuals all, int which) {
        
        if(age==null)
            return;
        int a = age.getYears()*360+age.getMonths()*30+age.getDays();
        
        switch(which) {
            case INDIS:
            case MARRIAGE:
                
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
                
            case CHILDBIRTH:
                
                single.childBirthNumber++;
                single.sumChildBirthAge = single.sumChildBirthAge + a;
                single.childBirthAge.add(new Integer(a), indi);
                
                if(a < single.minChildBirthAge)
                    single.minChildBirthAge = a;
                if(a > single.maxChildBirthAge)
                    single.maxChildBirthAge = a;
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
            Delta age = null;
            
            if(indi.getDeathDate()!=null)
                age = indi.getAge(indi.getDeathDate().getStart());
            
            switch (indi.getSex()) {
                
                case PropertySex.MALE:
                    males.number++;
                    analyzeAge(indi, age, males, all, INDIS);
                    break;
                    
                case PropertySex.FEMALE:
                    females.number++;
                    analyzeAge(indi, age, females, all, INDIS);
                    break;
                    
                default:
                    unknown.number++;
                    analyzeAge(indi, age, unknown, all, INDIS);
                    break;
            }
        }
    }
    
    /** @param e the individuals
     * @param lastNames to store the results */
    private void analyzeLastNames(Entity[] e, StatisticsLastNames lastNames) {
        
        String name = null;
        // sort indis by name
        for(int i=0;i<e.length;i++) {
            Indi indi = (Indi)e[i];
            lastNames.lastNamesIndis.add(indi.getLastName(), indi);
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
            all.which=ALL;
            StatisticsIndividuals males = new StatisticsIndividuals();
            males.which=MALES;
            StatisticsIndividuals females = new StatisticsIndividuals();
            females.which=FEMALES;
            StatisticsIndividuals unknown = new StatisticsIndividuals();
            unknown.which=UNKNOWN;
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
        String occu = "";
        for(int i=0;i<e.length;i++) {
            Indi indi = (Indi)e[i];
            occupations.numberIndis++;
            // an individual might have more than one occupation
            prop = e[i].getProperties("OCCU");
            if (prop!=null) {
                for(int j=0;j<prop.length;j++) {
                    occu = prop[j].getValue();
                    if(occu.length()>0)
                        occupations.occupations.add(occu, e[i]);
                }
            }
        }
    }
    
    /**
     * Persons with the same last name are basically a "family". Therefore this method is also
     * called from analyzeLastNames().
     *
     * @param families to store the result
     * @param lastName null for "real" families or string value for persons with a certain last name
     * @param e the families
     */
    private void analyzeFamilies(Entity[] e, String lastName, StatisticsFamilies families) {
        
        Delta age = null;
        for(int i=0;i<e.length;i++) {
            Fam fam = (Fam)e[i];
            
            // analyze marriage age of husband and wife
            Indi husband=fam.getHusband();
            Indi wife=fam.getWife();
            PropertyDate marriage = fam.getMarriageDate();
            
            if(marriage!=null) {
                if((husband!=null)&&((lastName==null)||husband.getLastName().equals(lastName))){
                    age = husband.getAge(marriage.getStart());
                    analyzeAge(husband, age, families.husbands, null, MARRIAGE);
                }
                if((wife!=null)&&((lastName==null)||wife.getLastName().equals(lastName))){
                    age= wife.getAge(marriage.getStart());
                    analyzeAge(wife, age, families.wifes, null, MARRIAGE);
                }
            }
            
            // analyze ages at child births
            Indi[] children = fam.getChildren();
            
            for(int j=0;j<children.length;j++) {
                PropertyDate birth = children[j].getBirthDate();
                if(birth!=null) {
                    if ((husband!=null)&&((lastName==null)||(husband.getLastName().equals(lastName)))) {
                        age = husband.getAge(birth.getStart());
                        analyzeAge(husband, age, families.husbands, null, CHILDBIRTH);
                    }
                    if ((wife!=null)&&((lastName==null)||(wife.getLastName().equals(lastName)))) {
                        age = wife.getAge(birth.getStart());
                        analyzeAge(wife, age, families.wifes, null, CHILDBIRTH);
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
     * @param ages all ages added up in days
     * @param numAges number of persons added up
     * @return int[] with average age (year, month, day)
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
     * @param printIndis 1=all, 2=min./max. age, 3=none
     * @param indent level for indent printing
     * @param which indis to print
     */
    private void printAges(int printIndis, int indent, StatisticsIndividuals stats, int which) {
        
        int[] age;
        int keys;
        
        switch(which) {
            case INDIS:
            case MARRIAGE:
                keys = stats.age.getKeys().size();
                if(keys>0) {
                    if(keys==1){
                        age = calculateAverageAge(stats.sumAge,stats.age.getSize());
                        println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+new Delta(age[2], age[1], age[0])+" "+i18n("oneIndi"));
                        if(printIndis<3) {
                            Indi indi = (Indi)new ArrayList(stats.age.getReferences((Integer)stats.age.getKeys().get(0))).get(0);
                            String[] output = {indi.getId(), indi.getName()};
                            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("entity", output));
                        }
                    }
                    else {
                        // min. age
                        printMinMaxAge(printIndis, indent, "minAge", stats.minAge, new ArrayList(stats.age.getReferences(new Integer(stats.minAge))));
                        // average age
                        age = calculateAverageAge(stats.sumAge,stats.age.getSize());
                        println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("avgAge")+" "+new Delta(age[2], age[1], age[0]));
                        // max. age
                        printMinMaxAge(printIndis, indent, "maxAge", stats.maxAge, new ArrayList(stats.age.getReferences(new Integer(stats.maxAge))));
                    }
                }
                else
                    println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("noData"));
                break;
            case CHILDBIRTH:
                keys = stats.childBirthAge.getKeys().size();
                if(keys>0) {
                    if(keys==1) {
                        age = calculateAverageAge(stats.sumChildBirthAge,stats.childBirthNumber);
                        println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+new Delta(age[2], age[1], age[0])+" "+i18n("oneIndi"));
                        if(printIndis<3) {
                            Indi indi = (Indi)new ArrayList(stats.childBirthAge.getReferences((Integer)stats.childBirthAge.getKeys().get(0))).get(0);
                            String[] output = {indi.getId(), indi.getName()};
                            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("entity", output));
                        }
                    }
                    else{
                        // min. age
                        printMinMaxAge(printIndis, indent, "minAge", stats.minChildBirthAge, new ArrayList(stats.childBirthAge.getReferences(new Integer(stats.minChildBirthAge))));
                        // avg age
                        age = calculateAverageAge(stats.sumChildBirthAge,stats.childBirthNumber);
                        println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("avgAge")+" "+new Delta(age[2], age[1], age[0]));
                        // max. age
                        printMinMaxAge(printIndis, indent, "maxAge", stats.maxChildBirthAge, new ArrayList(stats.childBirthAge.getReferences(new Integer(stats.maxChildBirthAge))));
                    }
                }
                else
                    println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("noData"));
                break;
        }
    }
    
    /**
     * @param prefix e. g. "min. age:"
     * @param age to print
     * @param ages individuals with this age
     * @param indent level for indent printing
     * @param printIndis 1=all, 2=min./max. age, 3=none
     */
    private void printMinMaxAge(int reportIndis, int indent, String prefix, int age, ArrayList ages) {
        
        int[] avg = calculateAverageAge(age,1);
        println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n(prefix)+" "+new Delta(avg[2], avg[1], avg[0]));
        if(reportIndis<3) {
            for(int i=0;i<ages.size();i++) {
                Indi indi = (Indi)ages.get(i);
                String[] output = {indi.getId(), indi.getName()};
                println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("entity", output));
            }
        }
    }
    
    /**
     * prints individuals (all, males, females, unknown gender, wifes, husbands, same last name, ...)
     * @param printIndis which indis should be printed (1=all, 2=min./max. age, 3=none)
     * @param lastName null if all indis of a gedcom file are reported or
     * a string value if indis with same last name should be reported
     * @param numberAllIndis number of inidividuals in the gedcom file (only needed when last names are reported)
     */
    private void reportIndividuals(int printIndis, String lastName, double numberAllIndis, StatisticsIndividuals all, StatisticsIndividuals males, StatisticsIndividuals females, StatisticsIndividuals unknown) {
        
        String[] str = new String[2];
        int indent;
        if(lastName==null) {
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("people"));
            println(getIndent(2, SPACES_PER_LEVEL, FRONT_SECOND_LEVEL)+i18n("number",all.number));
            indent=3;
        }
        else {
            println(getIndent(2, SPACES_PER_LEVEL, FRONT_SECOND_LEVEL)+"\""+lastName+"\""+": "+all.number+" ("+roundNumber((double)all.number/(double)numberAllIndis*100, fractionDigits)+"%)");
            println(getIndent(3, SPACES_PER_LEVEL, FRONT_THIRD_LEVEL)+i18n("ages"));
            println(getIndent(4, SPACES_PER_LEVEL, FRONT_FOURTH_LEVEL)+i18n("all"));
            indent=5;
        }
        
        if((lastName==null) || (all.number>0))
            printAges(printIndis, indent, all, INDIS);
        
        if((lastName==null) || (males.number>0)) {
            str[0] = Integer.toString(males.number);
            str[1] = Double.toString(roundNumber((double)males.number/(double)all.number*100, fractionDigits));
            println(getIndent(indent-1, SPACES_PER_LEVEL, getFront(indent-1))+i18n("males",str));
            printAges(printIndis, indent, males, INDIS);
        }
        
        if((lastName==null) || (females.number>0)) {
            str[0] = Integer.toString(females.number);
            str[1] = Double.toString(roundNumber((double)females.number/(double)all.number*100, fractionDigits));
            println(getIndent(indent-1, SPACES_PER_LEVEL, getFront(indent-1))+i18n("females",str));
            printAges(printIndis, indent, females, INDIS);
        }
        
        if((lastName==null) || (unknown.number>0)) {
            str[0] = Integer.toString(unknown.number);
            str[1] = Double.toString(roundNumber((double)unknown.number/(double)all.number*100, fractionDigits));
            println(getIndent(indent-1, SPACES_PER_LEVEL, getFront(indent-1))+i18n("unknown",str));
            printAges(printIndis, indent, unknown, INDIS);
        }
        
        if(lastName==null)
            println();
    }
    
    /** print children of families
     * @param families data source for printing
     * @param childs print only families with this number of children
     * @param indent level for indent printing
     **/
    private void printChildren(StatisticsFamilies families, int childs, int indent) {
        ArrayList children = new ArrayList(families.children.getReferences(new Integer(childs)));
        for(int i=0;i<children.size();i++) {
            Fam fam = (Fam)children.get(i);
            String[] output = {fam.getId(), fam.toString()};
            println(getIndent(indent+2, SPACES_PER_LEVEL, getFront(indent+2))+i18n("entity", output));
        }
    }
    
    
    /** prints the output for families ("real" families or persons with same last name)
     *
     * @param families the statistic
     * @param reportIndisToMarriageAge if indis to marriage ages should be printed
     * @param reportIndisToChildBirths if indis to child births should be printed
     * @param reportFamsToChildren which families with children should be reported (1=all, 2=min./max. age, 3=none)
     * @param lastName whether we report "real" families or indis with the same last name
     **/
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
            println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("families")+": "+families.number);
            indent = 2;
        }
        else
            indent = 3;
        
        if(families.number>0) {
            //ages at marriage
            println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("ageAtMarriage"));
            //husbands
            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("husbands"));
            printAges(i, indent+2, families.husbands, MARRIAGE);
            // wifes
            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("wifes"));
            printAges(i, indent+2, families.wifes, MARRIAGE);
            
            //children
            String[] output = { Integer.toString(families.withChildren), Double.toString(roundNumber((double)families.withChildren/(double)families.number*100,fractionDigits)) };
            println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("withChildren", output));
            
            switch(reportFamsToChildren) {
                case 0:
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    Iterator f = families.children.getKeys().iterator();
                    while(f.hasNext()) {
                        int children = ((Integer)f.next()).intValue();
                        println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("children")+": "+children);
                        printChildren(families, children, indent);
                    }
                    break;
                case 1:
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("minChildren",families.minChildren));
                    printChildren(families, families.minChildren, indent);
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("maxChildren",families.maxChildren));
                    printChildren(families, families.maxChildren, indent);
                    break;
                case 2:
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("minChildren",families.minChildren));
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("avgChildren",Double.toString(roundNumber((double)families.withChildren/(double)families.number,fractionDigits))));
                    println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("maxChildren",families.maxChildren));
                    break;
            }
            
            //ages at child birth
            println(getIndent(indent, SPACES_PER_LEVEL, getFront(indent))+i18n("agesAtChildBirths"));
            //husbands
            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("husbands"));
            printAges(j, indent+2, families.husbands, CHILDBIRTH);
            //wifes
            println(getIndent(indent+1, SPACES_PER_LEVEL, getFront(indent+1))+i18n("wifes"));
            printAges(j, indent+2, families.wifes, CHILDBIRTH);
        }
        
        if(lastName==false)
            println();
    }
    
    /** print the output for playes
     *
     * @param reportIndisToPlaces if indis to places should be reported
     * @param sortPlacesByName if places should be sorted by name
     * @param places our statistic
     */
    private void reportPlaces(boolean reportIndisToPlaces, boolean sortPlacesByName, StatisticsPlaces places) {
        
        String place = null;
        Iterator p = places.places.getKeys(sortPlacesByName).iterator();
        while(p.hasNext()) {
            place = (String)p.next();
            int number = places.places.getSize(place);
            println(getIndent(2, SPACES_PER_LEVEL, FRONT_SECOND_LEVEL)+place+": "+number+" ("+roundNumber((double)number/(double)places.entitiesWithKnownPlaces*100, fractionDigits)+"%)");
            if(reportIndisToPlaces) {
                ArrayList entities = new ArrayList(places.places.getReferences(place));
                String[] output = new String[2];
                for(int i=0;i<entities.size();i++){
                    
                    if(places.which==MARRIAGE) {
                        Fam fam = (Fam)entities.get(i);
                        output[0] = fam.getId();
                        output[1] = fam.toString();
                    }
                    else {
                        Indi indi = (Indi)entities.get(i);
                        output[0] = indi.getId();
                        output[1] = indi.getName();
                    }
                    println(getIndent(3, SPACES_PER_LEVEL, FRONT_THIRD_LEVEL)+i18n("entity", output));
                }
            }
        }
        println();
    }
    /** print info about indis with the same last name. this method calls reportIndividuals() and reportFamilies().
     *
     * @param lastNames statistical data
     * @param numberAllIndis number of indis in gedcom file
     */
    private void reportLastNames(StatisticsLastNames lastNames, int numberAllIndis) {
        
        String[] output = { Integer.toString(lastNames.lastNamesIndis.getKeys().size()), Integer.toString(numberAllIndis) };
        println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("lastNames", output));
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
                        case ALL:
                            all=(StatisticsIndividuals)stat;
                            break;
                        case MALES:
                            males=(StatisticsIndividuals)stat;
                            break;
                        case FEMALES:
                            females=(StatisticsIndividuals)stat;
                            break;
                        case UNKNOWN:
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
    
    /** print info about occupations
     *
     * @param occupations statistic with data
     */
    private void reportOccupations(StatisticsOccupations occupations) {
        
        String[] output = new String[3];
        println(getIndent(1, SPACES_PER_LEVEL, FRONT_FIRST_LEVEL)+i18n("occupations"));
        println(getIndent(2, SPACES_PER_LEVEL, FRONT_SECOND_LEVEL)+i18n("number", occupations.occupations.getKeys().size()));
        Iterator it = occupations.occupations.getKeys(sortOccupationsByName).iterator();
        while(it.hasNext()) {
            String occupation = (String)it.next();
            output[0] = occupation;
            output[1] = Integer.toString(occupations.occupations.getSize(occupation));
            output[2] = Double.toString(roundNumber((double)occupations.occupations.getSize(occupation)/(double)occupations.occupations.getSize()*100, fractionDigits));
            println(getIndent(3, SPACES_PER_LEVEL, FRONT_THIRD_LEVEL)+i18n("occupation", output));
            if(reportIndisToOccupations) {
                ArrayList indis = new ArrayList(occupations.occupations.getReferences(occupation));
                for(int i=0;i<indis.size();i++) {
                    Indi indi = (Indi)indis.get(i);
                    output[0] = indi.getId();
                    output[1] = indi.getName();
                    println(getIndent(4, SPACES_PER_LEVEL, FRONT_FOURTH_LEVEL)+i18n("entity", output));
                }
            }
        }
        println();
    }
    
    /** returns the front string for the getIndent() calls
     * @param indent level for indent printing
     */
    private String getFront(int indent) {
        switch(indent) {
            case 1: return FRONT_FIRST_LEVEL;
            case 2: return FRONT_SECOND_LEVEL;
            case 3: return FRONT_THIRD_LEVEL;
            case 4: return FRONT_FOURTH_LEVEL;
            case 5: return FRONT_FIFTH_LEVEL;
            case 6: return FRONT_SIXTH_LEVEL;
            default: return "";
        }
    }
    
} //ReportGedcomStatistics