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

import java.util.*;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportBirthdays implements Report {

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
    return "Birthdays";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints individuals with a birthday for a given month. "+
           "Sorting the result is possible in case you run Java2 - change the "+
           "code as indicated by uncommenting a three-line code-block in "+
           "ReportBirthdays.java (recompilation necessary).";
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

    /*    
    try {
      Thread.currentThread().sleep(3000);
    } catch (InterruptedException e) {
      throw new ReportCancelledException();
    }
    */

    final String months[] = { "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    // Calculate Month
    String s = (String)bridge.getValueFromUser("Please select a month",months,null);
    if (s==null) {
      return false;
    }

    int month=0;
    for (;month<months.length;month++) {
      if (months[month].equals(s)) {
        break;
      }
    }
    month++;

    // Look for candidates
    Vector candidates = new Vector(100);

    EntityList indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.getSize();i++) {
      Indi indi = indis.getIndi(i);
      PropertyDate birth = indi.getBirthDate();
      if (birth==null) {
        continue;
      }
      if (birth.getStart().getMonth(-1) == month) {
        candidates.addElement(indi);
      }
    }

    // Sort the birthdays - uncomment the following block if
    // you're running JAVA2 and want the result to be sorted
    // by date
    Comparator comparator = new Comparator() {
      // LCD
      public int compare(Object o1, Object o2) {
        // O.K. here are the birthdays (might be null!)
        PropertyDate b1 = ((Indi)o1).getBirthDate();
        PropertyDate b2 = ((Indi)o2).getBirthDate();

        // So we check whether we can get a day
        int d1 = 0;
        if (b1!=null)
                d1 = b1.getStart().getDay(0);

        // So we check whether we can get a day
        int d2 = 0;
        if (b2!=null)
                d2 = b2.getStart().getDay(0);

        // Comparison at last
        return d1-d2;
      }
      // EOC
    };
    Collections.sort(candidates, comparator);

    // Show birthdays
    bridge.println("The following individuals are born in month "+s);

    Enumeration e = candidates.elements();
    while (e.hasMoreElements()) {
      Indi indi = (Indi)e.nextElement();
      bridge.println(indi.getName()+" (*"+indi.getBirthDate()+")");
    }

    // Done
    return true;
  }

}
