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

/**
 * @author nmeier
 */
/*package*/ class TestSpouseGender extends Test {

  /**
   * Constructor
   */
  /*package*/ TestSpouseGender() {
    super(new String[]{"FAM:HUSB","FAM:WIFE"}, Property.class);
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath)
   */
  /*package*/ Issue test(Property prop, TagPath path) {
    
    // assuming family
    Fam fam = (Fam)prop.getEntity();
    
    // check husband/wife
    Indi indi;
    String msg;
    final int sex;
    if ("WIFE".equals(path.getLast())) {
      msg = "Non-Female wife";
      sex = PropertySex.FEMALE;
      indi = fam.getWife();
      if (indi.getSex()==sex)
        return null;
    } else {
      msg = "Non-Male husband";
      sex = PropertySex.MALE;
      indi = fam.getHusband();
      if (indi.getSex()==sex)
        return null;
    }
      
    // prepare a fix
    return new SolvableIssue(msg, indi.getImage(false), indi) {
      String solution() {
        return "Set Gender to "+PropertySex.getLabelForSex(sex);
      }
      void solve() {
        System.out.println(sex);
      }
    };
  }

} //TestHusbandGender