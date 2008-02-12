/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.filter;

import tree.IndiBox;

public class Transpose extends TreeFilterBase {
    protected void preFilter(IndiBox indibox) {
        int tmp;

        tmp = indibox.x;
        indibox.x = indibox.y;
        indibox.y = tmp;

        tmp = indibox.width;
        indibox.width = indibox.height;
        indibox.height = tmp;

        tmp = indibox.hPlus;
        indibox.hPlus = indibox.wPlus;
        indibox.wPlus = tmp;

        tmp = indibox.hMinus;
        indibox.hMinus = indibox.wMinus;
        indibox.wMinus = tmp;

        if (indibox.family != null) {
            tmp = indibox.family.width;
            indibox.family.width = indibox.family.height;
            indibox.family.height = tmp;
        }
    }
}