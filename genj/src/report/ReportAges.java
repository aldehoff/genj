/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PointInTime;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.report.Report;
import genj.report.ReportBridge;

/**
 * GenJ - ReportAges
 * (based on ReportDescendants)
 */
public class ReportAges implements Report {

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
    return "Ages";
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints out the age of an individual at all events";
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
    return "Daniel P. Kionka";
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

    // Display the ages
    iterate(bridge, indi, 1);

    // Done
    return true;
  }

  /**
   * Iterates over events
   */
  private void iterate(ReportBridge bridge, Indi indi, int level) {

    // Here comes the individual
    bridge.println(indi.getName());

    // Give up if no birth date
    if (indi.getBirthDate() == null) {
      bridge.println("no birth date");
      return;
    }

    // And we loop through its properties
    int fcount = indi.getNoOfProperties();
    for (int f=0;f<fcount;f++) {
      Property prop = indi.getProperty(f);
      if ( prop.isValid() && (prop instanceof PropertyEvent) ) {

        // we found an event
        PropertyEvent event = (PropertyEvent) prop;
        PropertyDate pDate = event.getDate(true);
        if (pDate != null) {
          String indiAge = indi.getAgeString(pDate.getStart());
          String out = event.getTag() + ": " + pDate;
          if (indiAge.length() > 0)
             out += ", " + indiAge;
          bridge.println(out);
        }
      }
    }

    // in case no DEAT, or if he were alive today
    bridge.println("Since birth: " + indi.getAgeString(PointInTime.getNow()));
  }

} //ReportAges
