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

import genj.util.ActionDelegate;
import genj.util.Registry;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * The default 'heavyweight' window manager
 */
public class DefaultWindowManager extends AbstractWindowManager {

  /** screen we're dealing with */
  private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
  
  /** registry */
  private Registry registry;

  /** open frames */
  private Map key2frame = new HashMap();

  /** open dialogs */
  private Map key2dlg = new HashMap();

  /** 
   * Constructor
   */
  public DefaultWindowManager(Registry regiStry) {
    registry = regiStry;
  }
  
  /**
   * @see genj.window.WindowManager#createFrame
   */
  public void openFrame(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, final Runnable onClosing, final Runnable onClose) {

    // close if already open
    close(key);

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
    if (box==null) {
      frame.pack();
      Dimension dim = frame.getSize();
      box = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
    }
    frame.setBounds(clip(box,screen));

    // show
    frame.show();
    
    // done
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.ImageIcon, java.awt.Dimension, javax.swing.JComponent)
   */
  public int openDialog(final String key, String title, Icon image, JComponent content, String[] options, JComponent owner, final Runnable onClosing, final Runnable onClose) {

    // Create a dialog 
    final JDialog dlg = new JOptionPane().createDialog(owner, title);
    dlg.getContentPane().removeAll();
    
    // setup looks
    dlg.setResizable(true);
    dlg.setModal(true);
    
    // setup choices
    final int choice;
    
    if (options==null) options = new String[] { UIManager.getString("OptionPane.okButtonText")};
    ActionDelegate[] actions = new ActionDelegate[options.length];
    for (int i=0; i<options.length; i++) {
      actions[i] = new ActionDelegate() {
        /** choose an option */
        protected void execute() {
          setEnabled(false);
          dlg.dispose();
        }
      }.setText(options[i]);
    }
    
    // assemble content
    assembleDialogContent(dlg.getContentPane(), image, content, actions);

    // DISPOSE_ON_CLOSE?
    if (onClosing==null) {
      dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    } else {
      dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    // remember
    if (key!=null)
      key2dlg.put(key, dlg);

    // prepare to forget
    dlg.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (onClosing!=null) try {
          onClosing.run();
        } catch (Throwable t) {
        }
      }
      public void windowClosed(WindowEvent e) {
        if (key!=null) {
          registry.put(key, dlg.getBounds());
          key2dlg.remove(key);
        }
        if (onClose!=null) onClose.run();
      }
    });

    // place
    Rectangle box = registry.get(key,(Rectangle)null);
    if (box==null) {
      dlg.pack();
      dlg.setLocationRelativeTo(owner);
      box = dlg.getBounds();
    }
    dlg.setBounds(clip(box,screen));
    
    // show
    dlg.show();
    
    // analyze
    for (int i=0; i<actions.length; i++) {
      if (!actions[i].enabled) return i;
    }
        
    // done    
    return -1;
  }

  /**
   * @see genj.window.WindowManager#isFrame(java.lang.String)
   */
  public boolean isOpen(String key) {
    return key2frame.containsKey(key) || key2dlg.containsKey(key);
  }
  
  /**
   * @see genj.window.WindowManager#closeAllFrames()
   */
  public void closeAll() {
    JFrame[] frames = (JFrame[])key2frame.values().toArray(new JFrame[0]);
    for (int i = 0; i < frames.length; i++) {
    	frames[i].dispose();
    }
    JDialog[] dlgs = (JDialog[])key2dlg.values().toArray(new JDialog[0]);
    for (int i = 0; i < dlgs.length; i++) {
      dlgs[i].dispose();
    }
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void close(String key) {
    JFrame frame = (JFrame)key2frame.get(key);
    if (frame!=null) { 
      frame.dispose();
      return;
    }
    JDialog dlg = (JDialog)key2dlg.get(key);
    if (dlg!=null) {
      dlg.dispose();
      return;
    }
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {
    List result = new ArrayList();
    Iterator frames = key2frame.keySet().iterator();
    while (frames.hasNext()) 
      result.add(getRootComponent(frames.next().toString()));
    Iterator dlgs = key2dlg.keySet().iterator();
    while (dlgs.hasNext())
      result.add(getRootComponent(dlgs.next().toString()));
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponent(java.lang.String)
   */
  public JComponent getRootComponent(String key) {
    JFrame frame = (JFrame)key2frame.get(key);
    if (frame!=null) 
      return (JComponent)frame.getContentPane().getComponent(0);
    JDialog dlg = (JDialog)key2dlg.get(key);
    if (dlg!=null) 
      return (JComponent)dlg.getContentPane().getComponent(0);
    return null;
  }

} //DefaultWindowManager