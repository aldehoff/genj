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

/**
 * Test age
 */
public class TestAge extends Test {

  /** comparisons */
  /*package*/ final static int 
    GREATER = 0,
    LESS = 1;
    
  /** the mode GREATER, LESS, ... */
  private int comparison;
  
  /** the value */
  private int years;

  /**
   * Constructor
   */
  /*package*/ TestAge(String path, int comp, int yrs) {
    // delegate to super
    super(path, PropertyDate.class);
    // remember
    comparison = comp;
    years = yrs;
  }

  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath)
   */
  /*package*/ Issue test(Property prop, TagPath path) {
    
    // assuming date
    PropertyDate date = (PropertyDate)prop;
    if (!date.isValid())
      return null;
    PointInTime pit2 = date.getStart();

    // get birth
    Indi indi = (Indi)prop.getEntity();
    PropertyDate birt = indi.getBirthDate();
    if (birt==null||!birt.isValid())
      return null;
    PointInTime pit1 = birt.getStart();
    
    // calculate delta
    int[] delta = PointInTime.getDelta(pit1, pit2);
    if (delta==null)
      return null;
      
    // test it 
    if (isError(delta[0])) 
      return new Issue(getError(path), date.getImage(false), date);
    
    // all good
    return null;
  }
  
  /**
   * test
   */
  private boolean isError(int age) {
    switch (comparison) {
      case GREATER:
        return age > years;
      case LESS:
        return age < years;
    }
    return false;
  }

  /**
   * Calculate error messag from two paths
   */
  private String getError(TagPath path) {
    
    // prepare it
    return 
        "Age at "
      + Gedcom.getName(path.get(path.length()-2))
      + (comparison==LESS ? " less " : " greater ") 
      + years + " years";
  }
  
} //TestAge