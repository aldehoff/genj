/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;

/**
 * Determines the width and height of individual boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class DetermineBoxSize implements TreeFilter {

    int defaultHeight;
    int defaultWidth;
    int imageWidth;

    public DetermineBoxSize(int defaultHeight, int defaultWidth, int imageWidth) {
        this.defaultHeight = defaultHeight;
        this.defaultWidth = defaultWidth;
        this.imageWidth = imageWidth;
    }

    public void filter(IndiBox indibox) {
        if (indibox == null)
            return;

        determineSize(indibox);

        filter(indibox.parent);
        filter(indibox.spouse);
        filter(indibox.nextMarriage);
        if (indibox.hasChildren())
            for (int i = 0; i < indibox.children.length; i++)
                filter(indibox.children[i]);
    }

    public void determineSize(IndiBox indibox) {
        indibox.height = defaultHeight;
        indibox.width = defaultWidth;

        // Image
        if (imageWidth > 0) {
            PropertyFile file = (PropertyFile)indibox.individual.getProperty(new TagPath("INDI:OBJE:FILE"));
            if (file != null && file.getValueAsIcon() != null)
                indibox.width += imageWidth;
        }
    }
}
