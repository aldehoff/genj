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
    Method method = (Method)mapping.get(action);
    if (method==null) {
      try {
        method = target.getClass().getMethod(action, EMPTY_CLASS_ARRAY);
        mapping.put(action,method);
      } catch (Throwable t) {
        System.out.println("[Debug]Action "+action+" couldn't be delegated to "+target.getClass().getName());
        return;
      }
    }
    
    // call it
    try {
      method.invoke(target, EMPTY_OBJECT_ARRAY);
    } catch (Throwable t) {
      System.out.println("[Debug]Action "+action+" delegated to "+target.getClass().getName()+" failed with:");
      t.printStackTrace();
    }

    // done
  }

}
