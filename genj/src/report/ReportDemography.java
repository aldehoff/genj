/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.IndexedSeries;
import genj.gedcom.Fam;
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

  public int reportType = 0;
  public String[] reportTypes = { 
          PropertyAge.getLabelForAge(), // age at death 
          translate("ageAtChildsBith"), 
          translate("ageAtFirstMariage"), 
          translate("ageAtParentsDeath"), 
          translate("ageOfYoungestChildLeftBehind"), 
  };

  /** how to group ages */
  private int ageGroupSize = 10;

  /** Accessor - age grouping */
  public int getAgeGroupSize() {
    return ageGroupSize;
  }

  /** Accessor - age grouping */
  public void setAgeGroupSize(int set) {
    ageGroupSize = Math.max(1, Math.min(25, set));
  }

  /** n/a */
  public boolean usesStandardOut() {
    return false;
  }

  /** main */
  public void start(Gedcom gedcom) throws Exception {

    DemographicAnalyzer analyzer;

    // labels
    String title = translate("title", gedcom.getName());
    String fathers = translate("fathers");
    String mothers = translate("mothers");
    String men = translate("men");
    String woman = translate("women");

    // setup - the analyzer uses two series, one for males the other for females.
    switch ( reportType ) { 
    case 0: analyzer = new DeathAnalyzer         (men,     woman,   ageGroupSize); break; 
    case 1: analyzer = new BirthAnalyzer         (fathers, mothers, ageGroupSize); break;
    case 2: analyzer = new MariageAnalyzer       (men,     woman,   ageGroupSize); break;
    case 3: analyzer = new OrphanAnalyzer        (fathers, mothers, ageGroupSize); break;
    case 4: analyzer = new YoungestChildAnalyzer (men,     woman,   ageGroupSize); break;
    default: throw new Exception ("programming error: report not implemented");
    }
    
    // Gather data - looping over each individual in gedcom
    Iterator indis = gedcom.getEntities( Gedcom.INDI ).iterator();
    while ( indis.hasNext() ) {
      analyzer.add( (Indi) indis.next() );
    }

    // show it in a chart
    Chart chart = analyzer.createChart (title, reportTypes[reportType]);
    showChartToUser( chart );
  }

  /**
   * Age distribution for males/females
   */
  private static abstract class DemographicAnalyzer {
      
    private int max, ageGroupSize; 
    private IndexedSeries males;
    private IndexedSeries females;
    private String[] categories;
    
    public DemographicAnalyzer (String maleLabel, String femaleLabel, int ageGroupSize) {
    
      categories = new String[100/ageGroupSize + 1];
      int max = 100/ageGroupSize*ageGroupSize;
      categories[0] = max+"+";
      for (int i=1;i<categories.length;i++) {
        if (ageGroupSize<5 && i%Math.ceil(5F/ageGroupSize)!=0)
          categories[i] = "";
        else
          categories[i] = (max - (i*ageGroupSize)) + "+";
      }

      this.max = max;
      this.ageGroupSize = ageGroupSize;
      this.males = new IndexedSeries(maleLabel, categories.length);
      this.females = new IndexedSeries(femaleLabel, categories.length);
    }
    
    /** for the male series we decrease the number of individuals
     * and for females we increase. That's how we get the male
     * bars on the left and the females on the right of the axis.
     */
    public abstract void add (Indi indi);

    public Chart createChart (String title, String ageLabel) {
      // + we're using a custom format so that the male series' negative
      //   values show up as a positive ones.
      // + isStacked makes sure the bars for the series are stacked instead
      //   of being side by side
      // + isVertical makes the main axis for the categories go from top
      //   to bottom
      IndexedSeries[] nestedSeries = new IndexedSeries[]{ getMales(), getFemales()};
      return new Chart(title, ageLabel, nestedSeries, categories, new DecimalFormat("#; #"), true, true);
    }

    /** Throws IllegalArgumentException or NullPointerException in case something is missing */
    protected int calculateGroup(Indi indi, PropertyDate event) {
      
      PropertyDate birth = indi.getBirthDate();
      if ( ! birth.isValid() || ! event.isValid() )
        throw new IllegalArgumentException();
      
      Delta delta = Delta.get(birth.getStart(), event.getStart());
      if ( delta.getYears() < 0 )
        throw new IllegalArgumentException();
      
      int years = delta.getYears();
      
      return years >= max ? 0 : (max-years-1)/ageGroupSize + 1;
    }
    
    public IndexedSeries getFemales() {
      return females;
    }

    public IndexedSeries getMales() {
      return males;
    }
  }

  private static class DeathAnalyzer extends DemographicAnalyzer {

    DeathAnalyzer (String malesTitle, String femalesTitle, int ageGroupSize) {
      super(malesTitle, femalesTitle, ageGroupSize);
    }

    public void add (Indi indi) {
      try { // events or dates might not be available, catch and skip them
        int group = calculateGroup(indi, indi.getDeathDate());
        if (indi.getSex() == PropertySex.MALE)
          getMales().dec(group);
        else
          getFemales().inc(group);
      }
      catch (IllegalArgumentException e) {}
      catch (NullPointerException e) {}
    }
  }
  
  private static class MariageAnalyzer extends DemographicAnalyzer {
    
    MariageAnalyzer (String malesTitle, String femalesTitle, int ageGroupSize) {
      super(malesTitle, femalesTitle, ageGroupSize);
    }
    
    public void add (Indi indi) {
      try { // events or dates might not be available, catch and skip them
        int group = calculateGroup(indi, indi.getFamiliesWhereSpouse()[0].getMarriageDate());
        if (indi.getSex() == PropertySex.MALE)
          getMales().dec(group);
        else
          getFemales().inc(group);
      }
      catch (IllegalArgumentException e) {}
      catch (ArrayIndexOutOfBoundsException e) {}
      catch (NullPointerException e) {}
    }
  }
  
  private static class BirthAnalyzer extends DemographicAnalyzer {
    
    BirthAnalyzer(String malesTitle, String femalesTitle, int ageGroupSize) {
      super(malesTitle, femalesTitle, ageGroupSize);
    }

    public void add (Indi indi) {
      try { // events or dates might not be available, catch and skip them
        getMales().dec(calculateGroup(indi, indi.getBiologicalFather().getBirthDate()));
        getFemales().inc(calculateGroup(indi, indi.getBiologicalMother().getBirthDate()));
      }
      catch (IllegalArgumentException e) {}
      catch (NullPointerException e) {}
    }
  }
  
  private static class OrphanAnalyzer extends DemographicAnalyzer {
    
    OrphanAnalyzer(String malesTitle, String femalesTitle, int ageGroupSize) {
      super(malesTitle, femalesTitle, ageGroupSize);
    }
    
    public void add (Indi indi) {
      try { // events or dates might not be available, catch and skip them
        getMales().dec(calculateGroup(indi, indi.getBiologicalFather().getDeathDate()));
        getFemales().inc(calculateGroup(indi, indi.getBiologicalMother().getDeathDate()));
      }
      catch (IllegalArgumentException e) {}
      catch (NullPointerException e) {}
    }
  }
  
  private static class YoungestChildAnalyzer extends DemographicAnalyzer {
    
    YoungestChildAnalyzer(String malesTitle, String femalesTitle, int ageGroupSize) {
      super(malesTitle, femalesTitle, ageGroupSize);
    }
    
    public void add (Indi indi) {
      try { // events or dates might not be available, catch and skip them

        // find youngest child
        Fam[] fams = indi.getFamiliesWhereSpouse();
        Fam fam = fams[fams.length - 1 ];
        Indi[] children = fam.getChildren();
        Indi child = children[children.length - 1];
      
        // add its age when left behind
        int group = calculateGroup(child, indi.getDeathDate());
        if (indi.getSex() == PropertySex.MALE)
          getMales().dec(group);
        else
          getFemales().inc(group);
      }
      catch (IllegalArgumentException e) {}
      catch (ArrayIndexOutOfBoundsException e) {}
      catch (NullPointerException e) {}
    }
  }
} 
