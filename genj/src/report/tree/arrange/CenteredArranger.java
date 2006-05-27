/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.arrange;

import tree.IndiBox;
import tree.TreeFilter;
import tree.IndiBox.Direction;

/**
 * Centers the tree. Uses AlignRightArranger and AlignLeftArranger for
 * parts of the tree.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class CenteredArranger extends AlignLeftArranger {

	private TreeFilter leftArranger;
	private TreeFilter rightArranger;

	public CenteredArranger(int horizontalGap) {
		super(horizontalGap);
		leftArranger = new AlignRightArranger(horizontalGap);
		rightArranger = new AlignLeftArranger(horizontalGap);
	}

	protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
		if (indibox.parent != null) {
			parent.x = horizontalGap / 2;
			rightArranger.filter(parent);
		} else {
			parent.x = indibox.spouse.width / 2 - parent.width;
			filter(parent);
		}
		parent.y = -parent.hPlus;
	}

	protected void arrangeParent(IndiBox indibox, IndiBox parent) {
        // If spouse has a parent
		if (indibox.spouse != null && indibox.spouse.parent != null) {
            parent.x = indibox.width - parent.width - horizontalGap / 2;
			leftArranger.filter(parent);
		} else { // No spouse or no spouse's parent
            if (!parent.hasChildren())
                parent.x = -parent.width / 2;
			filter(parent);
		}
		parent.y = -parent.hPlus;
	}

	protected void arrangeChildren(IndiBox indibox) {
		int currentX = 0;
		if (indibox.getDir() == Direction.PARENT)
		    currentX = indibox.prev.width / 2 - indibox.x + horizontalGap;

		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.y = 1;
			filter(child);
			child.x = currentX + child.wMinus;
			currentX += child.wMinus + child.wPlus + horizontalGap;
		}
		if (indibox.getDir() == Direction.PARENT)
			return;
		int min = indibox.children[0].x - indibox.children[0].wMinus;
		int diff = min + (currentX - horizontalGap - min) / 2;
		diff -= (indibox.wPlus + indibox.wMinus) / 2;
		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.x -= diff;
		}
	}
}
