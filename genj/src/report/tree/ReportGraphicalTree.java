/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

import genj.gedcom.Indi;
import genj.report.Report;
import genj.util.Registry;

import java.io.IOException;

import tree.arrange.AlignLeftArranger;
import tree.arrange.CenteredArranger;
import tree.build.BasicTreeBuilder;
import tree.build.NoSpouseFilter;
import tree.build.TreeBuilder;
import tree.output.OutputFactory;
import tree.output.TreeOutput;

/**
 * GenJ - ReportGraphicalTree.
 * The report works in 3 phases:
 * <ol>
 *   <li> Choose people to display and build the target tree structure</li>
 *   <li> Arrange the individual boxes - assign (x, y) coordinates to all boxes</li>
 *   <li> Output the final tree to a file or to the screen and display the result</li>
 * </ol>
 * Each of these steps can be separately customized.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version 0.15
 */
public class ReportGraphicalTree extends Report {

    /**
     * Indibox width in pixels.
     */
    private static final int DEFAULT_INDIBOX_WIDTH = 110;

    /**
     * Indibox height in pixels.
     */
    private static final int DEFAULT_INDIBOX_HEIGHT = 64;

    /**
     * Minimal vertical gap between boxes.
     */
    private static final int VERTICAL_GAP = 16;

    /**
     * Minimal horizontal gap between boxes.
     */
    private static final int HORIZONTAL_GAP = 10;

    /**
     * Family box width in pixels.
     */
    private static final int FAMBOX_WIDTH = 100;

    /**
     * Family box height in pixels.
     */
    private static final int FAMBOX_HEIGHT = 27;

    /**
     * Width of the image inside an individual box.
     */
    private static final int MAX_IMAGE_WIDTH = 50;

    /**
     * Output type.
     */
    public int output_type = 0;

    public String[] output_types = { translate("output_type.svg"),
            translate("output_type.pdf"), translate("output_type.screen")};

    /**
     * Number of generations of ancestors.
     */
    public int gen_ancestors = 0;

    public String[] gen_ancestorss = { translate("nolimit"), "0", "1", "2",
            "3", "4", "5", "6", "7", "8", "9", "10" };

    /**
     * Number of generations of descentants of ancestors.
     */
    public int gen_ancestor_descendants = 0;

    public String[] gen_ancestor_descendantss = { translate("nolimit"), "0",
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };

    /**
     * Number of generations of descentants.
     */
    public int gen_descendants = 0;

    public String[] gen_descendantss = { translate("nolimit"), "0", "1", "2",
            "3", "4", "5", "6", "7", "8", "9", "10" };

    /**
     * Maximal number of first names to display.
     */
    public int max_names = 0;

    public String[] max_namess = { translate("nolimit"), "1", "2", "3" };

    /**
     * Whether to display places of birth and death.
     */
    public boolean draw_places = true;

    /**
     * Whether to display occupations.
     */
    public boolean draw_occupation = true;

    /**
     * Whether to display images.
     */
    public boolean draw_images = true;

    /**
     * Whether to display sex symbols.
     */
    public boolean draw_sex_symbols = true;

    /**
     * Whether to IDs of individuals.
     */
    public boolean draw_indi_ids = false;

    /**
     * Whether to IDs of families.
     */
    public boolean draw_fam_ids = false;

    /**
     * Whether to display other marriages of ancestors.
     */
    public boolean other_marriages = true;

    /**
     * Whether to display spouses (excluding ancestors).
     */
    public boolean show_spouses = true;

    /**
     * Whether to display the family box.
     */
    public boolean display_fambox = true;

    /**
     * Whether to use colors (or only black and white).
     */
    public boolean use_colors = true;

    /**
     * Type of arrangement.
     */
    public int arrangement = 0;

    public String[] arrangements = { translate("arrangement.center"),
            translate("arrangement.left") };

    /**
     * The result is stored in files
     */
    public boolean usesStandardOut() {
        return false;
    }

    /**
     * The report's entry point
     */
    public void start(Indi indi) {

        Registry properties = new Registry();
        properties.put("genAncestors", gen_ancestors - 1);
        properties.put("genAncestorDescendants", gen_ancestor_descendants - 1);
        properties.put("genDescendants", gen_descendants - 1);
        properties.put("maxNames", max_names);
        properties.put("defaultIndiboxHeight", DEFAULT_INDIBOX_HEIGHT);
        properties.put("defaultIndiboxWidth", DEFAULT_INDIBOX_WIDTH);
        properties.put("verticalGap", VERTICAL_GAP);
        properties.put("horizontalGap", HORIZONTAL_GAP);
        properties.put("famboxWidth", FAMBOX_WIDTH);
        properties.put("famboxHeight", FAMBOX_HEIGHT);
        properties.put("displayFambox", display_fambox);
        properties.put("useColors", use_colors);
        properties.put("otherMarriages", other_marriages);
        properties.put("showSpouses", show_spouses);
        properties.put("drawSexSymbols", draw_sex_symbols);
        properties.put("drawIndiIds", draw_indi_ids);
        properties.put("drawFamIds", draw_fam_ids);
        properties.put("maxImageWidth", draw_images ? MAX_IMAGE_WIDTH : 0);
        properties.put("drawPlaces", draw_places);
        properties.put("drawOccupation", draw_occupation);

        // Build the tree
        TreeBuilder builder = new BasicTreeBuilder(properties);
        IndiBox indibox = builder.build(indi);
        if (!show_spouses)
            new NoSpouseFilter().filter(indibox);

        new DetermineBoxSize(properties).filter(indibox);

        // Arrange the tree boxes
        TreeFilter arranger;
        if (arrangement == 0)
            arranger = new CenteredArranger(HORIZONTAL_GAP);
        else
            arranger = new AlignLeftArranger(HORIZONTAL_GAP);
        arranger.filter(indibox);

        // Render and display the tree
        OutputFactory outputFactory = new OutputFactory(this, properties);
        TreeOutput output = outputFactory.createOutput(output_type);
        if (output == null)
            return;

        try {
            output.output(indibox);
            output.display(this);
        } catch (IOException e) {
            println("Error generating output: " + e.getMessage());
        }
    }
}
