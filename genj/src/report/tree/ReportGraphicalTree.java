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
 * @author Przemek Więch <pwiech@losthive.org>
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

        // TODO: Set file filter in JFileChooser to *.svg
        File svgFile = getFileFromUser(translate("output.file"),
                Action2.TXT_OK, true);
        PrintWriter svgOut = null;
        try {
            svgOut = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(svgFile), Charset.forName("UTF-8")));
        } catch (IOException e) {
            println("Error: Couldn't create file: " + e.getMessage());
            return;
        }

        TreeBuilder builder = new BasicTreeBuilder(gen_ancestors - 1,
                gen_ancestor_descendants - 1, gen_descendants - 1);
        TreeArranger arranger;
        if (arrangement == 0)
            arranger = new CenteredArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);
        else
            arranger = new AlignLeftArranger(INDIBOX_WIDTH, HORIZONTAL_GAP);
        TreeRenderer outputter = new SvgTreeRenderer(svgOut, max_names,
                INDIBOX_WIDTH, INDIBOX_HEIGHT, VERTICAL_GAP);

        IndiBox indibox = builder.build(indi);
        arranger.arrange(indibox);
        outputter.render(indibox);

        svgOut.close();

        showFileToUser(svgFile);
    }
}
