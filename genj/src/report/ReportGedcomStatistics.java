/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.gedcom.EntityList;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.report.Report;
import genj.report.ReportBridge;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * GenJ - Report
 * Note: this report requires Java2
 * $Header: /cygdrive/c/temp/cvs/genj/genj/src/report/ReportGedcomStatistics.java,v 1.13 2002-04-29 18:43:27 nmeier Exp $
 * @author Francois Massonneau <fmas@celtes.com>
 * @version 1.1
 */
public class ReportGedcomStatistics implements Report {

  /**
   * A data object that contains the statistical data we gather
   */
  private static class Statistics {
    /**package*/ int numMales = 0;
    /**package*/ int numFemales = 0;
    /**package*/ int numUnknown = 0;
    /**package*/ TreeMap birthPlaces = new TreeMap();
    /**package*/ TreeMap deathPlaces = new TreeMap();
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
    return "Gedcom Statistics";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report gives you some statistics about the current Gedcom File.\n"+
		"   . How many families, persons.\n"+
		"   . Number of males, females, and individuals with undefined sex.\n"+
		"   . Stats about birth places.\n"+
		"   . Stats about death places.\n\n"+
		"(Note: this report requires Java2)";
  }

  /**
   * Indication of how this reports shows information
   * to the user. Standard Out here only.
   */
  public boolean usesStandardOut() {
    return true;
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Francois Massonneau <fmas@celtes.com>";
  }

  /**
   * Tells whether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * This method actually starts this report
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {

    // Here's the data object that we use while looking
    // at the statistical characteristics
    Statistics stats = new Statistics();

    // So we loop over the Individuals
    EntityList indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.getSize();i++) {
      analyzeIndividual(indis.getIndi(i), stats);
    }
    
    // And report what we've found
    reportResults(bridge, gedcom, stats);
    
    // Done
    return true;
  }

  /**
   * Helper that looks up an Integer from a AbstractMap
   * and increases it 
   * <ul>
   * <li> if it doesn't exist it will be created with initial value 1
   * <li> if it does exist it will be incremented by one
   * </ul>
   */
  private void incrementIntegerInMap(AbstractMap map, Object key) {
    
    // We make sure to use the text representation of the key
    key = key.toString();
    
    // look it up
    Integer integer = (Integer)map.get(key);
    
    // Increment it (or start at zero)
    int value = 0;
    if (integer!=null) {
      value = integer.intValue();
    }
    integer = new Integer(value+1);      

    // Remember it
    map.put(key, integer);      
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
        case Gedcom.MALE:
            stats.numMales++;
            break;
        case Gedcom.FEMALE:
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
      Object place = indi.getProperty("INDI:BIRT:PLAC");
      if ((place==null) || (place.toString().trim().length()==0)){
        place = UNKNOWN_PLACE;
      }
       
      // .. and a simple increment
      incrementIntegerInMap(stats.birthPlaces, place);
      
      // Done
  }
  
  /**
   * Analyzes an Individual's DEATH
   */
  private void analyzeIndividualDeath(Indi indi, Statistics stats) {
      
      // We only look at individuals with a DEATH property
      Object deat = indi.getProperty("INDI:DEAT");
      if (deat==null) {
        return;
      }
      
      // And here comes the check for place
      Object place = indi.getProperty("INDI:DEAT:PLAC");
      if ((place==null) || (place.toString().trim().length()==0)){
        place = UNKNOWN_PLACE;
      }
       
      // .. and a simple increment
      incrementIntegerInMap(stats.deathPlaces, place);
      
      // Done
  }
  
  /**
   * Reports the result of our information-gathering
   */
  private void reportResults(ReportBridge bridge, Gedcom gedcom, Statistics stats) {

    // Header :
    bridge.println("In the Gedcom file named '"+gedcom.getName()+"', there are :\n");
    bridge.println("  * Stats about people :");
		
    // One: We show the number of families :
    bridge.println("     - "+gedcom.getEntities(Gedcom.FAMILIES).getSize()
      +" families (soit : "+gedcom.getEntities(Gedcom.FAMILIES).getSize()+" familles).");

    // Two: We show the number of individuals :
    bridge.println("     - "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()
      +" Individuals (soit : "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()+" personnes).");

    // Three: We show the number of males :
    bridge.println("         . "+stats.numMales+" males (soit : "+stats.numMales+" hommes).");

    // Four: We show the number of females :
    bridge.println("         . "+stats.numFemales+" females (soit : "+stats.numFemales+" femmes).");

    // Five: We show the number of people whose sex is undefined :
    bridge.println("         . "+stats.numUnknown+" with undefined sex (soit : "
      +stats.numUnknown+" personnes dont le sexe n'est pas connu).");

    bridge.println("");

    // Six: We show the birth places
    bridge.println("  * Stats about birth places :");
    Iterator it = stats.birthPlaces.keySet().iterator();
    while (it.hasNext()) {
      String place = (String)it.next();
      Integer count = (Integer)stats.birthPlaces.get(place);
      bridge.println("     - "+count+" individuals were born in "+place);
    }

    bridge.println("");

    // Seven: We show the death places
    bridge.println("  * Stats about death places :");
    Iterator death_it = stats.deathPlaces.keySet().iterator();
    while (death_it.hasNext()) {
      String death_place = (String)death_it.next();
      Integer death_count = (Integer)stats.deathPlaces.get(death_place);
      bridge.println("     - "+death_count+" individuals died in "+death_place);
    }

    // Done
  }
  
}
