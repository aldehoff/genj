/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.util.Registry;
import tree.IndiBox;

/**
 * Common code for family tree rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class TreeRendererBase {

    /**
     * Size of left and right image margin.
     */
    protected static final int VERTICAL_MARGIN = 10;

    /**
     * Size of top and bottom image margin.
     */
	protected static final int HORIZONTAL_MARGIN = 10;

    protected int indiboxHeight;
    protected int verticalGap;
    protected int verticalUnit;
    protected int famboxWidth;
    protected int famboxHeight;
    protected boolean displayFambox;

    protected IndiBox firstIndi;

    /**
     * Draws tree elements (boxes, lines, ...).
     */
    protected TreeElements elements;

    /**
     * Constructs the object.
     * @param indiboxWidth  width of the individual box in pixels
     * @param indiboxHeight height of the individual box in pixels
     * @param verticalGap minimal vertical gap between individual boxes
     */
	public TreeRendererBase(TreeElements elements, IndiBox firstIndi, Registry properties) {
        this.elements = elements;
        this.firstIndi = firstIndi;

		indiboxHeight = properties.get("indiboxHeight", 0);
		verticalGap = properties.get("verticalGap", 0);
        famboxWidth = properties.get("famboxWidth", 0);
        famboxHeight = properties.get("famboxHeight", 0);
        displayFambox = properties.get("displayFambox", true);
		verticalUnit = indiboxHeight + verticalGap;
        if (displayFambox)
            verticalUnit += famboxHeight;
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
     * Returns the image width (in pixels, including margins)
     */
    public abstract int getImageWidth();

    /**
     * Returns the image height (in pixels, including margins)
     */
    public abstract int getImageHeight();

    /**
     * Draws the tree.
     *
     */
    protected abstract void drawTree();
}
