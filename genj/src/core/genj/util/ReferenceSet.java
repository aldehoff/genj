package genj.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A hashmap that keeps track of values and their references
 */
public class ReferenceSet {

  /** the map we use for key->referrers */
  private Map key2references;
  
  /**
   * Constructor - uses a TreeMap that keeps
   * keys sorted by their natural order
   */
  public ReferenceSet() {
    this(new TreeMap());
  }
  
  /**
   * Constructor - uses given map for 
   * tracking key->value Sets
   */
  public ReferenceSet(Map map) {
    key2references = map;
  }
  
  /**
   * Returns the references for value
   */
  public Collection getReferences(Object val) {
    // null is ignored
    if (val==null) 
      return Collections.EMPTY_LIST;
    // lookup
    Set references = (Set)key2references.get(val);
    if (references==null) 
      return Collections.EMPTY_LIST;
    // return references
    return references;
  }
  
  /**
   * Returns the reference count of given object
   */
  public int getCount(Object val) {
    // null is ignored
    if (val==null) return 0;
    // lookup
    Set references = (Set)key2references.get(val);
    if (references==null) return 0;
    // done
    return references.size();
  }

  /**
   * Add a value
   */
  public boolean add(Object val) {
    return add(val, null);
  }

  /**
   * Add a value and its reference
   */
  public boolean add(Object val, Object reference) {
    // null is ignored
    if (val==null) return false;
    // lookup
    Set references = (Set)key2references.get(val);
    if (references==null) {
      references = new HashSet();
      key2references.put(val, references);
    }
    // keep reference
    if (reference!=null)
      references.add(reference);
    // done
    return true;
  }
  
  /**
   * Remove a value for given reference
   */
  public boolean remove(Object val, Object reference) {
    // null is ignored
    if (val==null) 
      return false;
    // lookup
    Set references = (Set)key2references.get(val);
    if (references==null) 
      return false;
    // remove
    if (!references.remove(reference))
      return false;
    // remove value
    if (references.isEmpty())
      key2references.remove(val);
    // done
    return true; 
  }
  
  /**
   * Return all values
   */
  public List getValues() {
    return new ArrayList(key2references.keySet());
  }


} //ReferenceSet
