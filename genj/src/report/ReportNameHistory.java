/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import genj.chart.XYSheet;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

import java.util.Iterator;

/**
 * Chart names and their usage in a gedcom file
 */
public class ReportNameHistory extends Report {

  /**
   * no stdout necessary for this report
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * Main
   */
  public void start(Object context) {
    
    // assuming gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // loop over individuals
    XYSheet sheet = new XYSheet();
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      // look for individuals with a birth and name
      Indi indi = (Indi)indis.next();
      PropertyDate birth = indi.getBirthDate();
      PropertyDate death = indi.getDeathDate();
      PropertyName name = (PropertyName)indi.getProperty("NAME");
      if (birth==null||!birth.isValid()||name==null||!name.isValid())
        continue;
      String last = name.getLastName();
      if (last.length()==0)
        continue;
      // keep only if year > 1700
      int start = birth.getStart().getYear();
      if (start<1700)
        continue;
      // and minimum 10 total persons
      if (PropertyName.getPropertyNames(gedcom, last).size()<10)
        continue;
      // calculate end
      int end = death==null||!death.isValid() ? start+80 : death.getStart().getYear();
      end = Math.min( PointInTime.getNow().getYear(), Math.max(start, end));
      // keep it's x*y
      for (;start<=end;start++)
        sheet.inc(last, start);
    }
    
    // show it
    showChartToUser(getName(), sheet);

    // done
  }
  
} //ReportNameUsage