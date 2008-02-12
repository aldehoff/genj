/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.filter;

import tree.IndiBox;

public class SameWidthSpouses extends TreeFilterBase {
    protected void preFilter(IndiBox indibox) {
        if (indibox.spouse != null) {
            if (indibox.spouse.width > indibox.width)
                indibox.width = indibox.spouse.width;
            else
                indibox.spouse.width = indibox.width;
        }
    }
}