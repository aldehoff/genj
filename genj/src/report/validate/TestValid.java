/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.report.PropertyList;

/**
 * Test for property validity
 * @author nmeier
 */
/*package*/ class TestValid extends Test {

  /** the report */
  private ReportValidate report;
  
  /**
   * Constructor
   */
  /*package*/ TestValid(ReportValidate report) {
    super((String[])null, Property.class);
    this.report = report;
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, PropertyList issues, ReportValidate report) {
    
    // always an issue with private
    if (!report.isPrivateValueValid&&prop.isPrivate()) {
      // got an issue with that
      issues.add(report.i18n("err.private", path.toString()), prop.getImage(true), prop);
    }

    // no issue if valid 
    if (prop.isValid())
      return;
      
    // no issue if isEmptyValid&&getValue() is empty
    if (report.isEmptyValueValid&&prop.getValue().length()==0)
      return;
      
    // got an issue with that
    issues.add(report.i18n("err.notvalid", path.toString()), prop.getImage(true), prop);
    
    // done
  }

} //TestValid