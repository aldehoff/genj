/**
 * This GenJ Sosa Plugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genj.plugin.sosa;

/* genj imported classes */
import genj.app.ExtendGedcomClosed;
import genj.app.ExtendGedcomOpened;
import genj.app.ExtendMenubar;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomLifecycleEvent;
import genj.gedcom.GedcomLifecycleListener;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyChild;
import genj.gedcom.PropertyFamilyChild;
import genj.gedcom.PropertyFamilySpouse;
import genj.gedcom.PropertyHusband;
import genj.gedcom.PropertyWife;
import genj.plugin.ExtensionPoint;
import genj.plugin.Plugin;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.view.ExtendContextMenu;
/* java imported classes */
// import java.util.ArrayList;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Iterator;
// import java.util.Map;
// import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Indexation of individuals
 */
public class SosaPlugin implements Plugin, GedcomLifecycleListener, GedcomListener {

	static String SOSA_SET="Set indexation with...";
	static String SOSA_CHANGE="Change indexation to...";
	static String SOSA_GET="Get individual from index...";
	static String SOSA_REMOVE="Remove all indexes...";
	private boolean fileRecordedDataFlag = true;

	/* we need this to be fixed as the image cannot be displayed anywhere */
	private final ImageIcon IMG = new ImageIcon(this, "/Sosa.gif");

	/* we need some information on this RESOURCES plugin use */
	private final Resources RESOURCES = Resources.get(this);

	private Logger LOG = Logger.getLogger("genj.plugin.sosa");

	private Registry sosaRegistry;

	private ExtendMenubar menuSosa;

	private SosaMenuAction menuActionSetOrChangeIndexation;
	private GetIndividualFromIndexMenuAction menuActionGetIndividualFromIndex;
	private RemoveIndexationMenuAction menuActionRemoveIndexation;

	private Indi sosaRoot;

	//FIX ME :no way to get access to a non null sosaIndexation from MenuAction
	private SosaIndexation sosaIndexation;

	// private Entity addedEntity;
	// private Indi addedIndi;
	// private Fam addedFam;
	private Indi _toIndi;

	private Indi _fromIndi;

	private Fam _fromFam;

	// private Fam _toFam;
	private String _sosaValue;

	private Property _sosaProperty;

	// private boolean deletedFamcFromIndi=false;
	// private boolean deletedFamsFromIndi=false;
	// private boolean deletedWifeCutFromFam=false;
	// private boolean deletedHusbCutFromFam=false;
	// private boolean childCutFromFam=false;
	// private boolean deletedChildFromFam=false;
	// private boolean famsCutFromIndi=false;
	// private boolean deletedSosaProperty=false;
	// private boolean addedSosaProperty=false;
	// private boolean childAddedToFamc=false;
	// private boolean createIndi=false;
	// private boolean createFam=false;
	// private boolean addWifeToFam=false;
	// private boolean addFamsToIndi=false;
	// private boolean addHusbToFam=false;
	// private boolean _CHILCutFromFAM=false;
	// private boolean _CHILAddedToFAM=false;

	// String family;
	// String individual;
	Indi _indi;

	Fam _fam;

	/* we remove added _SOSA property from indi */
	// private boolean removeSosa=false;
	// private Property propertySosa;
	// private Indi indiSosa;
	private enum interactionType {
		_NULL, _SOSACutFromINDI, _SOSAAddedToINDI, _SOSAModifiedInINDI, _SOSADeletedFromINDI, _SOSASetValueToINDI, _CHILCutFromFAM, _CHILAddedToFAM, _newINDIInFAM, _newFAM
	};

	private interactionType action = interactionType._NULL;

	// private ArrayList<String>myList=new ArrayList<String>();
	// public String sosaIndexArray[];
	///**
	// * Constructor
	// * 
	// * @see genj.plugin.Plugin#extend(genj.plugin.ExtensionPoint)
	// * 
	// */
	//SosaPlugin (SosaIndexation sosaIndexation) {
	//	this.sosaIndexation=sosaIndexation;
	//}
	/**
	 * Our change to enrich an extension point
	 * 
	 * @see genj.plugin.Plugin#extend(genj.plugin.ExtensionPoint)
	 * 
	 */
	public void extend(ExtensionPoint ep) {

		if (ep instanceof ExtendGedcomOpened) {
			LOG.fine("Flag= " + isExtendSosaIndexation());
			/* we attach the plugin to gedcom */
			Gedcom gedcom = ((ExtendGedcomOpened) ep).getGedcom();
			gedcom.addLifecycleListener(this);
			gedcom.addGedcomListener(this);
			LOG.fine("2-Ouverture Plugin");
			LOG.fine("3-Vérification sosa.root");
			sosaRegistry = genj.util.Registry.lookup(gedcom);
			/* we get sosa.root */
			String registryValue = sosaRegistry.get("sosa.root", (String) null);
			// note value to be removed
			registryValue = "tagada tsouin tsoin (I222)";
			// note : after plugin installation sosa.root is not be initialized
			if (registryValue == null) {
				/* no sosa.root : first installation of plugin */
				LOG.fine("Première installation : pas d'indexation Sosa");
				sosaRoot=null;
				/* we set here a sub-menu = Install Sosa indexation */
				menuActionSetOrChangeIndexation.setString(SosaMenuAction.myMenuEnum.SOSA_SET.getItem());
				menuSosa.addAction(SosaMenuAction.SOSA_MENU, menuActionSetOrChangeIndexation);
				// done
			} else {
				/* we have sosa.root : we check for value recorded */
				if (registryValue.equals("")) {
					/*
					 * we have sosa.root = blank -> we install a "Set
					 * indexation" menu item
					 */
					// note : this test is necessary if we cannot remove
					// sosa.root and therefore
					// it may have to be blanked ; to be confirmed
					LOG.fine("3-a : Pas d'indexation Sosa");
					/* we set here a sub-menu = "Install indexation" */
					menuActionSetOrChangeIndexation.setString(SosaMenuAction.myMenuEnum.SOSA_SET.getItem());
					menuSosa.addAction(SosaMenuAction.SOSA_MENU,menuActionSetOrChangeIndexation);
					// done
				} else {
					/* there is a sosa.root = DeCujus */
					LOG.fine("3-b : sosa.root = " + registryValue);
					/* we extract DeCujus individual */
					sosaRoot = (Indi) gedcom.getEntity(Gedcom.INDI,	registryValue.substring(registryValue.lastIndexOf("(") + 1, registryValue.lastIndexOf(")")));
					LOG.fine("Sosa root=" + sosaRoot);
					/* we check for recorded sosa indexation */
					// fileRecordedDataFlag is supposed to be changes in
					// "Option"
					boolean setIndexationFlag;
					if (fileRecordedDataFlag) {
						LOG.fine("Enregistrement indexation Sosa = "+ fileRecordedDataFlag);
						LOG.fine("Rien à faire");
						setIndexationFlag = false;
					} else {
						LOG.fine("Enregistrement indexation Sosa = "+ fileRecordedDataFlag);
						/* we set sosa indexation */
						LOG.fine("Indexation Sosa construite");
						LOG.fine("We have to set Sosa from DeCuJus");
						setIndexationFlag = true;
					}
					/* we set here a sub-menu = "Change indexation" */
					menuActionSetOrChangeIndexation.setString(SosaMenuAction.myMenuEnum.SOSA_CHANGE.getItem());
					menuSosa.addAction(SosaMenuAction.SOSA_MENU,menuActionSetOrChangeIndexation);
					/* we set sosa indexation */
					sosaIndexation = new SosaIndexation(sosaRoot, gedcom);
					/* we set sosa indexation value in menuAction instances */
					menuActionSetOrChangeIndexation.setSosaIndexationValue(sosaIndexation);
					menuActionGetIndividualFromIndex.setSosaIndexationValue(sosaIndexation);
					menuActionRemoveIndexation.setSosaIndexationValue(sosaIndexation);
					LOG.fine("Indexation Sosa installée avec :"+sosaIndexation.getSosaRoot());
					// done
				}
			}
			// done
			return;
		}

		if (ep instanceof ExtendGedcomClosed) {
			/* we detach plugin from gedcom */
			Gedcom gedcom = ((ExtendGedcomClosed) ep).getGedcom();
			/* we have to initiate all actions needed */
			/* we save sosaRoot */
			sosaRegistry.put("sosa.root", sosaRoot.toString());
			LOG.fine("Sauvegarde de sosaRoot = " + sosaRoot.toString());
			/* we check whether _SOSA tags must be saved or not */
			// ne marche pas : à revoir
			if (!fileRecordedDataFlag) {
				/* we remove all _SOSA tags from gedcom */
				sosaIndexation.removeSosaIndexationFromAllIndis();
				LOG.fine("Pas de sauvegarde des index");
			}
			gedcom.removeLifecycleListener(this);
			gedcom.removeGedcomListener(this);
			LOG.fine("Fermeture Plugin");
			// done
			return;
		}

		if (ep instanceof ExtendContextMenu) {
			// show a context related sosa action
			ExtendContextMenu _menuSosa = (ExtendContextMenu) ep;
			// _menuSosa.addAction(SOSA_MENU,new MenuAction();
			// ((ExtendContextMenu)ep).getContext().addAction(SOSA_MENU)));
			// ((ExtendContextMenu)ep).getContext().addAction("SSS",new
			// Action2(RESOURCES.getString("info"),true));
			// show a context related tracker action
			// ((ExtendContextMenu)ep).getContext().addAction("Sosa
			// indexation",new
			// Action2(RESOURCES.getString("action.remove"),false));
			// _menuSosa.getContext().addAction("Tools",new
			// Action2(RESOURCES.getString("HELP"),false));
			// ((ExtendContextMenu)ep).getContext().addAction("Tools",new
			// Action2(RESOURCES.getString("HELP"), false));
			// log("cocou");
			LOG.fine("passe dans ExtendContextMenu");
		}

		if (ep instanceof ExtendMenubar) {
			Gedcom gedcom = ((ExtendMenubar) ep).getGedcom();
			/* we show a sosa action */
			menuSosa = (ExtendMenubar) ep;
			LOG.fine("1 : installation menuSosa = " + menuSosa);
			/* we add sub-menu info */
			LOG.fine("1a : Addition of Info sub-menu");
			/* we display info */
			menuSosa.addAction(SosaMenuAction.SOSA_MENU, new Action2(RESOURCES.getString("info"), true));
			/* we add sub-menu menuActionSetOrChangeIndexation */
			LOG.fine("Addition of menuActionSetOrChangeIndexation sub-menu");
			menuActionSetOrChangeIndexation = new SosaMenuAction(SosaMenuAction.myMenuEnum.SOSA_SET.getItem(), sosaIndexation, gedcom);
			/* we display menuActionSetOrChangeIndexation */
			menuSosa.addAction(SosaMenuAction.SOSA_MENU, menuActionSetOrChangeIndexation);
			/* we add sub-menu menuActionGetIndividualFromIndex */
			LOG.fine("1b : Addition of menuActionGetIndividualFromIndex sub-menu");
			SosaMenuAction menuActionGetIndividualFromIndex = new SosaMenuAction(SosaMenuAction.myMenuEnum.SOSA_GET.getItem(), sosaIndexation, gedcom);
			/* we display menuActionSetOrChangeIndexation */
			menuSosa.addAction(SosaMenuAction.SOSA_MENU,menuActionGetIndividualFromIndex);
			/* we add sub-menu SOSA_REMOVE */
			LOG.fine("1c : ********Addition of RemoveIndexationMenuAction sub-menu");
			//SosaMenuAction menuActionRemoveAllIndexation = new SosaMenuAction(SosaMenuAction.myMenuEnum.SOSA_REMOVE.getItem(), sosaIndexation, gedcom);
			RemoveIndexationMenuAction menuActionRemoveAllIndexation = new RemoveIndexationMenuAction(SOSA_REMOVE, sosaIndexation, gedcom);
			/* we display menuActionSetOrChangeIndexation */
			menuSosa.addAction(SosaMenuAction.SOSA_MENU, menuActionRemoveAllIndexation);

		}

	}

	public void handleLifecycleEvent(GedcomLifecycleEvent event) {
		/* more stuff to clarify with Nils */
		// HEADER_CHANGED = 0,
		// WRITE_LOCK_ACQUIRED = 1,
		// BEFORE_UNIT_OF_WORK = 2,
		// AFTER_UNIT_OF_WORK = 3,
		// WRITE_LOCK_RELEASED = 4;
		LOG.fine("Lifecycle event ID = " + event.getId());
		if (event.getId() == GedcomLifecycleEvent.AFTER_UNIT_OF_WORK) {
			switch (action) {
			case _CHILCutFromFAM:
				sosaIndexation.restoreSosaInChildCutFromFam(_toIndi, _fromFam);
				action = interactionType._NULL;
				break;
			case _CHILAddedToFAM:
				sosaIndexation.restoreSosaInChildAddedToFam(_toIndi, _fromFam);
				action = interactionType._NULL;
				break;
			case _SOSACutFromINDI:
				action = interactionType._SOSAAddedToINDI;
				sosaIndexation.restoreSosaValueToIndi(_fromIndi, _sosaValue);
				action = interactionType._NULL;
				break;
			case _SOSAAddedToINDI:
				action = interactionType._SOSACutFromINDI;
				sosaIndexation.deleteExistingSosaIndexFromIndi(_toIndi, _sosaProperty);
				action = interactionType._NULL;
				break;
			case _SOSADeletedFromINDI:
				_toIndi.delProperty(_sosaProperty);
				action = interactionType._NULL;
				break;
			case _SOSASetValueToINDI:
				// on ne pase pas ici
				LOG.fine("passe ici coucou");
				_sosaProperty.setValue(_sosaValue);
				action = interactionType._NULL;
				break;
			case _newINDIInFAM:
				// something to be done
				action = interactionType._NULL;
				break;
			case _newFAM:
				// something to be done
				action = interactionType._NULL;
				break;
			default:
				LOG.fine("2- Lifecycle event ID = " + event.getId());
				break;
			}
		}
	}

	public void gedcomPropertyLinked(Gedcom gedcom, Property from, Property to) {
		LOG.fine("Link Property from : " + from.getValue());
		LOG.fine("Link Property to : " + to.getValue());
		LOG.fine("Link Property from : " + from.getEntity());
		LOG.fine("Link Property to : " + to.getEntity());
		if (from instanceof PropertyChild) {
			/* case CHIL added to FAM */
			_toIndi = (Indi) to.getEntity();
			_fromFam = (Fam) from.getEntity();
			action = interactionType._CHILAddedToFAM;
		} else {
			if (from instanceof PropertyFamilyChild) {
				/* case FAM added to INDI */
				LOG.fine("PASS:PropertyFamilyChild");
			} else {
				if (from instanceof PropertyFamilySpouse) {
					/* case FAMS added to INDI */
					LOG.fine("PASS:PropertyFamilySpouse");
				} else {
					if (from instanceof PropertyHusband) {
						/* case HUB added to FAM */
						LOG.fine("PASS:PropertyHusband");
					} else {
						if (from instanceof PropertyWife) {
							/* case WIFE added to FAM */
							LOG.fine("PASS:PropertyWife");
						}
					}
				}
			}
		}
	}

	public void gedcomPropertyUnlinked(Gedcom gedcom, Property from, Property to) {
		LOG.fine("Unlink Property from : " + from.getValue());
		LOG.fine("Unlink Property to : " + to.getValue());
		LOG.fine("Unlink Property from : " + from.getEntity());
		LOG.fine("Unlink Property to : " + to.getEntity());
		action = interactionType._NULL;
		if (from instanceof PropertyChild) {
			/* case CHIL cut from FAM */
			_toIndi = (Indi) to.getEntity();
			_fromFam = (Fam) from.getEntity();
			action = interactionType._CHILCutFromFAM;
		} else {
			if (from instanceof PropertyFamilyChild) {
				/* case FAM cut from INDI */
				LOG.fine("PASS:PropertyFamilyChild");
			} else {
				if (from instanceof PropertyFamilySpouse) {
					/* case FAMS cut from INDI */
					LOG.fine("PASS:PropertyFamilySpouse");
				} else {
					if (from instanceof PropertyHusband) {
						/* case HUSB cut from FAM */
						LOG.fine("PASS:PropertyHusband");
					} else {
						if (from instanceof PropertyWife) {
							/* case WIFE cut from FAM */
							LOG.fine("PASS:PropertyWife");
						}
					}
				}
			}
		}
	}

	/**
	 * notification that an entity has been added
	 * 
	 * @see GedcomListener#gedcomEntityAdded(Gedcom, Entity)
	 */

	public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
		// more stuff to clarify with Nils
		/* we test here the type of entity added */
		LOG.fine("Entity added : " + entity);
		if (entity.getTag().equals(Gedcom.INDI)) {

			// FIXME
			// addedIndi=(Indi)entity;
			action = interactionType._newINDIInFAM;
		} else {
			if (entity.getTag().equals(Gedcom.FAM)) {
				// FIXME
				// addedFam=(Fam)entity;
				// action=interactionType._NULL;
				action = interactionType._newFAM;
			}
		}
	}

	/**
	 * notification that an entity has been deleted
	 * 
	 * @see GedcomListener#gedcomEntityDeleted(Gedcom, Entity)
	 */

	public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
		// more stuff to clarify with Nils\"
		LOG.fine("Entity deleted : " + entity);
	}

	/**
	 * notification that a property has been added
	 * 
	 * @see GedcomListener#gedcomPropertyAdded(Gedcom, Property, int, Property)
	 */

	public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos,
			Property added) {
		// more stuff to clarify with Nils
		/**
		 * notification that a property has been deleted we track here the
		 * following cut actions : - add _SOSA to an individual : sequence _SOSA
		 * to INDI (A) - cut a child from a family : sequence FAMC from INDI
		 * (1b) + sequence CHIL from FAM (2a) - cut a husband from a family :
		 * sequence FAMS from INDI (3c) + sequence HUSB from FAM (4a) - cut a
		 * wife from a family : sequence FAMS from INDI (3c) + sequence HUSB
		 * from FAM (5a) - cut a individual mariage from a male individual :
		 * sequence HUB from FAM (4b) + sequence FAMS from FAM (3a) - cut a
		 * individual mariage from a female individual : sequence WIFE from FAM
		 * (5a) + sequence FAMS from FAM (3a) - cut a parent mariage from a
		 * individual : sequence HUB from FAM (4b) + sequence FAMS from FAM (3a)
		 * 
		 * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int,
		 *      Property)
		 */
		String propertyTag = property.getTag();
		String addedTag = added.getTag();
		// following line just to help building code to be removed
		LOG.fine("((addedTag.equals(\"" + addedTag
				+ "\")) && (propertyTag.equals(\"" + propertyTag + "\")))");
		// --BEGIN add action of _SOSA tag----
		// here we prevent users from adding _SOSA property
		if (addedTag.equals("_SOSA") && (propertyTag.equals("INDI"))) {
			/* (A) case added _SOSA to INDI */
			switch (action) {
			case _NULL:
				boolean b = false;
				if (b) {
					LOG
							.fine("1 - Sorry addition of _SOSA tag is not possible !");
					_toIndi = (Indi) added.getEntity();
					_sosaProperty = added;
					/* we set action for process */
					action = interactionType._SOSAAddedToINDI;
				} else {
					// new possibility
					LOG
							.fine("1 - Sorry addition of _SOSA tag is not possible !");
					_toIndi = (Indi) added.getEntity();
					_sosaProperty = added;
					// sosaIndexation.deleteSosaIndexFromIndi(_toIndi);
					action = interactionType._SOSAAddedToINDI;
				}
				break;
			case _SOSAAddedToINDI:
				LOG.fine("Addition is ok");
				break;
			default:
				LOG.fine("Cut action is cancelled");
				break;
			}
		}
		// --END add action on _SOSA----
	}

	/**
	 * notification that a property has been changed
	 * 
	 * @see GedcomListener#gedcomPropertyChanged(Gedcom, Property)
	 */

	public void gedcomPropertyChanged(Gedcom gedcom, Property property) {

		// more stuff to clarify with Nils
		LOG.fine("sosa indexation " + isExtendSosaIndexation());
		LOG.fine("Property modified = " + property.getTag());
		LOG.fine("Property value = " + property.getValue());
		if (property.getTag().equals("_SOSA")) {
			LOG.fine("_SOSA modified");
			switch (action) {
			case _SOSAModifiedInINDI:
				/*
				 * we go though this when modifying Sosa properties in an after
				 * 3 cycle
				 */
				action = interactionType._NULL;
				LOG.fine("_SOSA modification : confirmed");
				break;
			default:
				LOG.fine("_SOSA : passe ici");
				break;
			}
		}
	}

	/**
	 * notification that a property has been deleted we track here the following
	 * cut actions : - cut a _SOSA from an individual : sequence _SOSA from INDI
	 * (A)
	 * 
	 * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int,
	 *      Property)
	 */

	public void gedcomPropertyDeleted(Gedcom gedcom, Property property,
			int pos, Property deleted) {
		// more stuff to clarify with Nils
		String propertyTag = property.getTag();
		String deletedTag = deleted.getTag();
		LOG.fine("((deletedTag.equals(\"" + deletedTag
				+ "\")) && (propertyTag.equals(\"" + propertyTag + "\")))");
		// --BEGIN cut action on _SOSA----
		// here we prevent users from cutting _SOSA property
		if (deletedTag.equals("_SOSA") && (propertyTag.equals("INDI"))) {
			/* (A) case deleted _SOSA from INDI */
			switch (action) {
			case _NULL:
				LOG.fine("Sorry cut of _SOSA tag is not possible !");
				_fromIndi = (Indi) deleted.getEntity();
				_sosaValue = deleted.getValue();
				action = interactionType._SOSACutFromINDI;
				break;
			case _SOSADeletedFromINDI:
				LOG.fine("_SOSA removal is confirmed");
				action = interactionType._NULL;
				break;
			default:
				LOG.fine("Add action is cancelled");
				break;
			}
		}
		//--END cut action on _SOSA---- 
	}

	/**
	 * Check whether sosa indexation is actually turned on by user
	 */
	private boolean isExtendSosaIndexation() {
		return SosaOptions.getInstance().isExtendSosaIndexation;
	}
}