/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.report.Report;

import java.io.IOException;

import tree.IndiBox;

/**
 * Interface for classes writing the family tree to an output.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface TreeOutput {

    /**
     * Writes the family tree to the output.
     */
	public void output(IndiBox indibox) throws IOException;

    /**
     * Displays the generated content.
     */
    public void display(Report report);
}
