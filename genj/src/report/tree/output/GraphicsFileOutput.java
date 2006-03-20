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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import tree.IndiBox;
import tree.render.GraphicsRenderer;
import tree.render.GraphicsTreeRenderer;

/**
 * Writes the family tree to a file in an appropriate format.
 * The GraphicsWriter object generates the data using the
 * GraphicsTreeRenderer object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsFileOutput implements TreeOutput {

    /**
     * Report properties.
     */
    private Registry properties;

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
    public GraphicsFileOutput(Registry properties, File file,
            GraphicsWriter writer) {
        this.properties = properties;
        this.file = file;
        this.writer = writer;
    }

    /**
     * Writes the family tree to the output file.
     */
    public void output(IndiBox indibox) throws IOException {
        GraphicsRenderer renderer = new GraphicsTreeRenderer(indibox, properties);
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
