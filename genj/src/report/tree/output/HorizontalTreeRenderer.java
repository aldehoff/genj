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
 * Horizontal family tree rendering.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class HorizontalTreeRenderer extends TreeRendererBase {

    /**
     * Constructs the object.
     */
    public HorizontalTreeRenderer(IndiBox firstIndi, TreeElements elements, Registry properties) {
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

        int midY = baseY + getMidY(indibox);

        // Lines (draw lines first so that boxes hide line ends)
        int lineX = 0;
        if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
            int midX = baseX + indibox.width;
            lineX = midX + spacing;
            if (indibox.spouse != null)
                midX -= indibox.width / 2;

            if (indibox.family != null) {
                midX = baseX + indibox.width + indibox.family.width;
                lineX += indibox.family.width;
            }

            elements.drawLine(midX, midY, lineX, midY);

            SortedSet ySet = new TreeSet();
            ySet.add(new Integer(midY));
            if (indibox.getDir() == Direction.PARENT)
                ySet.add(new Integer(baseY - indibox.y + indibox.prev.height / 2));
            if (indibox.hasChildren())
                for (int i = 0; i < indibox.children.length; i++)
                    ySet.add(new Integer(baseY + indibox.children[i].y + indibox.children[i].height / 2));
            int y1 = ((Integer)ySet.first()).intValue();
            int y2 = ((Integer)ySet.last()).intValue();

            elements.drawLine(lineX, y1, lineX, y2);
        }

		// Parent
		if (indibox.parent != null) {
            int parentLineX = baseX + indibox.parent.x + indibox.parent.width + spacing;
            if (indibox.parent.family != null)
                parentLineX += indibox.parent.family.width;
            elements.drawLine(baseX, baseY + indibox.height / 2,
                parentLineX, baseY + indibox.height / 2);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
                int y = baseY + indibox.children[i].y + indibox.children[i].height / 2;
                elements.drawLine(baseX + indibox.children[i].x, y, lineX, y);
			}

		// Next marriage
		if (indibox.nextMarriage != null) {
            lineX = indibox.width / 2;
            if (indibox.nextMarriage.width < indibox.width)
                lineX = indibox.nextMarriage.width / 2;
			if (indibox.nextMarriage.y > 0)
                elements.drawDashedLine(baseX + lineX, baseY + indibox.height,
				        baseX + lineX, baseY + indibox.nextMarriage.y);
			else
                elements.drawDashedLine(baseX + lineX, baseY,
				        baseX + lineX, baseY + indibox.nextMarriage.y + indibox.nextMarriage.height);
		}
	}

    /**
     * Returns the position of the family box relative to the individual box.
     */
    protected Dimension getFamboxCoords(IndiBox indibox) {
        int x = indibox.width;
        int y = getMidY(indibox) - indibox.family.height / 2;
        return new Dimension(x, y);
    }

    private int getMidY(IndiBox indibox) {
        if (indibox.spouse == null)
            return indibox.height / 2;
        if (indibox.spouse.y > 0)
            return (indibox.spouse.y + indibox.height) / 2;
        return (indibox.spouse.y + indibox.spouse.height) / 2;
    }
}
