package genj.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * An ActionListener that delegates actions to a java object - either
 * provide that target in constructor or inherit and provide implementations
 * in concrete sub-class
 */
public class ActionDelegate implements ActionListener {
  
  /** a marker for "no method there -> fallback" */
  private final static Object FALLBACK = new Object();
  
  /** the target */
  private Object target;
  
  /** the mapping */
  private Hashtable mapping = new Hashtable();
  
  /** constants */
  private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  private final static Class[] EMPTY_CLASS_ARRAY = new Class[0];
  
  /**
   * Constructor
   */
  public ActionDelegate(Object target) {
    this.target=target;
  }

  /**
   * Constructor
   */
  protected ActionDelegate() {
    this.target=this;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {

    String action = e.getActionCommand();
    
    // do we know a method already?
    Object method = mapping.get(action);
    if (method==null) {
      try {
        method = target.getClass().getMethod(action, EMPTY_CLASS_ARRAY);
      } catch (Throwable t) {
        method = FALLBACK;
      }
      mapping.put(action,method);
    }
    
    // call it
    try {
      if (method==FALLBACK) {
        fallback(action);
      } else {
        ((Method)method).invoke(target, EMPTY_OBJECT_ARRAY);
      }
    } catch (Throwable t) {
      System.out.println("[Debug]Action "+action+" delegated to "+target.getClass().getName()+" failed with:");
      t.printStackTrace();
    }

    // done
  }

  /**
   * Fallback in case delegation couldn't be matched (no action for method).
   * Override if necessary
   */
  public void fallback(String action) {
    // noop
  }
  
}
