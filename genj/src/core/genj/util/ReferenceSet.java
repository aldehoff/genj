package genj.util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A hashmap that counts the number of This comment is specified in template 'typecomment'. (Window>Preferences>Java>Templates)
 */
public class ReferenceSet extends AbstractSet {

  /** the map we use for counting */
  private HashMap key2ref = new HashMap();
  
  /**
   * Ref
   */
  private static class Ref {
    int count = 0; 
  } //Ref

  /**
   * @see genj.util.ReferenceSet#add(java.lang.Object)
   */
  public boolean add(Object o) {
    // increase counter
    Ref ref = (Ref)key2ref.get(o);
    if (ref==null) {
      ref = new Ref();
      key2ref.put(o, ref);
    } 
    ref.count++;
    // done
    return true;
  }
  
  /**
   * @see genj.util.ReferenceSet#remove(java.lang.Object)
   */
  public boolean remove(Object o) {
    // find counter
    Ref ref = (Ref)key2ref.get(o);
    if (ref==null)
      return false;
    // decrease
    ref.count--;
    // remove?
    if (ref.count<=0) {
      key2ref.remove(o);
    }
    // done
    return true;
  }

  /**
   * @see java.util.Collection#iterator()
   */
  public Iterator iterator() {
    return key2ref.keySet().iterator();
  }

  /**
   * @see java.util.Collection#size()
   */
  public int size() {
    return key2ref.size();
  }
  
  /**
   * @see genj.util.ReferenceSet#toArray()
   */
  public Object[] toArray() {
    return key2ref.keySet().toArray();
  }
  
  /**
   * @see genj.util.ReferenceSet#toArray(java.lang.Object[])
   */
  public Object[] toArray(Object[] a) {
    return key2ref.keySet().toArray(a);
  }


} //ReferenceSet
