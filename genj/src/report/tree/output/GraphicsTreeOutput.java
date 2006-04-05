package tree.output;

import genj.report.Report;
import genj.util.Registry;

import java.io.IOException;

import tree.IndiBox;
import tree.graphics.GraphicsOutput;

public class GraphicsTreeOutput implements TreeOutput {

    private GraphicsOutput reportOutput;

    private Registry properties;

    public GraphicsTreeOutput(GraphicsOutput reportOutput, Registry properties) {
        this.reportOutput = reportOutput;
        this.properties = properties;
    }

    public void output(IndiBox indibox) throws IOException {
        reportOutput.output(new GraphicsTreeRenderer(indibox, properties));
    }

    public void display(Report report) {
        reportOutput.display(report);
    }
}
