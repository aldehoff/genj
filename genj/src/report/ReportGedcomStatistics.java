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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * GenJ - Report
 * Note: this report requires Java2
 * @author Francois Massonneau <fmas@celtes.com>
 * @version 0.04
 */
public class ReportGedcomStatistics implements Report {

  /** the place that is not known */
  private final static String UNKNOWN_PLACE = "[unknown places]";
  
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
		"            Have Fun and Enjoy\n\n\n(version 0.04)";
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
   * Tells wether this report doesn't change information in the Gedcom-file
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * This method actually starts this report
   */
  public boolean start(ReportBridge bridge, Gedcom gedcom) {

    int numMales = 0;
    int numFemales = 0;
    int numUnknown = 0;

    // We Look thru individuals to check their sex
    // .. at the same time we check for birth places
    TreeMap places = new TreeMap();
    // .. at the same time we check for death places
    TreeMap death_places = new TreeMap();
    
    EntityList indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.getSize();i++) {

			// This is the guy we're looking at     
      Indi indi = indis.getIndi(i);
      
      // Here comes the Sex check
      int sex = indi.getSex();
      switch (indi.getSex()) {
        case Gedcom.MALE:
            numMales++;
            break;
        case Gedcom.FEMALE:
            numFemales++;
            break;
        default:
            numUnknown++;
            break;
      }
      
      // And here comes the check for birth place
      Object place = indi.getProperty("INDI:BIRT:PLAC");
      if ((place==null) || (place.toString().trim().length()==0)){
        place = UNKNOWN_PLACE;
      }
       
      // .. check if we know that already (or start at 0)
      Integer count = (Integer)places.get(place.toString());
      if (count==null) {
        count = new Integer(1);
      } else {
        count = new Integer(count.intValue()+1);
      }
        
      // .. remember
      places.put(place.toString(), count);

      // And here comes the check for death place
      Object death_place = indi.getProperty("INDI:DEAT:PLAC");
      if ((death_place==null) || (death_place.toString().trim().length()==0)){
        death_place = UNKNOWN_PLACE;
      }
       
      // .. check if we know that already (or start at 0)
      Integer death_count = (Integer)death_places.get(death_place.toString());
      if (death_count==null) {
        death_count = new Integer(1);
      } else {
        death_count = new Integer(death_count.intValue()+1);
      }
        
      // .. remember
      death_places.put(death_place.toString(), death_count);

      // Next one
    }

    // Header :

    bridge.println("In the Gedcom file named '"+gedcom.getName()+"', there are :");
    bridge.println("  * Stats about people :");
		
    // One: We show the number of families :
    bridge.println("     - "+gedcom.getEntities(Gedcom.FAMILIES).getSize()
      +" families (soit : "+gedcom.getEntities(Gedcom.FAMILIES).getSize()+" familles).");

    // Two: We show the number of individuals :
    bridge.println("     - "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()
      +" Individuals (soit : "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()+" personnes).");

    // Three: We show the number of males :
    bridge.println("         . "+numMales+" males (soit : "+numMales+" hommes).");

    // Four: We show the number of females :
    bridge.println("         . "+numFemales+" females (soit : "+numFemales+" femmes).");

    // Five: We show the number of people whose sex is undefined :
    bridge.println("         . "+numUnknown+" with undefined sex (soit : "
      +numUnknown+" personnes dont le sexe n'est pas connu).");

    // Six: We show the birth places
    bridge.println("  * Stats about birth places :");
    Iterator it = places.keySet().iterator();
    while (it.hasNext()) {
      String place = (String)it.next();
      Integer count = (Integer)places.get(place);
      bridge.println("     - "+count+" individuals born in "+place);
    }

    // Seven: We show the death places
    bridge.println("  * Stats about death places :");
    Iterator death_it = death_places.keySet().iterator();
    while (death_it.hasNext()) {
      String death_place = (String)death_it.next();
      Integer death_count = (Integer)death_places.get(death_place);
      bridge.println("     - "+death_count+" individuals dead in "+death_place);
    }

    // Done
    return true;

  }

}
