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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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

  public final static String
    INDI = "INDI", 
    FAM  = "FAM" ,
    OBJE = "OBJE", 
    NOTE = "NOTE", 
    SOUR = "SOUR", 
    SUBM = "SUBM", 
    REPO = "REPO";
    
  public final static String[] 
    ENTITIES = { INDI, FAM, OBJE, NOTE, SOUR, SUBM, REPO };      

  private final static Map 
    E2PREFIX = new HashMap();
    static {
      E2PREFIX.put(INDI, "I");
      E2PREFIX.put(FAM , "F");
      E2PREFIX.put(OBJE, "M");
      E2PREFIX.put(NOTE, "N");
      E2PREFIX.put(SOUR, "S");
      E2PREFIX.put(SUBM, "B");
      E2PREFIX.put(REPO, "R");
    }
    
  private final static Map 
    E2TYPE = new HashMap();
    static {
      E2TYPE.put(INDI, Indi.class);
      E2TYPE.put(FAM , Fam .class);
      E2TYPE.put(OBJE, Media.class);
      E2TYPE.put(NOTE, Note.class);
      E2TYPE.put(SOUR, Source.class);
      E2TYPE.put(SUBM, Submitter.class);
      E2TYPE.put(REPO, Repository.class);
    }
    
  private final static Map
    E2IMAGE = new HashMap();

  /** image */
  private final static ImageIcon image = new ImageIcon(Gedcom.class, "images/Gedcom.gif");
  
  /** submitter of this Gedcom */
  private Submitter submitter;

  /** origin of this Gedcom */
  private Origin origin;
  
  /** entities */
  private LinkedList entities = new LinkedList();
  private Map e2entities = new HashMap(); // values are maps id->entitiy
  
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
    if (!getEntityMap(SUBM).containsValue(set))
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
  public Entity createEntity(String tag) throws GedcomException {
    return createEntity(tag, null);
  }    
    
  /**
   * Create a entity by tag
   * @exception GedcomException in case of unknown tag for entity
   */
  public Entity createEntity(String tag, String id) throws GedcomException {
    
    // lookup a type
    Class clazz = (Class)E2TYPE.get(tag);
    if (clazz==null)
      clazz = Entity.class;
    
    // Generate id if necessary
    if (id==null) 
      id = createEntityId(tag);

    // Create entity
    Entity result; 
    try {
      result = (Entity)clazz.newInstance();
    } catch (Throwable t) {
      throw new GedcomException("Unexpected problem creating instance of "+tag);
    }

    // remember     
    getEntityMap(tag).put(id, result);
    entities.add(result);
    
    // initialize
    result.setId(id);
    
    // notify
    result.addNotify(this, tag);

    // Done
    return result;
  }

  /**
   * Deletes entity
   * @exception GedcomException in case unknown type of entity
   */
  public void deleteEntity(Entity which) throws GedcomException {

    // Lookup entity map
    Map ents = getEntityMap(which.getTag());

    // Entity exists ?
    String id = which.getId();
    if (!ents.containsKey(id))
      throw new GedcomException("Unknown entity with id "+which.getId());

    // Tell it
    which.delNotify();

    // Delete it
    ents.remove(id);
    entities.remove(which);

    // was it the submitter?    
    if (submitter==which) submitter = null;

    // Done
  }

  /**
   * Internal entity lookup
   */
  private Map getEntityMap(String tag) {
    // lookup map of entities for tag
    Map ents = (Map)e2entities.get(tag);
    if (ents==null) {
      ents = new HashMap();
      e2entities.put(tag, ents);
    }
    // done
    return ents;
  }

  /**
   * Returns all entities
   */
  public Collection getEntities() {
    return Collections.unmodifiableCollection(entities);
  }

  /**
   * Returns entities of given type
   */
  public Collection getEntities(String tag) {
    return Collections.unmodifiableCollection(getEntityMap(tag).values());
  }

  /**
   * Returns entities of given type sorted by given path (can be empty or null)
   */
  public Entity[] getEntities(String tag, String sortPath) {
    return getEntities(tag, sortPath!=null&&sortPath.length()>0 ? new PropertyComparator(sortPath) : null);
  }

  /**
   * Returns entities of given type sorted by comparator (can be null)
   */
  public Entity[] getEntities(String tag, Comparator comparator) {
    Collection ents = getEntityMap(tag).values();
    Entity[] result = (Entity[])ents.toArray(new Entity[ents.size()]);
    // sort by comparator or entity
    if (comparator!=null) 
      Arrays.sort(result, comparator);
    else
      Arrays.sort(result);
    // done
    return result;
  }

  /**
   * Returns the entity with given id (or null)
   */
  public Entity getEntity(String id) {
    // arg check
    if (id==null) 
      throw new IllegalArgumentException("id cannot be null");
    // loop all types
    for (Iterator tags=e2entities.keySet().iterator();tags.hasNext();) {
      String tag = (String)tags.next();
      Entity result = getEntity(tag, id);
      if (result!=null)
        return result;
    }
    
    // not found
    return null;
  }

  /**
   * Returns the entity with given id of given type or null if not exists
   */
  public Entity getEntity(String tag, String id) {
    // arg check
    if (id==null) 
      throw new IllegalArgumentException("id cannot be null");
    return (Entity)getEntityMap(tag).get(id);
    
  }
  
  /**
   * Returns any instance of entity with given type if exists
   */
  public Entity getAnyEntity(String tag) {
    Map ents = getEntityMap(tag);
    return ents.isEmpty() ? null : (Entity)ents.values().iterator().next();
  }

  /**
   * Creates a random ID for given type of entity which is free in this Gedcom
   */
  private String createEntityId(String tag) {
    
    // Lookup current entities of type
    Map ents = getEntityMap(tag);
    
    // We might to do this several times
    String prefix = getEntityPrefix(tag);
    String result;
    int id = ents.size();
    while (true) {
      // next one
      id ++;
      // trim to 000
      result = prefix + (id<100?(id<10?"00":"0"):"") + id;
      // try it
      if (!ents.containsKey(result)) break;
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
  public static String getEntityName(String tag, boolean plural) {
    return resources.getString("type."+getEntityPrefix(tag)+(plural?"s":""));
  }

  /**
   * Returns the prefix of the given entity
   */
  public static String getEntityPrefix(String tag) {
    String result = (String)E2PREFIX.get(tag);
    if (result==null)
      result = "X";
    return result;
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
  public static ImageIcon getEntityImage(String tag) {
    ImageIcon result = (ImageIcon)E2IMAGE.get(tag);
    if (result==null) {
      result = MetaProperty.get(new TagPath(tag)).getImage();
      E2IMAGE.put(tag, result);
    }
    return result;
  }
  
  /**
   * Returns the Resources (lazily)
   */
  public static Resources getResources() {
    return resources;
  }
  
} //Gedcom
