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
public class DetermineIndiboxSize extends TreeFilterBase {

    private static final int DEFAULT_LINES = 2;
    private static final int NAME_LINE_HEIGHT = 12;
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

    /**
     * Font for individual and family ID.
     */
    private static final Font ID_FONT = new Font("verdana", Font.ITALIC, 10);

    private int defaultHeight;
    private int defaultWidth;
    private int maxImageWidth;
    private boolean drawPlaces;
    private boolean drawDates;
    private boolean drawOccupation;
    private int maxNames;
    private int maxNamesPerLine;
    private boolean drawSexSymbols;
    private boolean drawIndiIds;


    public DetermineIndiboxSize(Registry properties) {
        defaultHeight = properties.get("defaultIndiboxHeight", 0);
        defaultWidth = properties.get("defaultIndiboxWidth", 0);
        maxImageWidth = properties.get("maxImageWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawDates = properties.get("drawDates", true);
        drawOccupation = properties.get("drawOccupation", true);
        maxNames = properties.get("maxNames", -1);
        maxNamesPerLine = properties.get("maxNamesPerLine", 2);
        drawSexSymbols = properties.get("drawSexSymbols", true);
        drawIndiIds = properties.get("drawIndiIds", false);
    }

    public void preFilter(IndiBox indibox) {
        indibox.height = defaultHeight;
        indibox.width = defaultWidth;

        Indi i = indibox.individual;

        // Number of lines
        int lines = 0;
        if (drawDates && i.getBirthDate() != null && i.getBirthDate().isValid())
            lines++;
        Property birthPlace = i.getProperty(PATH_INDIBIRTPLAC);
        if (drawPlaces && birthPlace != null && !birthPlace.toString().equals(""))
            lines++;

        Property deathPlace = i.getProperty(PATH_INDIDEATPLAC);
        if (deathPlace != null && deathPlace.toString().equals(""))
            deathPlace = null;
        if (i.getDeathDate() != null || deathPlace != null) {
            lines++;
            if (drawDates && drawPlaces && i.getDeathDate() != null && i.getDeathDate().isValid() && deathPlace != null)
                lines++;
        }
        if (drawOccupation && i.getProperty(PATH_INDIOCCU) != null)
            lines++;
        if (lines - DEFAULT_LINES > 0)
            indibox.height += (lines - DEFAULT_LINES) * LINE_HEIGHT;

        // height and width computations for first names
        int width = 0;
        String[] firstNames = getFirstNames(i);
        for (int j = 0; j < firstNames.length; j++) {
            int w2 = getTextWidth(firstNames[j], NAME_FONT);
            width = width>w2?width:w2;
        }

        // Additional first names
        indibox.height += (firstNames.length - 1) * NAME_LINE_HEIGHT;

        // Text data width
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

        if (drawIndiIds) {
        	width = getTextWidth(i.getId(), ID_FONT);
        	if (drawSexSymbols)
        		width += 14;
            if (width + 8+TEXT_MARGIN > indibox.width)
                indibox.width = width + 8+TEXT_MARGIN;
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
     * given names. The names are split into lines, where the maximum number
     * of names in one line is specified by <code>maxNamesPerLine</code>.
     * if <code>maxNamesPerLine</code> is 0, only one line is returned.
     * @return array of lines to display
     */
    private String[] getFirstNames(Indi indi) {
        String firstName = indi.getFirstName();
        if (maxNames <= 0 && maxNamesPerLine <= 0)
            return new String[] {firstName};
        if (firstName.trim().equals(""))
            return new String[] {""};

        String[] names = firstName.split("  *");
        int namesCount = names.length;
        if (maxNames > 0 && maxNames < namesCount)
            namesCount = maxNames;
        int linesCount = 1;
        if (maxNamesPerLine > 0)
            linesCount = (namesCount - 1) / maxNamesPerLine + 1;
        String[] lines = new String[linesCount];
        int currentName = 0;
        for (int j = 0; j < linesCount; j++) {
            StringBuffer sb = new StringBuffer();
            for (int k = 0; k < maxNamesPerLine; k++) {
                int n = j * maxNamesPerLine + k;
                if (n >= namesCount)
                    break;
                sb.append(names[n]).append(" ");
            }
            lines[j] = sb.substring(0, sb.length() - 1);
        }

        return lines;
    }
}
