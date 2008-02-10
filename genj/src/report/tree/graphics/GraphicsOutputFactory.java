/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.report.Report;
import genj.util.swing.Action2;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates classes that write report output. This can be
 * a file type or the screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class GraphicsOutputFactory {

    private static final String PREFIX = "output_type.";

    private Map/*<String, Class>*/ outputs = new LinkedHashMap();
    private List/*<Class>*/ outputList = new ArrayList();

    private static GraphicsOutputFactory instance = null;

    /**
     * Creates the object
     * @param report  containing report object
     */
    protected GraphicsOutputFactory() {
        add("svg", SvgWriter.class);
        add("pdf", PdfWriter.class);
        add("png", PngWriter.class);
        add("screen", ScreenOutput.class);
    }

    public static GraphicsOutputFactory getInstance()
    {
        if (instance == null)
            instance = new GraphicsOutputFactory();
        return instance;
    }

    /**
     * Creates the output class for the given type.
     * @param type  output type
     * @param report  Containing report. Used to show dialogs and translate strings.
     */
    public GraphicsOutput createOutput(int type, Report report) {

        GraphicsOutput output = createOutput((Class)outputList.get(type));

        if (output == null)
            return null;

        if (output instanceof GraphicsFileOutput) {
            GraphicsFileOutput fileOutput = (GraphicsFileOutput)output;
            String extension = fileOutput.getFileExtension();

            // Get filename from users
            File file = report.getFileFromUser(report.translate("output.file"),
                        Action2.TXT_OK, true, extension);
            if (file == null)
                return null;

            // Add appropriate file extension
            String suffix = "." + extension;
            if (!file.getPath().endsWith(suffix))
                file = new File(file.getPath() + suffix);
            fileOutput.setFile(file);
        }

        return output;
    }

    public void add(String name, Class clazz)
    {
        outputs.put(name, clazz);
        outputList.add(clazz);
    }

    public String[] getChoices(Report report)
    {
        Iterator iter = outputs.keySet().iterator();
        String[] choices = new String[outputs.size()];
        int i = 0;
        while (iter.hasNext()) {
            choices[i] = report.translate(PREFIX + (String)iter.next());
            i++;
        }
        return choices;
    }

    private GraphicsOutput createOutput(Class clazz) {
        try
        {
            return (GraphicsOutput)clazz.newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
