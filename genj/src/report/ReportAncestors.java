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
 * GenJ -  ReportAncestors
 * @version 0.1
 */
public class ReportAncestors extends Report {

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
    return "Ancestors";
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    return "This report prints out all ancestors of an individual";
  }

  /**
   * Author
   */
  public String getAuthor() {
    return "YON - Jan C. Hardenbergh";
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
      
      indi = (Indi)getEntityFromUser("Descendant", gedcom, Gedcom.INDIVIDUALS, "INDI:NAME");
      if (indi==null) 
        return;
      
    }
    
    // Display the descendants
    parent(indi, 1);
    
    // Done
  }
  
  /**
   * parent - prints information about one parent and then recurses
   */
  private void parent(Indi indi, int level) {

    // Here comes the individual
    println(getIndent(level)+level+" "+format(indi));
    
    Fam famc = indi.getFamc();

    if (famc==null) {
	//      println(getIndent(level) +"  + leaf node "+ format(indi));
      return;
    }

    if (famc.getWife()!=null) {
        parent(famc.getWife(), level+1);
    }
    if (famc.getHusband()!=null) {
        parent(famc.getHusband(), level+1);
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
  
} //ReportAncestors

