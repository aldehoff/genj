/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.render;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.report.Options;
import genj.util.Registry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import tree.IndiBox;

/**
 * Outputs the generated tree to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeRenderer extends AbstractTreeRenderer implements GraphicsRenderer {

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

        graphics.setFont(new Font("verdana", Font.BOLD, 12));
        String firstNames = getFirstNames(i);
        String lastName = i.getLastName();
        Rectangle2D rect = graphics.getFont().getStringBounds(firstNames,
                graphics.getFontRenderContext());
        int firstW = (int)rect.getWidth();
        rect = graphics.getFont().getStringBounds(lastName,
                graphics.getFontRenderContext());
        int lastW = (int)rect.getWidth();

        graphics.drawString(firstNames, x + (indiboxWidth - firstW)/2, y + 14);
        graphics.drawString(lastName, x + (indiboxWidth - lastW)/2, y + 26);

        graphics.setFont(new Font("verdana", Font.PLAIN, 10));
        if (i.getBirthDate() != null && i.getBirthDate().isValid())
            graphics.drawString(Options.getInstance().getBirthSymbol() + " " +
                    i.getBirthDate(), x + 4, y + 38);
        if (i.getDeathDate() != null)
            graphics.drawString(Options.getInstance().getDeathSymbol() + " " +
                    i.getDeathDate(), x + 4, y + 48);
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

        graphics.setFont(new Font("verdana", Font.PLAIN, 10));
        if (f.getMarriageDate() != null)
            graphics.drawString(Options.getInstance().getBirthSymbol() + " " +
                    f.getMarriageDate(), x + 4, y + 12);
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
        graphics.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                new float[] { 3.0f, 3.0f }, 0.0f));
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
}
