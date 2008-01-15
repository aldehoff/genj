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

public class SosaMenuAction extends Action2 {

	private String menuItem;

	private Gedcom gedcom;

	private Indi sosaRoot;

	private SosaIndexation sosaIndexation;

	private SosaMenuAction menuItemSETCHANGE;

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private final Resources RESOURCES = Resources.get(this);

	static final String SOSA_MENU = "Sosa indexation";

	public enum myMenuEnum {
		/* we initialise enum constants */
		SOSA_SET("Set indexation with..."), SOSA_CHANGE(
				"Change indexation to..."), SOSA_GET(
				"Get individual from index..."), SOSA_REMOVE(
				"Remove all indexes...");

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
	 * Menu constructor
	 */
	public SosaMenuAction(String menuItem, Gedcom gedcom) {
		this.menuItem = menuItem;
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
		/* we check which menu item is displayed */
		SelectEntityWidget select = new SelectEntityWidget(gedcom, Gedcom.INDI,
				null);
		int rc;
		switch (myMenuEnum.valueOf(menuItem)) {
		case SOSA_SET:
			/* we get Decujus */
			// LOG.fine("DeCujus = ");
			/* we set Sosa indexation */
			// LOG.fine("Need here to set Sosa from DeCujus");
			/* we set here a sub-menu = Change indexation */
			LOG.fine("Change menu item = " + menuItem);
			setString(myMenuEnum.SOSA_CHANGE.getItem());
			// SelectEntityWidget select = new
			// SelectEntityWidget(gedcom,Gedcom.INDI, null);
			rc = WindowManager.getInstance(getTarget()).openDialog(null,
					"Select Sosa Root", WindowManager.QUESTION_MESSAGE, select,
					Action2.okCancel(), getTarget());
			if (rc != 0) {
				LOG.fine("No selection");
			} else {
				sosaRoot = (Indi) select.getSelection();
				/* we set sosa root */
				sosaIndexation.setSosaRoot(sosaRoot);
				/* we set sosa gedcom */
				sosaIndexation.setSosaGedcom(gedcom);
				/* we build sosa indexation */
				sosaIndexation.setSosaIndexation(sosaRoot);
				LOG.fine("Indexation Sosa construite with :"
						+ sosaRoot.toString());
				/* we set sub-menu to change indexation */
				LOG.fine("Change menu item = "+ myMenuEnum.SOSA_CHANGE.getItem());
				menuItemSETCHANGE.setString(myMenuEnum.SOSA_CHANGE.getItem());
			}
			break;
		case SOSA_CHANGE:
			/* we change sosa indexation */
			LOG.fine("Need here ask for DeCujus");
			// SelectEntityWidget select = new
			// SelectEntityWidget(gedcom,Gedcom.INDI, null);
			rc = WindowManager.getInstance(getTarget()).openDialog(null,
					"Select Sosa Root", WindowManager.QUESTION_MESSAGE, select,
					Action2.okCancel(), getTarget());
			sosaRoot = rc == 0 ? (Indi) select.getSelection() : null;
			if (sosaRoot != null) {
				LOG.fine("Sosa root=" + sosaRoot.toString());
				if (sosaRoot != sosaIndexation.getSosaRoot()) {
					/*
					 * we remove previous indexation including Sosa properties
					 * and map entry
					 */
					sosaIndexation.removeSosaIndexationFromIndi(sosaIndexation
							.getSosaRoot(), 1);
					/* we set sosa root */
					sosaIndexation.setSosaRoot(sosaRoot);
					/* we set sosa gedcom */
					sosaIndexation.setSosaGedcom(gedcom);
					/* we build sosa indexation */
					sosaIndexation.setSosaIndexation(sosaRoot);
					LOG.fine("Indexation Sosa built with :"
							+ sosaRoot.toString());
				}
			}
			break;
		}
	}
}
