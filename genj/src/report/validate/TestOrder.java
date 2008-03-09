/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;
import genj.gedcom.PropertyComparator;
import genj.gedcom.TagPath;
import genj.view.ViewContext;

import java.util.Arrays;
import java.util.List;

/**
 * Test for dupes in information about being biological child
 */
public class TestOrder extends Test {

  private String tagToSort;
  private String pathToSort;
  
  /**
   * Constructor
   */
  /*package*/ TestOrder(String trigger, String tagToSort, String pathToSortBy) {
    // delegate to super
    super(trigger, Property.class);
    this.tagToSort = tagToSort;
    this.pathToSort = pathToSortBy;
  }
  
  /**
   * Test properties for order
   */
  /*package*/ void test(Property prop, TagPath trigger, List issues, ReportValidate report) {

    Property[] unsorted = prop.getProperties(tagToSort, true);
    
    Property[] sorted = prop.getProperties(tagToSort, true);
    Arrays.sort(sorted, new PropertyComparator(pathToSort));

    if (!Arrays.asList(sorted).equals(Arrays.asList(unsorted)))
        issues.add(new ViewContext(prop).setText(report.translate("warn.order."+tagToSort)));
    
    // done
  }
  

} //TestBiologicalChild