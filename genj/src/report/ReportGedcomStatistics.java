/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;
import genj.util.ReferenceSet;

import java.util.Iterator;
import java.util.List;

/**
 * GenJ - Report
 * Note: this report requires Java2
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.24 2003-06-01 05:12:30 tfmorris Exp $
 * @author Francois Massonneau <fmas@celtes.com>
 * @version 1.1
 */
public class ReportGedcomStatistics extends Report {

  /**
   * A data object that contains the statistical data we gather
   */
  private static class Statistics {
    /**package*/ int numMales = 0;
    /**package*/ int numFemales = 0;
    /**package*/ int numUnknown = 0;
    /**package*/ ReferenceSet birthPlaces = new ReferenceSet();
    /**package*/ ReferenceSet deathPlaces = new ReferenceSet();
  }
   
  /** the place that is not known */
  private final static String UNKNOWN_PLACE = "[unknown places]";
  
  /** this report's version */
  public static final String VERSION = "1.1";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return VERSION;
  }
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return i18n("script_name");
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return i18n("script_info");
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Francois Massonneau <fmas@celtes.com>";
  }

  /**
   * This method actually starts this report
   */
  public void start(Object context) {

    // expecting only gedcom
    Gedcom gedcom = (Gedcom)context;

    // Here's the data object that we use while looking
    // at the statistical characteristics
    Statistics stats = new Statistics();

    // So we loop over the Individuals
    List indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.size();i++) {
      analyzeIndividual((Indi)indis.get(i), stats);
    }
    
    // And report what we've found
    reportResults(gedcom, stats);
    
    // Done
  }

  /**
   * Analyzes an Individual for the Statistics
   */
  private void analyzeIndividual(Indi indi, Statistics stats) {
    analyzeIndividualSex(indi, stats);
    analyzeIndividualBirth(indi, stats);
    analyzeIndividualDeath(indi, stats);
  }
  
  /**
   * Analyzes an Individual's SEX
   */
  private void analyzeIndividualSex(Indi indi, Statistics stats) {

      // Here comes the Sex check
      int sex = indi.getSex();
      switch (indi.getSex()) {
        case PropertySex.MALE:
            stats.numMales++;
            break;
        case PropertySex.FEMALE:
            stats.numFemales++;
            break;
        default:
            stats.numUnknown++;
            break;
      }
  }
  
  /**
   * Analyzes an Individual's BIRTH
   */
  private void analyzeIndividualBirth(Indi indi, Statistics stats) {
      
    // And here comes the check for place
    Property prop = indi.getProperty(new TagPath("INDI:BIRT:PLAC"));
    if (prop==null) return;
    
    String place = prop.getValue();
    if (place.length()==0) place = UNKNOWN_PLACE;
    
    // keep track
    stats.birthPlaces.add(place, prop);
      
    // Done
  }
  
  /**
   * Analyzes an Individual's DEATH
   */
  private void analyzeIndividualDeath(Indi indi, Statistics stats) {
      
      // We only look at individuals with a DEATH property
      Object deat = indi.getProperty("DEAT");
      if (deat==null) return;
      
      // And here comes the check for place
      String place = "";
      Property prop = indi.getProperty(new TagPath("INDI:DEAT:PLAC"));
      if (prop!=null) place = prop.getValue();
      if (place.length()==0) place = UNKNOWN_PLACE;
       
      // keep track of that
      stats.deathPlaces.add(place, prop);
      
      // Done
  }
  
  /**
   * Reports the result of our information-gathering
   */
  private void reportResults(Gedcom gedcom, Statistics stats) {

    // Header :
    println(i18n("header",gedcom.getName()));
		println();
    println("  "+i18n("about_people"));
		
    // One: We show the number of families :
    println("     - "+
						i18n("families",gedcom.getEntities(Gedcom.FAMILIES).size()+""));

    // Two: We show the number of individuals :
    println("     - "+ i18n("individuals",
														gedcom.getEntities(Gedcom.INDIVIDUALS).size()+""));

    // Three: We show the number of males :
    println("         . "+i18n("males",stats.numMales+""));

    // Four: We show the number of females :
    println("         . "+i18n("females",stats.numFemales+""));

    // Five: We show the number of people whose sex is undefined :
    println("         . "+i18n("sex_unknown",stats.numUnknown+""));

    println();

    // Six: We show the birth places
    println("  "+i18n("about_birthplaces"));
    Iterator births = stats.birthPlaces.getValues().iterator();
    while (births.hasNext()) {
      String place = (String)births.next();
      int count = stats.birthPlaces.getCount(place);
			String[] msgargs = {count+"",place};
      println("     - "+i18n("indi_born",msgargs));
    }

    println("");

    // Seven: We show the death places
    println("  "+i18n("about_deathplaces"));
    Iterator deaths = stats.deathPlaces.getValues().iterator();
    while (deaths.hasNext()) {
      String place = (String)deaths.next();
      int count = stats.deathPlaces.getCount(place);
			String[] msgargs = {count+"",place};
      println("     - "+i18n("indi_died",msgargs));
    }

    // Done
  }
  
} //ReportGedcomStatistics
