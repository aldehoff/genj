/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAge;
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

  /** how to group ages */
  private int ageGroupSize = 10;

  /**
   * Accessor - age grouping
   */
  public int getAgeGroupSize() {
    return ageGroupSize;
  }

  /**
   * Accessor - age grouping
   */
  public void setAgeGroupSize(int set) {
    ageGroupSize = Math.max(1, Math.min(25, set));
  }

  /**
   * n/a
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * main
   */
  public void start(Gedcom gedcom) {

    // gather data - we're using two series, one for males the other
    // for females.
    String[] categories = new String[100/ageGroupSize + 1];
    int max = 100/ageGroupSize*ageGroupSize;
    categories[0] = max+"+";
    for (int i=1;i<categories.length;i++) {
      if (ageGroupSize<5 && i%Math.ceil(5F/ageGroupSize)!=0)
        categories[i] = "";
      else
        categories[i] = (max - (i*ageGroupSize)) + "+";
    }

    // create category series for that
    IndexedSeries
      males = new IndexedSeries(translate("men"), categories.length),
      females = new IndexedSeries(translate("women"), categories.length);

    // Looping over each individual in gedcom
    Iterator indis = gedcom.getEntities(Gedcom.INDI).iterator();
    while (indis.hasNext()) {
      Indi indi = (Indi)indis.next();
      analyze(indi, males, females, max);
      // next
    }

    // the title is a simple localization
    String title = translate("title", gedcom.getName());

    // show it in a chart
    // + we're using a custom format so that the male series' negative
    //   values show up as a positive ones.
    // + isStacked makes sure the bars for the series are stacked instead
    //   of being side by side
    // + isVertical makes the main axis for the categories go from top
    //   to bottom
    showChartToUser(new Chart(title, PropertyAge.getLabelForAge(), new IndexedSeries[]{ males, females}, categories, new DecimalFormat("#; #"), true, true));

    // done
  }

  /**
   * Analyze one individual
   */
  private void analyze(Indi indi, IndexedSeries males, IndexedSeries females, int max) {

    // check it's birth and death
    PropertyDate birth = indi.getBirthDate();
    PropertyDate death = indi.getDeathDate();
    if (birth==null||death==null)
      return;

    // compute a delta
    Delta delta = Delta.get(birth.getStart(), death.getStart());
    if (delta==null||delta.getYears()<0)
      return;
    int years = delta.getYears();

    // for the male series we decrease the number of individuals
    // and for females we increase. That's how we get the male
    // bars on the left and the females on the right of the axis.
    int group = years>=max ? 0 : (max-years-1)/ageGroupSize + 1;
    if (indi.getSex() == PropertySex.MALE)
      males.dec(group);
    else
      females.inc(group);

    // done
  }

} //ReportLifeExpectancy
