/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.gedcom;

import java.util.*;
import java.io.*;
import genj.util.*;

/**
 * The object-representation of a Gedom file
 */
public class Gedcom implements GedcomListener {

  private boolean          isTransaction = false;
  private boolean          hasUnsavedChanges;
  private Origin            origin;
  private Vector            listeners = new Vector();
  private Entity            lastEntity = null;
  private Vector            addedEntities     ,
                             deletedEntities   ;
  private Vector            addedProperties   ,
                             deletedProperties ,
                             modifiedProperties;
  private EntityList[]      entities = new EntityList[LAST_ETYPE-FIRST_ETYPE+1];
  private IDHashtable[]     ids = new IDHashtable[LAST_ETYPE-FIRST_ETYPE+1];

  static private Random    seed = new Random();
  static private Resources resources;

  public final static int
    REL_NONE    = 1,
    REL_PARENT  = 2,
    REL_CHILD   = 3,
    REL_SPOUSE  = 4,
    REL_SIBLING = 5;

  public final static int
    TAG_ENTITY_SOURCE   = 1,
    TAG_PROPERTY_SOURCE = 2;

  public final static int
    MALE   = 1,
    FEMALE = 2;

  private final static String[]
    ePrefixs  = { "I", "F", "M", "N", "S", "B", "R"},
    eTags     = { "INDI", "FAM", "OBJE", "NOTE", "SOUR", "SUBM", "REPO" };

  public final static int
    INDIVIDUALS  = 0,
    FAMILIES     = 1,
    MULTIMEDIAS  = 2,
    NOTES        = 3,
    SOURCES      = 4,
    SUBMITTERS   = 5,
    REPOSITORIES = 6,
    FIRST_ETYPE  = INDIVIDUALS,
    LAST_ETYPE   = REPOSITORIES;

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin) {
    init(origin,100);
  }

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin, int initialCapacity) {
    init(origin,initialCapacity);
  }

  /**
   * Adds a Listener which will be notified when data changes
   */
  public synchronized void addListener(GedcomListener which) {
    listeners.addElement(which);
  }

  /**
   * Close Gedcom object
   */
  public void close() {

    // Signal to listeners
    Object ls[] = new Object[listeners.size()];
    listeners.copyInto(ls);

    GedcomListener listener;
    for (int i=0;i<ls.length;i++) {
      listener  = (GedcomListener)ls[i];
      listener.handleClose(this);
    }

    // Done
  }

  /**
   * Creates a child for given fam
   * @exception GedcomException in case of error during creation
   */
  private Indi createChildOf(Fam fam, String lastName, String firstName, int sex) throws GedcomException {

    // Create new child & add to list of indis
    Indi child = createIndi(lastName,firstName,sex);

    // Connect fam, and child
    fam.addChild(child);

    // Done
    return child;
  }

  /**
   * Creates a child for given indi
   * @exception GedcomException in of error during creation
   */
  private Indi createChildOf(Indi indi, String lastName, String firstName, int sex) throws GedcomException {

    // Can new child be another child in indi's 1st fam ?
    Fam fam = indi.getFam(0);

    if (fam==null) {
      fam = createFam();
      // Connect Fam, Indi and Spouse
      fam.setParents(indi,null);
    }

    // Create child in family
    return createChildOf(fam,lastName,firstName,sex);

  }
  /**
   * Create a entity by tag
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(String tag, String id) throws GedcomException {

    // Indi ?
    if (getTagFor(INDIVIDUALS).equals(tag.toUpperCase())) {
      return createIndi(id);
    }
    // Fam ?
    if (getTagFor(FAMILIES).equals(tag.toUpperCase())) {
      return createFam(id);
    }
    // Media ?
    if (getTagFor(MULTIMEDIAS).equals(tag.toUpperCase())) {
      return createMedia(id);
    }
    // Notes
    if (getTagFor(NOTES).equals(tag.toUpperCase())) {
      return createNote(id);
    }
    // Sources
    if (getTagFor(SOURCES).equals(tag.toUpperCase())) {
      return createSource(id);
    }
    // Submitters
    if (getTagFor(SUBMITTERS).equals(tag.toUpperCase())) {
      return createSubmitter(id);
    }
    // Repository
    if (getTagFor(REPOSITORIES).equals(tag.toUpperCase())) {
      return createRepository(id);
    }
    // Unknown
    throw new GedcomException("Unknown tag for entity");
  }

  /**
   * Creates a non-related family
   * @exception GedcomException in of error during creation
   */
  public Fam createFam() throws GedcomException {
    return createFam(null);
  }

  /**
   * Creates a family in gedcom (related or not)
   * @exception GedcomException in case relative of family is not member of same gedcom
   */
  public Fam createFam(int memberIs, Entity member) throws GedcomException {

    // Is the member of that fam a member of this gedcom (individual)
    if (member!=null) {
      boolean ok = false;
      for (int i=0;i<getEntities(INDIVIDUALS).getSize();i++) {
        if (getIndi(i)==member) {
          ok = true;
          break;
        }
      }
      if (!ok)
        throw new GedcomException("Given Relative isn't individual in this gedcom");
    }

    // Prepare variable of returned individual
    Fam newfam = null;

    // Check relation
    switch (memberIs) {
      // .. no relationship
      case REL_NONE:
        newfam = createFam();
        break;
      // .. parent
      case REL_PARENT:
        // .. parm o.k.
        if ( (member==null) || (member instanceof Fam) )
          throw new GedcomException("Creating a family with given parent needs parameter parent !");
        // .. do it
        newfam = createFamilyFor((Indi)member);
        break;
      // .. new child
      case REL_CHILD:
        // .. parm o.k.
        if ( (member==null) || (member instanceof Fam) )
          throw new GedcomException("Creating a family with given child needs parameter child !");
        // .. do it
        newfam = createParentalFamilyFor((Indi)member);
        break;
    // .. end case
    }

    // No supported relation ?
    if (newfam==null)
      throw new GedcomException("Gedcom.Unsupported relation !");

    // Done
    return newfam;
  }

  /**
   * Creates a non-related family
   * @exception GedcomException in of error during creation
   */
  public Fam createFam(String id) throws GedcomException {

    // Check the id
    if ((id!=null)&&(ids[FAMILIES].contains(id))) {
      throw new DuplicateIDException("Family with id "+id+" is alread defined");
    }

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(FAMILIES);
    }
    
    // Create fam & add to list of indis
    Fam fam = new Fam(this);
    noteAddedEntity(fam);
    entities[FAMILIES].add(fam);

    // Store id
    ids[FAMILIES].put(id,fam);

    fam.setId(id);

    // Done
    return fam;
  }

  /**
   * Creates a family for a given parent
   * @exception GedcomException in case of error during creation
   */
  private Fam createFamilyFor(Indi indi) throws GedcomException {

    // Create family & add to list of fams
    Fam fam = createFam();

    // Connect Fam &  Indi
    fam.setParents(indi,null);

    // Create child in family
    return fam;
  }

  /**
   * Creates a non-related individual
   * @exception GedcomException in of error during creation
   */
  public Indi createIndi() throws GedcomException {
    return createIndi(null);
  }

  /**
   * Creates a non-related individual
   * @exception GedcomException in of error during creation
   */
  public Indi createIndi(String id) throws GedcomException {

    // Check the id
    if ((id!=null)&&(ids[INDIVIDUALS].contains(id))) {
      throw new DuplicateIDException("Individual with id "+id+" is alread defined");
    }

    // Calculate id if necessary
    if (id==null) {
      id = getRandomIdFor(INDIVIDUALS);
    }

    // Create indi & add to list of indis
    Indi indi = new Indi(this);
    noteAddedEntity(indi);
    entities[INDIVIDUALS].add(indi);

    // Store id in individual
    ids[INDIVIDUALS].put(id,indi);
    indi.setId(id);

    // Done
    return indi;
  }

  /**
   * Creates a non-related individual
   * @exception GedcomException in of error during creation
   */
  public Indi createIndi(String lastName, String firstName, int sex) throws GedcomException {

    Indi indi = createIndi(null);

    // NAME
    if ((lastName!=null)&&(firstName!=null)) {
      PropertyName pn = new PropertyName();
      pn.setName(firstName, lastName);
      indi.addProperty(pn);
    }

    // SEX
    if ((sex == Gedcom.MALE) || (sex == Gedcom.FEMALE)) {
      indi.addProperty(new PropertySex(sex));
    } else {
      indi.addProperty(new PropertySex());
    }

    // BIRT
    indi.addProperty(Property.createInstance("BIRT", true));

    // DEAT
    indi.addProperty(Property.createInstance("DEAT", true));

    // OCCU
    indi.addProperty(Property.createInstance("OCCU", true));

    // ADDR
    indi.addProperty(Property.createInstance("ADDR", true));

    // NOTE
    indi.addProperty(Property.createInstance("NOTE", true));

    return indi;
  }

  /**
   * Creates a new person in gedcom (related or not)
   * @exception GedcomException in of error during creation
   */
  public Indi createIndi(String lastName, String firstName, int sex, int relatedTo, Entity relative)  throws GedcomException {

    // Is the relative a member of this gedcom
    while (relative!=null) {
      boolean ok = false;
      for (int i=0;i<getEntities(INDIVIDUALS).getSize();i++)
      if (getIndi(i)==relative) {
        ok = true;
        break;
      }
      if (ok) break;
      for (int i=0;i<getEntities(FAMILIES).getSize();i++)
      if (getFam(i)==relative) {
        ok = true;
        break;
      }
      if (ok) break;

      throw new GedcomException("Gedcom.Given Relative isn't member of this gedcom");
    }

    // Prepare variable of returned individual
    Indi newindi = null;

    // Check relation
    switch (relatedTo) {
      // .. no relationship
      case REL_NONE:
        newindi = createIndi(lastName,firstName,sex);
        break;
      // .. new parent
      case REL_PARENT:
        // .. parm o.k.
        if ( (relative==null) || (relative instanceof Fam) )
          throw new GedcomException("Creating a parent needs parameter child !");
        // .. do it
        newindi = createParentOf((Indi)relative,lastName,firstName,sex);
        break;
      // .. new child
      case REL_CHILD:
        // .. parm o.k.
        if (relative==null)
          throw new GedcomException("Creating a child needs parameter parent !");
        // .. do it
        if (relative instanceof Fam)
          newindi = createChildOf((Fam)relative,lastName,firstName,sex);
        else
          newindi = createChildOf((Indi)relative,lastName,firstName,sex);
        break;
      // .. new spouse
      case REL_SPOUSE:
        // .. parm o.k.
        if (relative==null)
          throw new GedcomException("Creating a spouse needs parameter spouse !");
        // .. do it
        if (relative instanceof Fam)
          newindi = createSpouseOf((Fam)relative,lastName,firstName,sex);
        else
          newindi = createSpouseOf((Indi)relative,lastName,firstName,sex);
        break;
      // .. new sibling
      case REL_SIBLING:
        // .. parm o.k.
        if ( (relative==null) || (relative instanceof Fam) )
          throw new GedcomException("Creating a sibling needs parameter sibling !");
        // .. do it
        newindi = createSiblingOf((Indi)relative,lastName,firstName,sex);
        break;
    // .. end case
    }

    // No supported relation ?
    if (newindi==null)
      throw new GedcomException("Gedcom.Unsupported relation !");

    // Done
    return newindi;
  }

  /**
   * Creates a non-related media
   * @exception GedcomException in of error during creation
   */
  public Media createMedia() throws GedcomException {
    return createMedia(null, null);
  }

  /**
   * Creates a related media
   * @exception GedcomException in of error during creation
   */
  public Media createMedia(Entity attachedTo) throws GedcomException {
    return createMedia(null, attachedTo);
  }

  /**
   * Creates a non-related media
   * @exception GedcomException in of error during creation
   */
  /*package*/ Media createMedia(String id) throws GedcomException {
    return createMedia(id,null);
  }

  /**
   * Creates a related media
   * @exception GedcomException in of error during creation
   */
  /*package*/ Media createMedia(String id, Entity attachedTo) throws GedcomException {

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(MULTIMEDIAS);
    }

    // Create media & add to list of indis
    Media media = new Media(this);
    noteAddedEntity(media);
    entities[MULTIMEDIAS].add(media);

    // Store id
    ids[MULTIMEDIAS].put(id,media);

    media.setId(id);

    // Attach?
    if (attachedTo!=null) {
      media.getProperty().addDefaultProperties();
      attachedTo.getProperty().addMedia(media);
    }

    // Done
    return media;
  }

  /**
   * Creates a non-related note
   * @exception GedcomException in of error during creation
   */
  public Note createNote() throws GedcomException {
    return createNote(null, null);
  }

  /**
   * Creates a related note
   * @exception GedcomException in of error during creation
   */
  public Note createNote(Entity attachedTo) throws GedcomException {
    return createNote(null, attachedTo);
  }

  /**
   * Creates a non-related note
   * @exception GedcomException in of error during creation
   */
  /*package*/ Note createNote(String id) throws GedcomException {
    return createNote(id, null);
  }

  /**
   * Creates a non-related note
   * @exception GedcomException in of error during creation
   */
  /*package*/ Note createNote(String id, Entity attachedTo) throws GedcomException {

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(NOTES);
    }

    // Create note & add to list of notes
    Note note = new Note(this);
    noteAddedEntity(note);
    entities[NOTES].add(note);

    // Store id
    ids[NOTES].put(id,note);
    note.setId(id);

    // Attach?
    if (attachedTo!=null) {
      attachedTo.getProperty().addNote(note);
    }

    // Done
    return note;
  }

  /**
   * Creates a Repository entity.
   * @exception GedcomException in of error during creation
   */
  /*package*/ Source createSource(String id) throws GedcomException {
    return createSource(id, null);
  }

  /**
   * Creates a Repository entity.
   * @exception GedcomException in of error during creation
   */
  /*package*/ Source createSource(String id, Entity attachedTo) throws GedcomException {

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(SOURCES);
    }

    // Create source & add to list of sources
    Source source = new Source(this);
    noteAddedEntity(source);
    entities[SOURCES].add(source);

    // Store id
    ids[SOURCES].put(id,source);
    source.setId(id);

    // Attach?
    if (attachedTo!=null) {
      attachedTo.getProperty().addSource(source);
    }
    // Done
    return source;
  }

  /**
   * Creates a non-related submitter
   * @exception GedcomException in of error during creation
   * dkionka: blindly copied createNote()
   */
  /*package*/ Submitter createSubmitter(String id) throws GedcomException {

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(SUBMITTERS);
    }

    // Create submitter & add to list of submitters
    Submitter submitter = new Submitter(this);
    noteAddedEntity(submitter);
    entities[SUBMITTERS].add(submitter);

    // Store id
    ids[SUBMITTERS].put(id,submitter);
    submitter.setId(id);

    // Done
    return submitter;
  }

  /**
   * Creates a Repository entity.
   * @exception GedcomException in of error during creation
   */
  /*package*/ Repository createRepository(String id) throws GedcomException {
    return createRepository(id, null);
  }

  /**
   * Creates a Repository entity.
   * @exception GedcomException in of error during creation
   */
  /*package*/ Repository createRepository(String id, Entity attachedTo) throws GedcomException {

    // Generate id if necessary
    if (id==null) {
      id = getRandomIdFor(REPOSITORIES);
    }

    // Create repository & add to list of repositorys
    Repository repository = new Repository(this);
    noteAddedEntity(repository);
    entities[REPOSITORIES].add(repository);
    // Store id
    ids[REPOSITORIES].put(id,repository);
    repository.setId(id);

    // Attach?
    if (attachedTo!=null) {
      attachedTo.getProperty().addRepository(repository);
    }
    // Done
    return repository;
  }

  /**
   * Creates a family as childhood for given indi
   * @exception GedcomException in case individual already has parents
   */
  private Fam createParentalFamilyFor(Indi indi) throws GedcomException {

    // Does that indi already have a childhood ?
    if ( indi.getFamc() != null)
      throw new GedcomException("Individual has parents already");

    // Create family & add to list of fams
    Fam fam = createFam();

    // Connect Fam &  Indi
    fam.addChild(indi);

    // Create child in family
    return fam;
  }

  /**
   * Creates a parent for given indi
   * @exception GedcomException in case individual already has two parents
   */
  private Indi createParentOf(Indi indi, String lastName, String firstName, int sex) throws GedcomException {

    // Prepare fam for new Parent
    Fam fam;

    // Check if indi has one or two parents
    if (indi.getFamc()!=null) {
      // .. existing fam
      fam = indi.getFamc();
      // .. two spouses ?
      if (!fam.hasMissingSpouse()) {
        throw new GedcomException("Tried to create 3rd spouse in family !");
      }
      // .. max one spouse !
      Indi spouse = fam.getOtherSpouse(null);
      if (spouse!=null) {
        sex = PropertySex.calcOppositeSex(spouse, sex);
      }
    } else {
      // .. fam
      fam = createFam();
      fam.addChild(indi);
    }

    // Create new parent & add to list of indis
    Indi parent = createIndi(lastName,firstName,sex);

    // Connect fam & parent
    fam.setParents(parent,fam.getOtherSpouse(null));

    // Done
    return parent;
  }

  /**
   * Creates a sibling for given indi
   * @exception GedcomException in case of error during creation
   */
  private Indi createSiblingOf(Indi indi, String lastName, String firstName, int sex) throws GedcomException {

    // Prepare fam for Parent of siblings
    Fam fam;

    // Check if indi has parental family
    if (indi.getFamc()!=null) {
      fam = indi.getFamc();
    } else {
      fam = createFam();
      fam.addChild(indi);
    }

    // Create new sibling & add to list of indis
    Indi sibling = createIndi(lastName,firstName,sex);

    // Connect fam & parent
    fam.addChild(sibling);

    // Done
    return sibling;
  }

  /**
   * Creates a spouse for given relative fam
   * @exception GedcomException in case family already has two spouses
   */
  private Indi createSpouseOf(Fam fam, String lastName, String firstName, int sex) throws GedcomException {

    // Check if fam has a missing spouse
    if (!fam.hasMissingSpouse())
      throw new GedcomException("Tried to create 3rd spouse in family !");

    // Create new spouse & add to list of indis
    Indi spouse = createIndi(
      lastName,
      firstName,
      PropertySex.calcOppositeSex(fam.getOtherSpouse(null),sex)
    );

    // Connect fam, spouse and indi
    fam.setParents(spouse,fam.getOtherSpouse(null));

    // Done
    return spouse;
  }

  /**
   * Creates a spouse for given relative indi
   * @exception GedcomException in case of error during creation
   */
  private Indi createSpouseOf(Indi indi, String lastName, String firstName, int sex) throws GedcomException {

    // Can new spouse be missing partner in indi's 1st fam ?
    if ( (indi.getNoOfFams()>0) && (indi.getFam(0).hasMissingSpouse()) ) {
      return createSpouseOf(indi.getFam(0),lastName,firstName,sex);
    }

    // Create new spouse & add to list of indis
    Indi spouse = createIndi(
      lastName,
      firstName,
      PropertySex.calcOppositeSex(indi, sex)
    );

    // Create family & add to list of fams
    Fam fam = createFam();

    // Connect Fam, Indi and Spouse
    fam.setParents(indi,spouse);

    // Done
    return spouse;
  }

  /**
   * Deletes entity
   * @exception GedcomException in case unknown type of entity
   */
  public void deleteEntity(Entity which) throws GedcomException {

    // Type of entity
    int type = which.getType();

    // Entity exists ?
    if (!ids[type].contains(which.getId()))
      throw new GedcomException("Unknown entity with id "+which.getId());

    // Notify deletion
    noteDeletedEntity(which);

    // Delete it
    entities[type].del   (which);
    ids     [type].remove(which);

    which.delNotify();

    // Done
  }

  /**
   * Ends Transaction
   * @return the change that has been made
   */
  public synchronized Change endTransaction() {

    // Is there a transaction going on?
    if (!isTransaction)
      throw new RuntimeException("Attempt to end non existing Transaction");

    // Changes that need to signaled to listeners ?
    final Change change = new Change(
      this,
      addedEntities,
      deletedEntities,
      addedProperties,
      deletedProperties,
      modifiedProperties
    );

    hasUnsavedChanges |= (change.getChange()!=0);

    addedEntities      = null;
    deletedEntities    = null;
    addedProperties    = null;
    deletedProperties  = null;
    modifiedProperties = null;

    // ... send message to all listeners
    Object ls[] = new Object[listeners.size()];
    listeners.copyInto(ls);

    for (int i=0;i<listeners.size();i++) {
      ((GedcomListener)ls[i]).handleChange(change);
    }

    // ... End
    isTransaction = false;

    // ... Done
    return change;
  }

  /**
   * Proxy for notifying the selection of an entity. Can be used
   * by listeners to notify others in case of a selection event.
   */
  public synchronized void fireEntitySelected(GedcomListener from,Entity which,boolean doubleClick)  {

    // Is there a transaction running?
    if (isTransaction) {
        return;
    }

    // Entity ?
    if (which==null)
      throw new IllegalArgumentException("Selection can't be null");

    lastEntity = which;

    // Signal to listeners
    Selection selection = new Selection(which, from, doubleClick);
    GedcomListener listener;

    for (int i=0;i<listeners.size();i++) {
      listener  = (GedcomListener)listeners.elementAt(i);
      if (listener != from) {
        listener.handleSelection(selection);
      }
    }

    // Done
  }

  /**
   * Returns all entities
   */
  public EntityList[] getEntities() {
    return entities;
  }

  /**
   * Returns entities of given type
   */
  public EntityList getEntities(int type) {
    return entities[type];
  }

  /**
   * Returns the entity with given id
   */
  public Entity getEntityFromId(String id) throws DuplicateIDException {
    Entity result = null;
    for (int i=0;i<ids.length;i++) {
      result = ids[i].get(id);
      if (result!=null)
        break;
    }
    return result;
  }

  /**
   * Returns the entity with given id of given type or null if not exists
   */
  public Entity getEntityFromId(String id, int type) throws DuplicateIDException {
    return ids[type].get(id);
  }

  /**
   * Returns the family at the given index
   */
  public Fam getFam(int index) {
    return entities[FAMILIES].getFam(index);
  }

  /**
   * Returns the family with given id
   */
  public Fam getFamFromId(String id) throws DuplicateIDException {
    return (Fam)ids[FAMILIES].get(id);
  }

  /**
   * Returns IDHashtables with IDs of entities
   */
  public IDHashtable[] getIDs() {
    return ids;
  }

  /**
   * Returns the individual at the given position
   */
  public Indi getIndi(int index) {
    return entities[INDIVIDUALS].getIndi(index);
  }

  /**
   * Returns the individual with given id
   */
  public Indi getIndiFromId(String id) throws DuplicateIDException {
    return (Indi)ids[INDIVIDUALS].get(id);
  }

  /**
  Returns the last selected entity
  */
  public Entity getLastEntity()  {
    return lastEntity;
  }

  /**
   * Returns the multimedia at the given position
   */
  public Media getMedia(int index) {
    return entities[MULTIMEDIAS].getMedia(index);
  }

  /**
   * Returns the multimedia with given id
   */
  public Media getMediaFromId(String id) throws DuplicateIDException {
    return (Media)ids[MULTIMEDIAS].get(id);
  }

  /**
   * Returns the name of this gedcom
   */
  public String getName() {
    return origin.getName();
  }

  /**
   * Returns the readable name of the given entity type
   */
  public static String getNameFor(int type, boolean plural) {
    return getResources().getString("type."+ePrefixs[type]+(plural?"s":""));
  }

  /**
   * Returns number of all entities
   */
  public int getNoOfEntities() {

    int total = 0;
    for (int i=0;i<entities.length;i++) {
      total+=entities[i].getSize();
    }

    return total;
  }

  /**
   * Returns the note at the given position
   */
  public Note getNote(int index) {
    return entities[NOTES].getNote(index);
  }

  /**
   * Returns the note with given id
   */
  public Note getNoteFromId(String id) throws DuplicateIDException {
    return (Note)ids[NOTES].get(id);
  }

  /**
   * Returns the source with given id
   */
  public Source getSourceFromId(String id) throws DuplicateIDException {
    return (Source)ids[SOURCES].get(id);
  }

  /**
   * Returns the submitter with given id
   */
  public Submitter getSubmitterFromId(String id) throws DuplicateIDException {
    return (Submitter)ids[SUBMITTERS].get(id);
  }

  /**
   *
   * Returns the repository with given id
   */
  public Repository getRepositoryFromId(String id) throws DuplicateIDException {
    return (Repository)ids[REPOSITORIES].get(id);
  }

  /**
   * Returns the origin of this gedcom
   */
  public Origin getOrigin() {
    return origin;
  }

  /**
   * Returns the prefix of the given entity type
   */
  public static String getPrefixFor(int type) {
    return ePrefixs[type];
  }

  /**
   * Creates a random ID for given type of entity which is free in this Gedcom
   */
  public String getRandomIdFor(int type) {
    return getRandomIdFor(type,this,null);
  }

  /**
   * Creates a random ID for given type of entity which is free in two Gedcoms
   */
  public static String getRandomIdFor(int type, Gedcom g1, Gedcom g2) {

    String result = null;

    // We might to do this several times
    int id = Math.max(g1==null?0:g1.entities[type].getSize(),g2==null?0:g2.entities[type].getSize());

    while (true) {
      
      id ++;

      // Trim to 000
      result = ePrefixs[type] + (id<100?(id<10?"00":"0"):"") + id;

      if ((g1!=null)&&(g1.ids[type].contains(result)))
        continue;
      if ((g2!=null)&&(g2.ids[type].contains(result)))
        continue;

      // Found one
      break;
    };

    // Done
    return result;
  }

  /**
   * Returns the Resources (lazily)
   */
  public static Resources getResources() {
    if (resources==null) {
      resources = new Resources("genj.gedcom");
    }
    return resources;
  }

  /**
   * Returns the tag for given entity type
   */
  public static String getTagFor(int type) {
    return eTags[type];
  }
  
  /**
   * Notification that a change in a Gedcom-object took place.
   */
  public void handleChange(Change change) {
  }

  /**
   * Notification that the gedcom is being closed
   */
  public void handleClose(Gedcom which) {
  }

  /**
   * Notification that an entity has been selected.
   */
  public void handleSelection(Selection selection) {
  }

  /**
   * Returns wether there are two entities with same ID
   */
  public boolean hasDuplicates() {
    for (int i=0;i<ids.length;i++) {
      if (ids[i].hasDuplicates())
      return true;
    }
    return false;
  }

  /**
   * Has the gedcom unsaved changes ?
   */
  public boolean hasUnsavedChanges() {
    return hasUnsavedChanges;
  }

  /**
   * Private initialiser
   */
  private void init(Origin origin, int initialCapacity) {

    if (origin==null)
      throw new IllegalArgumentException("Origin has to specified");

    this.origin = origin;

    for (int i=0;i<entities.length;i++) {
      entities[i] = new EntityList (initialCapacity);
      ids     [i] = new IDHashtable(initialCapacity);
    }

    // Done
  }

  /**
   * Merging of two Gedcom candidates - all unsatisfied links in those
   * candidates will be removed and all entities get new IDs.
   * @param g1 candidate 1
   * @param g2 candidate 2
   * @param options a value of TAG_ENTITY_SOURCE, TAG_PROPERTY_SOURCE
   *        for tagging all entities where they came from and/or all
   *        properties of merged entities.
   */
  public static Gedcom merge(final Gedcom g1, final Gedcom g2, Entity[][] matches, int options) {

    // Valid parameters?
    if ((g1==null)||(g2==null))
      throw new IllegalArgumentException("Candidates have to be non null");
    if (g1==g2)
      throw new IllegalArgumentException("Candidates have to be not equal");

    // Prepare candidates
    g1.close();
    g2.close();

    g1.removeDuplicates();
    g2.removeDuplicates();

    g1.removeUnsatisfiedLinks();
    g2.removeUnsatisfiedLinks();

    // Create new Gedcom
    final Gedcom result;
    try {
      result = new Gedcom(
        Origin.create(g2.getOrigin(),"Merged.ged"),
        g1.getNoOfEntities()+g2.getNoOfEntities()
      );
    } catch (java.net.MalformedURLException muex) {
      Debug.log(Debug.ERROR, Gedcom.class, "Fatal error creating new origin from "+g1.getName()+" and "+g2.getName(), muex);
      return null;
    }

    // Any tagging ?
    Note n1=null,
       n2=null;
    if (((options&TAG_ENTITY_SOURCE)!=0)||((options&TAG_PROPERTY_SOURCE)!=0)) {

      // .. Prepare Source Tags
      n1 = new Note(result);
      n2 = new Note(result);
      n1.setValue("Source is "+g1.getOrigin()+"\n Merged on "+new Date());
      n2.setValue("Source is "+g2.getOrigin()+"\n Merged on "+new Date());
    }

    // Merge matched entities
    for (int m=0;m<matches.length;m++) {

      // Two to One
      Entity e1 = matches[m][0];
      Entity e2 = matches[m][1];

      // Tag kept properties from G2 (if needed)
      if ((options&TAG_PROPERTY_SOURCE)!=0) {
        for (int p=0;p<e2.getProperty().getNoOfProperties();p++) {
          Property prop = e2.getProperty().getProperty(p);
          prop.addProperty(new PropertyNote(n2));
        }
      }

      // Move properties from G1 (and tag moved properties if needed)
      for (int p=0;p<e1.getProperty().getNoOfProperties();p++) {

        Property prop = e1.getProperty().getProperty(p);
  
        // .. A PropertyXRef?
        if (prop instanceof PropertyXRef)
          continue;
  
        // .. Tag it
        if ((options&TAG_PROPERTY_SOURCE)!=0)
          prop.addProperty(new PropertyNote(n1));
  
        // .. Add it
        e2.getProperty().addProperty(prop);
      }

      // Forget entity E1 in G1
      e1.getGedcom().entities[e1.getType()].del(e1);

      // next Entity to be merged
    }

    // Tag entities from two sources
    if ((options&TAG_ENTITY_SOURCE)!=0) {

      // .. Tag entities
      for (int l=0;l<g1.entities.length;l++) {
        for (int e=0;e<g1.entities[l].getSize();e++) {
          g1.entities[l].get(e).getProperty().addProperty(new PropertyNote(n1));
        }
      }

      // .. Tag entities
      for (int l=0;l<g2.entities.length;l++) {
        for (int e=0;e<g2.entities[l].getSize();e++) {
          g2.entities[l].get(e).getProperty().addProperty(new PropertyNote(n2));
        }
      }

      // .. done
    }

    // Fill in entities
    if (n1!=null) {
      result.entities[n1.getType()].add(n1);
      result.entities[n2.getType()].add(n2);
    }

    for (int i=0;i<g1.entities.length;i++) {
      // .. add entities of type from G1
      result.entities[i].add(g1.entities[i]);
      // .. add entities of type from G1
      result.entities[i].add(g2.entities[i]);
      // .. loop through entities of type from Result
      for (int e=0;e<result.entities[i].getSize();e++) {
        Entity ent=result.entities[i].get(e);
        // .. remember Daddy
        ent.setGedcom(result);
        // .. get new ID
        String id = result.getRandomIdFor(ent.getType());
        // .. change entity
        ent.setId(id);
        // .. remember ID
        result.ids[ent.getType()].put(id,ent);
      }
    }

    // Done
    return result;
  }

  /**
   * Notification that a set of entities have been added.
   * That change will be notified to listeners after unlocking write.
   */
  void noteAddedEntities(EntityList entities) {

    // Is there a transaction running?
    if (!isTransaction) {
      return;
    }

    addedEntities.addElement(entities);
  }

  /**
   * Notification that an entity has been added.
   * That change will be notified to listeners after unlocking write.
   */
  void noteAddedEntity(Entity entity) {

    // Is there a transaction running?
    if (!isTransaction) {
      return;
    }
    addedEntities.addElement(entity);
  }

  /**
   * Notification that a property has been added.
   * That change will be notified to listeners after unlocking write.
   */
  void noteAddedProperty(Property prop) {

    // Is there a transaction running?
    if (!isTransaction) {
      return;
    }
    addedProperties.addElement(prop);
    // Done
  }

  /**
   * Notification that a set of entities have been deleted.
   * That change will be notified to listeners after unlocking write.
   */
  void noteDeletedEntities(EntityList entities) {

    // Is there a transaction running?
    if (!isTransaction) {
        return;
    }

    deletedEntities.addElement(entities);
    // Done
  }

  /**
   * Notification that an entity has been deleted.
   * That change will be notified to listeners after unlocking write.
   */
  void noteDeletedEntity(Entity entity) {

    // Is there a transaction running?
    if (!isTransaction) {
      return;
    }

    // Remember
    deletedEntities.addElement(entity);

    // Last one ?
    if (lastEntity==entity) {
      lastEntity=null;
    }

    // Done
  }

  /**
   * Notification that a property has been deleted.
   * That change will be notified to listeners after unlocking write.
   */
  void noteDeletedProperty(Property prop) {

    // Is there a transaction running?
    if (!isTransaction)
      return;

    deletedProperties.addElement(prop);
    // Done
  }

  /**
   * Notification that a property has been modified
   * That change will be notified to listeners after unlocking write.
   */
  void noteModifiedProperty(Property property) {

    // Is there a transaction running?
    if (!isTransaction)
      return;

    modifiedProperties.addElement(property);
    // Done
  }

  /**
   * Removes all duplicates in Gedcom
   */
  public void removeDuplicates() {

    for (int i=0;i<ids.length;i++) {

      // .. indefinite?
      if (!ids[i].hasDuplicates())
        continue;

      // .. make definit!
      Entity[] ents = ids[i].getDuplicates();
      for (int e=0;e<ents.length;e++) {
        // .. change id
        try {
          setIdOf(ents[e],getRandomIdFor(ents[e].getType()));
        } catch (GedcomException ex) {
          // can't happen
        }

        // .. next
      }

      // .. next
    }

    // Done
  }

  /**
   * Removes a Listener from receiving notifications
    */
  public synchronized void removeListener(GedcomListener which) {
    listeners.removeElement(which);
  }

  /**
   * Remove unsatisfied links (PropertyXRef) from Gedcom
   */
  public void removeUnsatisfiedLinks() {

    // Get list of EntityLists
    for (int t=0;t<entities.length;t++) {
      for (int e=0;e<entities[t].getSize();e++) {
        removeUnsatisfiedLinks(entities[t].get(e).getProperty());
      }
    }

    // Done
  }

  /**
   * Helper that removes unsatisfied links (PropertyXRef) from Property
   */
  private void removeUnsatisfiedLinks(Property property) {

    // Check wether any property of this property is unsatisfied
    for (int p=property.getNoOfProperties()-1;p>=0;p--) {

      // .. candidate
      Property prop = property.getProperty(p);

      // .. unsatisfied XRef?
      if ( (prop instanceof PropertyXRef)&&(!((PropertyXRef)prop).isValid()) ) {
        // .. delete
        property.delProperty(prop);
      } else {
        // .. divide & conquer
        removeUnsatisfiedLinks(prop);
      }

      // .. next candidate
    }

    // Done
  }

  /**
   * Sets an entity's id
   * @exception GedcomException if id-argument is null oder of zero length
   */
  public void setIdOf(Entity entity, String id) throws GedcomException {

    // Known entity?
    if (!entities[entity.getType()].contains(entity)) {
      throw new GedcomException("Illegal argument for this Gedcom");
    }

    // ID o.k. ?
    id = id.trim();
    if (id.length()==0) {
      throw new GedcomException("Length of entity's ID has to be non-zero");
    }

    // Remember change
    noteModifiedProperty(entity.getProperty());

    // Prepare change
    int type = entity.getType();

    // Change it by removing old id
    ids[type].remove(entity);

    // .. remember as new
    ids[type].put(id,entity);

    // ... store info in entity
    entity.setId(id);

    // Done
  }

  /**
   * Sets the origin of this gedcom
   */
  public void setOrigin(Origin newOrigin) {

    // Remember new origin
    origin = newOrigin;

    // Done
  }

  /**
   * Clears flag for unsaved changes
   */
  public void setUnsavedChanges(boolean set) {
    hasUnsavedChanges=set;
  }
  /**
   * Starts changing of mankind
   */
  public synchronized boolean startTransaction() {

    // Is there a transaction running?
    if (isTransaction) {
      return false;
    }

    // Start
    isTransaction = true;

    // ... prepare rememberance of changes
    addedEntities      = new Vector(64);
    deletedEntities    = new Vector(64);
    addedProperties    = new Vector(64);
    deletedProperties  = new Vector(64);
    modifiedProperties = new Vector(64);

    // .. done
    return true;
  }

  /**
   * toString overridden
   */
  public String toString() {
    return getName();
  }

  /**
   * Returns all Entities with given id
   * @param which one of INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES
   */
  public EntityList getEntitiesFromId(int which, String id) {
    return ids[which].getAll(id);
  }

  /**
   * Returns an image for given entity type
   */
  public static ImgIcon getImage(int type) {
    try {
      return Images.get(eTags[type]);
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Unknown type");
    }
  }


  /**
   * Little helper that retuns type for given entity and know
   * how to handle null
   */
  public static int getType(Entity entity) {
    if (entity==null) {
      return -1;
    }
    return entity.getType();
  }

}
