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
    
    // gather data - we're using two series, one for males the other
    // for females. The categories we're collecting is ages/lifespans
    // for people between 0-9 and 10-19 etc. years old
    String[] series = { PropertySex.TXT_MALE, PropertySex.TXT_FEMALE };
    String[] categories = { "100+", "90-99", "80-89", "70-79", "60-69", "50-59", "40-49", "30-39", "20-29", "10-19", "0-9" };

    CategorySheet sheet = new CategorySheet(series, categories);

    // Looping over each individual in gedcom
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      
      Indi indi = (Indi)indis.next();
      
      // check it's birth and death
      PropertyDate birth = indi.getBirthDate();
      PropertyDate death = indi.getDeathDate();
      if (birth==null||death==null)
        continue;
      
      // compute a delta
      Delta delta = Delta.get(birth.getStart(), death.getStart());
      if (delta==null||delta.getYears()<0)
        continue;

      // for the male series we decrease the number of individuals
      // and for females we increase. That's how we get the male
      // bars on the left and the females on the right of the axis.
      int col = 10 - (delta.getYears()>=100 ? 10 : delta.getYears()/10);
      if (indi.getSex() == PropertySex.MALE)
        sheet.dec(0,col);
      else
        sheet.inc(1,col);

      // next
    }

    // the title is a simple localization
    String title = i18n("title", gedcom.getName());
    
    // show it in a chart 
    // + we're using a custom format so that the male series' negative 
    //   values show up as a positive ones.
    // + isStacked makes sure the bars for the series are overlapping
    //   instead of stacked
    // + isVertical makes the main axis for the categories go from top
    //   to bottom
    showChartToUser(title, sheet, new DecimalFormat("#; #"), true, true);
      
    // done
  }

} //ReportLifeExpectancy
