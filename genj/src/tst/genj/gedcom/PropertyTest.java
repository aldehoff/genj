/**
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 */
package genj.gedcom;

import genj.util.Origin;

import java.util.Arrays;

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
   * Test formatting 
   */
  public void testPrivacyPolicy() {     
    
    Options.getInstance().maskPrivate = "xxx";

    Indi indi = createIndi();
    
    Property birt = indi.addProperty("BIRT", "");
    Property date = birt.addProperty("DATE", "25 May 1970");
    Property plac = birt.addProperty("PLAC", "Rendsburg");
    
    // normal: have date and place all public -> get all
    assertEquals("born 25 May 1970, Rendsburg", birt.format("born{ $D}{, $P}", PrivacyPolicy.PUBLIC));
    
    // normal: have all with non applying policy -> get all back
    assertEquals("born 25 May 1970, Rendsburg", birt.format("born{ $D}{, $P}", new PrivacyPolicy(false, 10, "_SECRET")));
    
    // case of privacy: the  info is there but event is too recent -> getting masks back
    assertEquals("born xxx, xxx", birt.format("born{ $D}{, $P}", new PrivacyPolicy(false, Integer.MAX_VALUE, "_SECRET")));

    // special case: making date secret -> date is masked
    date.addProperty("_SECRET", "");
    assertEquals("born xxx Rendsburg", birt.format("born{ $D}{ $P}", new PrivacyPolicy(false, 0, "_SECRET")));

    // broader case: the event is marked private -> with public policy all's there with sensitive policy everything collapses
    birt.addProperty("_SECRET", "");
    assertEquals("born 25 May 1970, Rendsburg", birt.format("born{ $D}{, $P}", PrivacyPolicy.PUBLIC));
    assertEquals("born xxx, xxx", birt.format("born{ $D}{, $P}", new PrivacyPolicy(false, 0, "_SECRET")));
    
    // ... note how only one mask is shown here since there's no prefix in {$P}
    assertEquals("born xxx", birt.format("born{ $D}{ $P}", new PrivacyPolicy(false, 0, "_SECRET")));

    // exception to the rule: the person is deceased -> everything is public IF infoOfDeceasedIsPublic
    indi.addProperty("DEAT", "").addProperty("DATE", "(im Hohen Alter)");
    assertEquals("born xxx in xxx", birt.format("born{ $D}{ in $P}", new PrivacyPolicy(false, Integer.MAX_VALUE, null)));
    assertEquals("born xxx", birt.format("born{ $D}{ $P}", new PrivacyPolicy(false, Integer.MAX_VALUE, null)));
    assertEquals("born 25 May 1970, Rendsburg", birt.format("born{ $D}{, $P}", new PrivacyPolicy(true, Integer.MAX_VALUE, null)));

    // all good ;)
  }
  
  /**
   * Test formatting 
   */
  public void testFormatting() {     
    
    Indi indi = createIndi();
    
    assertFormatted(indi, "BIRT", "", "25 May 1970", "Rendsburg, SH", "geboren{ am $D}{ in $P}", "geboren am 25 May 1970 in Rendsburg, SH");
    assertFormatted(indi, "BIRT", "", null                 , "Rendsburg, SH", "geboren{ am $D}{ in $P}", "geboren in Rendsburg, SH");
    assertFormatted(indi, "BIRT", "", null                 , "Rendsburg, SH", "geboren{ am $D}{ in $p}", "geboren in Rendsburg");
    assertFormatted(indi, "BIRT", "", null                 , null                    , "geboren{ am $D}{ in $p}", "");
    assertFormatted(indi, "BIRT", "", "25 May 1970", ""                        , "born {$y}{ in $P}", "born 1970");
    assertFormatted(indi, "BIRT", "", ""                     , ""                        , "born {$y}{ in $P}", "");
    assertFormatted(indi, "OCCU", "Pilot", null        , null                    , "{$V}{ in $p}", "Pilot");
    assertFormatted(indi, "OCCU", "Pilot", null        , "Ottawa"            , "{$V}{ in $p}", "Pilot in Ottawa");
    assertFormatted(indi, "OCCU", ""      , null         , "Ottawa"            , "{$V }{in $p}", "in Ottawa");
    assertFormatted(indi, "OCCU", ""      , null         , "Ottawa"            , "Occupation: {$V}", "");
    assertFormatted(indi, "IMMI", ""      , null           , "Vancouver"       , "Immigration{ in $p (landed)}{ on $D}", "Immigration in Vancouver (landed)");
    
    assertFormatted(indi, "BIRT", "", "25 May 1970", "Rendsburg, SH", "{$T}{ $D}{ in $p}", "Birth 25 May 1970 in Rendsburg");
    assertFormatted(indi, "BIRT", "", ""                     , "Rendsburg, SH", "{$t}{ $D}{ $P}"    , "BIRT Rendsburg, SH");
    assertFormatted(indi, "BIRT", "", ""                     , ""                        , "{$T}{ $D}{ in $p}", "");
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
