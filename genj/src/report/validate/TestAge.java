/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Indi;
import genj.gedcom.PointInTime;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.util.WordBuffer;

import java.util.List;

/**
 * Test age of individuals at specific dates
 */
public class TestAge extends Test {

  /** comparisons */
  /*package*/ final static int 
    OVER = 0,
    UNDER = 1;
    
  /** tag path to date (optional) */
  private TagPath path2date;    
    
  /** tag path to indi */
  private TagPath path2indi;    
    
  /** the mode GREATER, LESS, ... */
  private int comparison;
  
  /** the value */
  private int years;

  /**
   * Constructor
   * @param trigger the path that triggers this test (pointing to date)
   * @param p2indi to get to indi to test for age
   * @param comp either OVER or UNDER
   * @param yrs age in years 
   */
  /*package*/ TestAge(String trigger, String p2indi, int comp, int yrs) {
    this(trigger, null, p2indi, comp, yrs);
  }

  /**
   * Constructor
   * @param trigger the path that triggers this test
   * @param p2date path in entity to a date as basis for age calculation
   * @param p2indi path to get to indi to test for age
   * @param comp either OVER or UNDER
   * @param yrs age in years 
   */
  /*package*/ TestAge(String trigger, String p2date, String p2indi, int comp, int yrs) {
    // delegate to super
    super(trigger, p2date!=null?Property.class:PropertyDate.class);
    // remember
    path2date = p2date!=null?new TagPath(p2date):null;
    path2indi = new TagPath(p2indi);
    comparison = comp;
    years = yrs;
  }
  
  /**
   * Test individual(s)'s age at given date property 
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {
    
    // get to the date
    PropertyDate date ;
    if (path2date!=null) {
      date = (PropertyDate)prop.getEntity().getProperty(path2date, Property.QUERY_VALID_TRUE|Property.QUERY_FOLLOW_LINK);
    } else {
      date = (PropertyDate)prop;
    }
      
    if (date==null||!date.isValid())
      return;

    // find indi we compute age for (e.g. "INDI" or "MARR:HUSB")
    Property pindi = prop.getEntity().getProperty(path2indi, Property.QUERY_VALID_TRUE|Property.QUERY_FOLLOW_LINK);
    if (!(pindi instanceof Indi))
      return;
    Indi indi = (Indi)pindi;      

    // calc pit of date
    PointInTime pit2 = date.getStart();

    // get birth
    PropertyDate birt = indi.getBirthDate();
    if (birt==null||!birt.isValid())
      return;
    PointInTime pit1 = birt.getStart();
    
    // don't test if birth<date?
    if (pit1.compareTo(pit2)>0)
      return;
    
    // calculate delta
    int[] delta = PointInTime.getDelta(pit1, pit2);
    if (delta==null)
      return;
      
    // test it 
    if (isError(delta[0])) 
      issues.add(new Issue(getError(indi), date.getParent().getImage(false), prop));
    
    // done
  }
  
  /**
   * test
   */
  private boolean isError(int age) {
    switch (comparison) {
      case OVER:
        return age > years;
      case UNDER:
        return age < years;
    }
    return false;
  }

  /**
   * Calculate error messag from two paths
   */
  private String getError(Indi indi) {
    
    WordBuffer words = new WordBuffer();
    words.append("Age of");
    words.append(indi.toString());
    if (comparison==UNDER) {
      words.append("under");
    } else {
      words.append("over");
    }
    words.append(String.valueOf(years));
    return words.toString();
  }
  
} //TestAge