/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package validate;

import genj.gedcom.Property;

import javax.swing.ImageIcon;

/**
 * An issue that can be corrected
 * @author nmeier
 */
/*package*/ abstract class SolvableIssue extends Issue {

  /**
   * Constructor
   */
  /*package*/ SolvableIssue(String naMe, ImageIcon imG, Property taRget) {
    super(naMe, imG, taRget);
  }

  /**
   * solution description
   */
  /*package*/ abstract String solution();
  
  /**
   * solve it
   */
  /*package*/ abstract void solve();

} //CorrectableIssue