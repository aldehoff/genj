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
 */
public class ReportBirthdays extends Report {

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    // a call to i18n will lookup a string with given key in ReportBirthdays.properties
    return i18n("version");
  }
  
  /**
   * Returns the name of this report
   */
  public String getName() {
    // a call to i18n will lookup a string with given key in ReportBirthdays.properties
    return i18n("name");
  }

  /**
   * Some information about this report
   */
  public String getInfo() {
    // a call to i18n will lookup a string with given key in ReportBirthdays.properties
    return i18n("info");
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * Entry point into this report - by default reports are only run on a
   * context of type Gedcom. Depending on the logic in accepts either
   * an instance of Gedcom, Entity or Property can be passed in though. 
   */
  public void start(Object context) {
    
    // assuming Gedcom
    Gedcom gedcom = (Gedcom)context;

    // Show months and check user's selection
    String[] months = PointInTime.getMonths(true);
    String selection = (String)getValueFromUser(i18n("select"),months,null);
    if (selection==null) 
      return;

    // find out which month it was
    int month=0; while (month<months.length&&months[month]!=selection) month++;

    // Look for candidates - folks with birthdays in given month
    List candidates = new ArrayList(100);

    List indis = gedcom.getEntities(gedcom.INDIVIDUALS);
    for (int i=0;i<indis.size();i++) {
      Indi indi = (Indi)indis.get(i);
      PropertyDate birth = indi.getBirthDate();
      if (birth==null) 
        continue;
      if (birth.getStart().getMonth() == month)
        candidates.add(indi);
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
    
    // Sorting by date is possible, too 
    //
    // Comparator comparator = new genj.gedcom.PropertyComparator("INDI:BIRT:DATE");
    //
    Collections.sort(candidates, comparator);

    // Show birthdays - a call to i18n localizes 'result' and inserts the given selection
    println(i18n("result", selection));

    Iterator e = candidates.iterator();
    while (e.hasNext()) {
      Indi indi = (Indi)e.next();
			String[] msgargs = {indi.getName(),
													indi.getBirthDate()+""};
      println(i18n("format",msgargs));
    }

    // Done
  }

} //ReportBirthdays
