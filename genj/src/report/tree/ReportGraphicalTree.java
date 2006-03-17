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
import genj.util.swing.Action2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * GenJ - ReportGraphicalTree
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version 0.05
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

    public String[] arrangements = { translate("arrangement.0"),
            translate("arrangement.1") };

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

        TreeBuilder builder = new BasicTreeBuilder(gen_ancestors - 1,
                gen_ancestor_descendants - 1, gen_descendants - 1);
        TreeArranger arranger;
        if (arrangement == 0)
            arranger = new CenteredArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);
        else
            arranger = new AlignLeftArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);

        IndiBox indibox = builder.build(indi);
        arranger.arrange(indibox);

        // SVG
        // TODO: Set file filter in JFileChooser to *.svg
        File svgFile = getFileFromUser(translate("output.file"),
                Action2.TXT_OK, true);
        try {
            PrintWriter svgOut = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(svgFile), Charset.forName("UTF-8")));
            TreeRenderer renderer = new SvgTreeRenderer(svgOut, max_names,
                    INDIBOX_WIDTH, INDIBOX_HEIGHT, VERTICAL_GAP, FAMBOX_WIDTH,
                    FAMBOX_HEIGHT, display_fambox);

            renderer.render(indibox);

            svgOut.close();
            showFileToUser(svgFile);
        } catch (IOException e) {
            println("Error: Couldn't write to file: " + e.getMessage());
            return;
        }
    }
}
