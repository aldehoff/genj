/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.util;

import java.io.File;

import junit.framework.TestCase;

/**
 * Test origin functionality
 */
public class OriginTest extends TestCase {

  /**
   * test origin's way of calculating relative paths
   */
  public void testRelative() throws Throwable {
    
    String dir = new File("./gedcom").getCanonicalPath();
    Origin origin = Origin.create(new File(dir, "example.ged").toURL());

    assertEquals(null, origin.calcRelativeLocation("foo.jpg"));
    assertEquals("foo.jpg", origin.calcRelativeLocation(new File(dir, "foo.jpg").toString()));
    assertEquals("foo.jpg", origin.calcRelativeLocation(new File(dir, "../gedcom/foo.jpg").toString()));
    assertEquals("foo.jpg", origin.calcRelativeLocation(new File(dir, "./foo.jpg").toString()));
    assertEquals(null, origin.calcRelativeLocation("/foo.jpg"));
    
    // making sure that question marks don't break the computation check (canonical throws io)
    assertEquals("question marks are no good", origin.calcRelativeLocation(new File(dir, "question marks are no good").toString()));
    assertEquals(null, origin.calcRelativeLocation(new File(dir, "right?").toString()));
    assertEquals(null, origin.calcRelativeLocation("right?"));
    
    
  }
  
}
