/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.arrange;

import tree.IndiBox;
import tree.IndiBox.Direction;

/**
 * Aligns the tree to the left edge.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class AlignLeftArranger extends AbstractArranger {

    /**
     * Constructs the object.
     *
     * @param indiboxWidth  width of the individual box
     * @param horizontalGap minimal horizontal gap between individual boxes
     */
	public AlignLeftArranger(int indiboxWidth, int horizontalGap) {
		super(indiboxWidth, horizontalGap);
	}

	protected void arrangeSpouse(IndiBox indibox, IndiBox spouse) {
		indibox.spouse.x = indiboxWidth;
	}

	protected void arrangeChildren(IndiBox indibox) {
		int currentX = 0;
		if (indibox.getDir() == Direction.PARENT)
			currentX = indiboxWidth / 2 + horizontalGap;
		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.x = currentX;
			child.y = 1;
			filter(child);
			currentX += child.wPlus + horizontalGap;
		}
		if (indibox.children.length == 1 && indibox.children[0].spouse == null &&
				indibox.wMinus + 2 * indibox.children[0].x + indiboxWidth < indibox.wPlus)
			indibox.children[0].x = (indibox.wPlus - indibox.wMinus - indiboxWidth) / 2;
	}

	protected void arrangeNextMarriages(IndiBox indibox, IndiBox next) {
		filter(next);
        next.x = indibox.wPlus + next.wMinus + horizontalGap;
        if (indibox.spouse != null && indibox.spouse.nextMarriage == next)
            next.x -= indibox.spouse.x;
	}

	protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.y = -parent.hPlus;
	}

	protected void arrangeParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.y = -indibox.hMinus - parent.hPlus;
	}
}
