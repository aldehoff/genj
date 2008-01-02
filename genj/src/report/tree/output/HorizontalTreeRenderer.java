/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.util.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import tree.IndiBox;
import tree.TreeFilterBase;
import tree.IndiBox.Direction;

/**
 * Horizontal family tree rendering.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class HorizontalTreeRenderer extends TreeRendererBase {

    /**
     * Smallest level number.
     */
    private int levelMin = 0;

    /**
     * Largest level number.
     */
    private int levelMax = 0;

    /**
     * Widths of levels.
     */
    private Map levelWidth = new HashMap();

    /**
     * X coordinates of levels.
     */
    private Map levelCoord = new HashMap();

    /**
     * Constructs the object.
     */
	public HorizontalTreeRenderer(IndiBox firstIndi, Registry properties) {
        super(firstIndi, properties);

        new SameWidthSpouses().filter(firstIndi);

        // Determine width of levels
        new DetermineLevelWidth().filter(firstIndi);
        int xCoord = PAGE_MARGIN;
        for (int i = levelMin; i <= levelMax; i++) {
            levelCoord.put(new Integer(i), new Integer(xCoord));
            xCoord += ((Integer)levelWidth.get(new Integer(i))).intValue();
        }
        levelCoord.put(new Integer(levelMax + 1), new Integer(xCoord));
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     */
	protected void drawTree() {
        drawTree(firstIndi, 0, firstIndi.hMinus + PAGE_MARGIN, 0);
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

        int midY = baseY;
        if (indibox.spouse == null)
            midY += indibox.height / 2;
        else if (indibox.spouse.y > 0)
            midY += (indibox.spouse.y + indibox.height) / 2;
        else
            midY += (indibox.spouse.y + indibox.spouse.height) / 2;

        // Lines (draw lines first so that boxes hide line ends)
        if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
            int midX = getXCoord(baseX) + indibox.width;
            if (indibox.spouse != null)
                midX -= indibox.width / 2;

            if (displayFambox && indibox.family != null && indibox.spouse != null)
                midX = getXCoord(baseX) + indibox.width + indibox.family.width;

            elements.drawLine(midX, midY, getXCoord(baseX + 1) - spacing, midY);

            SortedSet ySet = new TreeSet();
            ySet.add(new Integer(midY));
            if (indibox.getDir() == Direction.PARENT)
                ySet.add(new Integer(baseY - indibox.y + indibox.prev.height / 2));
            if (indibox.hasChildren())
                for (int i = 0; i < indibox.children.length; i++)
                    ySet.add(new Integer(baseY + indibox.children[i].y + indibox.children[i].height / 2));
            int y1 = ((Integer)ySet.first()).intValue();
            int y2 = ((Integer)ySet.last()).intValue();

            elements.drawLine(getXCoord(baseX + 1) - spacing, y1,
                    getXCoord(baseX + 1) - spacing, y2);
        }

        // The individual
        elements.drawIndiBox(indibox, getXCoord(baseX), baseY, gen);

        // Family box
        // TODO: Should family boxes be displayed when there's no spouse?
        if (displayFambox && indibox.family != null && indibox.spouse != null)
            elements.drawFamBox(indibox.family, getXCoord(baseX) + indibox.width,
                    midY - indibox.family.height / 2, gen);

		// Spouse
		if (indibox.spouse != null)
			drawTree(indibox.spouse, baseX, baseY, gen);

		// Parent
		if (indibox.parent != null) {
            elements.drawLine(getXCoord(baseX), baseY + indibox.height / 2,
                getXCoord(baseX + indibox.parent.x + 1) -
			    spacing, baseY + indibox.height / 2);
            drawTree(indibox.parent, baseX, baseY, gen - 1);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
                int y = baseY + indibox.children[i].y + indibox.children[i].height / 2;
                elements.drawLine(getXCoord(baseX + indibox.children[i].x), y,
						getXCoord(baseX + 1) - spacing, y);
                drawTree(indibox.children[i], baseX, baseY, gen + 1);
			}

		// Next marriage
		if (indibox.nextMarriage != null) {
            int lineX = indibox.width / 2;
            if (indibox.nextMarriage.width < indibox.width)
                lineX = indibox.nextMarriage.width / 2;
			if (indibox.nextMarriage.y > 0)
                elements.drawDashedLine(getXCoord(baseX) + lineX, baseY + indibox.height,
				        getXCoord(baseX) + lineX, baseY + indibox.nextMarriage.y);
			else
                elements.drawDashedLine(getXCoord(baseX) + lineX, baseY,
				        getXCoord(baseX) + lineX, baseY + indibox.nextMarriage.y + indibox.nextMarriage.height);
            drawTree(indibox.nextMarriage, baseX, baseY, gen);
		}
	}

    /**
     * Returns the image width (in pixels, including margins)
     */
    public int getImageWidth() {
        return getXCoord(levelMax + 1) + PAGE_MARGIN - spacing * 2;
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    public int getImageHeight() {
        return firstIndi.hMinus + firstIndi.hPlus + 2 * PAGE_MARGIN;
    }

    /**
     * Converts the generation level number to image Y coordinate.
     */
    private int getXCoord(int level) {
        return ((Integer)levelCoord.get(new Integer(level))).intValue();
    }

    /**
     * Determines line widths. The width of a line is the maximum width of a box
     * in this line.
     */
    private class DetermineLevelWidth extends TreeFilterBase {

        /**
         * Current level.
         */
        private int level = 0;

        public DetermineLevelWidth() {
        }

        protected void preFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level += indibox.x;

            if (level > levelMax)
                levelMax = level;
            if (level < levelMin)
                levelMin = level;

            Integer lev = new Integer(level);
            Integer height = (Integer)levelWidth.get(lev);
            int widthInt = 0;
            if (height != null)
                widthInt = height.intValue();
            int newWidth = indibox.width + spacing * 2;
            if (indibox.family != null)
                newWidth += indibox.family.width;
            if (newWidth > widthInt)
                levelWidth.put(lev, new Integer(newWidth));
        }

        protected void postFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level -= indibox.x;
        }
    }

    /**
     * Expands spouses' box sizes to make them equal width.
     */
    private static class SameWidthSpouses extends TreeFilterBase {
        protected void preFilter(IndiBox indibox) {
            if (indibox.spouse != null) {
                if (indibox.spouse.width > indibox.width)
                    indibox.width = indibox.spouse.width;
                else
                    indibox.spouse.width = indibox.width;
            }
        }
    }
}
