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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

/**
 * The default 'heavyweight' window manager
 */
public class DefaultWindowManager extends AbstractWindowManager {

  /** screen we're dealing with */
  private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
  
  /** registry */
  private Registry registry;

  /** 
   * Constructor
   */
  public DefaultWindowManager(Registry regiStry) {
    registry = regiStry;
  }
  
  /**
   * @see genj.window.WindowManager#openFrame(String, String, ImageIcon, JComponent, JMenuBar, Runnable, Runnable)
   */
  public String openFrame(String key, String title, ImageIcon image, JComponent content, JMenuBar menu, final Runnable onClosing, final Runnable onClose) {
    // create a key?
    if (key==null) getTemporaryKey();
    // deal with it in impl
    openFrameImpl(key, title, image, content, menu, onClosing, onClose);
    // done
    return key;
  }
  
  /**
   * Frame implementation
   */
  private void openFrameImpl(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, final Runnable onClosing, final Runnable onClose) {
    
    // close if already open
    close(key);

    // Create a frame
    final JFrame frame = new JFrame() {
      /**
       * dispose is our onClose hook because
       * WindowListener.windowClosed is too 
       * late (one frame) after dispose()
       */
      public void dispose() {
        // forget about key but keep bounds
        forget(key, getBounds(), registry);
        // callback?
        if (onClose!=null) onClose.run();
        // continue
        super.dispose();
      }
    };

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
      // responsibility to dispose passed to onClosing?
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          onClosing.run();
        }
      });
    }

    // remember
    Rectangle bounds = remember(key, frame, registry);

    // place
    if (bounds==null) {
      frame.pack();
      Dimension dim = frame.getSize();
      bounds = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
    }
    frame.setBounds(clip(bounds,screen));

    // show
    frame.show();
    
    // done
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.ImageIcon, java.awt.Dimension, javax.swing.JComponent)
   */
  public int openDialog(String key, String title, Icon image, JComponent content, String[] options, Component owner) {
    // check options - default to OK
    if (options==null) options = OPTIONS_OK;
    // ask impl
    return openDialogImpl(key, title, image, content, options, owner, true);
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, javax.swing.JComponent, javax.swing.JComponent)
   */
  public String openNonModalDialog(String key, String title, Icon image, JComponent content, String option, Component owner) {
    // create a key?
    if (key==null) key = getTemporaryKey();
    // construct options
    String[] options = option==null?new String[0]:new String[]{option};
    // ask impl
    openDialogImpl(key, title, image, content, options, owner, false);
    // done
    return key;
  }
  
  /**
   * Dialog implementation
   */
  private int openDialogImpl(final String key, String title, Icon image, JComponent content, String[] options, Component owner, boolean isModal) {

    // Create a dialog 
    Window parent = getWindowForComponent(owner);
    final JDialog dlg = parent instanceof Dialog ? 
      (JDialog)new JDialog((Dialog)parent) {
        /** dispose is our onClose (WindowListener.windowClosed is too late after dispose() */
        public void dispose() {
          forget(key, getBounds(), registry);
          // continue
          super.dispose();
        }
      }
    :
      (JDialog)new JDialog((Frame)parent) {
        /** dispose is our onClose (WindowListener.windowClosed is too late after dispose() */
        public void dispose() {
          forget(key, getBounds(), registry);
          // continue
          super.dispose();
        }
      }
    ;
    
    // setup looks
    dlg.setTitle(title);
    dlg.setResizable(true);
    
    // setup options/modal or non-modal
    dlg.setModal(isModal);
    
    ActionDelegate[] actions;
    if (options!=null) {
      
      actions = new ActionDelegate[options.length];
      for (int i=0; i<options.length; i++) {
        actions[i] = new ActionDelegate() {
          /** choose an option */
          protected void execute() {
            setEnabled(false);
            dlg.dispose();
          }
        }.setText(options[i]);
      }
      
    } else {
      
      actions = new ActionDelegate[0];
      
    }
    
    // assemble content
    assembleDialogContent(dlg.getRootPane(), dlg.getContentPane(), image, content, actions);

    // DISPOSE_ON_CLOSE?
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // remember
    Rectangle bounds = remember(key, dlg, registry);

    // place
    if (bounds==null) {
      dlg.pack();
      try {
        dlg.setLocationRelativeTo(owner);
      } catch (Throwable t) {
        // apparently no in JDialog/Window on jdk 1.3.1_04
      }
    } else {
      dlg.setBounds(clip(bounds,screen));
    }
      
    // show
    dlg.show();

    // did we wait for something?
    if (isModal) {
      // analyze - the disabled action is the choosen one :)
      for (int i=0; i<actions.length; i++) {
        if (!actions[i].enabled) return i;
      }
    }
        
    // done    
    return -1;
  }

  /**
   * @see genj.window.WindowManager#show(java.lang.String)
   */
  public boolean show(String key) {

    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame) {
      ((JFrame)framedlg).toFront(); 
      return true;
    }

    if (framedlg instanceof JDialog) {
      ((JDialog)framedlg).toFront();
      return true;
    }

    return false;
  }
  
  /**
   * @see genj.window.WindowManager#closeFrame(java.lang.String)
   */
  public void close(String key) {

    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame) {
      ((JFrame)framedlg).dispose(); 
      return;
    }

    if (framedlg instanceof JDialog) {
      ((JDialog)framedlg).dispose();
      return;
    }

    // done
  }
  
  /**
   * @see genj.window.WindowManager#getRootComponents()
   */
  public List getRootComponents() {

    List result = new ArrayList();
    
    // loop through keys    
    String[] keys = recallKeys();
    for (int k=0; k<keys.length; k++) {
      
      Object framedlg = recall(keys[k]);

      if (framedlg instanceof JFrame)      
        result.add(((JFrame)framedlg).getRootPane());

      if (framedlg instanceof JDialog)      
        result.add(((JDialog)framedlg).getRootPane());
    }
    
    // done
    return result;
  }
  
  /**
   * @see genj.window.WindowManager#getContent(java.lang.String)
   */
  public JComponent getContent(String key) {
    
    Object framedlg = recall(key);
    
    if (framedlg instanceof JFrame)
      return (JComponent)((JFrame)framedlg).getContentPane().getComponent(0); 

    if (framedlg instanceof JDialog)
      return (JComponent)((JDialog)framedlg).getContentPane().getComponent(0);

    return null;
  }

  /**
   * Get the window for given owner component
   */  
  private Window getWindowForComponent(Component c) {
    if (c instanceof Frame || c instanceof Dialog || c==null)
      return (Window)c;
    return getWindowForComponent(c.getParent());
  }
  
} //DefaultWindowManager