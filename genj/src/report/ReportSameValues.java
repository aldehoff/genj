/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyName;
import genj.report.Report;

/**
 * A report that uses PropertyChoiceValue's referencing ability. For
 * a given PropertyChoiceValue's value it shows those properties
 * with the same value (e.g. everyone living in Rendsburg)
 * 
 * 20030529: NAME*, PLAC, CITY, POST, CTRY, FORM, OCCU, RELA
 * 
 * @author nils
 */
public class ReportSameValues extends Report {

  /**
   * We only accept instances of PropertyChoice and PropertyName
   * 
   * @see genj.report.Report#accepts(java.lang.Object)
   */
  public String accepts(Object context) {
    
    // accepting all PropertyChoices and PropertyNames
    String val = null;
    if (context instanceof PropertyChoiceValue)
      val = ((PropertyChoiceValue)context).getValue();
    if (context instanceof PropertyName)
      val = ((PropertyName)context).getLastName();
      
    // o.k.?
    if (val==null||val.length()==0) 
      return null;
    
    // return a meaningfull text for that context
    return i18n("xname", val );
  }

  /**
   * @see genj.report.Report#getAuthor()
   */
  public String getAuthor() {
    return "Nils Meier";
  }

  /**
   * @see genj.report.Report#getInfo()
   */
  public String getInfo() {
    return i18n("info");
  }

  /**
   * @see genj.report.Report#getName()
   */
  public String getName() {
    return i18n("name");
  }

  /**
   * @see genj.report.Report#getVersion()
   */
  public String getVersion() {
    return "0.1";
  }
  
  /**
   * We don't use STDOUT
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * @see genj.report.Report#start(java.lang.Object)
   */
  public void start(Object context) {
    
    // get properties that have the same choice
    Gedcom gedcom = null;
    Property[] sameProps = null;
    String val = null;
    
    if (context instanceof PropertyChoiceValue) {
      PropertyChoiceValue prop = (PropertyChoiceValue)context;
      val = prop.getValue();
      sameProps = prop.getSameChoices();
      gedcom = prop.getGedcom();
    }
    if (context instanceof PropertyName) {
      PropertyName name = (PropertyName)context;
      val = name.getLastName();
      sameProps = name.getSameLastNames();
      gedcom = name.getGedcom();
    }

    if (val==null||val.length()==0)
      return;
    
    // collect parents of sameProps
//    for (int i=0;i<sameProps.length;i++) {
//      Property parent = sameProps[i].getParent(); 
//      if (parent!=null) sameProps[i] = parent;  
//    }
    
    // show 'em
    showToUser( i18n("xname",val), gedcom, sameProps);
    
    // done
  }

} //ReportSameValues