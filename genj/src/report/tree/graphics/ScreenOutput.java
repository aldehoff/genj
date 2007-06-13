/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import genj.report.Report;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * Displays the report output in a component on screen.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class ScreenOutput extends JScrollPane implements GraphicsOutput {

    /**
     * Renders output to a Graphics2D object.
     */
    private GraphicsRenderer renderer = null;

    /**
     * The component containing the whole tree view.
     */
    private JComponent view;

    /**
     * Constructs the object.
     */
    public ScreenOutput() {
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
    public void output(GraphicsRenderer renderer) {
        this.renderer = renderer;
        view.setPreferredSize(new Dimension(renderer.getImageWidth(),
                renderer.getImageHeight()));

        /* This was supposed to center both scrollbars but it doesn't work
        JScrollBar sb = getHorizontalScrollBar();
        sb.setValue(1); // Without this the next instruction doesn't work correctly
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
        sb = getVerticalScrollBar();
        sb.setValue((sb.getMaximum() - sb.getMinimum()) / 2);
        */
    }

    /**
     * Displays the component.
     */
    public void display(Report report) {
        report.showComponentToUser(this);
    }

    /**
     * Return null because no file is produced.
     */
	public String getFileExtension()
	{
		return null;
	}
}
