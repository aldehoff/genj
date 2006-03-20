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
import genj.util.swing.Action2;

import java.io.File;

/**
 * Creates classes that write the family tree to an output. This can be
 * a file type or the screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class OutputFactory {

    public static int SVG_OUTPUT = 0;
    public static int PDF_OUTPUT = 1;
    public static int SCREEN_OUTPUT = 2;

    /**
     * Containing report. Used to show dialogs and translate strings.
     */
    private Report report;

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
        this.report = report;
        this.properties = properties;
    }

    /**
     * Creates the output class for the given type.
     * @param type  output type
     */
    public TreeOutput createOutput(int type) {
        File file = null;
        if (type == SVG_OUTPUT || type == PDF_OUTPUT) {
            file = report.getFileFromUser(report.translate("output.file"),
                    Action2.TXT_OK, true);
            if (file == null)
                return null;
        }

        if (type == SVG_OUTPUT) {
            return new GraphicsFileOutput(properties, file, new SvgWriter());
        } else if (type == PDF_OUTPUT) {
            return new GraphicsFileOutput(properties, file, new PdfWriter());
        } else if (type == SCREEN_OUTPUT) {
            return new ScreenOutput(properties);
        } else
            return null;
    }
}
