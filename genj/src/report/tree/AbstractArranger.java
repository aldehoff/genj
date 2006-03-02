/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

/**
 * Abstract class for arranger classes.
 *
 * @author Przemek Więch <pwiech@losthive.org>
 */
public abstract class AbstractArranger implements TreeArranger {

    /**
     * Width of the individual box.
     */
	protected int indiboxWidth;

    /**
     * Minimal horizontal gap between individual boxes.
     */
	protected int horizontalGap;

    /**
     * Constructs the object.
     *
     * @param indiboxWidth  width of the individual box
     * @param horizontalGap minimal horizontal gap between individual boxes
     */
	public AbstractArranger(int indiboxWidth, int horizontalGap) {
		this.indiboxWidth = indiboxWidth;
		this.horizontalGap = horizontalGap;
	}

    /**
     * Places the spouse box relative to the root individual box.
     * @param indibox root individual box
     * @param spouse  spouse box
     */
	protected abstract void arrangeSpouse(IndiBox indibox, IndiBox spouse);

    /**
     * Places child boxes relative to the root individual box.
     * @param indibox root individual box
     */
	protected abstract void arrangeChildren(IndiBox indibox);

    /**
     * Arranges the next marriage boxes.
     * @param indibox root individual
     * @param next    individual in next marriage
     */
	protected abstract void arrangeNextMarriages(IndiBox indibox, IndiBox next);

    /**
     * Places the spouse's parent box relative to the spouse box.
     * @param indibox spouse box
     * @param parent  spouse's parent box
     */
	protected abstract void arrangeSpouseParent(IndiBox indibox, IndiBox parent);

    /**
     * Places the parent box relative to the given individual.
     * @param indibox  root individual
     * @param parent   root individual's parent box
     */
	protected abstract void arrangeParent(IndiBox indibox, IndiBox parent);

    /**
     * Arranges the family tree starting from the selected individual.
     * @param indibox  root individual
     */
	public void arrange(IndiBox indibox) {

		indibox.wPlus = indiboxWidth;

		// 0. Arrange spouse
		if (indibox.spouse != null) {
			indibox.spouse.wPlus = indiboxWidth;
			arrangeSpouse(indibox, indibox.spouse);
			if (indibox.spouse.x > 0)
				indibox.wPlus = indibox.spouse.x + indiboxWidth;
			else
				indibox.wMinus = -indibox.spouse.x;
		}

		// 1. Arrange children
		if (indibox.hasChildren()) {
			arrangeChildren(indibox);

			for (int i = 0; i < indibox.children.length; i++) {
				IndiBox child = indibox.children[i];
				if (child.y + child.hPlus > indibox.hPlus)
					indibox.hPlus = child.y + child.hPlus;
				if (child.x + child.wPlus > indibox.wPlus)
					indibox.wPlus = child.x + child.wPlus;
				if (-child.x + child.wMinus > indibox.wMinus)
					indibox.wMinus = -child.x + child.wMinus;
			}
		}

		// 2. Arrange next marriages
		if (indibox.spouse != null && indibox.spouse.nextMarriage != null) {
			IndiBox next = indibox.spouse.nextMarriage;

			arrangeNextMarriages(indibox, next);

			if (next.hPlus > indibox.hPlus)
				indibox.hPlus = next.hPlus;
			indibox.spouse.wMinus = next.wMinus - next.x;
			indibox.spouse.wPlus = next.wPlus + next.x;
			if (indibox.spouse.wMinus - indibox.spouse.x > indibox.wMinus)
				indibox.wMinus = indibox.spouse.wMinus - indibox.spouse.x;
			if (indibox.spouse.wPlus + indibox.spouse.x > indibox.wPlus)
				indibox.wPlus = indibox.spouse.wPlus + indibox.spouse.x;
		}

		// 3. Arrange parents
		if (indibox.spouse != null && indibox.spouse.parent != null) {
			IndiBox parent = indibox.spouse.parent;

			arrangeSpouseParent(indibox, parent);

			indibox.spouse.hMinus = parent.hPlus + parent.hMinus;
			indibox.hMinus = indibox.spouse.hMinus;
			if (parent.wPlus > indibox.spouse.wPlus)
				indibox.spouse.wPlus = parent.wPlus;
			if (indibox.spouse.wPlus + indibox.spouse.x > indibox.wPlus)
				indibox.wPlus = indibox.spouse.wPlus + indibox.spouse.x;
			if (parent.wMinus > indibox.spouse.wMinus)
				indibox.spouse.wMinus = parent.wMinus;
			if (indibox.spouse.wMinus - indibox.spouse.x > indibox.wMinus)
				indibox.wMinus = indibox.spouse.wMinus - indibox.spouse.x;
		}
		if (indibox.parent != null) {
			IndiBox parent = indibox.parent;

			arrangeParent(indibox, parent);

			if (-parent.y + parent.hMinus > indibox.hMinus)
				indibox.hMinus = -parent.y + parent.hMinus;
			if (parent.wPlus + parent.x > indibox.wPlus)
				indibox.wPlus = parent.wPlus + parent.x;
			if (parent.wMinus - parent.x > indibox.wMinus)
				indibox.wMinus = parent.wMinus - parent.x;
		}
	}
}
