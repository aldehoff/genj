/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import javax.swing.ImageIcon;

import genj.gedcom.Property;
import genj.report.Report;

/**
 * A found issue
 * @author nmeier
 */
/*package*/ class Issue extends Report.Item {

  /**
   * Constructor
   */
  public Issue(String txt, ImageIcon img, Property target) {
    super(txt, img, target);
  }
  
} //Issue