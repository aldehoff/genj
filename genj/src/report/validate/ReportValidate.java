/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.option.OptionBoolean;
import genj.option.OptionNumeric;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A report that validates a Gedcom file and displays
 * anomalies and 'standard' compliancy issues
 */
public class ReportValidate extends Report {
  
  /** our options */
  private OptionBoolean 
    isEmptyValueValid = new OptionBoolean("Empty Values are Valid", true);

  /** options of reports are picked up via field-introspection */
  protected OptionNumeric
    maxLife    = new OptionNumeric("Maximum Lifespan", 90),
    minAgeMARR = new OptionNumeric("Minimum age when marrying", 15),
    maxAgeBAPM = new OptionNumeric("Maximum age when baptised",  6),
    maxAgeCHRI = new OptionNumeric("Maximum age when christened",  6),    
    minAgeRETI = new OptionNumeric("Minimum age when retiring", 45);
    
/** Jerome's check still to be migrated:

 x  1) individuals whose death occurs before their birth
 x  2) individuals who are older then MAX_LIVING_AGE years old when they died
    3) individuals who would be older then MAX_LIVING_AGE years old today
 x  4) individuals who are Christened after the age of MAX_CHRISTENING
 x  5) individuals who are Christened after their death
 x  6) individuals who are Christened before they are born
 x  7) individuals who are burried before they die
    8) individuals who are burried more than MAX_BURRYING_OR_CREM years 
    after they die
 x  9) individuals who are cremated before they die
    10) individuals who are creamted more than MAX_BURRYING_OR_CREM years 
    after they die
 x  11) families containing an individual who marries before before the age 
    of MIN_MARRIAGE_AGE
 o  12) families containing an individual who marries after their death
 o  13) families containing a woman who has given birth before the age of 
    MIN_CHILD_BIRTH
 O  14) families containing a woman who has given birth after the age of 
    MAX_CHILD_BIRTH
 o  15) families containing a woman who has given birth after they have died
 o  16) families containing a man who has fathered a child before the age of 
    MIN_FATHER_AGE
 o  17) families containing a man who has fathered a child after they have died

    // check all files exist and can be opened
 x  new CheckFiles(gedcom));
    
    //family checks
    new FamilyDateChecker(null, marrTag, husbTag, deathTag, MAX_LIVING_AGE, NONE, DateChecker.AFTER));
    new FamilyDateChecker(null, marrTag, wifeTag, deathTag, MAX_LIVING_AGE, NONE, DateChecker.AFTER));
 x  new FamilyDateChecker(null, marrTag, husbTag, birthTag, MIN_MARRIAGE_AGE, MAX_LIVING_AGE, DateChecker.BEFORE));
 x  new FamilyDateChecker(null, marrTag, wifeTag, birthTag, MIN_MARRIAGE_AGE, MAX_LIVING_AGE, DateChecker.BEFORE));
  
**/  

  private final static String[] LIFETIME_DATES = {
    "INDI:ADOP:DATE",
    "INDI:ADOP:DATE",
    "INDI:BAPM:DATE",
    "INDI:BAPL:DATE",
    "INDI:BARM:DATE",
    "INDI:BASM:DATE",
    "INDI:BLES:DATE",
    "INDI:CHRA:DATE",
    "INDI:CONF:DATE",
    "INDI:ORDN:DATE",
    "INDI:NATU:DATE",
    "INDI:EMIG:DATE",
    "INDI:IMMI:DATE",
    "INDI:CENS:DATE",
    "INDI:RETI:DATE"
  };

  /**
   * @see genj.report.Report#getAuthor()
   */
  public String getAuthor() {
    return "Nils Meier";
  }
  
  /**
   * @see genj.report.Report#getVersion()
   */
  public String getVersion() {
    return "0.1";
  }

  /**
   * @see genj.report.Report#getName()
   */
  public String getName() {
    return "Validate Gedcom";
  }
  
  /**
   * @see genj.report.Report#getInfo()
   */
  public String getInfo() {
    return "Validates Gedcom file or entity for Gedcom compliancy and anomalies";
  }

  /**
   * @see genj.report.Report#start(java.lang.Object)
   */
  public void start(Object context) {
    
    println("***This Report is not finished yet - work in progress***");
    
    // assuming Gedcom
    Gedcom gedcom = (Gedcom)context;

    // prepare tests
    List tests = createTests();
    
    // intit list of issues
    List issues = new ArrayList();

    // Loop through entities and test 'em
    for (Iterator es=gedcom.getEntities().iterator();es.hasNext();) {
      Entity e = (Entity)es.next();
      test(e, new TagPath(e.getTag()), tests, issues);
    }
    
    // any fixes proposed at all?
    if (issues.isEmpty()) {
      getOptionFromUser("No issues found!", new String[]{"Great"});
      return;
    }
    
    // show fixes
    showItemsToUser("Issues", gedcom, (Issue[])issues.toArray(new Issue[issues.size()]));
    
    // done
  }
  
  /**
   * Test a property (recursively)
   */
  private void test(Property prop, TagPath path, List tests, List issues) {
    // test tests
    for (int i=0, j=tests.size(); i<j; i++) {
      Test tst = (Test)tests.get(i);
      // applicable?
      if (!tst.applies(prop, path))
        continue;
      // test it
      tst.test(prop, path, issues);
      // next
    }
    // recurse into all its properties
    for (int i=0,j=prop.getNoOfProperties();i<j;i++) {
      // for non-system, non-transient children
      Property child = prop.getProperty(i);
      if (child.isTransient()||child.isSystem()) continue;
      // dive into
      path.add(child.getTag());
      test(child, path, tests, issues);
      path.pop();
      // next child
    }
    // done
  }
  
  /**
   * Create the tests we're using
   */
  private List createTests() {
    
    List result = new ArrayList();

    // non-valid properties
    result.add(new TestValid(isEmptyValueValid.isSelected()));
    
    // spouses with wrong gender
    result.add(new TestSpouseGender());

    // birth after death
    result.add(new TestDate("INDI:BIRT:DATE",TestDate.AFTER  ,"INDI:DEAT:DATE"));
    
    // max lifespane
    result.add(new TestAge ("INDI:DEAT:DATE",TestAge .GREATER,maxLife.getValue()));
    
    // burial before death
    result.add(new TestDate("INDI:BURI:DATE",TestDate.BEFORE ,"INDI:DEAT:DATE"));
    
    // events before birth
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.BEFORE ,"INDI:BIRT:DATE"));
    
    // events after death
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.AFTER  ,"INDI:DEAT:DATE"));

    // max BAPM age 
    result.add(new TestAge ("INDI:BAPM:DATE",TestAge .GREATER,maxAgeBAPM.getValue()));
    
    // max CHRI age 
    result.add(new TestAge ("INDI:CHRI:DATE",TestAge .GREATER,maxAgeCHRI.getValue()));
    
    // min RETI age
    result.add(new TestAge ("INDI:RETI:DATE",TestAge .LESS   ,minAgeRETI.getValue()));

    // min MARR age
    result.add(new TestAge ("FAM:MARR:DATE" ,TestAge .LESS   ,minAgeMARR.getValue()));

    // marriage after divorce 
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:DIV:DATE"));
    
    // non existing files
    result.add(new TestFile());

    // done
    return result;    
  }

} //ReportValidate