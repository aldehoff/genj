/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.report.Report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes report output to a file in an appropriate format.
 * A GraphicsWriter object is used to draw the content.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsFileOutput implements GraphicsOutput {

    /**
     * Destination file.
     */
    private File file;

    /**
     * Writes the rendered image to a file.
     */
    private GraphicsWriter writer;

    /**
     * Creates the object
     */
    public GraphicsFileOutput(File file, GraphicsWriter writer) {
        this.file = file;
        this.writer = writer;
    }

    /**
     * Writes the family tree to the output file.
     */
    public void output(GraphicsRenderer renderer) throws IOException {
        OutputStream out = new FileOutputStream(file);
        writer.write(out, renderer);
        out.close();
    }

    /**
     * Displays the generated file.
     */
    public void display(Report report) {
        report.showFileToUser(file);
    }
}
