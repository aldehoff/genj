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
   * Finding the earliest ancestor recursive.
   */
  public Indi findEarliest (Indi indi) {

    Indi earliest = indi;
    PropertyDate birth;

    Fam fam = indi.getFamc ();
    if (fam == null){
      return (earliest);
    }

    indi = fam.getHusband ();
    if (indi != null){
      indi = findEarliest (indi);

      birth = indi.getBirthDate();

      if ((birth!=null)&&(birth.compareTo(earliest.getBirthDate())< 0)) {
        earliest = indi;
      }
    }

    indi = fam.getWife ();
    if (indi != null){
      indi = findEarliest (indi);

      birth = indi.getBirthDate();

      if ((birth!=null)&&(birth.compareTo(earliest.getBirthDate())<0)) {
        earliest = indi;
      }
    }

    return (earliest);
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
    EntityList indis = gedcom.getEntities(Gedcom.INDIVIDUALS);

    // Search earliest
    Indi indi;
    try {
      indi = gedcom.getIndiFromId(id);
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
