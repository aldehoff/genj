package genj.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * An Action
 */
public abstract class ActionDelegate {
  
  public ImgIcon img,roll;
  public String  txt;
  public String  tip;
  
  /**
   * the interal triggering callback
   */
  protected final void trigger() {
    // run & catch
    try {
      execute();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    // done
  }
  
  /**
   * Implementor's functionality
   */
  protected abstract void execute();
  
  /**
   * Image 
   */
  public ActionDelegate setImage(ImgIcon i) {
    img=i;
    return this;
  }
  
  /**
   * Rollover
   */
  public ActionDelegate setRollover(ImgIcon r) {
    roll=r;
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
   * Tip
   */
  public ActionDelegate setTip(String t) {
    tip=t;
    return this;
  }
  
  /**
   * A default Frame close Action
   */
  public static class ActionDisposeFrame extends ActionDelegate {
    /** a frame */
    private Frame frame;
    /** constructor */
    public ActionDisposeFrame(Frame f) {
      frame = f;
    }
    /** run */
    public void execute() {
      frame.dispose();
    }
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
    // this will have to be done through java.lang.reflect.Proxy at some point
    if (contract==WindowListener.class) {
      return new AsWindowListener(selector);
    }
    if (contract==ActionListener.class) {
      return new AsActionListener();
    }
    if (contract==Runnable.class) {
      return new AsRunnable();
    }
    if (contract==ListSelectionListener.class) {
      return new AsListSelectionListener();
    }
    // don't know
    throw new RuntimeException("Unsupported contract '"+contract+"' for ActionDelegate");
  }

  /**
   * A converter - WindowListener
   */
  private class AsWindowListener extends WindowAdapter {
    /** selector */
    private String selector;
    /** constructor */
    protected AsWindowListener(String s) {
      selector=s;
    }
    /** the routed close */
    public void windowClosed(WindowEvent e) {
      if ("windowClosed".equals(selector)) trigger();
    }
    /** the routed closing */
    public void windowClosing(WindowEvent e) {
      if ("windowClosing".equals(selector)) trigger();
    }
  }
  
  /**
   * A converter - ListSelectionListener
   */
  private class AsListSelectionListener implements ListSelectionListener {
    /** the routed selection */
    public void valueChanged(ListSelectionEvent e) {
      trigger();
    }
  }
  
  /**
   * A converter - Runnable.run
   */
  private class AsRunnable implements Runnable {
    /** the routed call */
    public void run() {
      trigger();
    }
  }

  /**
   * A converter - ActionListener
   */
  private class AsActionListener implements ActionListener {
    /** the routed call */
    public void actionPerformed(ActionEvent e) {
      trigger();
    }
  }
  
}

