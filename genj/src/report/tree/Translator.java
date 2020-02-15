/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.report.Report;

/**
 * Uses the report object to translate strings.
 * This class' purpose is to prevent passing around the report object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class Translator
{
    private Report report;

    public Translator(Report report)
    {
        this.report = report;
    }

    /**
     * Translates a string.
     *
     * @param key the key to lookup in [ReportName].properties
     */
    public final String translate(String key)
    {
        return report.translate(key);
    }
}
