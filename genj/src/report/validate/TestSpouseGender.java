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
  /*package*/ void test(Property prop, TagPath path, List issues) {
    
    // assuming family
    Fam fam = (Fam)prop;
    
    // check husband/wife
    Indi husband = fam.getHusband();
    if (!testSex(husband, PropertySex.MALE)) 
      issues.add(new GenderChange(fam, "HUSB",husband, PropertySex.MALE  ));
    Indi wife = fam.getWife();
    if (!testSex(wife, PropertySex.FEMALE)) 
      issues.add(new GenderChange(fam, "WIFE",wife, PropertySex.FEMALE));

  }    

  /**
   * A gender change issue
   */      
  private static class GenderChange extends SolvableIssue {
    
    /** sex to change to */
    private int change;
    
    /**
     * Constructor
     */
    private GenderChange(Fam fam, String who, Indi indi, int sex) {
      super(who+' '+indi+" has wrong gender", indi.getImage(false), fam.getProperty(who));
      change = sex;
    }
    
    /**
     * @see validate.SolvableIssue#solution()
     */
    /*package*/ String solution() {
      return "Change to "+change;
    }

    /**
     * @see validate.SolvableIssue#solve()
     */
    void solve() {
      System.out.println(solution());
    }
    
  } //GenderChange  
  
  /**
   * Test an individual's sex
   */
  private boolean testSex(Indi indi, int sex) {
    // no indi -> assuming true
    if (indi==null)
      return true;
    // check it
    return indi.getSex()==sex;
  }

} //TestHusbandGender