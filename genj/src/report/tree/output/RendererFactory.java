/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree.output;

import genj.report.options.ComponentContainer;

import java.util.ArrayList;
import java.util.List;

import tree.IndiBox;
import tree.graphics.GraphicsRenderer;
import tree.graphics.TitleRenderer;

/**
 * Creates classes that render the tree to a graphics object.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public class RendererFactory implements ComponentContainer
{
    private TreeRendererBase renderer;
    private GraphicsRenderer rotateRenderer;
    private GraphicsRenderer titleRenderer;

    public RendererFactory()
    {
        renderer = new VerticalTreeRenderer();
        rotateRenderer = new RotateRenderer(renderer);
        titleRenderer = new TitleRenderer(rotateRenderer);
    }

    public GraphicsRenderer createRenderer(IndiBox firstIndi, TreeElements elements)
    {
        renderer.setElements(elements);
        renderer.setFirstIndi(firstIndi);

        return titleRenderer;
    }

    public List<Object> getComponents()
    {
        List<Object> components = new ArrayList<Object>();
        components.add(this);
        components.add(rotateRenderer);
        components.add(renderer);
        components.add(titleRenderer);
        return components;
    }
}
