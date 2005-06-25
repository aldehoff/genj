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
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

/**
 * The default 'heavyweight' window manager
 */
public class DefaultWindowManager extends AbstractWindowManager {

  /** screen we're dealing with */
  private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
  
  /** 
   * Constructor
   */
  public DefaultWindowManager(Registry registry) {
    super(registry);
  }
  
  /**
   * Frame implementation
   */
  protected Object openFrameImpl(final String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Rectangle bounds, boolean maximized, final Runnable close) {
    
    // Create a frame
    final JFrame frame = new JFrame() {
      /**
       * dispose is our onClose hook because
       * WindowListener.windowClosed is too 
       * late (one frame) after dispose()
       */
      public void dispose() {
        // forget about key but keep bounds
        closeNotify(key, getBounds(), getExtendedState()==MAXIMIZED_BOTH);
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
    if (close==null) {
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    } else {
      // responsibility to dispose passed to onClosing?
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          close.run();
        }
      });
    }

    // place
    if (bounds==null) {
      frame.pack();
      Dimension dim = frame.getSize();
      bounds = new Rectangle(screen.width/2-dim.width/2, screen.height/2-dim.height/2,dim.width,dim.height);
    }
    frame.setBounds(clip(bounds,screen));
    
    if (maximized)
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);

    // show
    frame.setVisible(true);
    
    // done
    return frame;
  }
  
  /**
   * Dialog implementation
   */
  protected Object openDialogImpl(final String key, String title, Icon image, JComponent content, ActionDelegate[] actions, Component owner, Rectangle bounds, boolean isModal) {

    // Create a dialog 
    Window parent = getWindowForComponent(owner);
    final JDialog dlg = parent instanceof Dialog ? 
      (JDialog)new JDialog((Dialog)parent) {
        /** dispose is our onClose (WindowListener.windowClosed is too late after dispose() */
        public void dispose() {
          closeNotify(key, getBounds(), false);
          // continue
          super.dispose();
        }
      }
    :
      (JDialog)new JDialog((Frame)parent) {
        /** dispose is our onClose (WindowListener.windowClosed is too late after dispose() */
        public void dispose() {
          closeNotify(key, getBounds(), false);
          // continue
          super.dispose();
        }
      }
    ;
    
//    dlg.addComponentListener(new ComponentAdapter() {
//      public void componentMoved(ComponentEvent e) {
//        System.out.println("moved");
//      }
//      public void componentResized(ComponentEvent e) {
//        System.out.println("resized");
//      }
//    });
    
    // hook up to ESC (=cancel)
    Action escape = new AbstractAction() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        dlg.dispose();
      }
    };
    dlg.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escape);
    dlg.getRootPane().getActionMap().put(escape, escape);
    
    // setup looks
    dlg.setTitle(title);
    dlg.setResizable(true);
    
    // setup options/modal or non-modal
    dlg.setModal(isModal);
    
    if (actions==null)
      actions = new ActionDelegate[0];
    for (int i=0; i<actions.length; i++) {
      if (actions[i] instanceof CloseWindow)
        ((CloseWindow)actions[i]).setDialog(dlg);
    }
      
    // assemble content
    assembleDialogContent(dlg.getRootPane(), dlg.getContentPane(), image, content, actions);

    // DISPOSE_ON_CLOSE?
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // place
    if (bounds==null) {
      dlg.pack();
      dlg.setLocationRelativeTo(owner);
    } else {
      dlg.setBounds(clip(bounds,screen));
    }
      
    // show
    dlg.setVisible(true);

    // done    
    return dlg;
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