/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.report.PropertyList;
import genj.report.Report;

/**
 * Compute the common ancestor of two individuals
 *
 */
public class ReportCommonAncestor extends Report {
  
  /**
   * we're not using the console
   */
  public boolean usesStandardOut() {
    return false;
  }
  
  /**
   * we operate on individuals
   */
  public String accepts(Object context) {
    return context instanceof Indi ? getName() : null;
  }

  /**
   * our main method
   */
  public void start(Object context) {
    
    // assuming it's an individual
    Indi indi  = (Indi)context;
    
    // ask for the other
    Indi other = (Indi)getEntityFromUser(i18n("select"), indi.getGedcom(), Gedcom.INDI);
    if (other==null)
      return;
    
    // Recurse into indi
    Indi ancestor = getCommonAncestor(indi, other);

    // nothing to show?
    if (ancestor==null) {
      getOptionFromUser(i18n("nocommon"), Report.OPTION_OK);
      return;
    }
    
    // show the result
    PropertyList list = new PropertyList(indi.getGedcom());
    list.add(i18n("result.first", indi), indi);
    list.add(i18n("result.second", other), other);
    list.add(i18n("result.ancestor", ancestor), ancestor);
    
    showPropertiesToUser(getName(), list);
    
  }
  
  private Indi getCommonAncestor(Indi indi, Indi other) {
    // check father and mother of indi
    Indi father = indi.getBiologicalFather();
    if (father!=null) {
      if (father.isAncestorOf(other))
        return father;
      Indi ancestor = getCommonAncestor(father, other);
      if (ancestor!=null) 
        return ancestor;
    }
    Indi mother = indi.getBiologicalMother();
    if (mother!=null) {
      if (mother.isAncestorOf(other))
        return mother;
      Indi ancestor = getCommonAncestor(mother, other);
      if (ancestor!=null) 
        return ancestor;
    }
    // none found
    return null;
  }

}
