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
 * $Revision: 1.82 $ $Author: nmeier $ $Date: 2005-05-02 19:52:12 $
 */
package genj.gedcom;

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
  private Map tag2id2entities = new HashMap();
  private Map tag2entities = new HashMap();
  private int minIDStringLen = 4; //lenght of ids e.g. I001
  
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
      }
    }
    // done
  }
  
  /**
   * Add entity 
   */
  /*package*/ void addEntity(Entity entity) {
    
    // remember id2entity
    String id = entity.getId();
    if (id.length()>0)
      getEntityMap(entity.getTag()).put(id, entity);
    
    // remember entity
    getEntityList(entity.getTag()).add(entity);
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
    
    // Generate id if necessary
    if (id==null)
      id = createEntityId(tag);
      
    // update minIDStringLen
    minIDStringLen = Math.max(id.length(), minIDStringLen);

    // lookup a type - all well known types need id
    Class clazz = (Class)E2TYPE.get(tag);
    if (clazz!=null) {
      if (id.length()==0)
        throw new GedcomException("Entity "+tag+" requires id");
    } else {
      clazz = Entity.class;
    }
    
    // Create entity
    Entity result; 
    try {
      result = (Entity)clazz.newInstance();
    } catch (Throwable t) {
      throw new GedcomException("Unexpected problem creating instance of "+tag);
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

    // Lookup entity map
    Map id2entity = getEntityMap(which.getTag());

    // id exists ?
    String id = which.getId();
    if (!id2entity.containsKey(id))
      throw new IllegalArgumentException("Unknown entity with id "+which.getId());

    // Tell it
    which.delNotify();

    // Delete it
    if (id2entity.get(id)==which) // might be duplicate
      id2entity.remove(id);
    getEntityList(which.getTag()).remove(which);
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
    Map id2entities = (Map)tag2id2entities.get(tag);
    if (id2entities==null) {
      id2entities = new HashMap();
      tag2id2entities.put(tag, id2entities);
    }
    // done
    return id2entities;
  }
  
  private List getEntityList(String tag) {
    List entities = (List)tag2entities.get(tag);
    if (entities==null) {
      entities = new LinkedList();
      tag2entities.put(tag, entities);
    }
    return entities;
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
    return Collections.unmodifiableCollection(getEntityList(tag));
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
    Collection ents = getEntityList(tag);
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
    for (Iterator tags=tag2id2entities.keySet().iterator();tags.hasNext();) {
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
    Map id2entity = getEntityMap(tag);
    
    // Look for an available ID
    String prefix = getEntityPrefix(tag);
    int id = Options.getInstance().isFillGapsInIDs ? 1 : id2entity.size();
    search: while (true) {
      // next one
      id ++;
      // calc result
      String suffix = Integer.toString(id);
      // check against longest id string length
      // we don't want to use I001 if there's a I00001
      while (true) {
        String candidate = prefix + suffix;
        // already used?
        if (id2entity.containsKey(candidate))
          continue search;
        // long enough?
        if (candidate.length()>=minIDStringLen) 
          break search;
        // increase length
        suffix = '0'+suffix;
      }
    }
    // patch id up to max(3,minIDStringLen-1) digits
    String result = Integer.toString(id);
    while (result.length()<Math.max(3,minIDStringLen-1))
      result = "0"+result;
    // done
    return prefix + result;
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
  public String getPlaceHierarchy() {
    return placeFormat;
  }
  
  /**
   * Accessor - place format
   */
  public void setPlaceHierarchy(String set) {
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
    return getEntityList(entity.getTag()).contains(entity);
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
          // same display language name - shortest one is best fit (e.g. 'en' instead of 'en_AU' for plain english)
          if (locales[i].getDisplayLanguage(Locale.ENGLISH).equals(language)) {
            if (cachedLocale==null||locales[i].toString().length()<cachedLocale.toString().length())
              cachedLocale = locales[i];
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
    if (cachedCollator==null) 
      cachedCollator = Collator.getInstance(getLocale());
    
    // done
    return cachedCollator;
  }
  
} //Gedcom
