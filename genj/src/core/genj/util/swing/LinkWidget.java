/**
 *
 */
package genj.util.swing;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * 
 */
public class LinkWidget extends JButton {
  
  /** status hover */
  private boolean hover = false;

  /** text */
  private String plain, underlined;
  
  /**
   * Constructor
   */
  public LinkWidget() {

    // change looks
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setBorder(null);
    setBorderPainted(false);
    try {
      super.setFocusable(false);
    } catch (Throwable t) {
      // try pre 1.4 instead
      super.setRequestFocusEnabled(false);
    }

    setFocusPainted(false);
    setContentAreaFilled(false);
    setHorizontalAlignment(SwingConstants.LEFT);
    
    // listen
    addMouseListener(new Callback());
    
    // done
  }
   
  /**
   * Overriden to try 1.4's super.setFocusable()
   * @see java.awt.Component#setFocusable(boolean)
   */
  public void setFocusable(boolean focusable) {
    try {
      super.setFocusable(focusable);
    } catch (Throwable t) {
      // try pre 1.4 instead
      super.setRequestFocusEnabled(false);
    }
  }
   
  /**
   * @see javax.swing.AbstractButton#setText(java.lang.String)
   */
  public void setText(String text) {
    // remember
    plain = ("<html>"+text);
    underlined = ("<html><u>"+text);
    // continue
    super.setText(plain);
  }

  /**
   * setText
   */
  private void setTextInternal(String text) {
    super.setText(text);
  }
  
  /**
   * A private callback code block
   */
  private class Callback extends MouseAdapter {
    /**
     * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
      setTextInternal(plain);
    }

    /**
     * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
      setTextInternal(underlined);
    }
    
  } //Callback

} //LinkWidget

