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
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;

import java.util.List;

/**
 * @author nmeier
 */
/*package*/ class TestSpouseGender extends Test {

  /**
   * Constructor
   */
  /*package*/ TestSpouseGender() {
    super(new String[]{"FAM"}, Property.class);
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    // assuming family
    Fam fam = (Fam)prop;
    
    // check husband/wife
    Indi husband = fam.getHusband();
    if (!testSex(husband, PropertySex.MALE)) 
      issues.add(new Issue(husband+" has wrong gender", husband.getImage(true), fam.getProperty("HUSB") ));
      
    Indi wife = fam.getWife();
    if (!testSex(wife, PropertySex.FEMALE)) 
      issues.add(new Issue(wife+" has wrong gender", husband.getImage(true), fam.getProperty("WIFE") ));

  }    

  /**
   * Test an individual's sex
   */
  private boolean testSex(Indi indi, int sex) {
    return indi==null ? true : indi.getSex()==sex;
  }

} //TestHusbandGender