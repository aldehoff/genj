/**
 * This GenJ SosaIndexation Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.plugin.sosa;

/* genj imported classes */
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import genj.util.Resources;
import genj.window.WindowManager;
import genj.gedcom.Indi;
import genj.util.swing.ChoiceWidget;
import genj.common.SelectEntityWidget;
/* java imported classes */
import java.util.logging.Logger;

/**
 * MenuAction
 */

public class GetInformationMenuAction extends Action2 {

	private String menuItem;

	private Gedcom gedcom;

	private Indi sosaRoot;

	private SosaIndexation sosaIndexation;

	private SosaMenuAction menuItemSETCHANGE;

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private final Resources RESOURCES = Resources.get(this);

	static final String SOSA_MENU = "Sosa indexation";

	public enum myMenuEnum {

		private String item;

		/* constructor */
		myMenuEnum(String item) {
			this.item = item;
		}

		/* method to retrieve constant value */
		public String getItem() {
			return item;
		}

	}

	/**
	 * Change Sosa Indexation
	 */
	public void setSosaIndexationValue(SosaIndexation sosaIndexation) {
		this.sosaIndexation=sosaIndexation;
	}


	/**
	 * Menu action constructor
	 */
	public GetInformationMenuAction(String menuItem, SosaIndexation  sosaIndexation, Gedcom gedcom) {
		this.menuItem = menuItem;
		this.sosaIndexation = sosaIndexation;
		this.gedcom = gedcom;
		LOG.fine("Set menu item = " + menuItem);
		setText(RESOURCES.getString(menuItem));
	}

	/**
	 * Change label of menu item
	 */
	public void setString(String menuItem) {
		this.menuItem = menuItem;
		setText(RESOURCES.getString(menuItem));
	}


	/**
	 * Execute click on menu item
	 */
	protected void execute() {
		LOG.fine("Click sur menu item = " + menuItem);
		LOG.fine("Et oui ! = " + menuItem);
		/* we check which menu item is displayed */
	}
}
