/**
 * Nils Abstract Window Toolkit
 *
 * Copyright (C) 2000-2002 Nils Meier <nils@meiers.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package awtx;

import java.awt.*;

/**
 * A Container in a AWT environment supporting double buffering
 */
public class Rootpane extends Panel {

  private static Image offImage;
  private static Graphics offGraphics;

  /**
   * Constructor
   */
  public Rootpane() {
  }

  /**
   * Constructor
   */
  public Rootpane(Component content) {
    setLayout(new GridLayout(1,1));
    add(content);
  }

  /**
   * Paint
   */
  public void paint(Graphics g) {

    Rectangle bounds = getBounds();

    // Something to do?
    if ((bounds.width<=0)||(bounds.height<=0)||(!isShowing())) {
      return;
    }

    // DoubleBuffering !
    if ( (offImage==null)
       ||(bounds.width >offImage.getWidth (null))
       ||(bounds.height>offImage.getHeight(null)) ) {
      offImage = createImage(
        bounds.width,
        bounds.height
      );
      offGraphics = offImage.getGraphics();
    } else {
    }

    // Create a temporary graphics
    Graphics tg = offGraphics.create();
    Rectangle clip = g.getClipBounds();
    tg.setClip(clip.x,clip.y,clip.width,clip.height);

    // Clear BG
    tg.setColor(getBackground());
    tg.fillRect(clip.x,clip.y,clip.width,clip.height);

    // Render children
    super.paint(tg);

    // Paint offScreenImage
    g.drawImage(offImage,0,0,null);

    // Dispose of temporary graphics
    tg.dispose();

    // Done
  }

  /**
   * Heavyweight UPDATE overriden to skip bg clear
   */
  public void update(Graphics g) {
    paint(g);
  }
}
