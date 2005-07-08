/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.io.GedcomReader;
import genj.io.GedcomWriter;
import genj.util.Origin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Testing Gedcom read/write diff
 */
public class GedcomReadWriteTest extends TestCase {
  
  /**
   * Read a file / write it / compare
   */
  public void testReadWrite() throws IOException, GedcomException {
    
    File original = new File("./gedcom/example.ged");
    File temp = File.createTempFile("test", ".ged");
    
    // read it
    Gedcom ged = new GedcomReader(Origin.create(original.toURL())).read();
    
    // write it
    FileOutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // compare line by line
    BufferedReader left = new BufferedReader(new InputStreamReader(new FileInputStream(original)));
    BufferedReader right = new BufferedReader(new InputStreamReader(new FileInputStream(temp)));
    
    Pattern ignore = Pattern.compile("2 VERS|1 DATE|2 TIME|1 FILE");
    
    while (true) {
      
      String 
        lineLeft = left.readLine(),
        lineRight = right.readLine();

      // done?
      if (lineLeft==null&&lineRight==null)
        break;
      
      // not critical?
      Matcher match = ignore.matcher(lineLeft);
      if (match.find()&&match.start()==0) continue;
      
      // assert equal
      assertEquals(lineLeft, lineRight);
    }
    
    left.close();
    right.close();
    
    // done
  }
  
  
} //GedcomIDTest
