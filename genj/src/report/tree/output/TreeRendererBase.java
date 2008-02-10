/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import java.awt.Graphics2D;

import genj.util.Registry;
import tree.IndiBox;
import tree.graphics.GraphicsRenderer;

/**
 * Common code for family tree rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class TreeRendererBase implements GraphicsRenderer {

    /**
     * Size of image margin.
     */
    protected static final int PAGE_MARGIN = 10;

    protected int spacing;
    protected boolean displayFambox;

    protected IndiBox firstIndi;

    /**
     * Draws tree elements (boxes, lines, ...).
     */
    protected TreeElements elements;

    /**
     * Constructs the object.
     */
    public TreeRendererBase(IndiBox firstIndi, TreeElements elements, Registry properties) {
        this.firstIndi = firstIndi;
        this.elements = elements;

		spacing = properties.get("spacing", 0);
        displayFambox = properties.get("displayFambox", true);
	}

    /**
     * Renders the family tree to the given Graphics2D object.
     */
	public void render(Graphics2D graphics)
	{
        elements.setGraphics(graphics);
        render();
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     */
	public void render() {
		elements.header(getImageWidth(), getImageHeight());
        drawTree();
        elements.footer();
	}

    /**
     * Draws the tree.
     *
     */
    protected abstract void drawTree();

}
