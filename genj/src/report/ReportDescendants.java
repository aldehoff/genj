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
import genj.util.WordBuffer;

/**
 * GenJ - ReportDescendants
 */
public class ReportDescendants extends Report {
    
    public boolean reportPlaceOfBirth = true;
    public boolean reportDateOfBirth = true;
    public boolean reportPlaceOfDeath = true;
    public boolean reportDateOfDeath = true;
    public boolean reportPlaceOfMarriage = true;
    public boolean reportDateOfMarriage = true;
    
    /**
     * Main for argument individual
     */
    public void start(Indi indi) {
      
      String formatBirt = OPTIONS.getBirthSymbol()+(reportDateOfBirth?"{ $D}":"")+(reportPlaceOfBirth?"{ $p}":"");
      String formatDeat = OPTIONS.getDeathSymbol()+(reportDateOfDeath?"{ $D}":"")+(reportPlaceOfDeath?"{ $p}":"");
      String formatMarr = OPTIONS.getMarriageSymbol()+(reportDateOfMarriage?"{ $D}":"")+(reportPlaceOfMarriage?"{ $p}":"");
      
      // iterate into individual and all its descendants
      iterate(indi, 1, formatBirt, formatDeat, formatMarr);
        
      // Done
    }
    
    /**
     * Iterates over descendants
     */
    private void iterate(Indi indi, int level, String formatBirt, String formatDeat, String formatMarr) {
        
        // Here comes the individual
        println(getIndent(level) + level + " "+format(indi, formatBirt, formatDeat));
        
        // And we loop through its families
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (int f=0;f<fams.length;f++) {
            
            // .. here's the fam and spouse
            Fam fam = fams[f];
            Indi spouse= fam.getOtherSpouse(indi);
            
            // .. a line for the spouse
            
            // j'ajoute                 
            println(getIndent(level) + "  "+ format(spouse, formatBirt, formatDeat) + " " + fam.format("MARR", formatMarr)); 
            
            // .. and all the kids
            Indi[] children = fam.getChildren();
            for (int c = 0; c < children.length; c++) {
                
                // do the recursive step
                iterate(children[c], level+1, formatBirt, formatDeat, formatMarr);
                
                // .. next child
            }
            
            // .. next family
        }
    }
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi, String formatBirt, String formatDeat) {

      // Might be null
      if (indi==null) 
          return "?";
      
      WordBuffer result = new WordBuffer();
      result.append(indi.getId()+" "+indi.getName());
      result.append(indi.format("BIRT", formatBirt));
      result.append(indi.format("DEAT", formatDeat));
        
      return result.toString();
    }
    
} //ReportDescendants