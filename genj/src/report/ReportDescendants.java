/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Entity;
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
      
      // iterate into individual and all its descendants
      iterate(indi, 1);
        
      // Done
    }
    
    /**
     * Iterates over descendants
     */
    private void iterate(Indi indi, int level) {
        
        // Here comes the individual
        println(getIndent(level) + level + " "+format(indi));
        
        // And we loop through its families
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (int f=0;f<fams.length;f++) {
            
            // .. here's the fam and spouse
            Fam fam = fams[f];
            Indi spouse= fam.getOtherSpouse(indi);
            
            // .. a line for the spouse
            
            // j'ajoute                 
            println(getIndent(level) + "  "+ format(spouse) + " " + formatDateAndPlace(OPTIONS.getMarriageSymbol(), fam, "MARR", reportDateOfMarriage, reportPlaceOfMarriage)); 
            
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
     * format date and place
     */
    private String formatDateAndPlace(String symbol, Entity entity, String tag, boolean isDate, boolean isPlace) {
      return entity==null ? "" : entity.format(tag, symbol+(isDate?"{ $D}":"")+(isPlace?"{ $p}":""));
    }
    
    /**
     * resolves the information of one Indi
     */
    private String format(Indi indi) {

      // Might be null
      if (indi==null) 
          return "?";
      
      WordBuffer result = new WordBuffer();
      result.append(indi.getId()+" "+indi.getName());
      result.append(formatDateAndPlace(OPTIONS.getBirthSymbol(), indi, "BIRT", reportDateOfBirth, reportPlaceOfBirth));
      result.append(formatDateAndPlace(OPTIONS.getDeathSymbol(), indi, "DEAT", reportDateOfDeath, reportPlaceOfDeath));
        
      return result.toString();
    }
    
} //ReportDescendants