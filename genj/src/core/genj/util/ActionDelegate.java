package genj.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Glue between ActionCommands and methods on instances
 */
public class ActionDelegate implements ActionListener {
  
  /** the instance the actions will be delegated to */
  private Object instance;
  
  /** the mapping */
  private Hashtable mapping = new Hashtable();
  
  /** constants */
  private final static Object[] EMPTY_OBJECT_ARRAY = new Object[0];
  private final static Class[] EMPTY_CLASS_ARRAY = new Class[0];

  /**
   * Constructor
   */
  public ActionDelegate(Object setInstance) {
    instance = setInstance;
  }
  
  /**
   * Add a delegate
   */
  public ActionDelegate add(String action, String method) throws IllegalArgumentException {
    
    try {
      mapping.put(
        action,
        instance.getClass().getMethod(method, EMPTY_CLASS_ARRAY)
      );
    } catch (Throwable t) {
      throw new IllegalArgumentException("Method "+method+" on "+instance.getClass().getName()+" is no good");
    }
    
    return this;
  }
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    
    // Something we know?
    Method m = (Method)mapping.get(e.getActionCommand());
    if (m==null) return;
    
    // call it
    try {
      m.invoke(instance, EMPTY_OBJECT_ARRAY);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    
    // done
  }

}
