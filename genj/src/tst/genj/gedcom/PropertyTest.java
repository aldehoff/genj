/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import java.util.Arrays;
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
    
    // reverse order
    Property[] props = indi.getProperties();
    for (int i=0;i<props.length;i++) {
      Property p = props[i];
      props[i] = props[props.length-1-i];
      props[props.length-1-i] = p;
    }
    
    List shuffled = Arrays.asList(props);
    try {
      indi.setProperties(shuffled);
    } catch (IllegalArgumentException e) {
      fail("couldn't shuffle properties of "+indi);
    }

    assertEquals("shuffle didn't shuffle", shuffled, Arrays.asList(indi.getProperties()));
    
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
  
} //PropertyTest
