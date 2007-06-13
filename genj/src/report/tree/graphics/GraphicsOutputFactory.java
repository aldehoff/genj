/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.report.Report;
import genj.util.swing.Action2;

import java.io.File;

/**
 * Creates classes that write report output. This can be
 * a file type or the screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsOutputFactory {

    public static final int SVG_OUTPUT = 0;
    public static final int PDF_OUTPUT = 1;
    public static final int SCREEN_OUTPUT = 2;

    /**
     * Containing report. Used to show dialogs and translate strings.
     */
    private Report report;

    /**
     * Creates the object
     * @param report  containing report object
     * @param properties  report properties
     */
    public GraphicsOutputFactory(Report report) {
        this.report = report;
    }

    /**
     * Creates the output class for the given type.
     * @param type  output type
     */
    public GraphicsOutput createOutput(int type) {

    	GraphicsOutput output = null;

    	if (type == SCREEN_OUTPUT)
    		output = new ScreenOutput();
    	else if (type == SVG_OUTPUT)
    		output = new SvgWriter();
    	else if (type == PDF_OUTPUT)
    		output = new PdfWriter();

    	if (output == null)
    		return null;

    	if (output instanceof GraphicsFileOutput) {
    		GraphicsFileOutput fileOutput = (GraphicsFileOutput)output;
    		String extension = fileOutput.getFileExtension();

        	// Get filename from users
            File file = report.getFileFromUser(report.translate("output.file"),
                        Action2.TXT_OK, true, extension);
            if (file == null)
                return null;

            // Add appropriate file extension
            String suffix = "." + extension;
            if (!file.getPath().endsWith(suffix))
            	file = new File(file.getPath() + suffix);
            fileOutput.setFile(file);
    	}

        return output;
    }
}
