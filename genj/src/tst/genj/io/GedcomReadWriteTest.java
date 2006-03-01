/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.io;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.util.Origin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Testing Gedcom read/write diff
 */
public class GedcomReadWriteTest extends TestCase {
  
  /**
   * test read/write with encrypted content
   */
  public void testEncryptDecrypt() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // read it
    File original = new File("./gedcom/example.ged");
    Gedcom ged = new GedcomReader(Origin.create(original.toURL())).read();
    
    // set everything to private
    ged.setPassword("password");
    for (Iterator ents = ged.getEntities().iterator(); ents.hasNext(); ) {
      Entity ent = (Entity)ents.next();
      ent.setPrivate(true, true);
    }
    
    // write it encrypted
    File temp = File.createTempFile("test", ".ged");
    FileOutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // read again
    GedcomReader reader = new GedcomReader(Origin.create(temp.toURL()));
    reader.setPassword(Gedcom.PASSWORD_UNKNOWN);
    ged = reader.read();
    
    // write it encrypted a second time
    temp = File.createTempFile("test", ".ged");
    out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // read again - this time with password
    reader = new GedcomReader(Origin.create(temp.toURL()));
    reader.setPassword("password");
    ged = reader.read();
    
    // write it deencrypted (without password) 
    temp = File.createTempFile("test", ".ged");
    out = new FileOutputStream(temp);
    ged.setPassword("");
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // compare original to last temp now
    assertEquals(original, temp);
    
    // done
    
  }
  
  /**
   * Read a stress file
   */
  public void testStressFile() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // try to read file
    Gedcom ged = new GedcomReader(getClass().getResourceAsStream("stress.ged")).read();
    
    assertEquals("should be INDI SUBM UNKNOWN FOO", 4, ged.getEntities().size());
    
  }
  
  /**
   * Read a file / write it / compare
   */
  public void testReadWrite() throws IOException, GedcomException {
    
    // we don't need log output for this
    Logger.getLogger("").setLevel(Level.OFF);

    // read/write file
    File original = new File("./gedcom/example.ged");
    File temp = File.createTempFile("test", ".ged");
    
    // read it
    Gedcom ged = new GedcomReader(Origin.create(original.toURL())).read();
    
    // write it
    FileOutputStream out = new FileOutputStream(temp);
    new GedcomWriter(ged, temp.getName(), null, out).write();
    out.close();
    
    // compare line by line
    assertEquals(original, temp);
    
  }
  
  private void assertEquals(File file1, File file2) throws IOException {
    
    BufferedReader left = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
    BufferedReader right = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));
    
    Pattern ignore = Pattern.compile("2 VERS|1 DATE|2 TIME|1 FILE");
    Pattern commaspace = Pattern.compile(", ");
    String comma = ",";
    
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
      
      // assume "," equals ", "
      lineLeft = commaspace.matcher(lineLeft).replaceAll(comma);
      lineRight = commaspace.matcher(lineRight).replaceAll(comma);
      
      // assert equal
      assertEquals(lineLeft, lineRight);
    }
    
    left.close();
    right.close();
    
    // done
  }
  
  
} //GedcomIDTest
