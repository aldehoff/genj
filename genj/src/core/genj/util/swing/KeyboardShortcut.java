/**
 * A keyboar
 */
package genj.util.swing;

import genj.util.ActionDelegate;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * A smart keyboard shortcut
 */
public class KeyboardShortcut extends AbstractAction {

  public final static int
   CTRL = InputEvent.CTRL_MASK,
   ALT  = InputEvent.ALT_MASK;
  
  /** keystroke */
  private KeyStroke keystroke;
  
  /** action */
  private ActionDelegate action;
  
  /**
   * Constructor
   */
  public KeyboardShortcut(int key, int mod, ActionDelegate action) throws IllegalArgumentException {
    this( KeyStroke.getKeyStroke(key, mod), action );
  }
  
  /**
   * Constructor
   */
  public KeyboardShortcut(String code, ActionDelegate action) throws IllegalArgumentException {
    this( KeyStroke.getKeyStroke(code), action);
  }
  
  /**
   * Constructor
   */
  public KeyboardShortcut(KeyStroke keystroke, ActionDelegate action) throws IllegalArgumentException {
    if (keystroke==null) 
      throw new IllegalArgumentException("illegal keystroke code");
    this.keystroke = keystroke;
    this.action = action;
  }

  /**
   * install shortcut
   */
  public void install(JComponent c) {
    c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keystroke, this);
    c.getActionMap().put(this, this);
  }
  
  /** 
   * exec callback
   */
  public void actionPerformed(ActionEvent e) {
    action.trigger();
  }
  
} //KeyboardShortcut
