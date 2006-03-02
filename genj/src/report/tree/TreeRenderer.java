/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

/**
 * Interface for classes writing the family tree to an output.
 *
 * @author Przemek Więch <pwiech@losthive.org>
 */
public interface TreeRenderer {
	public void render(IndiBox indibox);
}
