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

import genj.util.Origin;
import genj.util.ReferenceSet;
import genj.util.Resources;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The object-representation of a Gedom file
 */
public class Gedcom {
  
  /** static resourcs */
  static private Random seed = new Random();
  static /*package*/ Resources resources = Resources.get(Gedcom.class);

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

  /** image */
  private final static ImageIcon image = new ImageIcon(Gedcom.class, "images/Gedcom.gif");
  
  /** images */
  private static ImageIcon[] images = new ImageIcon[NUM_TYPES];

  /** submitter of this Gedcom */
  private Submitter submitter;

  /** origin of this Gedcom */
  private Origin origin;
  
  /** entities */
  private List[] entities    = new List   [NUM_TYPES];
  private Map[]  id2entities = new HashMap[NUM_TYPES];
  
  /** change/transaction support */
  private boolean isTransaction = false;
  private boolean hasUnsavedChanges;
  private Set[] changes;

  /** listeners */
  private List listeners = new ArrayList(10);
  
  /** mapping tags refence sets */
  private Map tags2refsets = new HashMap();

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
      entities   [i] = new ArrayList(initialCapacity);
      id2entities[i] = new HashMap  (initialCapacity);
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
   * Creates a non-related entity with id
   */
  public Entity createEntity(int type) throws GedcomException {
    return createEntity(type, null);
  }    
    
  /**
   * Create a entity by tag
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(String tag, String id) throws GedcomException {
    // check tag
    for (int type=0; type<NUM_TYPES; type++) {
      if (eTags[type].equals(tag)) return createEntity(type, id);
    }
    // unknown tag
    throw new IllegalArgumentException("unknown tag");
  }
  
  /**
   * Creates an entity
   */
  public Entity createEntity(int type, String id) throws GedcomException {
    
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
    result.setId(id);
    entities[type].add(result);
    
    // Store id
    id2entities[type].put(id,result);
    
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
    if (!id2entities[type].containsKey(which.getId()))
      throw new GedcomException("Unknown entity with id "+which.getId());

    // Tell it
    which.delNotify();

    // Delete it
    entities   [type].remove(which);
    id2entities[type].remove(which.getId());
    
    if (submitter==which) submitter = null;

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
   * Returns the entity with given id (or null)
   */
  public Entity getEntity(String id) {
    // arg check
    if (id==null) 
      throw new IllegalArgumentException("id cannot be null");
    // loop all types
    for (int t=0;t<NUM_TYPES;t++) {
      Entity e = getEntity(id, t);
      if (e!=null) return e;
    }
    // not found
    return null;
  }

  /**
   * Returns the entity with given id of given type or null if not exists
   */
  public Entity getEntity(String id, int type) {
    return (Entity)id2entities[type].get(id);
  }

  /**
   * Creates a random ID for given type of entity which is free in this Gedcom
   */
  private String getRandomIdFor(int type) {
    // We might to do this several times
    String result;
    int id = entities[type].size();
    while (true) {
      // next one
      id ++;
      // trim to 000
      result = ePrefixs[type] + (id<100?(id<10?"00":"0"):"") + id;
      // try it
      if (!id2entities[type].containsKey(result)) break;
      // try again
    };
    // done
    return result;
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
    changes = new Set[Change.NUM];
    for (int i=0;i<Change.NUM;i++)
      changes[i] = new HashSet(64);

    // .. done
    return true;
  }

  /**
   * Whether there's a transaction going on
   */
  /*package*/ boolean isTransaction() {
    return isTransaction;
  }
  
  /**
   * Returns change set
   */
  /*package*/ Set getTransactionChanges(int which) {
    return changes[which]; 
  }
  
  /**
   * Ends Transaction
   * @return the change that has been made
   */
  public synchronized void endTransaction() {

    // Is there a transaction going on?
    if (!isTransaction)
      throw new RuntimeException("Attempt to end non existing Transaction");

    // wrao in change
    Change change = new Change( this, changes );

    // end tx
    isTransaction = false;
    changes = null;

    // need to notify?
    if (change.isEmpty())
      return;
      
    // remember change
    hasUnsavedChanges = true;

    // send message to all listeners
    GedcomListener[] gls = (GedcomListener[])listeners.toArray(new GedcomListener[listeners.size()]);
    for (int l=0;l<gls.length;l++) {
      gls[l].handleChange(change);
    }

    // done
  }
  
  /**
   * Get a reference set for given tag
   */
  /*package*/ ReferenceSet getReferenceSet(String tag) {
    // lookup
    ReferenceSet result = (ReferenceSet)tags2refsets.get(tag);
    if (result==null) {
      // .. instantiate if necessary
      result = new ReferenceSet();
      tags2refsets.put(tag, result);
      // .. and pre-fill
      StringTokenizer tokens = new StringTokenizer(Gedcom.resources.getString(tag+".vals",""),",");
      while (tokens.hasMoreElements()) result.add(tokens.nextToken().trim(), null);
    }
    // done
    return result;
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
      if (images[type]==null) 
        images[type] = MetaProperty.get(new TagPath(getTagFor(type))).getImage();
      return images[type];
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
    for (int i=0; i<eTags.length; i++) {
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
