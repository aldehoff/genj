/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import java.util.HashMap;
import java.util.Map;

import tree.IndiBox;
import tree.filter.TreeFilter;
import tree.filter.TreeFilterBase;

/**
 * Converts line numbers to coordinates.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class VerticalLines implements TreeFilter {

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

    private int spacing;

    /**
     * Constructs the object.
     */
    public VerticalLines(int spacing) {
        this.spacing = spacing;
    }

    public void filter(IndiBox indibox)
    {
        // Determine width of levels
        new DetermineLevelWidth().filter(indibox);

        // Assign coordinates to levels
        int xCoord = 0;
        for (int i = levelMin; i <= levelMax; i++) {
            levelCoord.put(new Integer(i), new Integer(xCoord));
            xCoord += ((Integer)levelWidth.get(new Integer(i))).intValue();
        }
        levelCoord.put(new Integer(levelMax + 1), new Integer(xCoord));

        // Assign coordinates to boxes
        new AssignCoordinates().filter(indibox);
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
     * Assigns coordinates to boxes based on coordinates of levels.
     */
    private class AssignCoordinates extends TreeFilterBase {

        /**
         * Current level.
         */
        private int level = 0;

        protected void preFilter(IndiBox indibox) {
            if (indibox.prev != null)
                level += indibox.x;
        }

        protected void postFilter(IndiBox indibox) {
            int thisLevel = level;
            if (indibox.prev != null) {
                level -= indibox.x;
                indibox.x = getXCoord(thisLevel) - getXCoord(thisLevel - indibox.x);
            }
            indibox.wPlus = getXCoord(thisLevel + indibox.wPlus) - getXCoord(thisLevel);
            indibox.wMinus = getXCoord(thisLevel) - getXCoord(thisLevel - indibox.wMinus);
        }
    }
}
