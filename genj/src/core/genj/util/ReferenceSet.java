package genj.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A hashmap that keeps track of keys and their references
 */
public class ReferenceSet {

  /** the map we use for key->reference */
  private Map key2references;
  
  /** total number of references we know about */
  private int size = 0;
  
  /**
   * Constructor - uses a TreeMap that keeps
   * keys sorted by their natural order
   */
  public ReferenceSet() {
    this(new TreeMap());
  }
  
  /**
   * Constructor - uses given map for 
   * tracking key->references Sets
   */
  public ReferenceSet(Map map) {
    key2references = map;
  }
  
  /**
   * Returns the references for a given key
   */
  public Collection getReferences(Object key) {
    // null is ignored
    if (key==null) 
      return Collections.EMPTY_LIST;
    // lookup
    Set references = (Set)key2references.get(key);
    if (references==null) 
      return Collections.EMPTY_LIST;
    // return references
    return references;
  }
  
  /**
   * Returns the total number of references 
   */
  public int getSize() {
    return size;
  }
  
  /**
   * Returns the number of reference for given key
   */
  public int getSize(Object key) {
    // null is ignored
    if (key==null) 
      return 0;
    // lookup
    Set references = (Set)key2references.get(key);
    if (references==null) 
      return 0;
    // done
    return references.size();
  }

  /**
   * Add a key
   */
  public boolean add(Object key) {
    return add(key, null);
  }

  /**
   * Add a key and its reference
   * @return whether the reference was actually added (could have been known already) 
   */
  public boolean add(Object key, Object reference) {
    // null is ignored
    if (key==null) 
      return false;
    // lookup
    Set references = (Set)key2references.get(key);
    if (references==null) {
      references = new HashSet();
      key2references.put(key, references);
    }
    // safety check for reference==null - might be
    // and still was necessary to keep key    
    if (reference==null)
      return false;
    // add
    if (!references.add(reference)) 
      return false;
    // increase total
    size++;      
    // done
    return true;
  }
  
  /**
   * Remove a reference for given key
   */
  public boolean remove(Object key, Object reference) {
    // null is ignored
    if (key==null) 
      return false;
    // lookup
    Set references = (Set)key2references.get(key);
    if (references==null) 
      return false;
    // remove
    if (!references.remove(reference))
      return false;
    // decrease total
    size--;
    // remove value
    if (references.isEmpty())
      key2references.remove(key);
    // done
    return true; 
  }
  
  /**
   * Return all values
   */
  public List getKeys() {
    return getKeys(true);
  }

  /**
   * Return all values
   * @param sortByKey sort the list by keys or by the number of references
   */
  public List getKeys(boolean sortByKey) {
    ArrayList result = new ArrayList(key2references.keySet()); 
    if (sortByKey) 
      Collections.sort(result);
    else 
      Collections.sort(result, new Comparator() {
        public int compare(Object o1, Object o2) {
          return getSize(o1) - getSize(o2);
        }
      });
      
    return result;
  }


} //ReferenceSet
