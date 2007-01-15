/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test for a bad merge where dupe families contain the same parents but different children
 */
public class TestFamilyClone extends Test {
  
  // keeping some state here
  private Set reportedFams = new HashSet();

  /**
   * Constructor
   */
  public TestFamilyClone() {
    super("FAM", Property.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    // assuming Family
    Fam fam = (Fam)prop;
    if (reportedFams.contains(fam))
      return;
    
    // check it
    Indi husband = fam.getHusband();
    Indi wife = fam.getWife();

    if (husband!=null)
      test(fam, husband, wife, husband.getFamiliesWhereSpouse(), issues, report);
    else if (wife!=null)
      test(fam, husband, wife, wife.getFamiliesWhereSpouse(), issues, report);
  }
  
  private void test(Fam fam, Indi husband, Indi wife, Fam[] others, List issues, ReportValidate report) {

    for (int i = 0; i < others.length; i++) {
      Fam other = others[i];
      if (fam==other) continue;
      if (other.getHusband()==husband&&other.getWife()==wife) {
        if (!reportedFams.contains(fam)) {
          issues.add(new ViewContext(fam).setText(report.translate("warn.fam.cloned", fam.getId() )));
          reportedFams.add(fam);
        }
        issues.add(new ViewContext(other).setText(report.translate("warn.fam.clone", new String[]{ other.getId(), fam .getId() })));
        reportedFams.add(other);
      }
    }

  }

} //TestFiles