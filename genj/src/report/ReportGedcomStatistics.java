/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.*;
import genj.report.*;
import java.io.*;

/**
 * GenJ - Report
 * @author Francois Massonneau <fmas@celtes.com>
 * @version 0.02
 */
public class ReportGedcomStatistics implements Report {

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
    return "This report gives you some statistics about the current Gedcom File.\n\n            Have Fun and Enjoy\n\n\n(version 0.02)";
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

    EntityList indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.getSize();i++) {
      Indi indi = indis.getIndi(i);
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
    }

    // Header :
    bridge.println("This report gives you some statistics about your Family Tree :");
    bridge.println("     . How many families, persons,");
    bridge.println("     . Number of males, females, and individuals with undefined sex");

    bridge.println("                    -----------------------------");
    bridge.println("");

    bridge.println("In the Gedcom file named '"+gedcom.getName()+"', there are :");

    // One: We show the number of families :
    bridge.println("     - "+gedcom.getEntities(Gedcom.FAMILIES).getSize()
      +" families (soit : "+gedcom.getEntities(Gedcom.FAMILIES).getSize()+" familles).");

    // Two: We show the number of individuals :
    bridge.println("     - "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()
      +" Individuals (soit : "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()+" personnes).");

    // Three: We show the number of males :
    bridge.println("         . "+numMales+" males (soit : "+numMales+" hommes).");

    // Four: We show the number of males :
    bridge.println("         . "+numFemales+" females (soit : "+numFemales+" femmes).");

    // Five: We show the number of people whose sex is undefined :
    bridge.println("         . "+numUnknown+" with undefined sex (soit : "
      +numUnknown+" personnes dont le sexe n'est pas connu).");


    return true;

  }

}
