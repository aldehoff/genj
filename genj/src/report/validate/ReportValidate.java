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
import genj.report.Report;

import java.util.ArrayList;
import java.util.List;

/**
 * A report that validates a Gedcom file and displays
 * anomalies and 'standard' compliancy issues 
 * @author nmeier
 */
public class ReportValidate extends Report {

  private final static String[] LIFETIME_DATES = {
    "INDI:ADOP:DATE",
    "INDI:ADOP:DATE",
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

  /** PENDING move this in configuration file */
  private final static Test[] TESTS = {

    new TestValid(),
    
    new TestSpouseGender(),

    new TestDate("INDI:BIRT:DATE",TestDate.AFTER ,"INDI:DEAT:DATE"),
    new TestDate("INDI:BURI:DATE",TestDate.BEFORE,"INDI:DEAT:DATE"),
    
    new TestDate(LIFETIME_DATES  ,TestDate.BEFORE,"INDI:BIRT:DATE"),
    new TestDate(LIFETIME_DATES  ,TestDate.AFTER ,"INDI:DEAT:DATE"),

    new TestAge ("INDI:BAPM:DATE",TestAge .GREATER, 10),
    new TestAge ("INDI:RETI:DATE",TestAge .LESS   , 45)
    
  };
  
  /** universal tests */
  private List tests = new ArrayList();
  
  /** fixes we come up with */
  private List fixes = new ArrayList();
  
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
    
    // assuming Gedcom
    Gedcom gedcom = (Gedcom)context;
    
    // Loop through entities and test 'em
    for (int t=0; t<Gedcom.ETYPES.length; t++) {
      Entity[] es = gedcom.getEntities(Gedcom.ETYPES[t], "");
      for (int e=0;e<es.length;e++)
        test(es[e], new TagPath(es[e].getTag()));
    }
    
    // any fixes proposed at all?
    if (fixes.isEmpty()) {
      getOptionFromUser("No issues found!", new String[]{"Great"});
      return;
    }
    
    // show fixes
    showItemsToUser("Issues", gedcom, (Issue[])fixes.toArray(new Issue[fixes.size()]));
    
    // done
  }
  
  /**
   * Test a property (recursively)
   */
  private void test(Property prop, TagPath path) {
    // test tests
    for (int i=0; i<TESTS.length; i++) {
      Test tst = TESTS[i];
      // applicable?
      if (!tst.applies(prop, path))
        continue;
      // test it
      Issue fix = tst.test(prop, path);
      if (fix!=null) fixes.add(fix);
      // next
    }
    // recurse into all its properties
    for (int i=0,j=prop.getNoOfProperties();i<j;i++) {
      // for non-system, non-transient children
      Property child = prop.getProperty(i);
      if (child.isTransient()||child.isSystem()) continue;
      // dive into
      path.add(child.getTag());
      test(child, path);
      path.pop();
      // next child
    }
    // done
  }
  
  

} //ReportValidate