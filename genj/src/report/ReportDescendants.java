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
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportDescendants implements Report {

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
    return "Descendants";
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints out all descendants of an individual";
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
    return "Nils Meier <nils@meiers.net>";
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
    
    // Display the descendants
    iterate(bridge, indi, 1);
    
    // Done
    return true;
  }
  
  /**
   * Iterates over descendants
   */
  private void iterate(ReportBridge bridge, Indi indi, int level) {
    
    // Here comes the individual
    bridge.println(getIndent(level)+level+" "+format(indi));
    
    // And we loop through its families
    int fcount = indi.getNoOfFams();
    for (int f=0;f<fcount;f++) {
      
      // .. here's the fam and spouse
      Fam fam = indi.getFam(f);
      Indi spouse= fam.getOtherSpouse(indi);
      
      // .. a line for the spouse
      bridge.println(getIndent(level) +"  + "+ format(spouse));
      
      // .. and all the kids
      Indi[] children = fam.getChildren();
      for (int c = 0; c < children.length; c++) {
        
        // do the recursive step
        iterate(bridge, children[c], level+1);
        
        // .. next child
			}

      // .. next family
    }
  }
  
  /**
   * resolves the information of one Indi
   */
  private String format(Indi indi) {
    
    // Might be null
    if (indi==null) {
      return "?";
    }
    
    // name
    String n = indi.getName();
    if (Property.isEmptyOrNull(n)) {
      n = "?";
    }
    
    // birth?
    String b = indi.getBirthAsString();
    if (!Property.isEmptyOrNull(b)) {
      b = " b: " + b;
    }
    
    // death?
    String d = indi.getDeathAsString();
    if (!Property.isEmptyOrNull(d)) {
      d = " d: " + d;
    }
    
    // here's the result 
    return n + b + d;
    
    // FIXME
    //return "<a href=\"\">" + indi.getName() + "</a>" + b + d;
  }
  
  /**
   * Helper that indents to given level
   */
  private String getIndent(int level) {
    StringBuffer buffer = new StringBuffer(256);
    while (--level>0) {
      buffer.append("    ");
    }
    return buffer.toString();
  }
}
