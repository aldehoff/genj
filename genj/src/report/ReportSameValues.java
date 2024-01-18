/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyChoiceValue;
import genj.gedcom.PropertyName;
import genj.report.PropertyList;
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
   * We only accept instances of PropertyChoice and PropertyName - since
   * we're returning something more fancy than the report name this is overridden.
   * Normally implementing a start method with a compatible one arg parameter
   * is sufficient
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
    return i18n("xname", new String[]{ ((Property)context).getPropertyName(), val } );
  }

  /**
   * We don't use STDOUT
   * @see genj.report.Report#usesStandardOut()
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Our entry point for choices
   */
  public void start(PropertyChoiceValue choice) {
    start(choice.getGedcom(), choice.getPropertyName(), choice.getSameChoices(), choice.getDisplayValue());
  }
  
  /**
   * Our entry point for names
   */
  public void start(PropertyName name) {
    start(name.getGedcom(), name.getPropertyName(), name.getSameLastNames(), name.getLastName());
  }
  
  /**
   * our main logic
   */
  public void start(Gedcom gedcom, String propName, Property[] sameProps, String val) {

    if (val==null||val.length()==0)
      return;
    
    // collect parents of sameProps
    PropertyList items = new PropertyList(gedcom);
    for (int i=0; i<sameProps.length; i++) {

      // "Birth, Meier, Nils (I001)"
      Property prop = sameProps[i];      
      Property parent = prop.getParent();
      
      String txt;
      if (parent==null||parent instanceof Entity)
        txt = prop.getEntity().toString();
      else
        txt = parent.getPropertyName() + " | " +prop.getEntity();

      // one item for each
    	items.add(txt, prop);
    }
    
    // sort 'em
    items.sort();
    
    // show 'em
    showPropertiesToUser( i18n("xname",new String[]{ propName, val}), items);
    
    // done
  }

} //ReportSameValues