/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tree.IndiBox;
import tree.TreeFilter;

/**
 * Filters out all spouses (except ancestors).
 * When a spouse is removed, all children from all marriages are connected
 * to the parent that is left.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class NoSpouseFilter implements TreeFilter {

    /**
     * Runs the filter starting from the given individual.
     */
    public void filter(IndiBox indibox) {
        if (indibox == null)
            return;

        if (indibox.getDir() != IndiBox.Direction.PARENT) {
            indibox.children = getChildren(indibox);
            indibox.spouse = null;
        }

        filter(indibox.parent);
        filter(indibox.spouse);
        filter(indibox.nextMarriage);
        if (indibox.hasChildren())
            for (IndiBox i : indibox.children)
                filter(i);
    }

    /**
     * Returns all children of an individual (from all marriages)
     */
    private IndiBox[] getChildren(IndiBox indibox) {
        IndiBox[] children = indibox.children;
        if (indibox.nextMarriage != null)
            children = merge(children, getChildren(indibox.nextMarriage));
        if (indibox.spouse != null && indibox.spouse.nextMarriage != null)
            children = merge(children, getChildren(indibox.spouse.nextMarriage));
        return children;
    }

    /**
     * Merges two arrays into one.
     */
    private IndiBox[] merge(IndiBox[] a, IndiBox[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        List list = new ArrayList(Arrays.asList(a));
        list.addAll(Arrays.asList(b));
        return (IndiBox[])list.toArray(new IndiBox[0]);
    }
}
