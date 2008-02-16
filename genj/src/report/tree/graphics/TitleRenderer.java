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
    public static final int VERTICAL_MARGIN = 10;
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
    private int titleHeight = 24;

    /**
     * Creates the object.
     * @param renderer  image renderer
     * @param title     title to display
     */
    public TitleRenderer(GraphicsRenderer renderer, String title)
    {
        this(renderer, title, -1);
    }

    /**
     * Creates the object.
     * @param renderer  image renderer
     * @param title     title to display
     * @param titleHeight  height of the title in pixels (0 or less for automatic height)
     */
    public TitleRenderer(GraphicsRenderer renderer, String title, int titleHeight)
    {
        this.renderer = renderer;
        this.title = title;
        this.titleHeight = titleHeight;

        if (titleHeight <= 0)
            this.titleHeight = (renderer.getImageHeight() + renderer.getImageWidth()) / 40; // auto-size
    }

    public int getImageHeight()
    {
        return renderer.getImageHeight() + titleHeight + VERTICAL_MARGIN;
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
        graphics.setFont(new Font("verdana", Font.BOLD, titleHeight));
        GraphicsTreeElements.centerString(graphics, title, getImageWidth() / 2, titleHeight * 3/4 + VERTICAL_MARGIN);

        graphics.translate(0, titleHeight + VERTICAL_MARGIN); // Move rendered image below the title
        renderer.render(graphics);
    }
}
