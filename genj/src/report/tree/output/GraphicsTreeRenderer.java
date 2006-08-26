/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.util.Registry;

import java.awt.Graphics2D;

import tree.IndiBox;
import tree.graphics.GraphicsRenderer;

/**
 * Outputs the generated tree to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeRenderer implements GraphicsRenderer {

    /**
     * Renders the tree calling this classes methods to draw the tree's elements.
     */
    private TreeRendererBase treeRenderer;

    /**
     * Draws tree elements to a Graphics2D object.
     */
    private GraphicsTreeElements treeElements;

    /**
     * Constructs the object.
     */
	public GraphicsTreeRenderer(IndiBox indibox, Registry properties) {
        treeElements = new GraphicsTreeElements(null, properties); // We will set the graphics later
        treeRenderer = new VerticalTreeRenderer(treeElements, indibox, properties);
    }

    /**
     * Renders the family tree to the given Graphics2D object.
     */
    public void render(Graphics2D graphics) {
        treeElements.setGraphics(graphics);
        treeRenderer.render();
    }

    /**
     * Returns the image width (in pixels, including margins)
     */
    public int getImageWidth() {
        return treeRenderer.getImageWidth();
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    public int getImageHeight() {
        return treeRenderer.getImageHeight();
    }
}
