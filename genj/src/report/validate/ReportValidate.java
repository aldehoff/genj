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
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.report.Report;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A report that validates a Gedcom file and displays
 * anomalies and 'standard' compliancy issues
 */
public class ReportValidate extends Report {
  
  /** whether we consider an empty value to be valid */
  public boolean isEmptyValueValid = true;
  
  /** whether we consider 'private' information valid or not */
  public boolean isPrivateValueValid = true;
  
  /** whether we consider missing files as valid or not */
  public boolean isFileNotFoundValid = true;

  /** whether we consider underscore tags to be valid custom tags */
  public boolean isUnderscoreValid = true;

  /** options of reports are picked up via field-introspection */
  public int
    maxLife      = 90,
    minAgeMARR   = 15,
    maxAgeBAPM   =  6,
    minAgeRETI   = 45,
    minAgeFather = 14,
    minAgeMother = 16,
    maxAgeMother = 40;
    
  /** Jerome's checks that haven't made it yet

   [ ] individuals who are cremated more than MAX_BURRYING_OR_CREM years after they die
   [ ] families containing a man who has fathered a child (more than 9 months) after they have died
   [ ] age difference between husband and wife is not greater than SOME_VALUE.
   [ ] women who have given birth more than once within 9 months (discounting twins)
  
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
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    if (context instanceof Gedcom || context instanceof Entity)
      return getName();
    return null;
  }

  /**
   * @see genj.report.Report#start(java.lang.Object)
   */
  public void start(Object context) {

    // prepare tests
    List tests = createTests();
    
    // intit list of issues
    List issues = new ArrayList();
    
    // Entity to check?
    Gedcom gedcom;
    if (context instanceof Entity) {
      Entity e = (Entity)context;
      gedcom = e.getGedcom();
      TagPath path = new TagPath(e.getTag());
      test(e, path, MetaProperty.get(path), tests, issues);
    } else {
      
      // assuming Gedcom
      gedcom = (Gedcom)context;
      
      // test if there's a submitter
      if (gedcom.getSubmitter()==null)
        issues.add(new Issue(i18n("err.nosubmitter", gedcom.getName()), Gedcom.getImage(), null));
  
      // Loop through entities and test 'em
      for (int t=0;t<Gedcom.ENTITIES.length;t++) {
        for (Iterator es=gedcom.getEntities(Gedcom.ENTITIES[t]).iterator();es.hasNext();) {
          Entity e = (Entity)es.next();
          TagPath path = new TagPath(e.getTag());
          test(e, path, MetaProperty.get(path), tests, issues);
        }
      }
    }
    
    // any fixes proposed at all?
    if (issues.isEmpty()) {
      getOptionFromUser(i18n("noissues"), Report.OPTION_OK);
      return;
    }
    
    // show fixes
    showItemsToUser(i18n("issues"), gedcom, (Issue[])issues.toArray(new Issue[issues.size()]));
    
    // done
  }
  
  /**
   * Test a property (recursively)
   */
  private void test(Property prop, TagPath path, MetaProperty meta, List tests, List issues) {
    // test tests
    for (int i=0, j=tests.size(); i<j; i++) {
      Test tst = (Test)tests.get(i);
      // applicable?
      if (!tst.applies(prop, path))
        continue;
      // test it
      tst.test(prop, path, issues, this);
      // next
    }
    // don't recurse into custom underscore tags
    if (isUnderscoreValid&&prop.getTag().startsWith("_"))
      return;
    // recurse into all its properties
    for (int i=0,j=prop.getNoOfProperties();i<j;i++) {
      // for non-system, non-transient children
      Property child = prop.getProperty(i);
      if (child.isTransient()) 
        continue;
      // get child tag
      String ctag = child.getTag();
      // check if it's a custom tag
      if (isUnderscoreValid&&ctag.startsWith("_"))
        continue;
      // check if Gedcom grammar allows it
      if (!meta.allows(ctag)) {
        String msg = i18n("err.notgedcom", new String[]{ctag,path.toString()});
        issues.add(new Issue(msg, MetaProperty.IMG_ERROR, child));
        continue;
      }
      // dive into
      test(child, new TagPath(path, ctag), meta.get(child.getTag(), false), tests, issues);
      // next child
    }
    // done
  }
  
  /**
   * Create the tests we're using
   */
  private List createTests() {
    
    List result = new ArrayList();

    // ******************** SPECIALIZED TESTS *******************************

    // non-valid properties
    result.add(new TestValid(this));
    
    // spouses with wrong gender
    result.add(new TestSpouseGender());

    // non existing files
    if (!isFileNotFoundValid)
      result.add(new TestFile());

    // ****************** DATE COMPARISON TESTS *****************************

    // birth after death
    result.add(new TestDate("INDI:BIRT:DATE",TestDate.AFTER  ,"INDI:DEAT:DATE"));
    
    // burial before death
    result.add(new TestDate("INDI:BURI:DATE",TestDate.BEFORE ,"INDI:DEAT:DATE"));
    
    // events before birth
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.BEFORE ,"INDI:BIRT:DATE"));
    
    // events after death
    result.add(new TestDate(LIFETIME_DATES  ,TestDate.AFTER  ,"INDI:DEAT:DATE"));

    // marriage after divorce 
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:DIV:DATE"));
    
    // marriage after death of husband/wife
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:HUSB:INDI:DEAT:DATE"));
    result.add(new TestDate("FAM:MARR:DATE" ,TestDate.AFTER  ,"FAM:WIFE:INDI:DEAT:DATE"));

    // childbirth after death of mother
    result.add(new TestDate("FAM:CHIL"      ,"CHIL:INDI:BIRT:DATE", TestDate.AFTER  ,"FAM:WIFE:INDI:DEAT:DATE"));

    // childbirth before marriage / after div
    result.add(new TestDate("FAM:CHIL"      ,"CHIL:INDI:BIRT:DATE", TestDate.BEFORE ,"FAM:MARR:DATE"));
    result.add(new TestDate("FAM:CHIL"      ,"CHIL:INDI:BIRT:DATE", TestDate.AFTER  ,"FAM:DIV:DATE"));

    // ************************* AGE TESTS **********************************
    
    // max lifespane
    if (maxLife>0)
      result.add(new TestAge ("INDI:DEAT:DATE","..:..:INDI", TestAge.OVER ,   maxLife, "maxLife"  ));
    
    // max BAPM age
    if (maxAgeBAPM>0) 
      result.add(new TestAge ("INDI:BAPM:DATE","..:..:INDI", TestAge.OVER ,maxAgeBAPM,"maxAgeBAPM"));
    
    // max CHRI age
    if (maxAgeBAPM>0) 
      result.add(new TestAge ("INDI:CHRI:DATE","..:..:INDI", TestAge.OVER ,maxAgeBAPM,"maxAgeBAPM"));
    
    // min RETI age
    if (minAgeRETI>0)
      result.add(new TestAge ("INDI:RETI:DATE","..:..:INDI", TestAge.UNDER,minAgeRETI,"minAgeRETI"));

    // min MARR age of husband, wife
    if (minAgeMARR>0) 
      result.add(new TestAge ("FAM:MARR:DATE" ,"..:..:FAM:HUSB:INDI", TestAge.UNDER  ,minAgeMARR,"minAgeMARR"));
    if (minAgeMARR>0) 
      result.add(new TestAge ("FAM:MARR:DATE" ,"..:..:FAM:WIFE:INDI", TestAge.UNDER  ,minAgeMARR,"minAgeMARR"));
    
    // min/max age for father, mother
    if (minAgeMother>0) 
      result.add(new TestAge ("FAM:CHIL", "CHIL:INDI:BIRT:DATE" ,"..:FAM:WIFE:INDI", TestAge.UNDER,minAgeMother,"minAgeMother"));
    if (maxAgeMother>0) 
      result.add(new TestAge ("FAM:CHIL", "CHIL:INDI:BIRT:DATE" ,"..:FAM:WIFE:INDI", TestAge.OVER ,maxAgeMother,"maxAgeMother"));
    if (minAgeFather>0) 
      result.add(new TestAge ("FAM:CHIL", "CHIL:INDI:BIRT:DATE" ,"..:FAM:HUSB:INDI", TestAge.UNDER,minAgeFather,"minAgeFather"));
    

    // **********************************************************************
    return result;    
  }

} //ReportValidate