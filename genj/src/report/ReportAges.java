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

/**
 * GenJ - ReportAges
 * (based on ReportDescendants)
 */
public class ReportAges extends Report {

  /** this report's version */
  public static final String VERSION = "0.2";

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
    return i18n("ages");
  }

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return i18n("info");
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Daniel P. Kionka";
  }

  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    // we accept GEDCOM or Individuals 
    return context instanceof Indi || context instanceof Gedcom ? getName() : null;  
  }
  
  /**
   * This method actually starts this report
   */
  public void start(Object context) {
    Indi indi;

		// If we were passed a person to start at, use that
    if (context instanceof Indi) {
      indi = (Indi)context;
    } else {
    // Otherwise, ask the user select the root of the tree for analysis
			Gedcom gedcom=(Gedcom)context;
			indi = (Indi)getEntityFromUser (
				 i18n("select"), // msg
				 gedcom,                        // our gedcom instance
				 Gedcom.INDI,            // type INDIVIDUALS
				 "INDI:NAME"                    // sort by name
				 );
		}

    if (indi==null) return;

    // Display the ages
    iterate(indi, 1);

    // Done
  }

  /**
   * Iterates over events
   */
  private void iterate(Indi indi, int level) {

    // Here comes the individual
    println(indi.getName());

    // Give up if no birth date
    if (indi.getBirthDate() == null) {
      println(i18n("noresult"));
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
          println(out);
        }
      }
    }

    // in case no DEAT, or if he were alive today
    println(i18n("sincebirth") + indi.getAgeString(PointInTime.getNow()));
  }

} //ReportAges
