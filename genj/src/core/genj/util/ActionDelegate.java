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
package genj.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * An Action
 */
public abstract class ActionDelegate implements Cloneable {
  
  /** a noop ActionDelegate */
  public static final ActionDelegate NOOP = new ActionNOOP();
  
  /** async modes */
  public static final int 
    ASYNC_NOT_APPLICABLE = 0,
    ASYNC_SAME_INSTANCE  = 1,
    ASYNC_NEW_INSTANCE   = 2;
  
  /** attributes */
  public Icon       img,roll,toggle;
  public String     txt;
  public String     stxt;
  public String     tip;
  public JComponent target;
  public boolean   enabled = true;
  
  /** whether we're async or not */
  private int async = ASYNC_NOT_APPLICABLE;
  
  /** the thread executing asynchronously */
  private Thread thread;
  private Object threadLock = new Object();
  
  /**
   * trigger execution
   * @return status of preExecute (true unless overridden)
   */
  public final boolean trigger() {

    // do we have to create a new instance?
    if (async==ASYNC_NEW_INSTANCE) {
      try {
        ActionDelegate ad = (ActionDelegate)clone();
        ad.setAsync(ASYNC_SAME_INSTANCE);
        return ad.trigger();
      } catch (Throwable t) {
        t.printStackTrace();
        handleThrowable("trigger", new RuntimeException("Couldn't clone instance of "+getClass().getName()+" for ASYNC_NEW_INSTANCE"));
      }
      return false;
    }
    
    // pre
    boolean preExecuteOk;
    try {
      preExecuteOk = preExecute();
    } catch (Throwable t) {
      handleThrowable("preExecute",t);
      preExecuteOk = false;
    }
    
    // execute
    if (preExecuteOk) try {
      
      if (async!=ASYNC_NOT_APPLICABLE) {
        
        synchronized (threadLock) {
          getThread().start();
        }
        
      } else {
        execute();
      }
      
    } catch (Throwable t) {
      handleThrowable("execute(sync)", t);
     
      // guide into sync'd postExecute because
      // getThread().start() might fail in 
      // certain security contexts (e.g. applet)
      preExecuteOk = false;
    }
    
    // post
    if (async==ASYNC_NOT_APPLICABLE||!preExecuteOk) try {
      postExecute();
    } catch (Throwable t) {
      handleThrowable("postExecute", t);
    }
    
    // done
    return preExecuteOk;
  }
  
  /**
   * Setter 
   */
  protected void setAsync(int set) {
    async=set;
  }
  
  /** 
   * Stops asynchronous execution
   */
  public void cancel(boolean wait) {

    Thread cancel;
    synchronized (threadLock) {
      if (thread==null||!thread.isAlive()) 
        return;
      cancel = thread;      
      cancel.interrupt();
    }

    if (wait) try {
      cancel.join();
    } catch (InterruptedException e) {
    }

    // done
  }
  
  /**
   * The thread running this asynchronously
   * @return thread or null
   */
  protected Thread getThread() {
    if (async!=ASYNC_SAME_INSTANCE) return null;
    synchronized (threadLock) {
      if (thread==null) thread = new Thread(new CallAsyncExecute());
      return thread;
    }
  }
  
  /**
   * Implementor's functionality (always sync to EDT)
   */
  protected boolean preExecute() {
    // Default 'yes, continue'
    return true;
  }
  
  /**
   * Implementor's functionality 
   * (called asynchronously to EDT if !ASYNC_NOT_APPLICABLE)
   */
  protected abstract void execute();
  
  /**
   * Trigger a syncExecute callback
   */
  protected final void sync() {
    if (SwingUtilities.isEventDispatchThread())
      syncExecute();
    else
      SwingUtilities.invokeLater(new CallSyncExecute());
  }
  
  /**
   * Implementor's functionality
   * (sync callback)
   */
  protected void syncExecute() {
  }
  
  /**
   * Implementor's functionality (always sync to EDT)
   */
  protected void postExecute() {
    // Default NOOP
  }
  
  /** 
   * Handle an uncaught throwable (always sync to EDT)
   */
  protected void handleThrowable(String phase, Throwable t) {
    Debug.log(Debug.ERROR, this, "Action failed in "+phase, t); 
  }
  
  /**
   * Set frame
   */
  public ActionDelegate setTarget(JComponent t) {
    target = t;
    return this;
  }
  
  /**
   * Image 
   */
  public ActionDelegate setImage(Icon i) {
    img=i;
    return this;
  }
  
  /**
   * Rollover
   */
  public ActionDelegate setRollover(Icon r) {
    roll=r;
    return this;
  }
  
  /**
   * Toggle
   */
  public ActionDelegate setToggle(Icon t) {
    toggle=t;
    return this;
  }
  
  /**
   * Text
   */
  public ActionDelegate setText(String t) {
    txt=t;
    return this;
  }
  
  /**
   * Text
   */
  public ActionDelegate setShortText(String t) {
    stxt=t;
    return this;
  }
  
  /**
   * Tip
   */
  public ActionDelegate setTip(String t) {
    tip=t;
    return this;
  }
  
  /**
   * Enabled
   */
  public ActionDelegate setEnabled(boolean e) {
    enabled = e;
    return this;
  }
  
  /**
   * Returns this delegate wrapped in a proxy now triggered
   * by that contract (without selector)
   */  
  public Object as(Class contract) {
    return as(contract,null);
  }

  /**
   * Returns this delegate wrapped in a proxy now triggered
   * by that contract (with selector)
   */  
  public Object as(Class contract, String selector) {
    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{contract}, new InvocationHandlerTrigger(selector));
  }

  /**
   * InvocationHandler trigger
   */
  private class InvocationHandlerTrigger implements InvocationHandler {
    /** a selector */
    private String selector;
    /**
     * Constructor
     */
    private InvocationHandlerTrigger(String selector) {
      this.selector = selector;
    }
    /**
     * @see java.lang.reflect.InvocationHandler#invoke(Object, Method, Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (selector==null||selector.equals(method.getName())) trigger();
      return null;
    }
  } //InvocationHandlerTrigger

  /**
   * Async Execution
   */
  private class CallAsyncExecute implements Runnable {
    public void run() {
      
      Throwable thrown = null;
      try {
        execute();
      } catch (Throwable t) {
        thrown = t;
      }
      
      // forget thread
      synchronized (threadLock) {
        thread = null;
      }
      
      // queue handleThrowable
      if (thrown!=null)
        SwingUtilities.invokeLater(new CallSyncHandleThrowable(thrown));
      
      // queue postExecute
      SwingUtilities.invokeLater(new CallSyncPostExecute());
    }
  } //AsyncExecute
  
  /**
   * Sync (EDT) Post Execute
   */
  private class CallSyncPostExecute implements Runnable {
    public void run() {
      try {
        postExecute();
      } catch (Throwable t) {
        handleThrowable("postExecute", t);
      }
    }
  } //SyncPostExecute
  
  /**
   * Sync (EDT) syncExecute
   */
  private class CallSyncExecute implements Runnable {
    public void run() {
      try {
        syncExecute();
      } catch (Throwable t) {
        handleThrowable("syncExecute", t);
      }
    }
  } //SyncPostExecute
  
  /**
   * Sync (EDT) handle throwable
   */
  private class CallSyncHandleThrowable implements Runnable {
    private Throwable t;
    protected CallSyncHandleThrowable(Throwable set) {
      t=set;
    }
    public void run() {
      // an async throwable we're going to handle now?
      try {
        handleThrowable("execute(async)",t);
      } catch (Throwable t) {
      }
    }
  } //SyncHandleThrowable
  
  /**
   * Action - noop
   */
  private static class ActionNOOP extends ActionDelegate {
    /**
     * @see genj.util.ActionDelegate#execute()
     */
    protected void execute() {
      // ignored
    }
  } //ActionNOOP
  
} //ActionDelegate

