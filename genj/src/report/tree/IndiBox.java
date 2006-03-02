/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Fam;
import genj.gedcom.Indi;

/**
 * Class representing a single individual box and its links to adjecent boxes.
 *
 * @author Przemek Więch <pwiech@losthive.org>
 */
class IndiBox {

    /**
     * Direction of the previous connected IndiBox.
     */
    static class Direction {
        static int NONE = 0;
        static int SPOUSE = 1;
        static int PARENT = 2;
        static int CHILD = 3;
        static int NEXTMARRIAGE = 4;
    }
    /* This will wait for Java 5.0 compatibility
    static enum Direction {
        NONE, SPOUSE, PARENT, CHILD, NEXTMARRIAGE
    };
    */

    /**
     * Previous box in the tree.
     */
    IndiBox prev = null;

    /**
     * Spouse's box.
     */
    IndiBox spouse = null;

    /**
     * Parent's box.
     */
    IndiBox parent = null;

    /**
     * Children's boxes.
     */
    IndiBox[] children = null;

    /**
     * Box of an individual in the next marriage of one of this individual or
     * his/her spouse.
     */
    IndiBox nextMarriage = null;

    /**
     * X coordinate relative to the position of the previous IndiBox in pixels.
     */
    int x = 0;

    /**
     * Y coordinate relative to the position of the previous IndiBox in
     * generation lines.
     */
    int y = 0;

    // Space taken by all child-nodes of this IndiBox.
    int wPlus = 0;

    int wMinus = 0;

    int hPlus = 1;

    int hMinus = 0;

    /**
     * The individual connected with this box.
     */
    Indi individual;

    /**
     * Family where spouse.
     */
    Fam family;

    /**
     * Constructs the object.
     * @param individual  individual connected with this box
     */
    IndiBox(Indi individual) {
        this.individual = individual;
    }

    /**
     * Constructs the object
     * @param individual  individual connected with this box
     * @param prev
     */
    IndiBox(Indi individual, IndiBox prev) {
        this.individual = individual;
        this.prev = prev;
    }

    /**
     * Returns the direction of the previous connected IndiBox.
     */
    int getDir() {
        if (prev == null)
            return Direction.NONE;
        if (this == prev.spouse)
            return Direction.SPOUSE;
        if (this == prev.parent)
            return Direction.PARENT;
        if (this == prev.nextMarriage)
            return Direction.NEXTMARRIAGE;
        return Direction.CHILD;
    }

    /**
     * @return true if this IndiBox has child boxes connected.
     */
    boolean hasChildren() {
        return (children != null && children.length > 0);
    }
}