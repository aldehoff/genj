/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;

/**
 * @author nmeier
 */
/*package*/ class TestDate extends Test {
  
  /** comparisons */
  /*package*/ final static int 
    AFTER = 0,
    BEFORE = 1;
    
  /** the 'other' we test against */
  private TagPath path2;
  
  /** the mode AFTER, BEFORE, ... */
  private int comparison;

  /**
   * Constructor
   */
  /*package*/ TestDate(String paths, int comp, String path2) {
    this(new String[]{paths}, comp, path2);
  }
  
  /**
   * Constructor
   */
  /*package*/ TestDate(String[] paths, int comp, String path2) {
    // delegate to super
    super(paths, PropertyDate.class);
    // remember
    comparison = comp;
    // keep other tag path
    this.path2 = new TagPath(path2);
  }

  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath)
   */
  /*package*/ Issue test(Property prop, TagPath path) {
    
    // assuming date
    PropertyDate date1 = (PropertyDate)prop;

    // get other date
    PropertyDate date2 = (PropertyDate)prop.getEntity().getProperty(path2);
    if (date2==null)
      return null;
      
    // test it 
    if (isError(date1, date2)) 
      return new Issue(getError(path), date1.getImage(false), date1);
    
    // all good
    return null;
  }
  
  /**
   * test
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
  private String getError(TagPath path1) {
    
    // prepare it
    return 
        Gedcom.getName(path1.get(path1.length()-2))
      + (comparison==BEFORE ? " before " : " after ") 
      + Gedcom.getName(path2.get(path2.length()-2));
    
  }
  
} //TestEventTime