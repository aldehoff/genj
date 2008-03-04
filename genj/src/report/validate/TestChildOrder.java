/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.Arrays;
import java.util.List;

/**
 * Test for dupes in information about being biological child
 */
public class TestChildOrder extends Test {

  /**
   * Constructor
   */
  /*package*/ TestChildOrder() {
    // delegate to super
    super("FAM", Fam.class);
  }
  
  /**
   * Test family's children being in order of birth
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {

    Fam fam  = (Fam)prop;
    if (!Arrays.asList(fam.getChildren(true)).equals(Arrays.asList(fam.getChildren(false))))
        issues.add(new ViewContext(fam).setText(report.translate("warn.fam.childorder")));
    
    
    // done
  }
  

} //TestBiologicalChild