/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package dev.src.report;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.ReferenceSet;
import java.util.Iterator;

/**
 * GenJ - ReportSoundex
 * based on
 * a) ReportAges
 * b) http://www.java-tutor.com/java/devel/SoundexTest.java
 *    Thanks to Christian Ullenboom (C.Ullenboom@Java-tutor.com)
 *    for the permission to use the above mentioned code.
 *
 * @author Carsten Müssig <carsten.muessig@gmx.net>
 * @version 1.00
 */

public class ReportSoundex extends Report {
    
    // Mapping
    private static final char map[] =
    // ABCDEFGHIJKLMNOPQRSTUVWXYZ
    "01230120022455012623010202".toCharArray();
    
    /** this report's version */
    public static final String VERSION = "1.00";
    
    /**
     * Returns the version of this script
     */
    public String getVersion() {
        return VERSION;
    }
    
    /**
     * Returns the name of this report - should be localized.
     */
    public String getName() {
        return i18n("name");
    }
    
    /**
     * Some information about this report
     * @return Information as String
     */
    public String getInfo() {
        return i18n("info");
    }
    
    /**
     * Author
     */
    public String getAuthor() {
        return "Carsten M\u00FCssig <carsten.muessig@gmx.net>";
    }
    
    /**
     * @see genj.report.Report#accepts(java.lang.Object)
     */
    public String accepts(Object context) {
        // we accept GEDCOM or Individuals
        return context instanceof Indi || context instanceof Gedcom ? getName() : null;
    }
    
    /**
     * This method actually starts this report
     */
    public void start(Object context) {
        Indi indi;
        String lastName = null, soundex = null;
        
        // If we were passed a person to start at, use that
        if (context instanceof Indi) {
            indi = (Indi)context;
            lastName = indi.getLastName();
            soundex = buildSoundex(lastName);
            printSoundex(lastName, soundex);
        }
        else {
            // Otherwise, ask the user select the root of the tree for analysis
            Gedcom gedcom=(Gedcom)context;
            Entity[] indis = gedcom.getEntities(gedcom.INDI, "");
            ReferenceSet lastNames = new ReferenceSet();
            lastName = null;
            for(int i=0;i<indis.length;i++) {
                Indi in = (Indi)indis[i];
                lastName = in.getLastName();
                lastNames.add(lastName, buildSoundex(lastName));
            }
            Iterator it = lastNames.getKeys(true).iterator();
            while(it.hasNext()) {
                lastName = (String)it.next();
                soundex = lastNames.getReferences(lastName).toArray()[0].toString();
                printSoundex(lastName, soundex);
            }
        }
    }
    
    private void printSoundex(String lastName, String soundex) {
        if((lastName!=null)&&(lastName.length()>0))
            println(i18n("lastName")+": "+lastName+", "+i18n("soundex")+": "+soundex);
    }
    
    /** Build soundex index form incoming string */
    private String buildSoundex(String lastName) {
        int len = lastName.length();
        
        if ( len == 0 )
            return "";
        
        char c, last, mapped;
        char out[] = { '0', '0', '0', '0' };
        int  incount = 1, outcount = 1;
        
        char out0 = out[0] = Character.toUpperCase( lastName.charAt( 0 ) );
        
        last = (out0<'A')||(out0>'Z') ? '0' : map[ out0 - 'A' ];
        
        while ( ( incount < len ) && ( outcount < 4 ) ) {
            c = Character.toUpperCase( lastName.charAt( incount++ ) );
            
            mapped = ((c<'A')||(c>'Z')) ? '0' : map[ c - 'A' ];
            
            if ( ( mapped != '0' ) && ( mapped != last ) )
                out[outcount++] = mapped;
            
            if ( ( c != 'W') && ( c != 'H') )
                last = mapped;
        }
        return( new String(out) );
    }
}