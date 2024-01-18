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

import genj.util.Registry;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * The window manager for 'lightweight' JInternalFrames etc.
 */
public class LightweightWindowManager extends DefaultWindowManager {
  
  /** registry */
  private Registry registry;

  /** open frames */
  private Map key2frame = new HashMap();
  
  /** one desktop */
  private JDesktopPane desktop;
  
  /** 
   * Constructor
   */
  public LightweightWindowManager(Registry regiStry) {
    super(regiStry);
    // remember
    registry = regiStry;
  }
  
  /**
   * Accessor to desktop
   */
  private JDesktopPane getDesktop(String title, ImageIcon img) {
    // already there?
    if (desktop!=null) 
      return desktop;
    // create one
    desktop = new JDesktopPane() {
      /** max window */
      public Dimension getPreferredSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
      }
    };
    // and show
    JFrame frame = new JFrame(title);
    frame.setIconImage(img.getImage());
    frame.getContentPane().add(new JScrollPane(desktop));
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.pack();
    frame.show();
    // done
    return desktop; 
  }

  /**
   * @see genj.window.DefaultWindowManager#show(java.lang.String)
   */
  public boolean show(String key) {
    // one of our internals?
    JInternalFrame frame = (JInternalFrame)key2frame.get(key);
    if (frame!=null) {
      frame.toFront();
      return true;
    }
    // continue in default
    return super.show(key);
  }
  
  /**
   * @see genj.window.WindowManager#createFrame
   */
  public void openFrame(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, final Runnable onClosing, final Runnable onClose) {

    // close if already open
    close(key);

    // Create a frame
    final JInternalFrame frame = new JInternalFrame(title, true, true, true, true) {
      /**
       * our dispose serves as onClose - WindowListener.onClose() is one frame too late
       */
      public void dispose() {
        // remember bounds
        registry.put(key, getBounds());
        // forget frame
        key2frame.remove(key);
        // callback?
        if (onClose!=null) onClose.run();
        // continue
        super.dispose();
      }

    };

    // setup looks
    if (image!=null) frame.setFrameIcon(image);
    if (menu !=null) frame.setJMenuBar(menu);

    // add content
    frame.getContentPane().add(content);

    // DISPOSE_ON_CLOSE?
    if (onClosing==null) {
      frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    } else {
      // delegate responsibility to close
      frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
      frame.addInternalFrameListener(new InternalFrameAdapter() {
        public void internalFrameClosing(InternalFrameEvent e) {
          onClosing.run();
        }
      });
    }

    // remember
    key2frame.put(key, frame);

    // place
    JDesktopPane desktop = getDesktop(title, image);
    Dimension screen = desktop.getSize();
    
    Rectangle box = registry.get(key,(Rectangle)null);
    if (box==null) { 
      frame.pack();
      Dimension dim = frame.getSize();
      box = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
    }
    frame.setBounds(clip(box, screen));

    // show
    desktop.add(frame);
    
    frame.show();
    
    // done
  }
  
  /**
   * @see genj.window.WindowManager#closeAllFrames()
   */
  public void closeAll() {
    JInternalFrame[] frames = (JInternalFrame[])key2frame.values().toArray(new JInternalFrame[0]);
    for (int i = 0; i < frames.length; i++) {
    	frames[i].dispose();
    }
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void close(String key) {
    // only for key!=null
    if (key==null)
      return;
    // check internal frames
    JInternalFrame frame = (JInternalFrame)key2frame.get(key);
    if (frame!=null) { 
      frame.dispose();
      return;
    }
    // delegate
    super.close(key);
    // done
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {
    List result = super.getRootComponents();
    if (desktop!=null)
      result.add(desktop);
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponent(java.lang.String)
   */
  public JComponent getContent(String key) {
    JInternalFrame frame = (JInternalFrame)key2frame.get(key);
    return frame!=null ? (JComponent)frame.getContentPane().getComponent(0) : null;
  }
  
} //DefaultWindowManager