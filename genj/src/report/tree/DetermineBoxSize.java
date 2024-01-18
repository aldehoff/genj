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

import java.awt.Font;
import java.awt.font.FontRenderContext;

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

    private static final int TEXT_MARGIN = 5;

    /**
     * Font for individual and family details
     */
    private static final Font DETAILS_FONT = new Font("verdana", Font.PLAIN, 10);

    /**
     * Font for the first and last names.
     */
    private static final Font NAME_FONT = new Font("verdana", Font.BOLD, 12);

    private int defaultHeight;
    private int defaultWidth;
    private int maxImageWidth;
    private boolean drawPlaces;
    private boolean drawOccupation;
    private int maxNames;


    public DetermineBoxSize(Registry properties) {
        defaultHeight = properties.get("defaultIndiboxHeight", 0);
        defaultWidth = properties.get("defaultIndiboxWidth", 0);
        maxImageWidth = properties.get("maxImageWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawOccupation = properties.get("drawOccupation", true);
        maxNames = properties.get("maxNames", -1);
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

        Property deathPlace = i.getProperty(PATH_INDIDEATPLAC);
        if (i.getDeathDate() != null) {
            lines++;
            if (drawPlaces && i.getDeathDate().isValid() && deathPlace != null && !deathPlace.toString().equals(""))
                lines++;
        }
        if (drawOccupation && i.getProperty(PATH_INDIOCCU) != null)
            lines++;
        if (lines - DEFAULT_LINES > 0)
            indibox.height += (lines - DEFAULT_LINES) * LINE_HEIGHT;

        // Text data width
        int width = getTextWidth(getFirstNames(i), NAME_FONT);
        if (width + 2*TEXT_MARGIN > indibox.width)
            indibox.width = width + 2*TEXT_MARGIN;
        width = getTextWidth(i.getLastName(), NAME_FONT);
        if (width + 2*TEXT_MARGIN > indibox.width)
            indibox.width = width + 2*TEXT_MARGIN;

        if (i.getBirthDate() != null) {
            width = getTextWidth(i.getBirthDate().toString(), DETAILS_FONT);
            if (width + 13+TEXT_MARGIN > indibox.width)
                indibox.width = width + 13+TEXT_MARGIN;
        }
        if (i.getDeathDate() != null) {
            width = getTextWidth(i.getDeathDate().toString(), DETAILS_FONT);
            if (width + 13+TEXT_MARGIN > indibox.width)
                indibox.width = width + 13+TEXT_MARGIN;
        }

        if (drawPlaces) {
            if (birthPlace != null) {
                width = getTextWidth(birthPlace.toString(), DETAILS_FONT);
                if (width + 13+TEXT_MARGIN > indibox.width)
                    indibox.width = width + 13+TEXT_MARGIN;
            }
            if (deathPlace != null) {
                width = getTextWidth(deathPlace.toString(), DETAILS_FONT);
                if (width + 13+TEXT_MARGIN > indibox.width)
                    indibox.width = width + 13+TEXT_MARGIN;
            }
        }

        if (drawOccupation && i.getProperty(PATH_INDIOCCU) != null) {
            width = getTextWidth(i.getProperty(PATH_INDIOCCU).toString(), DETAILS_FONT);
            if (width + 7+TEXT_MARGIN > indibox.width)
                indibox.width = width + 7+TEXT_MARGIN;
        }

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

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, false, false);

    private int getTextWidth(String text, Font font) {
        return (int)font.getStringBounds(text, FONT_RENDER_CONTEXT).getWidth();
    }

    /**
     * Returns a maximum of <code>maxNames</code> given names of the given
     * individual. If <code>maxNames</code> is 0, this method returns all
     * given names.
     */
    private String getFirstNames(Indi indi) {
        String firstName = indi.getFirstName();
        if (maxNames <= 0)
            return firstName;

        String[] names = firstName.split("  *");
        firstName = "";
        for (int j = 0; j < maxNames && j < names.length; j++)
            firstName += names[j] + " ";
        return firstName.trim();
    }
}
