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
 * 
 * $Revision: 1.91 $ $Author: nmeier $ $Date: 2005-06-06 17:35:16 $
 */
package genj.gedcom;

import genj.util.Debug;
import genj.util.Origin;
import genj.util.ReferenceSet;
import genj.util.Resources;
import genj.util.swing.ImageIcon;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * The object-representation of a Gedom file
 */
public class Gedcom {
  
  /** static resourcs */
  static private Random seed = new Random();
  static /*package*/ Resources resources = Resources.get(Gedcom.class);

  public static final String
   // standard Gedcom encodings 
    UNICODE  = "UNICODE", 
    ASCII    = "ASCII",      // a.k.a US-ASCII
    ANSEL    = "ANSEL",
   // non-standard encodings
    LATIN1   = "LATIN1",     // a.k.a ISO-8859-1
    ANSI     = "ANSI";       // a.k.a. Windows-1252 (@see http://www.hclrss.demon.co.uk/demos/ansi.html)
  
  /** encodings including the non Gedcom-standard encodings LATIN1 and ANSI */  
  public static final String[] ENCODINGS = { 
    ANSEL, UNICODE, ASCII, LATIN1, ANSI 
  };

  /** languages as defined by the Gedcom standard */  
  public static final String[] LANGUAGES = {
    "Afrikaans","Albanian","Amharic","Anglo-Saxon","Arabic","Armenian","Assamese",
    "Belorusian","Bengali","Braj","Bulgarian","Burmese", 
    "Cantonese","Catalan","Catalan_Spn","Church-Slavic","Czech", 
    "Danish","Dogri","Dutch", 
    "English","Esperanto","Estonian", 
    "Faroese","Finnish","French", 
    "Georgian","German","Greek","Gujarati", 
    "Hawaiian","Hebrew","Hindi","Hungarian", 
    "Icelandic","Indonesian","Italian",
    "Japanese", 
    "Kannada","Khmer","Konkani","Korean",
    "Lahnda","Lao","Latvian","Lithuanian", 
    "Macedonian","Maithili","Malayalam","Mandrin","Manipuri","Marathi","Mewari", 
    "Navaho","Nepali","Norwegian",
    "Oriya", 
    "Pahari","Pali","Panjabi","Persian","Polish","Prakrit","Pusto","Portuguese", 
    "Rajasthani","Romanian","Russian", 
    "Sanskrit","Serb","Serbo_Croa","Slovak","Slovene","Spanish","Swedish", 
    "Tagalog","Tamil","Telugu","Thai","Tibetan","Turkish", 
    "Ukrainian","Urdu", 
    "Vietnamese", 
    "Wendic" ,
    "Yiddish"
  };

  /** record tags */
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
  private LinkedList allEntities = new LinkedList();
  private Map tag2id2entity = new HashMap();
  
  /** transaction support */
  private Transaction transaction = null;
  private boolean hasUnsavedChanges;

  private ArrayList 
    undos = new ArrayList(),
    redos = new ArrayList();

  /** listeners */
  private List listeners = new ArrayList(10);
  
  /** mapping tags refence sets */
  private Map tags2refsets = new HashMap();

  /** encoding */
  private String encoding = ANSEL;
  
  /** language */
  private String language = null;
  
  /** cached locale */
  private Locale cachedLocale = null;

  /** cached collator */
  private Collator cachedCollator = null;
  
  /** global place format */
  private String placeFormat = "";

  /** password for private information */
  private String password = PASSWORD_NOT_SET;

  public final static String
    PASSWORD_NOT_SET = "PASSWORD_NOT_SET",
    PASSWORD_UNKNOWN = "PASSWORD_UNKNOWN";

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin) {
    this(origin, false);
  }

  /**
   * Gedcom's Constructor
   */
  public Gedcom(Origin origin, boolean createAdam) {
    // safety check
    if (origin==null)
      throw new IllegalArgumentException("Origin has to specified");
    // remember
    this.origin = origin;
    // create Adam
    if (createAdam) {
      try {
        Indi adam = (Indi) createEntity(Gedcom.INDI);
        adam.addDefaultProperties();
        adam.setName("Adam","");
        adam.setSex(PropertySex.MALE);
      } catch (GedcomException e) {
      }
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
    if (!getEntityMap(SUBM).containsValue(set))
      throw new IllegalArgumentException("Submitter is not part of this gedcom");
    submitter = set;
    
    // propagate modified on submitter
    submitter.propagateChange(submitter.getValue());

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
  public synchronized void addGedcomListener(GedcomListener which) {
    listeners.add(which);
  }

  /**
   * Removes a Listener from receiving notifications
   */
  public synchronized void removeGedcomListener(GedcomListener which) {
    listeners.remove(which);
  }
  
  /**
   * Notify Listeners of possible changes through given transaction
   */
  private void notifyGedcomListeners(Transaction tx) {
    // send message to all listeners
    GedcomListener[] gls = (GedcomListener[])listeners.toArray(new GedcomListener[listeners.size()]);
    for (int l=0;l<gls.length;l++) {
      try {
        gls[l].handleChange(transaction);
      } catch (Throwable t) {
        Debug.log(Debug.ERROR, this, t);
      }
    }
    // done
  }
  
  /**
   * Add entity 
   */
  /*package*/ void addEntity(Entity entity) throws GedcomException {
    
    String id = entity.getId();
    
    // some entities (event definitions for example) don't have an
    // id - we'll keep them in our global list but not mapped id->entity
    if (id.length()>0) {
      Map id2entity = getEntityMap(entity.getTag());
      if (id2entity.containsKey(id))
        throw new GedcomException(resources.getString("error.entity.dupe", id));
      
      // remember id2entity
      id2entity.put(id, entity);
    }
    
    // remember entity
    allEntities.add(entity);
    
    // notify
    entity.addNotify(this);

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
    
    // generate new id if necessary - otherwise trim it
    if (id==null)
      id = getNextAvailableID(tag);
    else 
      id = trimID(id);

    // lookup a type - all well known types need id
    Class clazz = (Class)E2TYPE.get(tag);
    if (clazz!=null) {
      if (id.length()==0)
        throw new GedcomException(resources.getString("entity.error.noid", tag));
    } else {
      clazz = Entity.class;
    }
    
    // Create entity
    Entity result; 
    try {
      result = (Entity)clazz.newInstance();
    } catch (Throwable t) {
      throw new RuntimeException("Can't instantiate "+clazz);
    }

    // initialize
    result.init(tag, id);
    
    // keep it
    addEntity(result);

    // Done
    return result;
  }

  /**
   * Deletes entity
   * @exception GedcomException in case unknown type of entity
   */
  public void deleteEntity(Entity which) {

    // Some entities dont' have ids (event definitions for example) - for
    // all others we check the id once more
    String id = which.getId();
    if (id.length()>0) {
      
      // Lookup entity map
      Map id2entity = getEntityMap(which.getTag());
  
      // id exists ?
      if (!id2entity.containsKey(id))
        throw new IllegalArgumentException("Unknown entity with id "+which.getId());

      // forget id
      id2entity.remove(id);
    }
    
    // Tell it
    which.delNotify();

    // Forget it now
    allEntities.remove(which);

    // was it the submitter?    
    if (submitter==which) submitter = null;

    // Done
  }

  /**
   * Internal entity lookup
   */
  private Map getEntityMap(String tag) {
    // lookup map of entities for tag
    Map id2entity = (Map)tag2id2entity.get(tag);
    if (id2entity==null) {
      id2entity = new HashMap();
      tag2id2entity.put(tag, id2entity);
    }
    // done
    return id2entity;
  }
  
  /**
   * Returns all entities
   */
  public Collection getEntities() {
    return Collections.unmodifiableCollection(allEntities);
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
    // trim the id
    id = trimID(id);
    // loop all types
    for (Iterator tags=tag2id2entity.keySet().iterator();tags.hasNext();) {
      Entity result = (Entity)getEntityMap((String)tags.next()).get(id);
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
    // trim the id
    id = trimID(id);
    // check back in appropriate type map
    return (Entity)getEntityMap(tag).get(id);
  }
  
  /**
   * Trim an ID getting rid of leading zeros
   */
  /*package*/ static String trimID(String id) {
    id = id.trim();
    // has to be our format
    int len = id.length();
    if (len<2)
      return id;
    // .. leading type character prefix
    StringBuffer result = new StringBuffer(id.length());
    char prefix = id.charAt(0);
    if (!Character.isLetter(prefix))
      return id;
    result.append(prefix);
    // .. digits (strip leading zeros)
    for (int i=1; i<len; i++) {
      char digit = id.charAt(i);
      if (!Character.isDigit(digit))
        return id;
      if (result.length()>1||digit!='0') result.append(digit);
    }
    // done
    return result.toString();
  }
  
  /**
   * Returns any instance of entity with given type if exists
   */
  public Entity getAnyEntity(String tag) {
    Map ents = getEntityMap(tag);
    return ents.isEmpty() ? null : (Entity)ents.values().iterator().next();
  }

  /**
   * Return the next available ID for given type of entity
   */
  public String getNextAvailableID(String entity) {
    
    // Lookup current entities of type
    Map id2entity = getEntityMap(entity);
    
    // Look for an available ID
    String prefix = getEntityPrefix(entity);
    int id = Options.getInstance().isFillGapsInIDs ? 1 : id2entity.size();
    search: while (true) {
      // next one
      id ++;
      // not used yet?
      if (!id2entity.containsKey(prefix + Integer.toString(id)))
        break;
      // try next
    }
// 20050509 not patching IDs with zeros anymore - since we now have alignment
// in tableview there's not really a need to add leading zeros for readability. ID
// uniqueness is now guaranteed by trimming leading zeros when handling IDs
    // done
    return prefix + Integer.toString(id);
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
   * Starts a transaction
   */
  public synchronized Transaction startTransaction() throws IllegalStateException {

    // Is there a transaction running?
    if (transaction!=null)
      throw new IllegalStateException("Cannot start transaction for changes while concurrent transaction is active");

    // start one
    transaction = new Transaction(this);

    // done
    return transaction;
  }
  
  /**
   * Test for transaction going on
   */
  public synchronized boolean isTransaction() {
    return transaction!=null;
  }
  
  /**
   * Access current transaction
   */
  public synchronized Transaction getTransaction() {
    // check for no transaction while listeners present
    if (transaction==null&&!listeners.isEmpty())
      throw new IllegalStateException("No active transaction but listeners present");
    // return it
    return transaction;
  }

  /**
   * Ends Transaction
   */
  public synchronized Transaction endTransaction() {

    // any changes?
    if (transaction!=null&&transaction.hasChanges()) {

      // remember change
      hasUnsavedChanges = true;

      // keep it for undo
      undos.add(transaction);
      redos.clear();
      
      // check number of undos
      while (undos.size()>Options.getInstance().getNumberOfUndos())
        undos.remove(0);
    
      // let everyone know
      notifyGedcomListeners(transaction);

    }
    
    // forget current
    Transaction result = transaction;
    transaction = null;
    
    // done
    return result;
  }

  /**
   * Test for undo
   */
  public boolean canUndo() {
    return !undos.isEmpty();
  }
  
  /**
   * Performs an undo
   */
  public synchronized void undo() {
    
    // there?
    if (undos.isEmpty())
      throw new IllegalArgumentException("undo not possible");

    // Is there a transaction running?
    if (transaction!=null)
      throw new IllegalStateException("Cannot undo while concurrent transaction is active");

    Transaction undo = (Transaction)undos.remove(undos.size()-1);

    // start one
    transaction = new Transaction(this);
    transaction.setRollback(true);

    // rollback changes of last transaction
    Change[] changes = undo.getChanges();
    for (int i=changes.length-1;i>=0;i--)
      changes[i].undo();

    // reset change status
    hasUnsavedChanges = undo.hasUnsavedChangesBefore;

    // keep undos as redo
    redos.add(transaction);
    
    // let everyone know
    notifyGedcomListeners(transaction);

    // done
    transaction = null;

  }
    
  /**
   * Test for redo
   */
  public boolean canRedo() {
    return !redos.isEmpty();
  }
  
  /**
   * Performs a redo
   */
  public synchronized void redo() {

    // there?
    if (redos.isEmpty())
      throw new IllegalArgumentException("redo not possible");
    Transaction redo = (Transaction)redos.remove(redos.size()-1);

    // Is there a transaction running?
    if (transaction!=null)
      throw new IllegalStateException("Cannot redo while concurrent transaction is active");

    // start one
    transaction = new Transaction(this);
    transaction.setRollback(true);

    // rollback changes of last undo (reverse order again)
    Change[] changes = redo.getChanges();
    for (int i=changes.length-1;i>=0;i--)
      changes[i].undo();

    // keep change status
    hasUnsavedChanges = changes.length>0;

    // keep transaction as undo
    undos.add(transaction);
    
    // let everyone know
    notifyGedcomListeners(transaction);

    // done
    transaction = null;
    
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
      String defaults = Gedcom.resources.getString(tag+".vals",false);
      if (defaults!=null) {
        StringTokenizer tokens = new StringTokenizer(defaults,",");
        while (tokens.hasMoreElements()) result.add(tokens.nextToken().trim(), null);
      }
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
    return getName(tag, false);
  }

  /**
   * Returns the readable name for the given tag
   */
  public static String getName(String tag, boolean plural) {
    if (plural) {
      String name = resources.getString(tag+"s.name", false);
      if (name!=null)
        return name;
    }
    String name = resources.getString(tag+".name", false);
    return name!=null ? name : tag;
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
      result = Grammar.getMeta(new TagPath(tag)).getImage();
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

  /**
   * Accessor - encoding
   */
  public String getEncoding() {
    return encoding;
  }
  
  /**
   * Accessor - encoding
   */
  public void setEncoding(String set) {
    for (int e=0;e<ENCODINGS.length;e++) {
      if (ENCODINGS[e].equals(set)) {
        encoding = set;
        return;
      }
    }
  }
  
  /**
   * Accessor - place format
   */
  public String getPlaceFormat() {
    return placeFormat;
  }
  
  /**
   * Accessor - place format
   */
  public void setPlaceFormat(String set) {
    placeFormat = set;
  }
  
  /**
   * Accessor - language
   */
  public String getLanguage() {
    return language;
  }
  
  /**
   * Accessor - encoding
   */
  public void setLanguage(String set) {
    language = set;
  }
  
  /**
   * Accessor - password
   */
  public void setPassword(String set) {
    if (set==null)
      throw new IllegalArgumentException("Password can't be null");
    password = set;
  }
  
  /**
   * Accessor - password
   */
  public String getPassword() {
    return password;
  }
  
  /**
   * Accessor - password
   * @return password!=PASSWORD_UNKNOWN!=PASSWORD_NOT_SET
   */
  public boolean hasPassword() {
    return password!=PASSWORD_NOT_SET && password!=PASSWORD_UNKNOWN;
  }
  
  /**
   * Check for containment
   */
  public boolean contains(Entity entity) {
    return getEntityMap(entity.getTag()).containsValue(entity);
  }
  
  /**
   * Return an appropriate Locale instance
   */
  public Locale getLocale() {
    
    // not known?
    if (cachedLocale==null) {
      
      // known language?
      if (language!=null) {
        
        // look for it
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
          if (locales[i].getDisplayLanguage(Locale.ENGLISH).equalsIgnoreCase(language)) {
            cachedLocale = new Locale(locales[i].getLanguage(), Locale.getDefault().getCountry());
            break;
          }
        }
        
      }
      
      // default?
      if (cachedLocale==null)
        cachedLocale = Locale.getDefault();
      
    }
    
    // done
    return cachedLocale;
  }
  
  /**
   * Return an appropriate Collator instance
   */
  public Collator getCollator() {
    
    // not known?
    if (cachedCollator==null) {
      cachedCollator = Collator.getInstance(getLocale());
      
      // 20050505 when comparing gedcom values we really don't want it to be
      // done case sensitive. It surfaces in many places (namely for example
      // in prefix matching in PropertyTableWidget) so I'm restricting comparison
      // criterias to PRIMARY from now on
      cachedCollator.setStrength(Collator.PRIMARY);
    }
    
    // done
    return cachedCollator;
  }
  
} //Gedcom
