/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import java.util.List;

import genj.gedcom.Property;
import genj.gedcom.TagPath;

/**
 * Test for property validity
 * @author nmeier
 */
/*package*/ class TestValid extends Test {

  /** whether no-information is considered to be valid */
  private boolean isEmptyValid;

  /**
   * Constructor
   */
  /*package*/ TestValid(boolean emptyIsValid) {
    super((String[])null, Property.class);
    isEmptyValid = emptyIsValid;
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, List issues, ReportValidate report) {

    // no issue if valid 
    if (prop.isValid())
      return;
      
    // no issue if isEmptyValid&&getValue() is empty
    if (isEmptyValid&&prop.getValue().length()==0)
      return;
      
    // got an issue with that
    issues.add(new Issue(report.i18n("err.notvalid", path.toString()), prop.getImage(true), prop));
    
    // done
  }

} //TestValid