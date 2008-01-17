/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Fam;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Registry;

import java.awt.Font;
import java.awt.font.FontRenderContext;

/**
 * Determines the width and height of individual boxes.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class DetermineFamboxSize extends TreeFilterBase {

    private static final int DEFAULT_LINES = 1;
    private static final int LINE_HEIGHT = 10;
    private static final TagPath PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC");
    private static final TagPath PATH_FAMDIVPLAC = new TagPath("FAM:DIV:PLAC");

    private static final int TEXT_MARGIN = 5;

    /**
     * Font for individual and family details
     */
    private static final Font DETAILS_FONT = new Font("verdana", Font.PLAIN, 10);

    private int defaultHeight;
    private int defaultWidth;
    private boolean drawPlaces;
    private boolean drawDates;
    private boolean drawDivorce;

    public DetermineFamboxSize(Registry properties) {
        defaultHeight = properties.get("defaultFamboxHeight", 0);
        defaultWidth = properties.get("defaultFamboxWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawDates = properties.get("drawDates", true);
        drawDivorce = properties.get("drawDivorce", true);
    }

    public void preFilter(IndiBox indibox) {
        FamBox fambox = indibox.family;
        if (fambox == null)
            return;

        Fam f = fambox.family;
        fambox.width = defaultWidth;
        fambox.height = defaultHeight;

        // Number of lines
        int lines = 0;
        Property marriagePlace = f.getProperty(PATH_FAMMARRPLAC);
        if (f.getMarriageDate() != null) {
            lines++;
            if (drawDates && drawPlaces && f.getMarriageDate().isValid() && marriagePlace != null && !marriagePlace.toString().equals(""))
                lines++;
        }
        Property divorcePlace = f.getProperty(PATH_FAMDIVPLAC);
        if (drawDivorce && f.getDivorceDate() != null) {
            lines++;
            if (drawDates && drawPlaces && f.getDivorceDate().isValid() && divorcePlace != null && !divorcePlace.toString().equals(""))
                lines++;
        }

        if (lines - DEFAULT_LINES > 0)
            fambox.height += (lines - DEFAULT_LINES) * LINE_HEIGHT;

        // Text data width
        if (f.getMarriageDate() != null) {
            int width = getTextWidth(f.getMarriageDate().toString(), DETAILS_FONT);
            if (width + 25+TEXT_MARGIN > fambox.width)
                fambox.width = width + 25+TEXT_MARGIN;
        }
        if (drawDivorce && f.getDivorceDate() != null) {
            int width = getTextWidth(f.getDivorceDate().toString(), DETAILS_FONT);
            if (width + 25+TEXT_MARGIN > fambox.width)
                fambox.width = width + 25+TEXT_MARGIN;
        }

        if (drawPlaces) {
            if (marriagePlace != null) {
                int width = getTextWidth(marriagePlace.toString(), DETAILS_FONT);
                if (width + 25+TEXT_MARGIN > fambox.width)
                    fambox.width = width + 25+TEXT_MARGIN;
            }
            if (drawDivorce && divorcePlace != null) {
                int width = getTextWidth(divorcePlace.toString(), DETAILS_FONT);
                if (width + 25+TEXT_MARGIN > fambox.width)
                    fambox.width = width + 25+TEXT_MARGIN;
            }
        }
    }

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, false, false);

    private int getTextWidth(String text, Font font) {
        return (int)font.getStringBounds(text, FONT_RENDER_CONTEXT).getWidth();
    }
}
