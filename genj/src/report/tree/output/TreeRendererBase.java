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
     * Size of left and right image margin.
     */
    protected static final int VERTICAL_MARGIN = 10;

    /**
     * Size of top and bottom image margin.
     */
	protected static final int HORIZONTAL_MARGIN = 10;

    protected int verticalGap;
    protected boolean displayFambox;

    protected IndiBox firstIndi;

    /**
     * Draws tree elements (boxes, lines, ...).
     */
    protected GraphicsTreeElements elements;

    /**
     * Constructs the object.
     */
	public TreeRendererBase(IndiBox firstIndi, Registry properties) {
        this.firstIndi = firstIndi;
        elements = new GraphicsTreeElements(null, properties);

		verticalGap = properties.get("verticalGap", 0);
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
