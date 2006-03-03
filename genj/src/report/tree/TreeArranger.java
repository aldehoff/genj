/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package tree;

/**
 * Interface for classes used to arrange individual boxes in the family tree.
 *
 * @author Przemek Więch <pwiech@losthive.org>
 */
public interface TreeArranger {

    /**
     * Arranges the family tree starting from the selected individual.
     * @param indibox  root individual
     */
	public void arrange(IndiBox indibox);
}