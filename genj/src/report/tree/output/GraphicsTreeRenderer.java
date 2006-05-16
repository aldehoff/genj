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
import genj.gedcom.PropertySex;
import genj.report.Options;
import genj.util.Registry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import tree.IndiBox;
import tree.graphics.GraphicsRenderer;

/**
 * Outputs the generated tree to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeRenderer extends AbstractTreeRenderer implements GraphicsRenderer {

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

    /**
     * The graphics object to paint on.
     */
	private Graphics2D graphics = null;

    /**
     * Constructs the object.
     * @param out PrintWriter to write to
     * @param indiboxWidth  width of the individual box in pixels
     * @param indiboxHeight height of the individual box in pixels
     * @param verticalGap minimal vertical gap between individual boxes
     */
	public GraphicsTreeRenderer(IndiBox indibox, Registry properties) {
        super(indibox, properties);
        drawSexSymbols = properties.get("drawSexSymbols", true);
        drawIndiIds = properties.get("drawIndiIds", false);
        drawFamIds = properties.get("drawFamIds", false);
    }

    /**
     * Renders the family tree to the given Graphics2D object.
     */
    public void render(Graphics2D graphics) {
        this.graphics = graphics;
        graphics.setStroke(new BasicStroke(STROKE_WIDTH));
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, getImageWidth(), getImageHeight());
        render();
    }

    /**
     * Outputs an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
	protected void drawIndiBox(Indi i, int x, int y, int gen) {
        Color color = getBoxColor(gen);
        graphics.setColor(color);
        graphics.fillRoundRect(x, y, indiboxWidth, indiboxHeight, 15, 15);
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(x, y, indiboxWidth, indiboxHeight, 15, 15);

        graphics.setFont(NAME_FONT);
        centerString(graphics, getFirstNames(i), x + indiboxWidth/2, y + 14);
        centerString(graphics, i.getLastName(), x + indiboxWidth/2, y + 26);

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
            int symbolX = x + indiboxWidth  - 14;
            int symbolY = y + indiboxHeight - 5;
            graphics.setFont(sexSymbolFont);
            graphics.drawString(getSexSymbol(i.getSex()), symbolX, symbolY);
        }

        if (drawIndiIds) {
            graphics.setFont(ID_FONT);
            graphics.drawString(i.getId(), x + 8, y + indiboxHeight - 4);
        }
	}

    /**
     * Outputs a family box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    protected void drawFamBox(Fam f, int x, int y, int gen) {
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
	protected void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
	}

    /**
     * Outputs a dashed line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	protected void drawDashedLine(int x1, int y1, int x2, int y2) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(DASHED_STROKE);
        graphics.drawLine(x1, y1, x2, y2);
        graphics.setStroke(oldStroke);
    }

    /**
     * Does nothing.
     */
	protected void header() {
	}

    /**
     * Does nothing.
     */
	protected void footer() {
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
}
