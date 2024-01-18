/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import genj.util.Origin;
import junit.framework.TestCase;

/**
 * Test all kinds of Property methods
 */
public class PropertyTest extends TestCase {

  private Gedcom gedcom;
  
  /**
   * Prepare a fake indi
   */
  protected void setUp() throws Exception {

    // create gedcom
    gedcom = new Gedcom(Origin.create("file://foo.ged"));

    // done
  }
  
  /**
   * Helper - create an individual
   */
  private Indi createIndi() {
    
    Indi indi = null;
    
    try {
	    // create individual
	    indi = (Indi)gedcom.createEntity("INDI");
	    // .. with default sub-properties
	    indi.addDefaultProperties();
    } catch (GedcomException e) {
      fail(e.getMessage());
    }
    
    // done
    return indi;
  }
  
  /**
   * Test the 'new' shuffling 
   */
  public void testShuffle() {     
    
    Indi indi = createIndi();
    List original = Arrays.asList(indi.getProperties());
    
    // shuffle properties of indi
    List shuffled = Arrays.asList(indi.getProperties());
    Collections.shuffle(shuffled);

    // commit it
    gedcom.startTransaction();
    try {
      indi.setProperties(shuffled);
    } catch (IllegalArgumentException e) {
      fail("couldn't shuffle properties of "+indi);
    }
    Transaction tx = gedcom.endTransaction();
    
    // check resulting properties
    Property[] after = indi.getProperties();
    for (int i=0;i<shuffled.size();i++) {
      assertSame("expected shuffled at "+i, shuffled.get(i), after[i]);
    }
    
    // check changes
    Change[] changes = tx.getChanges();
    
    assertEquals("expected 2 changes", 2, changes.length);
    assertEquals("expected Change.shuffle", changes[0], new Change.PropertyShuffle(indi, original));
    assertEquals("expected Change.add/CHAN", changes[1], new Change.PropertyAdd(indi, after.length-1, after[after.length-1]));
    
    // done
  }
  
  /**
   * Test adding properties
   */
  public void testAdd() {     

    // create a new indi
    Indi indi = createIndi();
    Property[] before = indi.getProperties();
    
    // add property
    PropertyName name = new PropertyName();
    int pos = 1;
    
    gedcom.startTransaction();
    indi.addProperty(name, pos);
    Transaction tx = gedcom.endTransaction();
    
    // check result
    Property[] after = indi.getProperties();
    
    // .. should have certain set of changes
    Change[] changes = tx.getChanges();
    assertEquals("wrong # of changes", 2, changes.length);
    assertEquals("expected change add/NAME", changes[0], new Change.PropertyAdd(indi, pos, name));
    assertEquals("expected change add/CHAN", changes[1], new Change.PropertyAdd(indi, after.length-1, after[after.length-1]));
    
    // .. we should have additional NAME and CHAN now
    assertEquals("wrong # of properties", before.length+2, after.length);
    assertEquals("expected NAME/"+pos, after[pos], name);
    assertEquals("expected CHAN/"+pos, after[after.length-1].getTag(), "CHAN");
    
    // undo it
    gedcom.undo();
    
    // check result
    after = indi.getProperties();
    
    // .. we should have the same set of properties now
    assertEquals("undo didn't restore add", Arrays.asList(after), Arrays.asList(before));
    
  }
  
  /**
   * Test dates
   */
  public void testFormatting() {     
    
    Indi indi = createIndi();
    
    assertFormatted(indi, "BIRT", "", "25 May 1970", "Rendsburg, SH", "geboren{ am $D}{ in $P}", "geboren am 25 May 1970 in Rendsburg, SH");
    assertFormatted(indi, "BIRT", "", null                 , "Rendsburg, SH", "geboren{ am $D}{ in $P}", "geboren in Rendsburg, SH");
    assertFormatted(indi, "BIRT", "", null                 , "Rendsburg, SH", "geboren{ am $D}{ in $p}", "geboren in Rendsburg");
    assertFormatted(indi, "BIRT", "", null                 , null                    , "geboren{ am $D}{ in $p}", "");
    assertFormatted(indi, "BIRT", "", "25 May 1970", ""                        , "born {$y}{ in $P}", "born 1970");
    assertFormatted(indi, "BIRT", "", ""                     , ""                        , "born {$y}{ in $P}", "");
    assertFormatted(indi, "OCCU", "Pilot", null        , null                    , "{$v}{ in $p}", "Pilot");
    assertFormatted(indi, "OCCU", "Pilot", null        , "Ottawa"            , "{$v}{ in $p}", "Pilot in Ottawa");
    
  }
  
  private void assertFormatted(Indi indi, String evenTag, String evenValue, String date, String place, String format, String result) {
    
    // prep event
    Property p = indi.addProperty(evenTag, evenValue);
    if (date!=null)
      p.addProperty("DATE", date);
    if (place!=null)
      p.addProperty("PLAC", place);
    
    // format it
    assertEquals(result, p.format(format));
    
  }
    
} //PropertyTest
