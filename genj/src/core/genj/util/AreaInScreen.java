package genj.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

/**
 * A type that handles like a Rectangle but makes sure
 * it's a valid area 'within' the current screen
 */
public class AreaInScreen extends Rectangle {
  
  /**
   * Constructor
   */
  public AreaInScreen(Rectangle r) {
    
    // grab data
    x = r.x;
    y = r.y;
    width = r.width;
    height = r.height;
    
    // make sure that given Rectangle is within given dimension
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    if (width>screen.width) width=screen.width;
    if (height>screen.height) height=screen.height;        
    if (x<0) x=0;
    if (y<0) y=0;
    if (x+width>screen.width) x=screen.width-width;
    if (y+height>screen.height) y=screen.height-height;
    
    // done
  }

}
