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
 */package genj.util;

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
