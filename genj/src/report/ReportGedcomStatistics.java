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
 * @version 0.01
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
    return "This report gives you some statistics about the current Gedcom File.\n\n            Have Fun and Enjoy";
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

    // Header :
    bridge.println("This report gives you some statistics about your Family Tree :");
    bridge.println("     . Number of people,");
    bridge.println("     . Number of families,");
    bridge.println("                    -----------------------------");
    bridge.println("");


    // One: We show the number of individuals :
    bridge.println("In the Gedcom file named '"+gedcom.getName()+"', there are :");
    bridge.println("     - "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()
           +" Individuals (soit : "+gedcom.getEntities(Gedcom.INDIVIDUALS).getSize()+" personnes).");

    // Two: We show the number of families :
    bridge.println("     - "+gedcom.getEntities(Gedcom.FAMILIES).getSize()
           +" families (soit : "+gedcom.getEntities(Gedcom.FAMILIES).getSize()+" familles).");

    return true;

  }

}
