/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.report.Options;

import java.io.PrintWriter;

/**
 * Outputs the generated tree to a SVG file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class SvgTreeRenderer extends AbstractTreeRenderer {

	private PrintWriter out;

    /**
     * Constructs the object.
     * @param out PrintWriter to write to
     * @param indiboxWidth  width of the individual box in pixels
     * @param indiboxHeight height of the individual box in pixels
     * @param verticalGap minimal vertical gap between individual boxes
     */
	public SvgTreeRenderer(PrintWriter out, int maxNames, int indiboxWidth,
            int indiboxHeight, int verticalGap, int famboxWidth, int famboxHeight,
            boolean displayFambox) {
        super(maxNames, indiboxWidth, indiboxHeight, verticalGap, famboxWidth,
            famboxHeight, displayFambox);
		this.out = out;
	}

    /**
     * Outputs a an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
	protected void drawIndiBox(Indi i, int x, int y, int gen) {
        String color = getBoxColor(gen);
		String sex = "";
		if (i.getSex() == PropertySex.MALE)
			sex = "Male";
		else if (i.getSex() == PropertySex.FEMALE)
			sex = "Female";
        String firstName = getFirstNames(i);

		out.println("    <g transform=\"translate(" + x + ", " + y + ")\">");
		out.println("      <use xlink:href=\"#PersonBox" + sex + "\" fill=\"" + color + "\"/>");
		out.println("      <text x=\"" + indiboxWidth / 2 + "\" y=\"14\" font-family=\"Verdana\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">");
		out.println("        " + firstName);
		out.println("      </text>");
		out.println("      <text x=\"" + indiboxWidth / 2 + "\" y=\"26\" font-family=\"Verdana\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">");
		out.println("        " + i.getLastName());
		out.println("      </text>");
		if (i.getBirthDate() != null && i.getBirthDate().isValid()) {
			out.println("      <text x=\"4\" y=\"38\" font-family=\"Verdana\" font-size=\"10\">");
			out.println("        " + Options.getInstance().getBirthSymbol() + " " + i.getBirthDate());
			out.println("      </text>");
		}
		if (i.getDeathDate() != null) {
			out.println("      <text x=\"4\" y=\"48\" font-family=\"Verdana\" font-size=\"10\">");
			out.println("        " + Options.getInstance().getDeathSymbol() + " " + i.getDeathDate());
			out.println("      </text>");
		}
		out.println("      <text x=\"" + (indiboxWidth - 4) + "\" y=\"" + (indiboxHeight - 2) + "\" font-family=\"Verdana\" font-size=\"8\" text-anchor=\"end\">");
		out.println("        " + i.getId());
		out.println("      </text>");
		out.println("    </g>");
	}

    /**
     * Outputs a an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
    protected void drawFamBox(Fam f, int x, int y, int gen) {
        String color = getBoxColor(gen);

        out.println("    <g transform=\"translate(" + x + ", " + y + ")\">");
        out.println("      <use xlink:href=\"#FamilyBox\" fill=\"" + color + "\"/>");
        if (f.getMarriageDate() != null && f.getMarriageDate().isValid()) {
            out.println("      <text x=\"4\" y=\"12\" font-family=\"Verdana\" font-size=\"10\">");
            out.println("        " + Options.getInstance().getMarriageSymbol() + " " + f.getMarriageDate());
            out.println("      </text>");
        }
        out.println("      <text x=\"" + (famboxWidth - 4) + "\" y=\"" + (famboxHeight - 2) + "\" font-family=\"Verdana\" font-size=\"8\" text-anchor=\"end\">");
        out.println("        " + f.getId());
        out.println("      </text>");
        out.println("    </g>");
    }

    /**
     * Outputs a line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	protected void drawLine(int x1, int y1, int x2, int y2) {
		out.println("    <line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" stroke=\"black\"/>");
	}

    /**
     * Outputs a dashed line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	protected void drawDashedLine(int x1, int y1, int x2, int y2) {
		out.println("    <line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" stroke=\"black\" style=\"stroke-dasharray:3 3\"/>");
	}

    /**
     * Outputs the SVG header.
     * @param w family tree width in pixels
     * @param h family tree height in generation lines
     */
	protected void header() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
		out.println(" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-basic.dtd\">");
		out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        out.println("     width=\"" + getImageWidth() + "\"");
        out.println("     height=\"" + getImageHeight() + "\">");
		out.println("  <defs>");
		out.println("    <rect id=\"PersonBox\" rx=\"25\" ry=\"25\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"black\"/>");
		out.println("    <rect id=\"PersonBoxMale\" rx=\"5\" ry=\"5\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"#000044\"/>");
		out.println("    <rect id=\"PersonBoxFemale\" rx=\"15\" ry=\"15\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"#440000\"/>");
        out.println("    <rect id=\"FamilyBox\" rx=\"5\" ry=\"5\" width=\"" + famboxWidth + "\" height=\"" + famboxHeight + "\" stroke=\"#000044\"/>");
		out.println("  </defs>");
	}

    /**
     * Outputs the SVG footer.
     */
	protected void footer() {
		out.println("</svg>");
	}
}
