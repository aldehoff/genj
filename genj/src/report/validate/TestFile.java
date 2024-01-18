/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.report.PropertyList;

/**
 * Test whether files that are pointed to actually exist
 */
public class TestFile extends Test {

  /**
   * Constructor
   */
  public TestFile() {
    super((String[])null, PropertyFile.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, PropertyList issues, ReportValidate report) {
    
    // assuming PropertyFile
    PropertyFile file = (PropertyFile)prop;
    
    // check it
    if (file.getFile()==null) 
      issues.add(report.translate("err.nofile"), prop.getImage(false), prop);

  }

} //TestFiles