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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import genj.util.Origin;
import genj.util.Resources;
import genj.util.swing.ImageIcon;

/**
 * The object-representation of a Gedom file
 */
public class Gedcom {
  
  /** submitter of this Gedcom */
  private Submitter submitter;

  /** origin of this Gedcom */
  private Origin origin;
  
  /** image */
  private final static ImageIcon image = new ImageIcon(Gedcom.class, "images/Gedcom.gif");

  /** entities */
  private List[]  entities = new List[NUM_TYPES];
  private IDMap[] ids = new IDMap[NUM_TYPES];
  
  /** change/transaction support */
  private boolean isTransaction = false;
  private boolean hasUnsavedChanges;
  private List addedEntities     ,
                deletedEntities   ;
  private List addedProperties   ,
                deletedProperties ,
                modifiedProperties;

  /** listeners */
  private List listeners = new ArrayList(10);

  /** static resourcs */
  static private Random seed = new Random();
  static /*package*/ Resources resources = new Resources("genj.gedcom");

  private final static String[]
    ePrefixs  = { "I", "F", "M", "N", "S", "B", "R"},
    eTags     = { "INDI", "FAM", "OBJE", "NOTE", "SOUR", "SUBM", "REPO" };
    
  private final static Class[]
    eTypes    = { Indi.class, Fam.class, Media.class, Note.class, Source.class, Submitter.class, Repository.class };

  public final static int
    INDIVIDUALS  = 0,
    FAMILIES     = 1,
    MULTIMEDIAS  = 2,
    NOTES        = 3,
    SOURCES      = 4,
    SUBMITTERS   = 5,
    REPOSITORIES = 6,
    NUM_TYPES    = 7;

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin) {
    this(origin, 100);
  }

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin, int initialCapacity) {
    // safety check
    if (origin==null)
      throw new IllegalArgumentException("Origin has to specified");
    // remember
    this.origin = origin;
    // init
    for (int i=0;i<entities.length;i++) {
      entities[i] = new ArrayList  (initialCapacity);
      ids     [i] = new IDMap(initialCapacity);
    }
    // Done
  }

  /**
   * Returns the origin of this gedcom
   */
  public Origin getOrigin() {
    return origin;
  }

  /**
   * Sets the origin of this gedcom
   */
  public void setOrigin(Origin newOrigin) {
    origin = newOrigin;
  }
  
  /**
   * Returns the submitter of this gedcom (might be null)
   */
  public Submitter getSubmitter() {
    return submitter;
  }
  
  /** 
   * Sets the submitter of this gedcom
   */
  public void setSubmitter(Submitter set) {
    if (!entities[SUBMITTERS].contains(set)) 
      throw new IllegalArgumentException("Submitter is not part of this gedcom");
    submitter = set;
    if (isTransaction) hasUnsavedChanges = true;
  }
  
  /**
   * toString overridden
   */
  public String toString() {
    return getName();
  }

  /**
   * Adds a Listener which will be notified when data changes
   */
  public synchronized void addListener(GedcomListener which) {
    listeners.add(which);
  }

  /**
   * Removes a Listener from receiving notifications
    */
  public synchronized void removeListener(GedcomListener which) {
    listeners.remove(which);
  }

  /**
   * Create a entity by tag
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(String tag, String id) throws GedcomException {
    // check tag
    for (int e=0; e<eTags.length; e++) {
      if (eTags[e].equals(tag)) return createEntity(e, id);
    }
    // unknown tag
    throw new GedcomException("Unknown tag for entity");
  }
  
  /**
   * Create a entity by class
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(Class type, String id) throws GedcomException {
    // check tag
    for (int e=0; e<eTypes.length; e++) {
      if (eTypes[e].equals(type)) return createEntity(e, id);
    }
    // unknown tag
    throw new GedcomException("Unknown type for entity");
  }
  
  /**
   * Creates a non-related entity with id
   */
  public Entity createEntity(int type, String id) throws GedcomException {
    
    // Check the id
    if ((id!=null)&&(ids[type].contains(id))) {
      throw new DuplicateIDException(eTags[type]+" with id "+id+" is alread defined");
    }

    // Generate id if necessary
    if (id==null) id = getRandomIdFor(type);
    
    // The type of the entity
    Class clazz = eTypes[type];

    // Create entity
    Entity result;
    try {
      result = (Entity)clazz.newInstance();
    } catch (Throwable t) {
      throw new GedcomException("Unexpected problem creating instance of "+eTags[type]);
    }
     
    // Connect and initialize
    result.setGedcom(this);
    result.setId(id);
    entities[type].add(result);
    
    // Store id
    ids[type].put(id,result);
    
    // Mark change
    noteAddedEntity(result);
    
    // notify
    result.addNotify(this);

    // Done
    return result;
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

    // Tell it
    which.delNotify();

    // Notify deletion
    noteDeletedEntity(which);

    // Delete it
    entities[type].remove(which);
    ids     [type].remove(which);
    
    if (submitter==which) submitter = null;

    // Done
  }

  /**
   * Sets an entity's id
   * @exception GedcomException if id-argument is null oder of zero length
   */
  public void setId(Entity entity, String id) throws GedcomException {
    // Known entity?
    if (!entities[entity.getType()].contains(entity)) {
      throw new GedcomException("Entity isn't member in "+this);
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
   * Returns number of all entities
   */
  public int getNoOfEntities() {
    int total = 0;
    for (int i=0;i<entities.length;i++) {
      total+=entities[i].size();
    }
    return total;
  }

  /**
   * Returns entities of given type
   */
  public List getEntities(int type) {
    // 20030129 don't return original
    return new ArrayList(entities[type]);
  }

  /**
   * Returns the entity with given id
   */
  public Entity getEntity(String id) throws DuplicateIDException {
    if (id==null) throw new IllegalArgumentException("id cannot be null");
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
  public Entity getEntity(String id, int type) throws DuplicateIDException {
    return ids[type].get(id);
  }

  /**
   * Creates a random ID for given type of entity which is free in this Gedcom
   */
  public String getRandomIdFor(int type) {
    // We might to do this several times
    String result;
    int id = entities[type].size();
    while (true) {
      // next one
      id ++;
      // trim to 000
      result = ePrefixs[type] + (id<100?(id<10?"00":"0"):"") + id;
      // try it
      if (!ids[type].contains(result)) break;
      // try again
    };
    return result;
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
   * Clears flag for unsaved changes
   */
  public void setUnchanged() {
    hasUnsavedChanges=false;
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
    addedEntities      = new ArrayList(64);
    deletedEntities    = new ArrayList(64);
    addedProperties    = new ArrayList(64);
    deletedProperties  = new ArrayList(64);
    modifiedProperties = new ArrayList(64);

    // .. done
    return true;
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
    for (int i=listeners.size()-1;i>=0;i--) {
      ((GedcomListener)listeners.get(i)).handleChange(change);
    }

    // ... End
    isTransaction = false;

    // ... Done
    return change;
  }

  /**
   * Notification that a set of entities have been added.
   * That change will be notified to listeners after unlocking write.
   */
  void noteAddedEntities(List entities) {
    // Is there a transaction running?
    if (!isTransaction) {
      return;
    }
    addedEntities.addAll(entities);
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
    addedEntities.add(entity);
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
    addedProperties.add(prop);
    modifiedProperties.add(prop);
    // Done
  }

  /**
   * Notification that a set of entities have been deleted.
   * That change will be notified to listeners after unlocking write.
   */
  void noteDeletedEntities(List entities) {

    // Is there a transaction running?
    if (!isTransaction) {
        return;
    }

    deletedEntities.addAll(entities);
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
    deletedEntities.add(entity);

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

    deletedProperties.add(prop);
    modifiedProperties.add(prop);
    
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

    modifiedProperties.add(property);
    // Done
  }

  /**
   * Returns the name of this gedcom
   */
  public String getName() {
    return origin.getName();
  }

  /**
   * Returns a readable name for the given tag   */
  public static String getName(String tag) {
    return resources.getString(tag+".name");
  }

  /**
   * Returns the readable name of the given entity type
   */
  public static String getNameFor(int type, boolean plural) {
    return resources.getString("type."+ePrefixs[type]+(plural?"s":""));
  }

  /**
   * Returns the prefix of the given entity type
   */
  public static String getPrefixFor(int type) {
    return ePrefixs[type];
  }

  /**
   * Returns an image for Gedcom
   */
  public static ImageIcon getImage() {
    return image;
  }

  /**
   * Returns an image for given entity type
   */
  public static ImageIcon getImage(int type) {
    try {
      return MetaProperty.get(new TagPath(getTagFor(type))).getImage();
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Unknown type");
    }
  }
  
  /**
   * Returns the tag for given entity type
   */
  public static String getTagFor(int type) {
    return eTags[type];
  }

  /**
   * Returns a type for given class
   */
  public static int getTypeFor(Class clazz) {
    for (int i=0; i<eTypes.length; i++) {
      if (eTypes[i]==clazz) return i;
    }
    throw new IllegalArgumentException("Unknown class "+clazz);
  }

  /**
   * Returns a type for given tag
   */
  public static int getTypeFor(String tag) {
    for (int i=0; i<eTypes.length; i++) {
      if (eTags[i].equals(tag)) return i;
    }
    throw new IllegalArgumentException("Unknown tag "+tag);
  }

  /**
   * Returns the Resources (lazily)
   */
  public static Resources getResources() {
    return resources;
  }
  
} //Gedcom
