/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A report for displaying relatives of a person
 */
public class ReportRelatives extends Report {
  
  /**
   * A relative
   */
  static class Relative {
    
    /** how to get to it */
    String key;
    String path;
    int sex;
    
    /** constructor */
    Relative(String key, String path) {
      this(key, path, PropertySex.UNKNOWN);
    }
    
    /** constructor */
    Relative(String key, String path, int sex) {
      this.key = key;
      this.path = path.trim();
      this.sex = sex;
    }
    
  } //Relative
  
  private final static Relative[] RELATIVES = {
    new Relative("grandfather", "father+father|mother+father"),
    new Relative("grandmother", "father+mother|mother+mother"),
    new Relative("father"     , "INDI:FAMC:*:..:HUSB:*:.."   ),
    new Relative("mother"     , "INDI:FAMC:*:..:WIFE:*:.."   ),
    new Relative("husband"    , "INDI:FAMS:*:..:HUSB:*:.."   ),
    new Relative("wife"       , "INDI:FAMS:*:..:WIFE:*:.."   ),
    new Relative("daughter"   , "INDI:FAMS:*:..:CHIL:*:.."   , PropertySex.FEMALE),
    new Relative("son"        , "INDI:FAMS:*:..:CHIL:*:.."   , PropertySex.MALE),
    new Relative("brother"    , "INDI:FAMC:*:..:CHIL:*:.."   , PropertySex.MALE),
    new Relative("sister"     , "INDI:FAMC:*:..:CHIL:*:.."   , PropertySex.FEMALE),
    new Relative("uncle"      , "father+brother|mother+brother|father+ sister+husband|mother+sister +husband"),
    new Relative("aunt"       , "father+sister |mother+sister |father+brother+wife   |mother+brother+wife"   ),
    new Relative("mcousin"    , "uncle+son"     ),
    new Relative("fcousin"    , "uncle+daughter")
  };
  
  /**
   * no text output necessary
   */
  public boolean usesStandardOut() {
    return false;
  }

  /**
   * Run this on an individual only
   */
  public String accepts(Object context) {
    return context instanceof Indi ? getName(): null;
  }

  /**
   * Reports main
   */
  public void start(Object context) {

    // Assume Individual
    Indi indi = (Indi)context;
    Gedcom gedcom = indi.getGedcom();
    String title = i18n("title", indi);
    System.out.println(title);
    
    // prepare map of relationships
    Map key2relative = new HashMap();
    for (int i=0; i<RELATIVES.length;i++) {
      Relative relative = RELATIVES[i];
      key2relative.put(relative.key, relative);
    }
    
    // Loop over relative descriptions
    List items = new ArrayList();
    for (int i=0; i<RELATIVES.length; i++) {
      Relative relative = RELATIVES[i];
      List result = find(indi, relative.path, relative.sex, key2relative);
      for (int j=0;j<result.size();j++) {
        Indi found = (Indi)result.get(j);
        String name = i18n(relative.key) + ": " + found;
        items.add(new Item(name, found.getImage(false), found));
      }
    }
    
    // show it
    showItemsToUser(title, gedcom, items);

    // done
  }
  
  private List find(List roots, String path, int sex, Map key2relative) {
    
    List result = new ArrayList();
    for (int i=0;i<roots.size();i++) {
      result.addAll(find((Property)roots.get(i), path, sex, key2relative));
    }
    
    return result;
    
  }
  
  /**
   * Analyze a relationship of an individual
   */
  private List find(Property root, String path, int sex, Map key2relative) {
    
    List result = new ArrayList();
    
    // any 'OR's?
    int or = path.indexOf('|');
    if (or>0) {
      StringTokenizer ors = new StringTokenizer(path, "|");
      while (ors.hasMoreTokens()) 
        result.addAll(find(root, ors.nextToken().trim(), sex, key2relative));
      return result;
    }
    
    // is relationship recursive?
    int dot = path.indexOf('+');
    if (dot>0) {
      StringTokenizer cont = new StringTokenizer(path, "+");
      result.add(root);
      while (cont.hasMoreTokens()) {
        Relative relative = (Relative)key2relative.get(cont.nextToken().trim());
        result = find(result, relative.path, relative.sex, key2relative);
      }
      return result;
    }
    
    // it's a tagpath from here
    Property[] found = root.getProperties(new TagPath(path));
    for (int i = 0; i < found.length; i++) {
      if (found[i]!=root) {
        Indi indi = (Indi)found[i];
        if (sex==PropertySex.UNKNOWN||indi.getSex()==sex)
          result.add(found[i]);
      }
    }

    // done
    return result;
  }
  
} //ReportRelatives
