/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.fo;

import java.util.regex.Matcher;

import junit.framework.TestCase;

/**
 * Test Case for testing our formatted Document
 */
public class DocumentTest extends TestCase {

  public void testAttributeRegEx() {

    // build testcase - key/value pairs
    String[] testcase = {
       "foo", "bar",
       "bar", " foo  ",
       "a", "rgb(255,0,128)",
       " b", "#ff00ff",
       "c", "rgb(0,0,128)",
       "bar-tst", " foo  ",
       "x:z", " foo  ",
    };

    // assemble into parameter string
    StringBuffer buf = new StringBuffer();
    for (int i=0;i<testcase.length/2;i+=2) {
      buf.append(testcase[i+0]).append("=").append(testcase[i+1]).append(",");
    }
    
    // parse with Document attribute regular expression
    Matcher m = Document.REGEX_ATTR.matcher(buf.toString());
    for (int i=0;i<testcase.length/2;i+=2) {
      assertTrue(m.find());
      assertEquals(testcase[i+0].trim(), m.group(1).trim());
      assertEquals(testcase[i+1].trim(), m.group(2).trim());
    }

    // done
  }
  
}
