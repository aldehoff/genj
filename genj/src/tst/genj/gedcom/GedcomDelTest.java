/* 
 * 
 * JUNIT TESTCASE - DONT PACKAGE FOR DISTRIBUTION
 * GedcomDelTest.java
 *  $Header$
 */

package genj.gedcom;

import genj.io.GedcomIOException;
import genj.io.GedcomWriter;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.TestCase;

/**
 * Test for inconsistencies in the Entity.delete() method.
 * This was originally created to track the assicated 
 * <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=1489891&group_id=46817&atid=447494">SF bug</a>
 * @author Danny Sadinoff
 *
 */
public class GedcomDelTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(GedcomDelTest.class);
    }

    private Fam createTestFamily() throws GedcomException
    {
        Gedcom gedcom = new Gedcom();
        Indi husband = (Indi) gedcom.createEntity(Gedcom.INDI, "Ihusband");
        Indi wife = (Indi) gedcom.createEntity(Gedcom.INDI, "Iwife");
        Indi child = (Indi) gedcom.createEntity(Gedcom.INDI, "Ikid");
        Fam family = (Fam) gedcom.createEntity(Gedcom.FAM,"F1");
        family.setHusband(husband);
        family.setWife(wife);
        family.addChild(child);
        return family;
    }
    
    /*
     * Test method for 'genj.gedcom.Gedcom.deleteEntity(Entity)'
     */
    public void testDeleteEntity()  throws Exception{
        testDeleteParent(createTestFamily() );
        testDeleteKid(createTestFamily());
        testDeleteFamily(createTestFamily());
    }
    
    /**
     * A stream to nowhere
     * @author dsadinoff
     *
     */
    private static class SinkOutputStream extends OutputStream{
        public void write(int arg0) throws IOException {
            //NOP
        }
    }
    
    /**
     * Throws some sort of exception if the argument isn't a valid gedcom.
     * @param gedcom
     * @throws GedcomIOException
     */
    private static void validate(Gedcom gedcom) throws GedcomIOException
    {
        OutputStream fos = new SinkOutputStream();
        
        GedcomWriter writer = new GedcomWriter(gedcom,"test",null,fos);                    
        writer.write(); //closes fos
    }

    private void testDeleteFamily(Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
        gedcom.deleteEntity(fam);
        validate(gedcom);
    }

    private void testDeleteParent(Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
        gedcom.deleteEntity(fam.getHusband());
        validate(gedcom);
    }

    private void testDeleteKid( Fam fam) throws Exception {
        Gedcom gedcom = fam.getGedcom();
        gedcom.deleteEntity(fam.getChild(0));
        validate(gedcom);
    }

}