/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import java.util.List;

import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;

/**
 * Test whether files that are pointed to actually exist
 */
public class TestFile extends Test {

  /**
   * Constructor
   */
  public TestFile() {
    super((String[])null, PropertyFile.class);
  }

  /**
   * Do the test 
   * @see validate.Test#test(genj.gedcom.Property, genj.gedcom.TagPath, java.util.List)
   */
  void test(Property prop, TagPath path, List issues) {
    
    // assuming PropertyFile
    PropertyFile file = (PropertyFile)prop;
    
    // check it
    if (file.getFile()==null) 
      issues.add(new Issue("File doesn't exist", prop.getImage(false), prop));

  }

} //TestFiles