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
 * GenJ -  ReportAncestors
 * @version 0.1
 */
public class ReportAncestors implements Report {

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
    return "Ancestors";
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints out all ancestors of an individual";
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
    return "YON - Jan C. Hardenbergh";
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
    parent(bridge, indi, 1);
    
    // Done
    return true;
  }
  
  /**
   * parent - prints information about one parent and then recurses
   */
  private void parent(ReportBridge bridge, Indi indi, int level) {

    // Here comes the individual
    bridge.println(getIndent(level)+level+" "+format(indi));
    
    Fam famc = indi.getFamc();

    if (famc==null) {
	//      bridge.println(getIndent(level) +"  + leaf node "+ format(indi));
      return;
    }

    if (famc.getWife()!=null) {
        parent(bridge, famc.getWife(), level+1);
    }
    if (famc.getHusband()!=null) {
        parent(bridge, famc.getHusband(), level+1);
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
    
    // birth?
    String b = " b: " + indi.getBirthAsString();
    
    // death?
    String d = " d: " + indi.getDeathAsString();
    
    // here's the result 
    return n + b + d;
    
    // Could be a hyperlink, too
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

