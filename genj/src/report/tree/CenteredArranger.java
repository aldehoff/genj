/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import tree.IndiBox.Direction;

/**
 * Centers the tree. Uses AlignRightArranger and AlignLeftArranger for
 * parts of the tree.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class CenteredArranger extends AlignLeftArranger {

	private TreeArranger leftArranger;
	private TreeArranger rightArranger;

	public CenteredArranger(int indiboxWidth, int horizontalGap) {
		super(indiboxWidth, horizontalGap);
		leftArranger = new AlignRightArranger(indiboxWidth, horizontalGap);
		rightArranger = new AlignLeftArranger(indiboxWidth, horizontalGap);
	}

	protected void arrangeSpouseParent(IndiBox indibox, IndiBox parent) {
		if (indibox.parent != null) {
			parent.x = horizontalGap / 2;
			rightArranger.arrange(parent);
		} else {
			parent.x = -indiboxWidth / 2;
			arrange(parent);
		}
		parent.y = -parent.hPlus;
	}

	protected void arrangeParent(IndiBox indibox, IndiBox parent) {
		if (indibox.spouse != null && indibox.spouse.parent != null) {
			parent.x = -horizontalGap / 2;
			leftArranger.arrange(parent);
		} else {
            if (!parent.hasChildren())
                parent.x = -indiboxWidth / 2;
			arrange(parent);
		}
		parent.y = -parent.hPlus;
	}

	protected void arrangeChildren(IndiBox indibox) {
		int currentX = 0;
		if (indibox.getDir() == Direction.PARENT)
			currentX = indiboxWidth / 2 + horizontalGap;
		for (int i = 0; i < indibox.children.length; i++) {
			IndiBox child = indibox.children[i];
			child.y = 1;
			arrange(child);
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
