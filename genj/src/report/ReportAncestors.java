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
 */
public class ReportAncestors extends Report {

  /**
   * Returns the version of this script
   */
  public String getVersion() {
    // a call to i18n will lookup a string with given key in ReportAncestors.properties
    return i18n("version");
  }
  
  /**
   * Returns the name of this report - should be localized.
   */
  public String getName() {
    // a call to i18n will lookup a string with given key in ReportAncestors.properties
    return i18n("name");
  }  

  /**
   * Some information about this report
   * @return Information as String
   */
  public String getInfo() {
    // a call to i18n will lookup a string with given key in ReportAncestors.properties
    return i18n("info");
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
    // we accepts Gedom AND Individuals 
    return context instanceof Indi || context instanceof Gedcom;  
  }
  
  /**
   * Entry point into this report - by default reports are only run on a
   * context of type Gedcom but since we've overriden accepts we're
   * ready for Gedcom AND Indi
   */
  public void start(Object context) {
  
    // need Indi from context
    Indi indi;
    if (context instanceof Indi) {

      // either already there
      indi = (Indi)context;

    } else {

      // otherwise assume gedcom and let user choose one
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

