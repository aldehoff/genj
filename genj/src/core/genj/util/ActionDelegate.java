package genj.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * An Action
 */
public abstract class ActionDelegate implements ActionListener {
  
  public ImgIcon img,roll;
  public String  txt;
  public String  tip;
  
  /**
   * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
   */
  public final void actionPerformed(ActionEvent e) {
    // run & catch
    try {
      run();
    } catch (Throwable t) {
      t.printStackTrace();
    }
    // done
  }
  
  /**
   * Implementor's functionality
   */
  protected void run() {
  }
  
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
    public void run() {
      frame.dispose();
    }
  }
  
  /**
   * A specialized WindowClosed delegate
   */
  public static class WindowClosedRouter extends WindowAdapter implements WindowListener {
    /** the target we're routing to */
    private ActionDelegate delegate;
    /** constructor */
    public WindowClosedRouter(ActionDelegate delegate) {
      this.delegate=delegate;
    }
    /** the routed close */
    public void windowClosed(WindowEvent e) {
      delegate.actionPerformed(null);
    }
  }
}

