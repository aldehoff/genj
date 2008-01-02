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
 * Common code for family tree rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class VerticalTreeRenderer extends TreeRendererBase {

    /**
     * Smallest level number.
     */
    private int levelMin = 0;

    /**
     * Largest level number.
     */
    private int levelMax = 0;

    /**
     * Heights of levels.
     */
    private Map levelHeight = new HashMap();

    /**
     * Y coordinates of levels.
     */
    private Map levelCoord = new HashMap();

    /**
     * Constructs the object.
     */
	public VerticalTreeRenderer(IndiBox firstIndi, Registry properties) {
        super(firstIndi, properties);

        new SameHeightSpouses().filter(firstIndi);

        // Determine height of levels
        new DetermineLevelHeight().filter(firstIndi);
        int yCoord = PAGE_MARGIN;
        for (int i = levelMin; i <= levelMax; i++) {
            levelCoord.put(new Integer(i), new Integer(yCoord));
            yCoord += ((Integer)levelHeight.get(new Integer(i))).intValue();
        }
        levelCoord.put(new Integer(levelMax + 1), new Integer(yCoord));
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     */
	protected void drawTree() {
        drawTree(firstIndi, firstIndi.wMinus + PAGE_MARGIN, 0, 0);
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

        int midX = baseX;
        if (indibox.spouse == null)
            midX += indibox.width / 2;
        else if (indibox.spouse.x > 0)
            midX += (indibox.spouse.x + indibox.width) / 2;
        else
            midX += (indibox.spouse.x + indibox.spouse.width) / 2;

        // Lines (draw lines first so that boxes hide line ends)
        if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
            int midY = getYCoord(baseY) + indibox.height;
            if (indibox.spouse != null)
                midY -= indibox.height / 2;

            if (displayFambox && indibox.family != null && indibox.spouse != null)
                midY = getYCoord(baseY) + indibox.height + indibox.family.height;

            elements.drawLine(midX, midY, midX, getYCoord(baseY + 1) - spacing);

            SortedSet xSet = new TreeSet();
            xSet.add(new Integer(midX));
            if (indibox.getDir() == Direction.PARENT)
                xSet.add(new Integer(baseX - indibox.x + indibox.prev.width / 2));
            if (indibox.hasChildren())
                for (int i = 0; i < indibox.children.length; i++)
                    xSet.add(new Integer(baseX + indibox.children[i].x + indibox.children[i].width / 2));
            int x1 = ((Integer)xSet.first()).intValue();
            int x2 = ((Integer)xSet.last()).intValue();

            elements.drawLine(x1, getYCoord(baseY + 1) - spacing,
                    x2, getYCoord(baseY + 1) - spacing);
        }

        // The individual
        elements.drawIndiBox(indibox, baseX, getYCoord(baseY), gen);

        // Family box
        // TODO: Should family boxes be displayed when there's no spouse?
        if (displayFambox && indibox.family != null && indibox.spouse != null)
            elements.drawFamBox(indibox.family, midX - indibox.family.width / 2,
                    getYCoord(baseY) + indibox.height, gen);

		// Spouse
		if (indibox.spouse != null)
			drawTree(indibox.spouse, baseX, baseY, gen);

		// Parent
		if (indibox.parent != null) {
            elements.drawLine(baseX + indibox.width / 2, getYCoord(baseY),
                baseX + indibox.width / 2, getYCoord(baseY + indibox.parent.y + 1) - spacing);
            drawTree(indibox.parent, baseX, baseY, gen - 1);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
                int x = baseX + indibox.children[i].x + indibox.children[i].width / 2;
                elements.drawLine(x, getYCoord(baseY + indibox.children[i].y),
						x, getYCoord(baseY + 1) - spacing);
                drawTree(indibox.children[i], baseX, baseY, gen + 1);
			}

		// Next marriage
		if (indibox.nextMarriage != null) {
            int lineY = indibox.height / 2;
            if (indibox.nextMarriage.height < indibox.height)
                lineY = indibox.nextMarriage.height / 2;
			if (indibox.nextMarriage.x > 0)
                elements.drawDashedLine(baseX + indibox.width, getYCoord(baseY) + lineY,
				        baseX + indibox.nextMarriage.x, getYCoord(baseY) + lineY);
			else
                elements.drawDashedLine(baseX, getYCoord(baseY) + lineY,
				        baseX + indibox.nextMarriage.x + indibox.nextMarriage.width, getYCoord(baseY) + lineY);
            drawTree(indibox.nextMarriage, baseX, baseY, gen);
		}
	}

    /**
     * Returns the image width (in pixels, including margins)
     */
    public int getImageWidth() {
        return firstIndi.wMinus + firstIndi.wPlus + 2 * PAGE_MARGIN;
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    public int getImageHeight() {
        return getYCoord(levelMax + 1) + PAGE_MARGIN - spacing * 2;
    }

    /**
     * Converts the generation level number to image Y coordinate.
     */
    private int getYCoord(int level) {
        return ((Integer)levelCoord.get(new Integer(level))).intValue();
    }

    /**
     * Determines line heights. The height of a line is the maximum height of a box
     * in this line.
     */
    private class DetermineLevelHeight extends TreeFilterBase {

        /**
         * Current level.
         */
        private int level = 0;

        public DetermineLevelHeight() {
        }

        protected void preFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level += indibox.y;

            if (level > levelMax)
                levelMax = level;
            if (level < levelMin)
                levelMin = level;

            Integer lev = new Integer(level);
            Integer height = (Integer)levelHeight.get(lev);
            int heightInt = 0;
            if (height != null)
                heightInt = height.intValue();
            int newHeight = indibox.height + spacing * 2;
            if (indibox.family != null)
                newHeight += indibox.family.height;
            if (newHeight > heightInt)
                levelHeight.put(lev, new Integer(newHeight));
        }

        protected void postFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level -= indibox.y;
        }
    }

    /**
     * Expands spouses' box sizes to make them equal height.
     */
    private static class SameHeightSpouses extends TreeFilterBase {
        protected void preFilter(IndiBox indibox) {
            if (indibox.spouse != null) {
                if (indibox.spouse.height > indibox.height)
                    indibox.height = indibox.spouse.height;
                else
                    indibox.spouse.height = indibox.height;
            }
        }
    }
}
