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
import genj.gedcom.PropertyDate;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @version 0.2
 */
public class ReportBirthdays extends Report {

  /** this report's version */
  public static final String VERSION = "0.1";

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    return i18n("version");
  }
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    return i18n("name");
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
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * This method actually starts this report
   */
  public void start(Object context) {
    
    Gedcom gedcom = (Gedcom)context;

    // Calculate Month
    String[] months = PointInTime.getMonths(true);
    String selection = (String)getValueFromUser(i18n("select"),months,null);
    if (selection==null) 
      return;

    int month=0; while (month<months.length&&months[month]!=selection) month++;

    // Look for candidates
    List candidates = new ArrayList(100);

    List indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.size();i++) {
      Indi indi = (Indi)indis.get(i);
      PropertyDate birth = indi.getBirthDate();
      if (birth==null) {
        continue;
      }
      if (birth.getStart().getMonth() == month) {
        candidates.add(indi);
      }
    }

    // Sort the individuals by day of month
    Comparator comparator = new Comparator() {
      public int compare(Object o1, Object o2) {
        // O.K. here are the birthdays (might be null!)
        PropertyDate b1 = ((Indi)o1).getBirthDate();
        PropertyDate b2 = ((Indi)o2).getBirthDate();

        // So we check whether we can get the day information
        int
         d1 = b1!=null ? b1.getStart().getDay() : 0,
         d2 = b2!=null ? b2.getStart().getDay() : 0;

        // Comparison at last
        return d1-d2;
      }
    }; //Comparator
    
    // Change comparator here if you'd rather like to 
    // sort by year+day
    //
    // Comparator comparator = new genj.gedcom.PropertyComparator("INDI:BIRT:DATE");
    //
    Collections.sort(candidates, comparator);

    // Show birthdays
    println(i18n("result", selection));

    Iterator e = candidates.iterator();
    while (e.hasNext()) {
      Indi indi = (Indi)e.next();
      println(indi.getName()+" (*"+indi.getBirthDate()+")");
    }

    // Done
  }

} //ReportBirthdays
