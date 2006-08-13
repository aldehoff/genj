/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.util.Registry;

import java.util.SortedSet;
import java.util.TreeSet;

import tree.IndiBox;
import tree.IndiBox.Direction;

/**
 * Common code for family tree rendering classes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class VerticalTreeRenderer extends TreeRendererBase {

    /**
     * Constructs the object.
     */
	public VerticalTreeRenderer(TreeElements elements, IndiBox firstIndi, Registry properties) {
        super(elements, firstIndi, properties);
		verticalUnit = indiboxHeight + verticalGap;
        if (displayFambox)
            verticalUnit += famboxHeight;
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     */
	protected void drawTree() {
        drawTree(firstIndi, firstIndi.wMinus + HORIZONTAL_MARGIN, firstIndi.hMinus, 0);
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
            int midY = getYCoord(baseY) + indiboxHeight;
            if (indibox.spouse != null)
                midY -= indiboxHeight / 2;

            if (displayFambox && indibox.family != null & indibox.spouse != null)
                midY = getYCoord(baseY) + indiboxHeight + famboxHeight;

            elements.drawLine(midX, midY, midX, getYCoord(baseY + 1) - verticalGap / 2);

            SortedSet xSet = new TreeSet();
            xSet.add(new Integer(midX));
            if (indibox.getDir() == Direction.PARENT)
                xSet.add(new Integer(baseX - indibox.x + indibox.prev.width / 2));
            if (indibox.hasChildren())
                for (int i = 0; i < indibox.children.length; i++)
                    xSet.add(new Integer(baseX + indibox.children[i].x + indibox.children[i].width / 2));
            int x1 = ((Integer)xSet.first()).intValue();
            int x2 = ((Integer)xSet.last()).intValue();

            elements.drawLine(x1, getYCoord(baseY + 1) - verticalGap / 2,
                    x2, getYCoord(baseY + 1) - verticalGap / 2);
        }
        
        // The individual
        elements.drawIndiBox(indibox, baseX, getYCoord(baseY), gen);

        // Family box
        // TODO: Should family boxes be displayed when there's no spouse?
        if (displayFambox && indibox.family != null && indibox.spouse != null)
            elements.drawFamBox(indibox.family, midX - famboxWidth / 2,
                    getYCoord(baseY) + indiboxHeight, gen);

		// Spouse
		if (indibox.spouse != null)
			drawTree(indibox.spouse, baseX, baseY, gen);

		// Parent
		if (indibox.parent != null) {
            elements.drawLine(baseX + indibox.width / 2, getYCoord(baseY),
                baseX + indibox.width / 2, getYCoord(baseY + indibox.parent.y + 1) -
			    verticalGap / 2);
            drawTree(indibox.parent, baseX, baseY, gen - 1);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
                int x = baseX + indibox.children[i].x + indibox.children[i].width / 2;
                elements.drawLine(x, getYCoord(baseY + indibox.children[i].y),
						x, getYCoord(baseY + 1) - verticalGap / 2);
                drawTree(indibox.children[i], baseX, baseY, gen + 1);
			}

		// Next marriage
		if (indibox.nextMarriage != null) {
			if (indibox.nextMarriage.x > 0)
                elements.drawDashedLine(baseX + indibox.width, getYCoord(baseY) + indiboxHeight / 2,
				        baseX + indibox.nextMarriage.x, getYCoord(baseY) + indiboxHeight / 2);
			else
                elements.drawDashedLine(baseX, getYCoord(baseY) + indiboxHeight / 2,
				        baseX + indibox.nextMarriage.x + indibox.nextMarriage.width, getYCoord(baseY) + indiboxHeight / 2);
            drawTree(indibox.nextMarriage, baseX, baseY, gen);
		}
	}

    /**
     * Returns the image width (in pixels, including margins)
     */
    public int getImageWidth() {
        return firstIndi.wMinus + firstIndi.wPlus + 2 * HORIZONTAL_MARGIN;
    }

    /**
     * Returns the image height (in pixels, including margins)
     */
    public int getImageHeight() {
        return (firstIndi.hMinus + firstIndi.hPlus) * verticalUnit - verticalGap + 2 * VERTICAL_MARGIN;
    }

    /**
     * Converts the generation level number to image Y coordinate.
     */
    private int getYCoord(int level) {
        return level * verticalUnit + VERTICAL_MARGIN;
    }
}
