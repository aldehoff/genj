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
import java.awt.font.FontRenderContext;
import java.awt.geom.RoundRectangle2D;

import tree.FamBox;
import tree.IndiBox;

/**
 * Draws tree elements to a Graphics2D object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsTreeElements implements TreeElements {

    private static final int DEFAULT_INDIBOX_LINES = 2;
    private static final int DEFAULT_FAMBOX_LINES = 1;
    private static final int TEXT_MARGIN = 5;
    private static final int NAME_LINE_HEIGHT = 12;
    private static final int LINE_HEIGHT = 10;
    private static final TagPath PATH_INDIBIRTPLAC = new TagPath("INDI:BIRT:PLAC");
    private static final TagPath PATH_INDIDEATPLAC = new TagPath("INDI:DEAT:PLAC");
    private static final TagPath PATH_INDIOCCU = new TagPath("INDI:OCCU");
    private static final TagPath PATH_INDITITL = new TagPath("INDI:TITL");
    private static final TagPath PATH_FAMMARRPLAC = new TagPath("FAM:MARR:PLAC");
    private static final TagPath PATH_FAMDIVPLAC = new TagPath("FAM:DIV:PLAC");

    /**
     * Used to determine text width.
     */
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, false, false);

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

    private static final int COLOR_GENERATIONS = (BOX_COLORS.length - 1) / 2;

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
     * Font for the name suffix
     */
    private final Font NAME_SUFFIX_FONT;

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
     * Whether to draw the title of an individual.
     */
    private boolean drawTitle;

    /**
     * Whether to draw the suffix from a name.
     */
    private boolean drawNameSuffix;

    /**
     * Font layout for drawing the name suffix.
     */
    private int fontNameSuffix;

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

    private int maxNamesPerLine;

    private boolean useColors;

    private boolean drawPlaces;

    private boolean drawDates;

    private boolean drawOccupation;

    private boolean drawDivorce;

    private boolean swapNames;

    private int defaultIndiboxWidth;

    private int defaultIndiboxHeight;

    private int defaultFamboxWidth;

    private int defaultFamboxHeight;

    /**
     * The graphics object to paint on.
     */
    private Graphics2D graphics = null;

    /**
     * Constructs the object.
     */
    public GraphicsTreeElements(Graphics2D graphics, Registry properties) {
        this.graphics = graphics;

        drawTitle = properties.get("drawTitle", false);
        drawNameSuffix = properties.get("drawNameSuffix", false);
        drawSexSymbols = properties.get("drawSexSymbols", true);
        drawIndiIds = properties.get("drawIndiIds", false);
        drawFamIds = properties.get("drawFamIds", false);
        maxNames = properties.get("maxNames", -1);
        maxNamesPerLine = properties.get("maxNamesPerLine", 2);
        useColors = properties.get("useColors", true);
        maxImageWidth = properties.get("maxImageWidth", 0);
        drawPlaces = properties.get("drawPlaces", true);
        drawDates = properties.get("drawDates", true);
        drawOccupation = properties.get("drawOccupation", true);
        drawDivorce = properties.get("drawDivorce", true);
        swapNames = properties.get("swapNames", false);
        defaultIndiboxWidth = properties.get("defaultIndiboxWidth", 0);
        defaultIndiboxHeight = properties.get("defaultIndiboxHeight", 0);
        defaultFamboxHeight = properties.get("defaultFamboxHeight", 0);
        defaultFamboxWidth = properties.get("defaultFamboxWidth", 0);
        fontNameSuffix = properties.get("fontNameSuffix", Font.BOLD + Font.ITALIC);
        NAME_SUFFIX_FONT = new Font("verdana", fontNameSuffix, 12); // create font for name suffix
    }

    public GraphicsTreeElements(Registry properties)
    {
        this(null, properties);
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

        // Name suffix
        String nameSuffix = null;
        if (drawNameSuffix) {
            nameSuffix = i.getNameSuffix();
            if (nameSuffix != null && nameSuffix.equals(""))
            	nameSuffix = null;
        }

        // Name
        int currentY = y + 14;
        String[] firstNames = getFirstNames(i);
        String lastName = null;

        // generate LastName + Title
        if (drawTitle && i.getProperty(PATH_INDITITL) != null)
        	lastName = i.getLastName() + " " + i.getProperty(PATH_INDITITL);        	
        else
        	lastName = i.getLastName();        	
        
        if (swapNames) { // last name
            graphics.setFont(NAME_FONT);
            centerString(graphics, lastName, x + dataWidth/2, currentY);
            currentY += NAME_LINE_HEIGHT;

            if (nameSuffix != null) {
                graphics.setFont(NAME_SUFFIX_FONT);
            	centerString(graphics, nameSuffix, x + dataWidth/2, currentY);
                currentY += NAME_LINE_HEIGHT;
            }
        }

        graphics.setFont(NAME_FONT);
        for (int j = 0; j < firstNames.length; j++) { // first names
            centerString(graphics, firstNames[j], x + dataWidth/2, currentY);
            currentY += NAME_LINE_HEIGHT;
        }

        if (!swapNames) { // last name
            graphics.setFont(NAME_FONT);
            centerString(graphics, lastName, x + dataWidth/2, currentY);
            currentY += NAME_LINE_HEIGHT;

            if (nameSuffix != null) {
                graphics.setFont(NAME_SUFFIX_FONT);
            	centerString(graphics, nameSuffix, x + dataWidth/2, currentY);
                currentY += NAME_LINE_HEIGHT;
            }
        }
                
        graphics.setFont(DETAILS_FONT);

        Property birthDate = null;
        Property deathDate = null;
        Property birthPlace = null;
        Property deathPlace = null;
        Property occupation = null;

        if (drawDates) {
            birthDate = i.getBirthDate();
            if (birthDate != null && !birthDate.isValid())
                birthDate = null;
            deathDate = i.getDeathDate();
            if (deathDate != null && !deathDate.isValid())
                deathDate = null;
        }

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
        if (i.getDeathDate() != null || i.getProperty(PATH_INDIDEATPLAC) != null) {
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

        if (drawDates) {
            marriageDate = f.getMarriageDate();
            if (marriageDate != null && !marriageDate.isValid())
                marriageDate = null;
            divorceDate = f.getDivorceDate();
            if (divorceDate != null && !divorceDate.isValid())
                divorceDate = null;
        }

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
    public static void centerString(Graphics2D graphics, String text, int x, int y) {
        int width = getTextWidth(text, graphics.getFont(), graphics);
        graphics.drawString(text, x - width / 2, y);
    }

    private static int getTextWidth(String text, Font font, Graphics2D graphics) {
        FontRenderContext fontRenderContext = FONT_RENDER_CONTEXT;
        if (graphics != null)
            fontRenderContext = graphics.getFontRenderContext();
        return (int)font.getStringBounds(text, fontRenderContext).getWidth();
    }

    private static int getTextWidth(String text, Font font) {
        return getTextWidth(text, font, null);
    }

    private static String getSexSymbol(int sex) {
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
        if (gen == 0)
            return BOX_COLORS[COLOR_GENERATIONS];
        if (gen < 0)
            return BOX_COLORS[-((-gen - 1) % COLOR_GENERATIONS) + COLOR_GENERATIONS - 1];
        // else (gen > 0)
        return BOX_COLORS[(gen - 1) % COLOR_GENERATIONS + COLOR_GENERATIONS + 1];
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

    public void getIndiBoxSize(IndiBox indibox)
    {
        Indi i = indibox.individual;
        indibox.height = defaultIndiboxHeight;
        indibox.width = defaultIndiboxWidth;

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
        if (lines - DEFAULT_INDIBOX_LINES > 0)
            indibox.height += (lines - DEFAULT_INDIBOX_LINES) * LINE_HEIGHT;

        // height and width computations for first names
        int width = 0;
        String[] firstNames = getFirstNames(i);
        for (int j = 0; j < firstNames.length; j++) {
            int w2 = getTextWidth(firstNames[j], NAME_FONT);
            width = width>w2?width:w2;
        }

        // Additional first names
        indibox.height += (firstNames.length - 1) * NAME_LINE_HEIGHT;

        // optional name suffix
        if (drawNameSuffix && i.getNameSuffix() != null && i.getNameSuffix().length()>0)
        	indibox.height += NAME_LINE_HEIGHT;
        
        // Text data width
        if (width + 2*TEXT_MARGIN > indibox.width)
            indibox.width = width + 2*TEXT_MARGIN;
        if (drawTitle && i.getProperty(PATH_INDITITL) != null)
        	width = getTextWidth(i.getLastName() + " " + i.getProperty(PATH_INDITITL), NAME_FONT);
        else
        	width = getTextWidth(i.getLastName(), NAME_FONT);
        
        if (width + 2*TEXT_MARGIN > indibox.width)
            indibox.width = width + 2*TEXT_MARGIN;
        width = getTextWidth(i.getNameSuffix(), NAME_FONT);
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
                    int newWidth = icon.getIconWidth() * defaultIndiboxHeight / icon.getIconHeight();
                    if (newWidth < maxImageWidth)
                        indibox.width += newWidth;
                    else
                        indibox.width += maxImageWidth;
                }
            }
        }
    }

    public void getFamBoxSize(FamBox fambox)
    {
        Fam f = fambox.family;
        fambox.width = defaultFamboxWidth;
        fambox.height = defaultFamboxHeight;

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

        if (lines - DEFAULT_FAMBOX_LINES > 0)
            fambox.height += (lines - DEFAULT_FAMBOX_LINES) * LINE_HEIGHT;

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
}
