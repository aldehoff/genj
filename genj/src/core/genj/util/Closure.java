package genj.util;

import java.lang.reflect.*;
import java.security.*;

/**
 * A Closure captures a computation that can be run later
 */
public class Closure implements Runnable {

  /** the target */
  private Object target;

  /** the method */
  private Method method;

  /** the args */
  private Object[] args;

  /**
   * Constructor
   */
  public Closure(Object pTarget, String pMethod, boolean pArg) throws IllegalArgumentException {
    this(pTarget,pMethod,new Object[]{new Boolean(pArg)}, new Class[]{Boolean.TYPE});
  }

  /**
   * Constructor
   */
  public Closure(Object pTarget, String pMethod, Object o) throws IllegalArgumentException {
    this(pTarget, pMethod, new Object[]{ o });
  }

  /**
   * Constructor
   */
  public Closure(Object pTarget, String pMethod, Object[] pArgs) throws IllegalArgumentException {

    // Calculate argument types
    Class[] argTypes = new Class[pArgs.length];
    for (int at=0;at<argTypes.length;at++) {
      argTypes[at] = pArgs.getClass();
    }

    // continue
    init(pTarget, pMethod, pArgs, argTypes);

  }

  /**
   * Constructor
   */
  public Closure(Object pTarget, String pMethod, Object[] pArgs, Class[] pArgTypes) throws IllegalArgumentException {
    init(pTarget, pMethod, pArgs, pArgTypes);
  }

  /**
   * Initialization
   */
  private void init(Object pTarget, String pMethod, Object[] pArgs, Class[] pArgTypes) throws IllegalArgumentException {

    // Remember target & args
    target = pTarget;
    args   = pArgs;

    // Find method
    Class type = pTarget.getClass();

    try {
      method = type.getDeclaredMethod(pMethod, pArgTypes);
    } catch (Throwable t) {
      System.out.println(t);
      throw new IllegalArgumentException("There's no declared method "+pMethod+" with given args on "+type);
    }

    if (!Modifier.isPublic(method.getModifiers())) {
      throw new IllegalArgumentException("Declared method "+pMethod+" with given args on "+type+" is not public");
    }


    // Done
  }

  /**
   * Runnable compliant main
   */
  public void run() {
    try {
      invoke();
    } catch (InvocationTargetException invocation) {
      throw new RuntimeException("Invoking "+method+" on "+target+" failed :"+invocation.getTargetException());
    }
  }

  /**
   * Invoke the closure
   */
  public void invoke() throws InvocationTargetException {

      try {
        method.invoke(target, args);
      } catch (IllegalAccessException access) {
        // can't happen
      } catch (IllegalArgumentException argument) {
        // can't happen
      } catch (InvocationTargetException invocation) {
        throw invocation;
      }

/*
    System.out.println("Invoking "+method+" on "+target);

    InvocationTargetException invocation = (InvocationTargetException)AccessController.doPrivileged(
      new PrivilegedAction() {
        public Object run() {
          try {
            method.invoke(target, args);
          } catch (IllegalAccessException access) {
            access.printStackTrace();
          } catch (IllegalArgumentException argument) {
            argument.printStackTrace();
          } catch (InvocationTargetException invocation) {
            return invocation;
          }
          return null;
        }
      }
    );

    if (invocation!=null)
      throw invocation;
*/
  }
}
