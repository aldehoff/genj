/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.DuplicateIDException;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.report.Report;
import genj.report.ReportBridge;

import java.util.List;

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

    // Calculate id
    String id = bridge.getValueFromUser ( "Please enter id number (eg: 1)", new String[0], "id");
    if ((id==null)||(id.length()==0)) {
      bridge.println ("Expected parameter id - Aborting ...");
      return false;
    }
    id = "I"+id;

    // Search start individual
    List indis = gedcom.getEntities(Gedcom.INDIVIDUALS);

    // Search earliest
    Indi indi;
    try {
      indi = (Indi)gedcom.getEntity(id, Gedcom.INDIVIDUALS);
    } catch (DuplicateIDException e) {
      bridge.println ("There are more than one individuals with that ID");
      return false;
    }

    if (indi==null) {
      bridge.println ("Cannot find this individual.");
    } else {
      // Found earliest
      bridge.println ("You've asked for ID '" + id + "'.");
      bridge.println ("The Individual '" + id + "' is : " + indi.getName ());
      bridge.println ("                            ");
      bridge.println ("     Earliest ancestor of " + indi.getName () + " is:");
      bridge.println ("     =--->     " + findEarliest (indi).getName ());
      bridge.println ("                            ");
    }

    // Done
    return true;
  }

}
