/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.report.Report;
import genj.util.Registry;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import tree.IndiBox;
import tree.render.GraphicsRenderer;
import tree.render.GraphicsTreeRenderer;

/**
 * Displays the family tree in a component on screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ScreenOutput extends JScrollPane implements TreeOutput {

    /**
     * Report properties.
     */
    private Registry properties;

    /**
     * Renders the tree to a Graphics2D object.
     */
    private GraphicsRenderer renderer;

    /**
     * The component containing the whole tree view.
     */
    private JComponent view;

    /**
     * Constructs the object.
     */
    public ScreenOutput(Registry properties) {
        this.properties = properties;
        view = new JComponent() {
            public void paint(Graphics g) {
                renderer.render((Graphics2D)g);
            }
        };

        setViewportView(view);
        setPreferredSize(new Dimension(300, 200));
    }

    /**
     * Prepares the component to be displayed.
     */
    public void output(IndiBox indibox) {
        renderer = new GraphicsTreeRenderer(indibox, properties);
        view.setPreferredSize(new Dimension(renderer.getImageWidth(),
                renderer.getImageHeight()));

        JScrollBar sb = getHorizontalScrollBar();
        sb.setValue(1); // Without this the next instruction doesn't work correctly
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
        sb = getVerticalScrollBar();
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
    }

    /**
     * Displays the component.
     */
    public void display(Report report) {
        report.showComponentToUser(this);
    }
}
