/**
 *
 */
package genj.util.swing;

import genj.util.ActionDelegate;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * 
 */
public class LinkWidget extends JLabel {
  
  /** status hover */
  private boolean hover = false;

  /** text */
  private String plain, underlined;
  
  /** action */
  private ActionDelegate action;
  
  /**
   * Constructor
   */
  public LinkWidget(ActionDelegate action) {
    this(action.getText(), action.getImage());
    this.action = action;
  }
  
  /**
   * Constructor
   */
  public LinkWidget(String text, Icon img) {
    super(text, img, SwingConstants.LEFT);
    addMouseListener(new Callback());
  }
  
  /**
   * Constructor
   */
  public LinkWidget(ImageIcon img) {
    this(null, img);
  }
  
  /**
   * Constructor
   */
  public LinkWidget() {
    this(null,null);
  }
   
  /**
   * @see javax.swing.AbstractButton#setText(java.lang.String)
   */
  public void setText(String text) {
    // remember
    plain = ("<html>"+text);
    underlined = ("<html><u>"+text);
    // continue
    super.setText(text);
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
    
    /** click -> action */
    public void mouseClicked(MouseEvent e) {
      if (action!=null)
        action.actionPerformed(new ActionEvent(this, 0, ""));
    }
    /** exit -> plain */
    public void mouseExited(MouseEvent e) {
      setTextInternal(plain);
    }
    /** exit -> underlined */
    public void mouseEntered(MouseEvent e) {
      setTextInternal(underlined);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
  } //Callback

} //LinkWidget

