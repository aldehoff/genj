/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.Report;

/**
 * GenJ - Report
 * @author Nils Meier nils@meiers.net
 * @version 0.1
 */
public class ReportDescendants extends Report {

  /** this report's version */
  public static final String VERSION = "0.1";
  
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
    return "Descendants";
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints out all descendants of an individual";
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "Nils Meier <nils@meiers.net>";
  }

  /**
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public boolean accepts(Object context) {
    return context instanceof Indi || context instanceof Gedcom;  
  }
  
  /**
   * This method actually starts this report
   */
  public void start(Object context) {

    Indi indi;
    
    // check context
    if (context instanceof Indi) {
      indi = (Indi)context;
    } else {
      // expecting gedcom
      Gedcom gedcom = (Gedcom)context;
      
      indi = (Indi)getEntityFromUser("Ancestor", gedcom, Gedcom.INDIVIDUALS, "INDI:NAME");
      if (indi==null) 
        return;
    }
    
    // Display the descendants
    iterate(indi, 1);
    
    // Done
  }
  
  /**
   * Iterates over descendants
   */
  private void iterate(Indi indi, int level) {
    
    // Here comes the individual
    println(getIndent(level)+level+" "+format(indi));
    
    // And we loop through its families
    int fcount = indi.getNoOfFams();
    for (int f=0;f<fcount;f++) {
      
      // .. here's the fam and spouse
      Fam fam = indi.getFam(f);
      Indi spouse= fam.getOtherSpouse(indi);
      
      // .. a line for the spouse
      println(getIndent(level) +"  + "+ format(spouse));
      
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
    
    // name
    String n = indi.getName();
    
    // birth?
    String b = " b: " + indi.getBirthAsString();
    
    // death?
    String d = " d: " + indi.getDeathAsString();
    
    // here's the result 
    return n + b + d;
    
    // Could be a hyperlink, too
    //return "<a href=\"\">" + indi.getName() + "</a>" + b + d;
  }
  
  /**
   * Helper that indents to given level
   */
  private String getIndent(int level) {
    StringBuffer buffer = new StringBuffer(256);
    while (--level>0) {
      buffer.append("    ");
    }
    return buffer.toString();
  }

} //ReportDescendants
