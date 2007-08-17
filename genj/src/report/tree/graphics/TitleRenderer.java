/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import tree.output.GraphicsTreeElements;

/**
 * Displays a title above the rendered image.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class TitleRenderer implements GraphicsRenderer
{
    /**
     * The renderer that renders the actual image.
     */
    private GraphicsRenderer renderer;

    /**
     * The title to display.
     */
    private String title;

    /**
     * Title font height.
     */
    private static final int TITLE_HEIGHT = 24;

    /**
     * Font for the chart title.
     */
    private static final Font TITLE_FONT = new Font("verdana", Font.BOLD, TITLE_HEIGHT);

    /**
     * Creates the object.
     * @param renderer  image renderer
     * @param title     title to display
     */
    public TitleRenderer(GraphicsRenderer renderer, String title)
    {
        this.renderer = renderer;
        this.title = title;
    }

    public int getImageHeight()
    {
        return renderer.getImageHeight() + TITLE_HEIGHT + 10;
    }

    public int getImageWidth()
    {
        return renderer.getImageWidth();
    }

    /**
     * Renders the title and calls the enclosed renderer to render the image.
     */
    public void render(Graphics2D graphics)
    {
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, getImageWidth(), getImageHeight());

        graphics.setColor(Color.BLACK);
        graphics.setFont(TITLE_FONT);
        GraphicsTreeElements.centerString(graphics, title, getImageWidth() / 2, TITLE_HEIGHT * 3/4 + 10);

        graphics.translate(0, TITLE_HEIGHT + 10); // Move rendered image below the title
        renderer.render(graphics);
    }
}
