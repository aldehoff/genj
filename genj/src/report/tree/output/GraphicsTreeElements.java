/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertySex;
import genj.gedcom.TagPath;
import genj.report.Options;
import genj.util.Registry;
import genj.util.swing.ImageIcon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import tree.IndiBox;

/**
 * Draws tree elements to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeElements implements TreeElements {

    /**
     * Box background colors.
     */
    private static final Color[] BOX_COLORS = {
        new Color(0xff, 0xff, 0xff), // -5
        new Color(0xff, 0xff, 0xff), // -4
        new Color(0xdd, 0xdd, 0xff), // -3
        new Color(0xff, 0xdd, 0xff), // -2
        new Color(0xff, 0xdd, 0xdd), // -1

        new Color(0xff, 0xff, 0xdd), //  0

        new Color(0xdd, 0xff, 0xdd), //  1
        new Color(0xdd, 0xff, 0xff), //  2
        new Color(0xdd, 0xdd, 0xff), //  3
        new Color(0xff, 0xff, 0xff), //  4
        new Color(0xff, 0xff, 0xff)  //  5
    };

    public static final float STROKE_WIDTH = 2.0f;

    /**
     * Male sex symbol.
     */
    private static final String MALE_SYMBOL = "\u2642";

    /**
     * Female sex symbol.
     */
    private static final String FEMALE_SYMBOL = "\u2640";

    /**
     * Unknown sex symbol.
     */
    private static final String UNKNOWN_SYMBOL = "?";

    /**
     * Stroke for drawing dashed lines.
     */
    private static final Stroke DASHED_STROKE = new BasicStroke(STROKE_WIDTH,
            BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
            new float[] { 3.0f, 6.0f }, 0.0f);

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

    /**
     * Font for drawing the sex symbols.
     */
    private static Font sexSymbolFont = null;
    static {
        // Find a font with the MALE_SYMBOL in it
        String[] candidateFontNames = {"sansserif", "apple symbol", "symbol"};
        for (int i = 0; i < candidateFontNames.length; i++)
        {
            Font candidateFont = new Font(candidateFontNames[i], Font.PLAIN, 10);
            if(candidateFont.canDisplay(MALE_SYMBOL.charAt(0)))
            {
                sexSymbolFont = candidateFont;
                break;
            }
        }
        if (sexSymbolFont == null)
            sexSymbolFont = new Font("SansSerif", Font.PLAIN,10);
    }

    private int famboxWidth;
    private int famboxHeight;

    private int imageWidth;

    /**
     * Whether to draw the sex symbol.
     */
    private boolean drawSexSymbols;

    /**
     * Whether do draw IDs of individuals.
     */
    private boolean drawIndiIds;

    /**
     * Whether to draw IDs of families.
     */
    private boolean drawFamIds;

    private int maxNames;
    private boolean useColors;

    /**
     * The graphics object to paint on.
     */
	private Graphics2D graphics = null;

    /**
     * Constructs the object.
     */
	public GraphicsTreeElements(Graphics2D graphics, Registry properties) {
        this.graphics = graphics;

        famboxWidth = properties.get("famboxWidth", 0);
        famboxHeight = properties.get("famboxHeight", 0);
        drawSexSymbols = properties.get("drawSexSymbols", true);
        drawIndiIds = properties.get("drawIndiIds", false);
        drawFamIds = properties.get("drawFamIds", false);
        maxNames = properties.get("maxNames", -1);
        useColors = properties.get("useColors", true);
        imageWidth = properties.get("imageWidth", 0);
    }

    /**
     * Sets the Graphics2D object to draw on.
     */
    public void setGraphics(Graphics2D graphics) {
        this.graphics = graphics;
    }

    /**
     * Outputs an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
	public void drawIndiBox(IndiBox indibox, int x, int y, int gen) {
        Indi i = indibox.individual;
        int dataWidth = indibox.width;
        PropertyFile file = null;
        if (imageWidth > 0) {
            file = (PropertyFile)i.getProperty(new TagPath("INDI:OBJE:FILE"));
            if (file != null && file.getValueAsIcon() != null)
                dataWidth -= imageWidth;
        }

        Color color = getBoxColor(gen);
        Shape box = new RoundRectangle2D.Double(x, y, indibox.width, indibox.height, 15, 15);
        graphics.setColor(color);
        graphics.fill(box);
        graphics.setColor(Color.BLACK);

        Shape oldClip = graphics.getClip();
        graphics.clip(box);

        graphics.setFont(NAME_FONT);
        centerString(graphics, getFirstNames(i), x + dataWidth/2, y + 14);
        centerString(graphics, i.getLastName(), x + dataWidth/2, y + 26);

        graphics.setFont(DETAILS_FONT);
        if (i.getBirthDate() != null && i.getBirthDate().isValid()) {
            centerString(graphics, Options.getInstance().getBirthSymbol(), x + 7, y + 38);
            graphics.drawString(""+i.getBirthDate(), x + 13, y + 38);
        }
        if (i.getDeathDate() != null) {
            centerString(graphics, Options.getInstance().getDeathSymbol(), x + 7, y + 48);
            graphics.drawString(""+i.getDeathDate(), x + 13, y + 48);
        }

        if (drawSexSymbols) {
            int symbolX = x + dataWidth  - 14;
            int symbolY = y + indibox.height - 5;
            graphics.setFont(sexSymbolFont);
            graphics.drawString(getSexSymbol(i.getSex()), symbolX, symbolY);
        }

        if (drawIndiIds) {
            graphics.setFont(ID_FONT);
            graphics.drawString(i.getId(), x + 8, y + indibox.height - 4);
        }

        if (imageWidth > 0 && file != null) {
            ImageIcon icon = file.getValueAsIcon();
            if (icon != null)
                graphics.drawImage(icon.getImage(), x + dataWidth, y,
                        imageWidth, indibox.height, null);
        }

        graphics.setClip(oldClip);
        graphics.draw(box);
	}

    /**
     * Outputs a family box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    public void drawFamBox(Fam f, int x, int y, int gen) {
        Color color = getBoxColor(gen);
        graphics.setColor(color);
        graphics.fillRoundRect(x, y, famboxWidth, famboxHeight, 5, 5);
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(x, y, famboxWidth, famboxHeight, 5, 5);

        graphics.setFont(DETAILS_FONT);
        if (f.getMarriageDate() != null)
            graphics.drawString(Options.getInstance().getMarriageSymbol() + " " +
                    f.getMarriageDate(), x + 4, y + 12);

        if (drawFamIds) {
            graphics.setFont(ID_FONT);
            graphics.drawString(f.getId(), x + 8, y + famboxHeight - 4);
        }
    }

    /**
     * Outputs a line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
	}

    /**
     * Outputs a dashed line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
    public void drawDashedLine(int x1, int y1, int x2, int y2) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(DASHED_STROKE);
        graphics.drawLine(x1, y1, x2, y2);
        graphics.setStroke(oldStroke);
    }

    /**
     * Initializes the graphics.
     */
    public void header(int width, int height) {
        graphics.setStroke(new BasicStroke(STROKE_WIDTH));
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, width, height);
	}

    /**
     * Does nothing.
     */
    public void footer() {
	}

    /**
     * Outputs a string centered.
     */
    private void centerString(Graphics2D graphics, String text, int x, int y) {
        Rectangle2D rect = graphics.getFont().getStringBounds(text,
                graphics.getFontRenderContext());
        int width = (int)rect.getWidth();
        graphics.drawString(text, x - width/2, y);
    }

    private String getSexSymbol(int sex) {
        if (sex == PropertySex.MALE)
            return MALE_SYMBOL;
        if (sex == PropertySex.FEMALE)
            return FEMALE_SYMBOL;
        return UNKNOWN_SYMBOL;
    }

    /**
     * Returns the box color for the given generation.
     */
    private Color getBoxColor(int gen) {
        if (!useColors)
            return Color.WHITE;
        if (gen + 5 < BOX_COLORS.length && gen + 5 >= 0)
            return BOX_COLORS[gen + 5];
        return BOX_COLORS[0];
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
