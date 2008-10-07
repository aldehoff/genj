/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.chart.Chart;
import genj.chart.DemographhicAnalyzer;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyAge;
import genj.gedcom.PropertySex;
import genj.report.Report;

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

    DemographhicAnalyzer analyzer;

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

  private class DeathAnalyzer extends DemographhicAnalyzer {

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
  
  private class MariageAnalyzer extends DemographhicAnalyzer {
    
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
  
  class BirthAnalyzer extends DemographhicAnalyzer {
    
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
  
  class OrphanAnalyzer extends DemographhicAnalyzer {
    
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
  
  class YoungestChildAnalyzer extends DemographhicAnalyzer {
    
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
