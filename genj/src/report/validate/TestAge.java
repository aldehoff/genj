/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import java.util.List;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
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
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  /*package*/ void test(Property prop, TagPath path, List issues) {
    
    // assuming date
    PropertyDate date = (PropertyDate)prop;
    if (!date.isValid())
      return;

    // check entity
    Entity entity = prop.getEntity();
    if (entity instanceof Indi)
      test(path, date, (Indi)entity, issues);
    if (entity instanceof Fam) {
      test(path, date, ((Fam)entity).getHusband(), issues);
      test(path, date, ((Fam)entity).getWife   (), issues);
    }

    // done      
  }
  
  /**
   * test an individual's birth against the date
   */
  private void test(TagPath path, PropertyDate date, Indi indi, List issues) {
    
    // no indi?
    if (indi==null)
      return;

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
  private String getError(Indi indi, TagPath path) {
    
    // prepare it
    return 
        "Age of "+indi+" at "
      + Gedcom.getName(path.get(path.length()-2))
      + (comparison==LESS ? " less " : " greater ") 
      + years + " years";
  }
  
} //TestAge