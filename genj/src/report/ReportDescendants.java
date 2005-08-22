/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.report.Report;
import genj.gedcom.TagPath;

/**
 * GenJ - ReportDescendants
 */
public class ReportDescendants extends Report {
    
    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
        
        iterate(indi, 1);
        
        // Done
    }
    
    private String trim(Object o) {
        if(o == null)
            return "";
        return o.toString();
    }
    
    /**
     * Iterates over descendants
     */
    private void iterate(Indi indi, int level) {
        
        // Here comes the individual
        println(getIndent(level, OPTIONS.getIndentPerLevel(), null)+level+" "+format(indi));
        
        // And we loop through its families
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (int f=0;f<fams.length;f++) {
            
            // .. here's the fam and spouse
            Fam fam = fams[f];
            Indi spouse= fam.getOtherSpouse(indi);
            
            // .. a line for the spouse
            println(getIndent(level, OPTIONS.getIndentPerLevel(), null) +"  + "+ format(spouse));
            
            // .. and all the kids
            Indi[] children = fam.getChildren();
            for (int c = 0; c < children.length; c++) {
                
                // do the recursive step
                iterate(children[c], level+1);
                
                // .. next child
            }
            
            // .. next family
        }
    }
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi) {
        
        // Might be null
        if (indi==null) {
            return "?";
        }
        
        // Assemble our substitution arguments
        
        String[] msgargs = {indi.getId(),
        indi.getName(),
        indi.getBirthAsString()+" "+trim(indi.getProperty(new TagPath("INDI:BIRT:PLAC"))),
        indi.getDeathAsString()+" "+trim(indi.getProperty(new TagPath("INDI:DEAT:PLAC")))};
        
        // Format the message with localization and return it
        return i18n("format",msgargs);
        
        
        // Could be a hyperlink, too
        //return "<a href=\"\">" + indi.getName() + "</a>" + b + d;
    }
    
} //ReportDescendants
