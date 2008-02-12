/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.util.Registry;

import java.awt.Dimension;
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
    public VerticalTreeRenderer(IndiBox firstIndi, TreeElements elements, Registry properties) {
        super(firstIndi, elements, properties);
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     * @param baseX  x coordinate
     * @param baseY  y coordinate
     * @param gen  generation number
     */
	protected void drawLines(IndiBox indibox, int baseX, int baseY) {

        int midX = baseX + getMidX(indibox);

        // Lines (draw lines first so that boxes hide line ends)
        int lineY = 0;
        if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
            int midY = baseY + indibox.height;
            lineY = midY + spacing;
            if (indibox.spouse != null)
                midY -= indibox.height / 2;

            if (indibox.family != null) {
                midY = baseY + indibox.height + indibox.family.height;
                lineY += indibox.family.height;
            }

            elements.drawLine(midX, midY, midX, lineY);

            SortedSet xSet = new TreeSet();
            xSet.add(new Integer(midX));
            if (indibox.getDir() == Direction.PARENT)
                xSet.add(new Integer(baseX - indibox.x + indibox.prev.width / 2));
            if (indibox.hasChildren())
                for (int i = 0; i < indibox.children.length; i++)
                    xSet.add(new Integer(baseX + indibox.children[i].x + indibox.children[i].width / 2));
            int x1 = ((Integer)xSet.first()).intValue();
            int x2 = ((Integer)xSet.last()).intValue();

            elements.drawLine(x1, lineY, x2, lineY);
        }

		// Parent
		if (indibox.parent != null) {
            int parentLineY = baseY + indibox.parent.y + indibox.parent.height + spacing;
            if (indibox.parent.family != null)
                parentLineY += indibox.parent.family.height;
            elements.drawLine(baseX + indibox.width / 2, baseY, baseX + indibox.width / 2, parentLineY);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
                int x = baseX + indibox.children[i].x + indibox.children[i].width / 2;
                elements.drawLine(x, baseY + indibox.children[i].y, x, lineY);
			}

		// Next marriage
		if (indibox.nextMarriage != null) {
            lineY = indibox.height / 2;
            if (indibox.nextMarriage.height < indibox.height)
                lineY = indibox.nextMarriage.height / 2;
			if (indibox.nextMarriage.x > 0)
                elements.drawDashedLine(baseX + indibox.width, baseY + lineY,
				        baseX + indibox.nextMarriage.x, baseY + lineY);
			else
                elements.drawDashedLine(baseX, baseY + lineY,
				        baseX + indibox.nextMarriage.x + indibox.nextMarriage.width, baseY + lineY);
		}
	}

    /**
     * Returns the position of the family box relative to the individual box.
     */
    protected Dimension getFamboxCoords(IndiBox indibox) {
        int x = getMidX(indibox) - indibox.family.width / 2;
        int y = indibox.height;
        return new Dimension(x, y);
    }

    private int getMidX(IndiBox indibox) {
        if (indibox.spouse == null)
            return indibox.width / 2;
        if (indibox.spouse.x > 0)
            return (indibox.spouse.x + indibox.width) / 2;
        return (indibox.spouse.x + indibox.spouse.width) / 2;
    }
}
