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
 * GenJ -  ReportAncestors
 */
public class ReportAncestors extends Report {
    
    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
      
        // Display the descendants
        parent(indi,1);
        
        // Done
    }
    
    private String trim(Object o) {
        if(o == null)
            return "";
        return o.toString();
    }
    
    /**
     * parent - prints information about one parent and then recurses
     */
    private void parent(Indi indi, int level) {
        
        // Here comes the individual
        println(getIndent(level, OPTIONS.getIndentPerLevel(), null)+level+" "+format(indi));
        
        Fam famc = indi.getFamilyWhereBiologicalChild();
        
        if (famc==null)
            return;
        
        if (famc.getWife()!=null)
            parent(famc.getWife(), level+1);
        
        if (famc.getHusband()!=null)
            parent(famc.getHusband(), level+1);
        
    }
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi) {
        
        // Might be null
        if (indi==null)
            return "?";
        
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
    
} //ReportAncestors

