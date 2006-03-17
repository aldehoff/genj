/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Fam;
import genj.gedcom.Indi;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import tree.IndiBox.Direction;

/**
 * Common code for rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class AbstractTreeRenderer implements TreeRenderer {

    /**
     * Size of left and right image margin.
     */
	protected final int VERTICAL_MARGIN = 10;

    /**
     * Size of top and bottom image margin.
     */
	protected final int HORIZONTAL_MARGIN = 10;

    /**
     * Box background colors.
     */
	private final String[] BOX_COLORS = { "#FFFFFF", "#FFFFFF", "#DDDDFF", "#FFDDFF", "#FFDDDD",
	                                      "#FFFFDD",
	                                      "#DDFFDD", "#DDFFFF", "#DDDDFF", "#FFFFFF", "#FFFFFF" };

    protected int maxNames;
	protected int indiboxWidth;
    protected int indiboxHeight;
    protected int verticalGap;
    protected int verticalUnit;
    protected int famboxWidth;
    protected int famboxHeight;
    protected boolean displayFambox;

    private IndiBox firstIndi;

    /**
     * Constructs the object.
     * @param indiboxWidth  width of the individual box in pixels
     * @param indiboxHeight height of the individual box in pixels
     * @param verticalGap minimal vertical gap between individual boxes
     */
	public AbstractTreeRenderer(int maxNames, int indiboxWidth,
            int indiboxHeight, int verticalGap, int famboxWidth, int famboxHeight,
            boolean displayFambox) {
        this.maxNames = maxNames;
		this.indiboxWidth = indiboxWidth;
		this.indiboxHeight = indiboxHeight;
		this.verticalGap = verticalGap;
        this.famboxWidth = famboxWidth;
        this.famboxHeight = famboxHeight;
        this.displayFambox = displayFambox;
		verticalUnit = indiboxHeight + verticalGap;
        if (displayFambox)
            verticalUnit += famboxHeight;
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     */
	public void render(IndiBox indibox) throws IOException {
        firstIndi = indibox;
		header();
        drawTree(indibox, indibox.wMinus + HORIZONTAL_MARGIN, indibox.hMinus, 0);
		footer();
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     * @param baseX  x coordinate
     * @param baseY  y coordinate
     * @param gen  generation number
     */
	private void drawTree(IndiBox indibox, int baseX, int baseY, int gen) {
		baseX += indibox.x;
		baseY += indibox.y;
		drawIndiBox(indibox.individual, baseX, getYCoord(baseY), gen);

        if (displayFambox && indibox.family != null) {
            int famX = baseX + (indiboxWidth - famboxWidth) / 2;
            if (indibox.spouse != null)
                famX += indibox.spouse.x / 2;
            drawFamBox(indibox.family, famX, getYCoord(baseY) + indiboxHeight, gen);
        }

		// Spouse
		if (indibox.spouse != null)
			drawTree(indibox.spouse, baseX, baseY, gen);

		// Parent
		if (indibox.parent != null) {
			drawTree(indibox.parent, baseX, baseY, gen - 1);
			drawLine(baseX + indiboxWidth / 2, getYCoord(baseY),
                baseX + indiboxWidth / 2, getYCoord(baseY + indibox.parent.y + 1) -
			    verticalGap / 2);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
				drawTree(indibox.children[i], baseX, baseY, gen + 1);
				drawLine(baseX + indibox.children[i].x + indiboxWidth / 2,
                        getYCoord(baseY + indibox.children[i].y),
						baseX + indibox.children[i].x + indiboxWidth / 2,
                        getYCoord(baseY + 1) - verticalGap / 2);
			}

		// Lines
		if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
			int midX = baseX + indiboxWidth / 2;
			int midY = getYCoord(baseY) + indiboxHeight;
			if (indibox.spouse != null) {
				midX = baseX + (indibox.spouse.x + indiboxWidth) / 2;
				midY -= indiboxHeight / 2;
			}
            if (displayFambox && indibox.family != null)
                midY = getYCoord(baseY) + indiboxHeight + famboxHeight;

			drawLine(midX, midY, midX, getYCoord(baseY + 1) - verticalGap / 2);

			SortedSet xSet = new TreeSet();
			xSet.add(new Integer(midX));
			if (indibox.getDir() == Direction.PARENT)
				xSet.add(new Integer(baseX - indibox.x + indiboxWidth / 2));
			if (indibox.hasChildren())
				for (int i = 0; i < indibox.children.length; i++)
					xSet.add(new Integer(baseX + indibox.children[i].x + indiboxWidth / 2));
			int x1 = ((Integer)xSet.first()).intValue();
			int x2 = ((Integer)xSet.last()).intValue();

			drawLine(x1, getYCoord(baseY + 1) - verticalGap / 2,
					x2, getYCoord(baseY + 1) - verticalGap / 2);
		}

		// Next marriage
		if (indibox.nextMarriage != null) {
			drawTree(indibox.nextMarriage, baseX, baseY, gen);
			if (indibox.nextMarriage.x > 0)
				drawDashedLine(baseX + indiboxWidth, getYCoord(baseY) + indiboxHeight / 2,
				        baseX + indibox.nextMarriage.x, getYCoord(baseY) + indiboxHeight / 2);
			else
				drawDashedLine(baseX, getYCoord(baseY) + indiboxHeight / 2,
				        baseX + indibox.nextMarriage.x + indiboxWidth, getYCoord(baseY) + indiboxHeight / 2);
		}
	}

    /**
     * Outputs an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
	protected abstract void drawIndiBox(Indi i, int x, int y, int gen);

    /**
     * Outputs a family box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    protected abstract void drawFamBox(Fam f, int x, int y, int gen);

    /**
     * Outputs a line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	protected abstract void drawLine(int x1, int y1, int x2, int y2);

    /**
     * Outputs a dashed line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	protected abstract void drawDashedLine(int x1, int y1, int x2, int y2);

    /**
     * Outputs the image header.
     * @param w family tree width in pixels
     * @param h family tree height in generation lines
     */
	protected abstract void header() throws IOException;

    /**
     * Outputs the image footer.
     */
    protected abstract void footer() throws IOException;

    /**
     * Returns the image width (in pixels, including margins)
     */
    protected int getImageWidth() {
        return firstIndi.wMinus + firstIndi.wPlus + 2 * HORIZONTAL_MARGIN;
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    protected int getImageHeight() {
        return (firstIndi.hMinus + firstIndi.hPlus) * verticalUnit - verticalGap + 2 * VERTICAL_MARGIN;
    }

    /**
     * Converts the generation level number to image Y coordinate.
     */
    private int getYCoord(int level) {
        return level * verticalUnit + VERTICAL_MARGIN;
    }

    /**
     * Returns the box color for the given generation.
     */
    protected String getBoxColor(int gen) {
        if (gen + 5 < BOX_COLORS.length && gen + 5 >= 0)
            return BOX_COLORS[gen + 5];
        return BOX_COLORS[0];
    }

    /**
     * Returns a maximum of <code>maxNames</code> given names of the given
     * individual. If <code>maxNames</code> is 0, this method returns all
     * given names.
     */
    protected String getFirstNames(Indi indi) {
        String firstName = indi.getFirstName();
        if (maxNames <= 0)
            return firstName;

        String[] names = firstName.split("  *");
        firstName = "";
        for (int j = 0; j < maxNames && j < names.length; j++)
            firstName += names[j] + " ";
        return firstName.trim();
    }
}
