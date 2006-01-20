/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.fo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

/**
 * Test Case for testing our formatted output feature
 */
public class FormatterTest extends TestCase {

  public void testFormatters() throws IOException, TransformerException {
    
    // create a document
    Document doc = new Document("Some Title");
    
    // transform it
    Formatter[] fs = Formatter.getFormatters();
    
    for (int i=0;i<fs.length;i++) {
      Formatter f = fs[i];
      try {
        f.format(doc, new ByteArrayOutputStream(1024));
      } catch (Throwable t) {
        fail("Formatter "+f.getClass().getName()+" failed with "+t.getMessage());
      }
    }

    // done
  }
  
  
}
