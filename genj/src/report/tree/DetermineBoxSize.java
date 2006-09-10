/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.TagPath;
import genj.util.Registry;
import genj.util.swing.ImageIcon;

/**
 * Determines the width and height of individual boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class DetermineBoxSize extends TreeFilterBase {

    private static final int DEFAULT_LINES = 2;
    private static final int LINE_HEIGHT = 10;
    private static final TagPath PATH_INDIBIRTPLAC = new TagPath("INDI:BIRT:PLAC");
    private static final TagPath PATH_INDIDEATPLAC = new TagPath("INDI:DEAT:PLAC");
    private static final TagPath PATH_INDIOCCU = new TagPath("INDI:OCCU");

    private int defaultHeight;
    private int defaultWidth;
    private int maxImageWidth;
    private boolean drawPlaces;
    private boolean drawOccupation;

    public DetermineBoxSize(Registry properties) {
        defaultHeight = properties.get("defaultIndiboxHeight", 0);
        defaultWidth = properties.get("defaultIndiboxWidth", 0);
        maxImageWidth = properties.get("maxImageWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawOccupation = properties.get("drawOccupation", true);
    }

    public void preFilter(IndiBox indibox) {
        indibox.height = defaultHeight;
        indibox.width = defaultWidth;

        Indi i = indibox.individual;

        // Number of lines
        int lines = 0;
        if (i.getBirthDate() != null && i.getBirthDate().isValid())
            lines++;
        Property birthPlace = i.getProperty(PATH_INDIBIRTPLAC);
        if (drawPlaces && birthPlace != null && !birthPlace.toString().equals(""))
            lines++;

        if (i.getDeathDate() != null) {
            lines++;
            Property deathPlace = i.getProperty(PATH_INDIDEATPLAC);
            if (drawPlaces && i.getDeathDate().isValid() && deathPlace != null && !deathPlace.toString().equals(""))
                lines++;
        }
        if (drawOccupation && i.getProperty(PATH_INDIOCCU) != null)
            lines++;
        if (lines - DEFAULT_LINES > 0)
            indibox.height += (lines - DEFAULT_LINES) * LINE_HEIGHT;


        // Image
        if(maxImageWidth > 0)
        {
            PropertyFile file = (PropertyFile)i.getProperty(new TagPath("INDI:OBJE:FILE"));
            if(file != null)
            {
                ImageIcon icon = file.getValueAsIcon();
                if(icon != null) {
                    int newWidth = icon.getIconWidth() * defaultHeight / icon.getIconHeight();
                    if (newWidth < maxImageWidth)
                        indibox.width += newWidth;
                    else
                        indibox.width += maxImageWidth;
                }
            }
        }
    }
}
