/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;
import genj.gedcom.TagPath;

/**
 * A test for validation
 * @author nmeier
 */
/*package*/ abstract class Test {
  
  private TagPath[] paths;
  
  private Class type;

  /**
   * Constructor
   */
  /*package*/ Test(String setPath, Class setType) {
    this(new String[]{setPath}, setType);
  }

  /**
   * Constructor
   */
  /*package*/ Test(String[] setPaths, Class setType) {
    if (setPaths!=null) {
      paths = new TagPath[setPaths.length];
      for (int i=0;i<paths.length;i++)
        paths[i] = new TagPath(setPaths[i]);
    }
    type = setType;
  }
  
  /**
   * Test whether test applies or not
   */
  /*package*/ boolean applies(Property prop, TagPath path) {
    // gotta match a path
    outer: while (paths!=null) {
      for (int j=0;j<paths.length;j++) {
        if (paths[j].equals(path)) break outer;
      }
      return false;
    }
    // and type
    return type.isAssignableFrom(prop.getClass());
  }
  
  /**
   * Perform Test 
   */
  /*package*/ abstract Issue test(Property prop, TagPath path);
   

} //Test