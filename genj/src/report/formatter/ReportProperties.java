/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package formatter;

import genj.report.Report;

/**
 * ReportProperties offers access to the properties for a report.
 */
public class ReportProperties {
  private final Report report;

  public ReportProperties(Report report) {
    this.report = report;
  }

  /**
   * @param key
   * @return null if property not configured
   */
  public final String getProp(String key) {
    String result = report.translate(key);
    if (result.equals(key)) return null; // second-guess Report's default mechanism (todo better way to tell if configured or not)
    // todo How get leading/trailing blanks in properties?
    if (result.startsWith("_")) {
      result = " " + result.substring(1);
    }
    if (result.endsWith("_")) {
      result = result.substring(0, result.length()-1) + " ";
    }
    System.err.println("Key " + key + " -> " + result);
    return result;
  }

}

