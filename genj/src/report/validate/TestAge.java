/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PointInTime;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;

import java.util.List;

/**
 * Test age of individuals at specific dates
 */
public class TestAge extends Test {

  /** comparisons */
  /*package*/ final static int 
    OVER = 0,
    UNDER = 1;
    
  /** tag path to indi */
  private TagPath path2indi;    
    
  /** the mode GREATER, LESS, ... */
  private int comparison;
  
  /** the value */
  private int years;

  /**
   * Constructor
   * @param path the path to a date being tested for indi's age
   * @param path to get to indi
   * @param comp either OVER or UNDER
   * @param yrs age in years 
   */
  /*package*/ TestAge(String path, String p2indi, int comp, int yrs) {
    // delegate to super
    super(path, PropertyDate.class);
    // remember
    path2indi = new TagPath(p2indi);
    comparison = comp;
    years = yrs;
  }
  
  /**
   * Test individual(s)'s age at given date property (PropertyDate.class) 
   * 
   */
  /*package*/ void test(Property prop, TagPath path, List issues) {
    
    // assuming date
    PropertyDate date = (PropertyDate)prop;
    if (!date.isValid())
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
    
    // calculate delta
    int[] delta = PointInTime.getDelta(pit1, pit2);
    if (delta==null)
      return;
      
    // test it 
    if (isError(delta[0])) 
      issues.add(new Issue(getError(indi, path), date.getParent().getImage(false), date));
    
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
  private String getError(Indi indi, TagPath path) {
    
    // prepare it
    return 
        "Age of "+indi+" at "
      + Gedcom.getName(path.get(path.length()-2))
      + (comparison==UNDER ? " less " : " greater ") 
      + years + " years";
  }
  
} //TestAge