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
 * Test for property validity
 * @author nmeier
 */
/*package*/ class TestValid extends Test {

  /**
   * Constructor
   */
  /*package*/ TestValid() {
    super((String[])null, Property.class);
  }
  
  /**
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath)
   */
  /*package*/ Issue test(Property prop, TagPath path) {
    // no need if valid
    if (prop.isValid())
      return null;
    // got an issue with that
    return new Issue(path.toString()+" is not valid", prop.getImage(true), prop);
  }

} //TestValid