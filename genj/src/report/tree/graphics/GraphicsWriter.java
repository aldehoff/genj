/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Interface for classes writing different file types based on drawing on
 * a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public interface GraphicsWriter {

    /**
     * Writes the drawing to the output stream.
     * @param out  destination output stream
     * @param renderer this object renders the drawing
     */
    public void write(OutputStream out, GraphicsRenderer renderer)
        throws IOException;
}
