/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import java.io.PrintWriter;
import java.util.SortedSet;
import java.util.TreeSet;

import tree.IndiBox.Direction;

import genj.gedcom.Indi;
import genj.gedcom.PropertySex;
import genj.report.Options;

/**
 * Outputs the generated tree to a SVG file.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class SvgTreeRenderer implements TreeRenderer {

    /**
     * Size of left and right image margin.
     */
	private final int VERTICAL_MARGIN = 10;

    /**
     * Size of top and bottom image margin.
     */
	private final int HORIZONTAL_MARGIN = 10;

    /**
     * Box background colors.
     */
	private final String[] BOX_COLORS = { "#FFFFFF", "#FFFFFF", "#DDDDFF", "#FFDDFF", "#FFDDDD",
	                                      "#FFFFDD",
	                                      "#DDFFDD", "#DDFFFF", "#DDDDFF", "#FFFFFF", "#FFFFFF" };

	private PrintWriter out;

    private int maxNames;
	private int indiboxWidth;
	private int indiboxHeight;
	private int verticalGap;
	private int verticalUnit;

    /**
     * Constructs the object.
     * @param out PrintWriter to write to
     * @param indiboxWidth  width of the individual box in pixels
     * @param indiboxHeight height of the individual box in pixels
     * @param verticalGap minimal vertical gap between individual boxes
     */
	public SvgTreeRenderer(PrintWriter out, int maxNames, int indiboxWidth, int indiboxHeight, int verticalGap) {
		this.out = out;
        this.maxNames = maxNames;
		this.indiboxWidth = indiboxWidth;
		this.indiboxHeight = indiboxHeight;
		this.verticalGap = verticalGap;
		verticalUnit = indiboxHeight + verticalGap;
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     */
	public void render(IndiBox indibox) {
		printSvgStart(indibox.wMinus + indibox.wPlus, indibox.hMinus + indibox.hPlus);
		printTree(indibox, indibox.wMinus, -indibox.hPlus + 1, 0);
		printSvgEnd();
	}

    /**
     * Outputs the family tree starting from the given IndiBox.
     * @param indibox root individual box
     * @param baseX  x coordinate
     * @param baseY  y coordinate
     * @param gen  generation number
     */
	private void printTree(IndiBox indibox, int baseX, int baseY, int gen) {
		baseX += indibox.x;
		baseY += indibox.y;
		printIndiBox(indibox.individual, baseX, baseY * verticalUnit, gen);

		// Spouse
		if (indibox.spouse != null)
			printTree(indibox.spouse, baseX, baseY, gen);

		// Parent
		if (indibox.parent != null) {
			printTree(indibox.parent, baseX, baseY, gen - 1);
			printLine(baseX + indiboxWidth / 2, baseY * verticalUnit,
			               baseX + indiboxWidth / 2, (baseY + indibox.parent.y + 1) * verticalUnit
			               - verticalGap / 2);
		}

		// Children
		if (indibox.hasChildren())
			for (int i = 0; i < indibox.children.length; i++) {
				printTree(indibox.children[i], baseX, baseY, gen + 1);
				printLine(baseX + indibox.children[i].x + indiboxWidth / 2,
						(baseY + indibox.children[i].y) * verticalUnit,
						baseX + indibox.children[i].x + indiboxWidth / 2,
						(baseY + 1) * verticalUnit - verticalGap / 2);
			}

		// Lines
		if (indibox.hasChildren() || indibox.getDir() == Direction.PARENT) {
			int midX = baseX + indiboxWidth / 2;
			int midY = baseY * verticalUnit + indiboxHeight;
			if (indibox.spouse != null) {
				midX = baseX + (indibox.spouse.x + indiboxWidth) / 2;
				midY -= indiboxHeight / 2;
			}
			printLine(midX, midY, midX, (baseY + 1) * verticalUnit - verticalGap / 2);

			SortedSet xSet = new TreeSet();
			xSet.add(new Integer(midX));
			if (indibox.getDir() == Direction.PARENT)
				xSet.add(new Integer(baseX - indibox.x + indiboxWidth / 2));
			if (indibox.hasChildren())
				for (int i = 0; i < indibox.children.length; i++)
					xSet.add(new Integer(baseX + indibox.children[i].x + indiboxWidth / 2));
			int x1 = ((Integer)xSet.first()).intValue();
			int x2 = ((Integer)xSet.last()).intValue();

			printLine(x1, (baseY + 1) * verticalUnit - verticalGap / 2,
					x2, (baseY + 1) * verticalUnit - verticalGap / 2);
		}

		// Next marriage
		if (indibox.nextMarriage != null) {
			printTree(indibox.nextMarriage, baseX, baseY, gen);
			if (indibox.nextMarriage.x > 0)
				printDashedLine(baseX + indiboxWidth, baseY * verticalUnit + indiboxHeight / 2,
						baseX + indibox.nextMarriage.x, baseY * verticalUnit + indiboxHeight / 2);
			else
				printDashedLine(baseX, baseY * verticalUnit + indiboxHeight / 2,
						baseX + indibox.nextMarriage.x + indiboxWidth, baseY * verticalUnit + indiboxHeight / 2);
		}
	}

    /**
     * Outputs a an individual box.
     * @param i  individual
     * @param x  x coordinate
     * @param y  y coordinate
     * @param gen generation number
     */
	private void printIndiBox(Indi i, int x, int y, int gen) {
        String color = BOX_COLORS[0];
        if (gen + 5 < BOX_COLORS.length && gen + 5 >= 0)
            color = BOX_COLORS[gen + 5];
		String sex = "";
		if (i.getSex() == PropertySex.MALE)
			sex = "Male";
		else if (i.getSex() == PropertySex.FEMALE)
			sex = "Female";
        String firstName = getFirstNames(i, maxNames);

		out.println("    <g transform=\"translate(" + x + ", " + y + ")\">");
		out.println("      <use xlink:href=\"#PersonBox" + sex + "\" fill=\"" + color + "\"/>");
		out.println("      <text x=\"" + indiboxWidth / 2 + "\" y=\"14\" font-family=\"Verdana\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">");
		out.println("        " + firstName);
		out.println("      </text>");
		out.println("      <text x=\"" + indiboxWidth / 2 + "\" y=\"26\" font-family=\"Verdana\" font-size=\"12\" font-weight=\"bold\" text-anchor=\"middle\">");
		out.println("        " + i.getLastName());
		out.println("      </text>");
		if (i.getBirthDate() != null && !i.getBirthDate().toString().equals("")) {
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
     * Outputs a line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	private void printLine(int x1, int y1, int x2, int y2) {
		out.println("    <line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" stroke=\"black\"/>");
	}

    /**
     * Outputs a dashed line.
     * @param x1 start x
     * @param y1 start y
     * @param x2 end x
     * @param y2 end y
     */
	private void printDashedLine(int x1, int y1, int x2, int y2) {
		out.println("    <line x1=\"" + x1 + "\" y1=\"" + y1 + "\" x2=\"" + x2 + "\" y2=\"" + y2 + "\" stroke=\"black\" style=\"stroke-dasharray:3 3\"/>");
	}

    /**
     * Outputs the SVG header.
     * @param w family tree width in pixels
     * @param h family tree height in generation lines
     */
	private void printSvgStart(int w, int h) {
		int y = h * verticalUnit - verticalGap;
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"");
		out.println(" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11-basic.dtd\">");
		out.println("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"" + (w + HORIZONTAL_MARGIN * 2) + "\" height=\"" + (y + VERTICAL_MARGIN * 2) + "\">");
		out.println("  <defs>");
		out.println("    <rect id=\"PersonBox\" rx=\"25\" ry=\"25\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"black\"/>");
		out.println("    <rect id=\"PersonBoxMale\" rx=\"5\" ry=\"5\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"#000044\"/>");
		out.println("    <rect id=\"PersonBoxFemale\" rx=\"15\" ry=\"15\" width=\"" + indiboxWidth + "\" height=\"" + indiboxHeight + "\" stroke=\"#440000\"/>");
		out.println("  </defs>");
		out.println("  <g transform=\"translate(" + HORIZONTAL_MARGIN + ", " + (y - indiboxHeight + VERTICAL_MARGIN) + ")\">");
	}

    /**
     * Outputs the SVG footer.
     */
	private void printSvgEnd() {
		out.println("  </g>");
		out.println("</svg>");
	}

    /**
     * Returns a maximum of <code>maxNames</code> given names of the given
     * individual. If <code>maxNames</code> is 0, this method returns all
     * given names.
     */
    private String getFirstNames(Indi indi, int maxNames) {
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
