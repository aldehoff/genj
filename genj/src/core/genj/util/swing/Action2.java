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
package genj.util.swing;


import genj.util.MnemonicAndText;
import genj.util.Resources;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * An Action
 */
public class Action2 extends AbstractAction implements Runnable, Cloneable {
  
  protected final static String 
    TEXT = Action.NAME,
    OLDTEXT = Action.NAME+".old",
    SHORT_TEXT = "shortname",
    TIP = Action.SHORT_DESCRIPTION,
    ENABLED = "enabled",
    MNEMONIC = Action.MNEMONIC_KEY,
    ICON = Action.SMALL_ICON;
    
  private final static Logger LOG = Logger.getLogger("genj.actions");
  
  /** a noop ActionDelegate */
  public static final Action2 NOOP = new ActionNOOP();
  
  /** async modes */
  public static final int 
    ASYNC_NOT_APPLICABLE = 0,
    ASYNC_SAME_INSTANCE  = 1,
    ASYNC_NEW_INSTANCE   = 2;
  
  /** attributes */
  private JComponent target;
  private KeyStroke accelerator;
  
  /** whether we're async or not */
  private int async = ASYNC_NOT_APPLICABLE;
  
  /** the thread executing asynchronously */
  private Thread thread;
  private Object threadLock = new Object();

  /** predefined strings */
  public final static String
    TXT_YES         = UIManager.getString("OptionPane.yesButtonText"),
    TXT_NO          = UIManager.getString("OptionPane.noButtonText"),
    TXT_OK          = UIManager.getString("OptionPane.okButtonText"),
    TXT_CANCEL  = UIManager.getString("OptionPane.cancelButtonText");
  
  /** constructor */
  public Action2() {
  }
  
  /** constructor */
  public Action2(Resources resources, String text) {
    this(resources.getString(text));
  }
  
  /** constructor */
  public Action2(String text) {
    setText(text);
  }
  
  /** constructor */
  public Action2(String text, boolean enabled) {
    this(text);
    setEnabled(enabled);
  }
  
  /**
   * trigger execution - ActionListener support
   * @see Action2#trigger()
   */
  public final void actionPerformed(ActionEvent e) {
    trigger();
  }
  
  /**
   * trigger execution - Runnable support
   * @see Action2#trigger()
   */
  public final void run() {
    trigger();
  }

  /**
   * trigger execution
   * @return status of preExecute (true unless overridden)
   */
  public final boolean trigger() {

    // do we have to create a new instance?
    if (async==ASYNC_NEW_INSTANCE) {
      try {
        Action2 ad = (Action2)clone();
        ad.setAsync(ASYNC_SAME_INSTANCE);
        return ad.trigger();
      } catch (Throwable t) {
        t.printStackTrace();
        handleThrowable("trigger", new RuntimeException("Couldn't clone instance of "+getClass().getName()+" for ASYNC_NEW_INSTANCE"));
      }
      return false;
    }
    
    // pre
    boolean preExecuteResult;
    try {
      preExecuteResult = preExecute();
    } catch (Throwable t) {
      handleThrowable("preExecute",t);
      preExecuteResult = false;
    }
    
    // execute
    if (preExecuteResult) try {
      
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
      preExecuteResult = false;
    }
    
    // post
    if (async==ASYNC_NOT_APPLICABLE||!preExecuteResult) try {
      postExecute(preExecuteResult);
    } catch (Throwable t) {
      handleThrowable("postExecute", t);
    }
    
    // done
    return preExecuteResult;
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
  protected void execute() {
    //noop
  }
  
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
  protected void postExecute(boolean preExecuteResult) {
    // Default NOOP
  }
  
  /** 
   * Handle an uncaught throwable (always sync to EDT)
   */
  protected void handleThrowable(String phase, Throwable t) {
    LogRecord record = new  LogRecord(Level.WARNING, "Action failed in "+phase);
    record.setThrown(t);
    record.setSourceClassName(getClass().getName());
    record.setSourceMethodName(phase);
    LOG.log(record); 
  }
  
  /**
   * accessor - target component
   */
  public Action2 setTarget(JComponent t) {
    target = t;
    return this;
  }
  
  /**
   * accessor - target component
   */
  public JComponent getTarget() {
    return target;
  }
  
  /** accessor - accelerator */
  public Action2 setAccelerator(String s) {
    accelerator = KeyStroke.getKeyStroke(s);
    return this;
  }
  
  /**
   * accessor - image 
   */
  public Action2 setImage(Icon icon) {
    super.putValue(ICON, icon);
    return this;
  }
  
  /**
   * accessor - text
   */
  public Action2 restoreText() {
    setText((String)super.getValue(OLDTEXT));
    return this;
  }
  
  /**
   * accessor - text
   */
  public Action2 setText(String txt) {
    return setText(null, txt);
  }
    
  /**
   * accessor - text
   */
  public Action2 setText(Resources resources, String txt) {
    
    // translate?
    if (resources!=null)
      txt = resources.getString(txt);
      
    // remember old
    super.putValue(OLDTEXT, getText());
    
    // check txt?
    if (txt!=null&&txt.length()>0)  {
        // calc mnemonic 
        MnemonicAndText mat = new MnemonicAndText(txt);
        txt  = mat.getText();
        // use accelerator keys on mac and mnemonic otherwise
        setMnemonic(mat.getMnemonic());
    }
    
    // remember new 
    super.putValue(TEXT, txt);
    
    return this;
  }
  
  /**
   * acessor - mnemonic
   */
  public Action2 setMnemonic(char c) {
    super.putValue(MNEMONIC, c==0 ? null : new Integer(c));
    return this;
  }

  /**
   * accessor - text
   */
  public String getText() {
    return (String)super.getValue(TEXT);
  }
  
  /**
   * accessor - tip
   */
  public Action2 setTip(String tip) {
    return setTip(null, tip);
  }
  
  /**
   * accessor - tip
   */
  public Action2 setTip(Resources resources, String tip) {
    if (resources!=null) tip = resources.getString(tip);
    super.putValue(TIP, tip);
    return this;
  }
  
  /**
   * accessor - tip
   */
  public String getTip() {
    return (String)super.getValue(TIP);
  }

  /**
     * accessor - image 
     */
  public Icon getImage() {
    return (Icon)super.getValue(ICON);
  }

  /** accessor - accelerator */
  public KeyStroke getAccelerator() {
    return accelerator;
  }

  /** convenience factory */
  public static Action yes() {
    return new Action2(Action2.TXT_YES);
  }

  /** convenience factory */
  public static Action no() {
    return new Action2(Action2.TXT_NO);
  }

  /** convenience factory */
  public static Action ok() {
    return new Action2(Action2.TXT_OK);
  }

  /** convenience factory */
  public static Action cancel() {
    return new Action2(Action2.TXT_CANCEL);
  }

  /** convenience factory */
  public static Action[] yesNo() {
    return new Action[]{ yes(), no() };
  }
  
  /** convenience factory */
  public static Action[] yesNoCancel() {
    return new Action[]{ yes(), no(), cancel() };
  }
  
  /** convenience factory */
  public static Action[] okCancel() {
    return new Action[]{ ok(), cancel() };
  }
  
  /** convenience factory */
  public static Action[] okOnly() {
    return new Action[]{ ok() };
  }
  
  /** convenience factory */
  public static Action[] cancelOnly() {
    return new Action[]{ cancel() };
  }
  
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
        postExecute(true);
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
  private static class ActionNOOP extends Action2 {
    /**
     * @see genj.util.swing.Action2#execute()
     */
    protected void execute() {
      // ignored
    }
  } //ActionNOOP
  
} //ActionDelegate

