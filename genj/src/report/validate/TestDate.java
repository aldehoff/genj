/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;

import java.util.List;

/**
 * Test two dates
 */
/*package*/ class TestDate extends Test {
  
  /** comparisons */
  /*package*/ final static int 
    AFTER = 0,
    BEFORE = 1;

  /** path1 pointing to date to compare */
  private TagPath path1;    
    
  /** path2 to compare to */
  private TagPath path2;
  
  /** the mode AFTER, BEFORE, ... */
  private int comparison;

  /**
   * Constructor
   * @see TestDate#TestDate(String[], int, String) 
   */
  /*package*/ TestDate(String trigger, int comp, String path2) {
    this(new String[]{trigger}, null, comp, path2);
  }

  /**
   * Constructor
   * @see TestDate#TestDate(String[], int, String) 
   */
  /*package*/ TestDate(String trigger, String path1, int comp, String path2) {
    this(new String[]{trigger}, path1, comp, path2);
  }

  /**
   * Constructor
   * @param paths to trigger test
   * @param comp either AFTER or BEFORE
   * @param path2 path to check against (pointing to date)
   */
  /*package*/ TestDate(String[] triggers, int comp, String path2) {
    this(triggers, null, comp, path2);
  }
  
  /**
   * Constructor
   * @param paths to trigger test
   * @param path1 check against path2 (can be null)
   * @param comp either AFTER or BEFORE
   * @param path2 path to check against (pointing to date)
   */
  /*package*/ TestDate(String[] triggers, String path1, int comp, String path2) {
    // delegate to super
    super(triggers, path1==null?PropertyDate.class:Property.class);
    // remember
    comparison = comp;
    // keep other tag path
    this.path1 = path1!=null ? new TagPath(path1) : null;
    this.path2 = new TagPath(path2);
  }

  /**
   * test a prop (PropertyDate.class) at given path 
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {
    
    Entity entity = prop.getEntity();
    PropertyDate date1;
    
    // did we get a path1 or assuming prop instanceof date?
    if (path1!=null) {
      date1 = (PropertyDate)prop.getProperty(path1, Property.QUERY_VALID_TRUE|Property.QUERY_FOLLOW_LINK);
    } else {
      date1 = (PropertyDate)prop;
    }
    if (date1==null)
      return;
    
    // get date to check against - won't continue if
    // that's not a PropertyDate
    Property date2 = entity.getProperty(path2, Property.QUERY_VALID_TRUE|Property.QUERY_FOLLOW_LINK);
    if (!(date2 instanceof PropertyDate))
      return;
      
    // test it 
    if (isError(date1, (PropertyDate)date2)) 
      issues.add(new Issue(getError(date2.getEntity(), trigger), date1.getParent().getImage(false), prop));
    
    // done
  }
  
  /**
   * test for error in date1 vs. date2
   */
  private boolean isError(PropertyDate date1, PropertyDate date2) {
    switch (comparison) {
      case AFTER:
        return date1.compareTo(date2) > 0;
      case BEFORE:
        return date1.compareTo(date2) < 0;
    }
    return false;
  }

  /**
   * Calculate error messag from two paths
   */
  private String getError(Entity entity, TagPath trigger) {
    
    // prepare it
    return 
        Gedcom.getName(trigger.get(trigger.length()-(trigger.getLast().equals("DATE")?2:1)))
      + (comparison==BEFORE ? " before " : " after ") 
      + Gedcom.getName(path2.get(path2.length()-2))
      + (entity instanceof Indi? " of "+entity : "");
    
  }
  
} //TestEventTime