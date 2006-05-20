/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.report.Report;
import genj.util.Registry;
import tree.graphics.GraphicsOutput;
import tree.graphics.GraphicsOutputFactory;

/**
 * Creates classes that write the family tree to an output. This can be
 * a file type or the screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class OutputFactory {

    /**
     * Report output factory.
     */
    private GraphicsOutputFactory reportOutputFactory;

    /**
     * Report properties.
     */
    private Registry properties;

    /**
     * Creates the object
     * @param report  containing report object
     * @param properties  report properties
     */
    public OutputFactory(Report report, Registry properties) {
        reportOutputFactory = new GraphicsOutputFactory(report);
        this.properties = properties;
    }

    /**
     * Creates the output class for the given type.
     * @param type  output type
     */
    public TreeOutput createOutput(int type) {
        GraphicsOutput reportOutput = reportOutputFactory.createOutput(type);
        if (reportOutput == null)
            return null;
        return new GraphicsTreeOutput(reportOutput, properties);
    }
}
