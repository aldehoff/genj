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
package genj.window;

import genj.util.AreaInScreen;
import genj.util.Registry;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * The default 'heavyweight' window manager
 */
public class DefaultWindowManager implements WindowManager {
  
  /** registry */
  private Registry registry;

  /** open frames */
  private Map key2frame = new HashMap();

  /** 
   * Constructor
   */
  public DefaultWindowManager(Registry regiStry) {
    registry = regiStry;
  }
  
  /**
   * @see genj.window.WindowManager#isFrame(java.lang.String)
   */
  public boolean isFrame(String key) {
    return key2frame.containsKey(key);
  }

  /**
   * @see genj.window.WindowManager#createFrame(java.lang.String, java.lang.String, javax.swing.ImageIcon, java.awt.Dimension, javax.swing.JComponent, java.lang.Runnable)
   */
  public void openFrame(final String key, String title, ImageIcon image, Dimension dimension, JComponent content, JMenuBar menu, final Runnable onClosing, final Runnable onClose) {

    // close if already open
    closeFrame(key);

    // Create a frame
    final JFrame frame = new JFrame();

    // setup looks
    if (title!=null) frame.setTitle(title);
    if (image!=null) frame.setIconImage(image.getImage());
    if (menu !=null) frame.setJMenuBar(menu);

    // add content
    frame.getContentPane().add(content);

    // DISPOSE_ON_CLOSE?
    if (onClosing==null) {
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    } else {
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    // remember
    key2frame.put(key, frame);

    // prepare to forget
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (onClosing!=null) try {
          onClosing.run();
        } catch (Throwable t) {
        }
      }
      public void windowClosed(WindowEvent e) {
        registry.put(key, frame.getBounds());
        key2frame.remove(key);
        if (onClose!=null) onClose.run();
      }
    });

    // place
    Rectangle box = registry.get(key,(Rectangle)null);
    if ((box==null)&&(dimension!=null)) 
      box = new Rectangle(0,0,dimension.width,dimension.height);
    if (box==null) {
      frame.pack();
    } else {
      frame.setBounds(new AreaInScreen(box));
    }

    // show
    frame.show();
    
    // done
  }
  
  /**
   * @see genj.window.WindowManager#closeAllFrames()
   */
  public void closeAllFrames() {
    JFrame[] frames = (JFrame[])key2frame.values().toArray(new JFrame[0]);
    for (int i = 0; i < frames.length; i++) {
    	frames[i].dispose();
    }
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void closeFrame(String key) {
    JFrame frame = (JFrame)key2frame.get(key);
    if (frame!=null) { 
      frame.dispose();
    }
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {
    List result = new ArrayList();
    Iterator frames = key2frame.values().iterator();
    while (frames.hasNext()) {
      JFrame frame = (JFrame)frames.next();
      result.add(frame.getContentPane());   
    }
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponent(java.lang.String)
   */
  public JComponent getRootComponent(String key) {
    JFrame frame = (JFrame)key2frame.get(key);
    return frame!=null ? (JComponent)frame.getContentPane().getComponent(0) : null;
  }


} //DefaultWindowManager