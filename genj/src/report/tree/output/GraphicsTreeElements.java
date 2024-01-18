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
import genj.gedcom.Property;
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

import tree.FamBox;
import tree.IndiBox;

/**
 * Draws tree elements to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeElements implements TreeElements {

    private static final int LINE_HEIGHT = 10;
    private static final TagPath PATH_INDIBIRTPLAC = new TagPath("INDI:BIRT:PLAC");
    private static final TagPath PATH_INDIDEATPLAC = new TagPath("INDI:DEAT:PLAC");
    private static final TagPath PATH_INDIOCCU = new TagPath("INDI:OCCU");
    private static final TagPath PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC");
    private static final TagPath PATH_FAMDIVPLAC = new TagPath("FAM:DIV:PLAC");

    /**
     * Box background colors.
     */
    private static final Color[] BOX_COLORS = {
            new Color(0xff, 0xff, 0xff), // -13
            new Color(0xce, 0xb6, 0xbd), // -12
            new Color(0xde, 0x55, 0xff), // -11
            new Color(0x84, 0x82, 0xff), // -10
            new Color(0xad, 0xae, 0xef), // -9
            new Color(0xad, 0xcf, 0xff), // -8
            new Color(0xe7, 0xdb, 0xe7), // -7
            new Color(0xd6, 0x5d, 0x5a), // -6
            new Color(0xff, 0x82, 0xb5), // -5
            new Color(0xef, 0xae, 0xc6), // -4
            new Color(0xff, 0xdd, 0xdd), // -3
            new Color(0xce, 0xaa, 0x31), // -2
            new Color(0xff, 0xdd, 0x00), // -1

            new Color(0xff, 0xff, 0x33), // 0

            new Color(0xff, 0xff, 0xdd), // 1
            new Color(0xde, 0xff, 0xde), // 2
            new Color(0x82, 0xff, 0x82), // 3
            new Color(0x1a, 0xe1, 0x1a), // 4
            new Color(0xa9, 0xd0, 0xa9), // 5
            new Color(0xa9, 0xd0, 0xbf), // 6
            new Color(0xbb, 0xbb, 0xbb), // 7
            new Color(0xaa, 0x95, 0x95), // 8
            new Color(0x9e, 0xa3, 0xb2), // 9
            new Color(0xcd, 0xd3, 0xe9), // 10
            new Color(0xdf, 0xe2, 0xe2), // 11
            new Color(0xfa, 0xfa, 0xfa), // 12
            new Color(0xff, 0xff, 0xff) // 13
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
        String[] candidateFontNames = { "sansserif", "apple symbol", "symbol" };
        for (int i = 0; i < candidateFontNames.length; i++) {
            Font candidateFont = new Font(candidateFontNames[i], Font.PLAIN, 10);
            if (candidateFont.canDisplay(MALE_SYMBOL.charAt(0))) {
                sexSymbolFont = candidateFont;
                break;
            }
        }
        if (sexSymbolFont == null)
            sexSymbolFont = new Font("SansSerif", Font.PLAIN, 10);
    }

    private int maxImageWidth;

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

    private boolean drawPlaces;

    private boolean drawOccupation;

    private boolean drawDivorce;

    /**
     * The graphics object to paint on.
     */
    private Graphics2D graphics = null;

    /**
     * Constructs the object.
     */
    public GraphicsTreeElements(Graphics2D graphics, Registry properties) {
        this.graphics = graphics;

        drawSexSymbols = properties.get("drawSexSymbols", true);
        drawIndiIds = properties.get("drawIndiIds", false);
        drawFamIds = properties.get("drawFamIds", false);
        maxNames = properties.get("maxNames", -1);
        useColors = properties.get("useColors", true);
        maxImageWidth = properties.get("maxImageWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawOccupation = properties.get("drawOccupation", true);
        drawDivorce = properties.get("drawDivorce", true);
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

        // Determine photo size
        int imageWidth = 0;
        int imageHeight = indibox.height;
        ImageIcon icon = null;
        if (maxImageWidth > 0) {
            PropertyFile file = (PropertyFile)i.getProperty(new TagPath("INDI:OBJE:FILE"));
            if (file != null) {
                icon = file.getValueAsIcon();
                if (icon != null) {
                    imageWidth = icon.getIconWidth() * indibox.height / icon.getIconHeight();
                    if (imageWidth > maxImageWidth) {
                        imageWidth = maxImageWidth;
                        imageHeight = icon.getIconHeight() * imageWidth / icon.getIconWidth();
                    }
                }
            }
        }
        int dataWidth = indibox.width - imageWidth;

        Color color = getBoxColor(gen);
        Shape box = new RoundRectangle2D.Double(x, y, indibox.width, indibox.height, 15, 15);
        graphics.setColor(color);
        graphics.fill(box);
        graphics.setColor(Color.BLACK);

        Shape oldClip = graphics.getClip();
        graphics.clip(box);

        // Name
        graphics.setFont(NAME_FONT);
        centerString(graphics, getFirstNames(i), x + dataWidth/2, y + 14);
        centerString(graphics, i.getLastName(), x + dataWidth/2, y + 26);

        int currentY = y + 38;

        graphics.setFont(DETAILS_FONT);

        Property birthDate = null;
        Property deathDate = null;
        Property birthPlace = null;
        Property deathPlace = null;
        Property occupation = null;


        birthDate = i.getBirthDate();
        if (birthDate != null && !birthDate.isValid())
            birthDate = null;
        deathDate = i.getDeathDate();
        if (deathDate != null && !deathDate.isValid())
            deathDate = null;

        if (drawPlaces) {
            birthPlace = i.getProperty(PATH_INDIBIRTPLAC);
            if (birthPlace != null && birthPlace.toString().equals(""))
                birthPlace = null;
            deathPlace = i.getProperty(PATH_INDIDEATPLAC);
            if (deathPlace != null && deathPlace.toString().equals(""))
                deathPlace = null;
        }

        if (drawOccupation)
            occupation = i.getProperty(PATH_INDIOCCU);

        // Date and place of birth
        if (birthDate != null || birthPlace != null) {
            centerString(graphics, Options.getInstance().getBirthSymbol(), x + 7, currentY);
            if (birthDate != null) {
                graphics.drawString(birthDate.toString(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (birthPlace != null) {
                graphics.drawString(birthPlace.toString(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
        }

        // Date and place of death
        if (i.getDeathDate() != null) {
            centerString(graphics, Options.getInstance().getDeathSymbol(), x + 7, currentY);
            if (deathDate != null) {
                graphics.drawString(deathDate.toString(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (deathPlace != null) {
                graphics.drawString(deathPlace.toString(), x + 13, currentY);
                currentY += LINE_HEIGHT;
            }
            if (deathDate == null && deathPlace == null)
                currentY += LINE_HEIGHT;
        }

        // Occupation
        if (occupation != null) {
            graphics.drawString(occupation.toString(), x + 6, currentY);
        }



        // Sex symbol
        if (drawSexSymbols) {
            int symbolX = x + dataWidth  - 14;
            int symbolY = y + indibox.height - 5;
            graphics.setFont(sexSymbolFont);
            graphics.drawString(getSexSymbol(i.getSex()), symbolX, symbolY);
        }

        // Id
        if (drawIndiIds) {
            graphics.setFont(ID_FONT);
            graphics.drawString(i.getId(), x + 8, y + indibox.height - 4);
        }

        // Photo
        if(imageWidth > 0)
            graphics.drawImage(icon.getImage(), x + dataWidth, y, imageWidth, imageHeight, null);

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
    public void drawFamBox(FamBox fambox, int x, int y, int gen) {
        Fam f = fambox.family;

        Color color = getBoxColor(gen);
        Shape box = new RoundRectangle2D.Double(x, y, fambox.width, fambox.height, 5, 5);
        graphics.setColor(color);
        graphics.fill(box);
        graphics.setColor(Color.BLACK);

        Shape oldClip = graphics.getClip();
        graphics.clip(box);

        int currentY = y + 12;

        graphics.setFont(DETAILS_FONT);

        Property marriageDate = null;
        Property divorceDate = null;
        Property marriagePlace = null;
        Property divorcePlace = null;

        marriageDate = f.getMarriageDate();
        if (marriageDate != null && !marriageDate.isValid())
            marriageDate = null;
        divorceDate = f.getDivorceDate();
        if (divorceDate != null && !divorceDate.isValid())
            divorceDate = null;

        if (drawPlaces) {
            marriagePlace = f.getProperty(PATH_FAMMARRPLAC);
            if (marriagePlace != null && marriagePlace.toString().equals(""))
                marriagePlace = null;
            divorcePlace = f.getProperty(PATH_FAMDIVPLAC);
            if (divorcePlace != null && divorcePlace.toString().equals(""))
                divorcePlace = null;
        }

        // Date and place of marriage
        if (f.getMarriageDate() != null) {
            centerString(graphics, Options.getInstance().getMarriageSymbol(), x + 13, currentY);
            if (marriageDate != null) {
                graphics.drawString(marriageDate.toString(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (marriagePlace != null) {
                graphics.drawString(marriagePlace.toString(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (marriageDate == null && marriagePlace == null)
                currentY += LINE_HEIGHT;
        }

        // Date and place of divorce
        if (drawDivorce && f.getDivorceDate() != null) {
            centerString(graphics, Options.getInstance().getDivorceSymbol(), x + 13, currentY);
            if (divorceDate != null) {
                graphics.drawString(divorceDate.toString(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (divorcePlace != null) {
                graphics.drawString(divorcePlace.toString(), x + 25, currentY);
                currentY += LINE_HEIGHT;
            }
            if (divorceDate == null && divorcePlace == null)
                currentY += LINE_HEIGHT;
        }

        // Id
        if (drawFamIds) {
            graphics.setFont(ID_FONT);
            graphics.drawString(f.getId(), x + 8, y + fambox.height - 4);
        }

        graphics.setClip(oldClip);
        graphics.draw(box);
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
        int width = (int) rect.getWidth();
        graphics.drawString(text, x - width / 2, y);
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
        if (gen + 13 < BOX_COLORS.length && gen + 13 >= 0)
            return BOX_COLORS[gen + 13];
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
