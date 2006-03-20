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
import tree.arrange.TreeArranger;
import tree.build.BasicTreeBuilder;
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
 * @version 0.09
 */
public class ReportGraphicalTree extends Report {

    /**
     * Indibox width in pixels.
     */
    private final int INDIBOX_WIDTH = 110;

    /**
     * Indibox height in pixels.
     */
    private final int INDIBOX_HEIGHT = 60;

    /**
     * Minimal vertical gap between boxes.
     */
    private final int VERTICAL_GAP = 16;

    /**
     * Minimal horizontal gap between boxes.
     */
    private final int HORIZONTAL_GAP = 10;

    /**
     * Family box width in pixels.
     */
    private final int FAMBOX_WIDTH = 100;

    /**
     * Family box height in pixels.
     */
    private final int FAMBOX_HEIGHT = 22;

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
     * Whether to display the family box.
     */
    public boolean display_fambox = true;

    /**
     * Maximal number of first names to display.
     */
    public int max_names = 0;

    public String[] max_namess = { translate("nolimit"), "1", "2", "3" };

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
        properties.put("indiboxWidth", INDIBOX_WIDTH);
        properties.put("indiboxHeight", INDIBOX_HEIGHT);
        properties.put("verticalGap", VERTICAL_GAP);
        properties.put("horizontalGap", HORIZONTAL_GAP);
        properties.put("famboxWidth", FAMBOX_WIDTH);
        properties.put("famboxHeight", FAMBOX_HEIGHT);
        properties.put("displayFambox", display_fambox);

        // Build the tree
        TreeBuilder builder = new BasicTreeBuilder(properties);
        IndiBox indibox = builder.build(indi);

        // Arrange the tree boxes
        TreeArranger arranger;
        if (arrangement == 0)
            arranger = new CenteredArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);
        else
            arranger = new AlignLeftArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);
        arranger.arrange(indibox);

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
