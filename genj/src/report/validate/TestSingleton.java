/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test whether properties adhere to their singleton status
 */
public class TestSingleton extends Test {

  /**
   * Constructor
   */
  public TestSingleton() {
    super((String[])null, Property.class);
  }

  /**
   * Do the test 
   */
  void test(Property prop, TagPath path, List issues, ReportValidate report) {
    
    // check children
    MetaProperty meta = prop.getMetaProperty();

    // check doubles
    Map seen = new HashMap();
    for (int i=0,j=prop.getNoOfProperties(); i<j ; i++) {
      Property child = prop.getProperty(i);
      String tag = child.getTag();
      if (meta.getNested(tag, false).isSingleton()) {
        if (!seen.containsKey(tag))
          seen.put(tag, child);
        else {
          Property first = (Property)seen.get(tag);
          if (first!=null) {
            seen.put(tag, null);
            issues.add(new ViewContext(first).setText(report.translate("err.notsingleton", first.getTag())));
          }
        }
      }
    }

    // done
  }

} //TestFiles