/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.report.Report;
import genj.report.ReportBridge;

/**
 * GenJ - Report
 * @author Sven Meier sven@meiers.net
 * @version 0.1
 */
public class ReportEarliestAncestor implements Report {

  /** this report's version */
  public static final String VERSION = "0.1";

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
    return "Earliest Ancestor";
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints the earliest ancestor of an individual";
  }
  
  /**
   * Return the individual with the earlier birthdate
   */
  private Indi getEarliest(Indi one, Indi two) {
    Property 
      bOne = one.getBirthDate(),
      bTwo = two.getBirthDate();
    if (bTwo==null) return one;
    if (bOne==null) return two;
    return bOne.compareTo(bTwo)<0 ? one : two;
  }

  /**
   * Finding the earliest ancestor recursive.
   */
  public Indi findEarliest (Indi indi) {

    // earlierst is indi himself
    Indi earliest = indi;

    // Check if there are ancestors
    Fam fam = indi.getFamc ();
    if (fam == null)
      return earliest;

    // get husband of family and recurse
    Indi husband = findEarliest(fam.getHusband());
    if (husband!=null)
      earliest = getEarliest(earliest, husband);

    // get wife of family and recurse
    Indi wife = findEarliest(fam.getHusband());
    if (wife!=null)
      earliest = getEarliest(earliest, findEarliest(wife));
    
    // done
    return earliest;
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
    return "Sven Meier <sven@meiers.net>";
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

    // Show the users in a combo to the user
    Indi indi = (Indi)bridge.getValueFromUser(
      "Please select an individual",
      gedcom.getEntities(Gedcom.INDIVIDUALS).toArray(),
      null
    );
    
    if (indi==null) {
      return false;
    }
    
    // Found earliest
    bridge.println ("     Earliest ancestor of " + indi.getName () + " is:");
    bridge.println ("     =--->     " + findEarliest (indi).getName ());

    // Done
    return true;
  }

}
