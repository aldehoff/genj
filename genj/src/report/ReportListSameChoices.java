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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A report that shows entities with properties that contain
 * the same choice as the criteria (e.g. everyone living in Rendsburg)
 * @author nmeier
 */
public class ReportListSameChoices extends Report {

  /**
   * We only accept instances of PropertyChoice
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
    if (val==null) 
      return null;
    
    // translate
    return getName() + " ("+Gedcom.getName( ((Property)context).getTag() )+"="+val+")";
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
   * @see genj.report.Report#start(java.lang.Object)
   */
  public void start(Object context) {
    
    // get properties that have the same choice
    Property[] sameProps = null;
    if (context instanceof PropertyChoiceValue)
      sameProps = ((PropertyChoiceValue)context).getSameChoices();
    if (context instanceof PropertyName)
      sameProps = ((PropertyName)context).getSameLastNames();
    if (sameProps==null)
      return;
    
    // collect entities
    Set entities = new HashSet();
    for (int i=0; i<sameProps.length; i++) entities.add(sameProps[i].getEntity());
    
    // show 'em
    for (Iterator it=entities.iterator(); it.hasNext();)
      println(it.next());
    
    // done
  }

} //ReportValueReferences