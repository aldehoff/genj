/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.time.Delta;
import genj.report.Report;

import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * A report showing age distribution for males/females
 */
public class ReportDemography extends Report {

  /**
   * n/a
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * main
   */
  public void start(Object context) {
    
    // assume gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // gather data
    String[] series = { PropertySex.TXT_MALE, PropertySex.TXT_FEMALE };
    String[] categories = { "100+", "90-99", "80-89", "70-79", "60-69", "50-59", "40-49", "30-39", "20-29", "10-19", "0-9" };

    CategorySheet sheet = new CategorySheet(series, categories);
    
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      Indi indi = (Indi)indis.next();
      
      PropertyDate birth = indi.getBirthDate();
      PropertyDate death = indi.getDeathDate();
      if (birth==null||death==null)
        continue;
      Delta delta = Delta.get(birth.getStart(), death.getStart());
      if (delta==null||delta.getYears()<0)
        continue;
        
      int col = 10 - (delta.getYears()>=100 ? 10 : delta.getYears()/10);
      if (indi.getSex() == PropertySex.MALE)
        sheet.dec(0,col);
      else
        sheet.inc(1,col);
      
    }
    
    // show it
    String title = i18n("title", gedcom.getName());
    showChartToUser(title, sheet, new DecimalFormat("#; #"), true, true);
      
    // done
  }

} //ReportLifeExpectancy
