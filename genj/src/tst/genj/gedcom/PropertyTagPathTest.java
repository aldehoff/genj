/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Origin;
import junit.framework.TestCase;

/**
 * Test Property Base class with TagPaths
 */
public class PropertyTagPathTest extends TestCase {

  private Gedcom gedcom;
  
  private Indi indi;
  
  /**
   * Prepare a fake indi
   */
  protected void setUp() throws Exception {

    // create gedcom
    gedcom = new Gedcom(Origin.create("file://foo.ged"));

    // create individual
    indi = (Indi)gedcom.createEntity("INDI");
    
    // .. with default sub-properties
    indi.addDefaultProperties();

    // done
  }
  
  /**
   * Test path matches that include selectors
   */
  public void testGetPropertyByPath() {
    
    assertProperty(indi, "INDI"                          , indi);
    assertProperty(indi, "INDI:BIRT:DATE:..:..:BIRT:DATE", get(indi, new String[]{"BIRT", "DATE"}));
    
  }

  /**
   * helper for finding sub-property by tags
   */
  private Property get(Property root, String[] tags) {
    for (int i=0;i<tags.length;i++)
      root = root.getProperty(tags[i], false);
    return root;
  }
  
  /**
   * helper for checking result of root.getProperty(path)
   */
  private Property assertProperty(Property root, String path, Property prop) {
    Property result = root.getProperty(new TagPath(path));
    assertSame(result, prop);
    return result;
  }
  
} //PropertyTest
