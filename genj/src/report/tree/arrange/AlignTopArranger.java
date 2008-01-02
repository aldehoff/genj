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
public class AlignTopArranger extends AbstractArranger {

    /**
     * Constructs the object.
     *
     * @param spacing minimal gap between boxes and lines
     */
	public AlignTopArranger(int spacing) {
		super(spacing);
	}

    public void filter(IndiBox indibox) {
        indibox.wPlus = 1;
        indibox.hPlus = indibox.height;
        super.filter(indibox);
    }

	protected void arrangeSpouse(IndiBox indibox, IndiBox spouse) {
        spouse.hPlus = spouse.height;
		spouse.y = indibox.height;
	}

	protected void arrangeChildren(IndiBox indibox) {
		int currentY = 0;
		if (indibox.getDir() == Direction.PARENT)
            currentY = indibox.prev.height / 2 - indibox.y + spacing;

		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.y = currentY;
			child.x = 1;
			filter(child);
			currentY += child.hPlus + spacing;
		}
		if (indibox.children.length == 1) {
            IndiBox child = indibox.children[0];
            int parentHeight = indibox.hMinus + indibox.hPlus;
            int childHeight = child.hMinus + child.hPlus;
            int centerY = (parentHeight - childHeight) / 2 - indibox.hMinus + child.hMinus;
            if (child.y < centerY)
                child.y = centerY;
        }
	}

	protected void arrangeNextMarriages(IndiBox indibox, IndiBox next) {
		filter(next);
        next.y = indibox.hPlus + next.hMinus + spacing;
        if (indibox.spouse != null && indibox.spouse.nextMarriage == next)
            next.y -= indibox.spouse.y;
	}

	protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.x = -parent.wPlus;
	}

	protected void arrangeParent(IndiBox indibox, IndiBox parent) {
		filter(parent);
		parent.x = -indibox.wMinus - parent.wPlus;
	}
}
