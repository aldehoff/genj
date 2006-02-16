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
import genj.util.swing.Action2;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextAreaWidget;
import genj.util.swing.TextFieldWidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Abstract base type for WindowManagers
 */
public abstract class AbstractWindowManager implements WindowManager {

  /** registry */
  protected Registry registry;

  /** a counter for temporary keys */
  private int temporaryKeyCounter = 0;  

  /** a mapping between key to framedlg */
  private Map key2framedlg = new HashMap();
  
  /** 
   * Constructor
   */
  public AbstractWindowManager(Registry regiStry) {
    registry = regiStry;
  }
  
  /**
   * @see genj.window.WindowManager#openFrame(java.lang.String, java.lang.String, javax.swing.ImageIcon, javax.swing.JComponent, java.lang.String)
   */
  public String openFrame(String key, String title, ImageIcon image, JComponent content, final Action action) {
    // key is necessary
    if (key==null) 
      key = getTemporaryKey();
    // create option
    JPanel south = new JPanel();
    new ButtonHelper().setContainer(south).create(action).addActionListener(new Close(key));
    // create new content with one option
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, content);
    panel.add(BorderLayout.SOUTH , south  );
    // delegate
    return openFrame(key, title, image, panel, null, null);
  }

  /**
   * Core frame handling implementation
   */
  public final String openFrame(String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Runnable close) {
    // create a key?
    if (key==null) 
      key = getTemporaryKey();
    // close if already open
    close(key);
    // grab parameters
    Rectangle bounds = registry.get(key, (Rectangle)null);
    boolean maximized = registry.get(key+".maximized", false);
    // deal with it in impl
    Object frame = openFrameImpl(key, title, image, content, menu, bounds, maximized, close);
    // remember it
    key2framedlg.put(key, frame);
    // done
    return key;
  }
  
  /**
   * Implementation for core frame handling
   */
  protected abstract Object openFrameImpl(String key, String title, ImageIcon image, JComponent content, JMenuBar menu, Rectangle bounds, boolean maximized, Runnable close);
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.lang.String, String[], javax.swing.JComponent)
   */
  public int openDialog(String key, String title,  int messageType, String txt, Action[] actions, Component owner) {
    
    // create a textpane for the txt
    TextAreaWidget text = new TextAreaWidget("", 2, 10);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setText(txt);
    text.setEditable(false);    
    text.setCaretPosition(0);
    text.setFocusable(false);

    // wrap in reasonable sized scroll
    JScrollPane content = new JScrollPane(text);
      
    // delegate
    return openDialog(key, title, messageType, content, actions, owner);
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.awt.Dimension, javax.swing.JComponent[], java.lang.String[], javax.swing.JComponent)
   */
  public int openDialog(String key, String title,  int messageType, JComponent[] content, Action[] actions, Component owner) {
    // assemble content into Box (don't use Box here because
    // Box extends Container in pre JDK 1.4)
    JPanel box = new JPanel();
    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
    for (int i = 0; i < content.length; i++) {
      if (content[i]==null) continue;
      box.add(content[i]);
      content[i].setAlignmentX(0F);
    }
    // delegate
    return openDialog(key, title, messageType, box, actions, owner);
  }

  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.lang.String, java.lang.String, javax.swing.JComponent)
   */
  public String openDialog(String key, String title,  int messageType, String txt, String value, Component owner) {

    // prepare text field and label
    TextFieldWidget tf = new TextFieldWidget(value, 24);
    JLabel lb = new JLabel(txt);
    
    // delegate
    int rc = openDialog(key, title, messageType, new JComponent[]{ lb, tf}, Action2.okCancel(), owner);
    
    // analyze
    return rc==0?tf.getText().trim():null;
  }

  /**
   * dialog core routine
   */
  public final int openDialog(String key, String title,  int messageType, JComponent content, Action[] actions, Component owner) {
    // check options - default to OK
    if (actions==null) 
      actions = Action2.okOnly();
    // key is necessary
    if (key==null) 
      key = getTemporaryKey();
    // close if already open
    close(key);
    // grab parameters
    Rectangle bounds = registry.get(key, (Rectangle)null);
    // do it
    Object result = openDialogImpl(key, title, messageType, content, actions, owner, bounds, true);
    // analyze - check which action was responsible for close
    for (int a=0; a<actions.length; a++) 
      if (result==actions[a]) return a;
    return -1;
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, javax.swing.JComponent, javax.swing.JComponent)
   */
  public final String openNonModalDialog(String key, String title,  int messageType, JComponent content, Action[] actions, Component owner) {
    // check options - none ok
    if (actions==null) actions = new Action[0];
    // key is necessary
    if (key==null) 
      key = getTemporaryKey();
    // close if already open
    close(key);
    // grab parameters
    Rectangle bounds = registry.get(key, (Rectangle)null);
    // do it
    Object dialog = openDialogImpl(key, title, messageType, content, actions, owner, bounds, false);
    // remember it
    key2framedlg.put(key, dialog);
    // done
    return key;
  }

  /**
   * Implementation for core frame handling
   */
  protected abstract Object openDialogImpl(String key, String title,  int messageType, JComponent content, Action[] actions, Component owner, Rectangle bounds, boolean modal);

  /**
   * Clip bounds
   */
  protected Rectangle clip(Rectangle r, Dimension screen) {

    // grab data
    int 
     x      = r.x,
     y      = r.y,
     width  = r.width,
     height = r.height;
  
    if (width>screen.width) width=screen.width;
    if (height>screen.height) height=screen.height;        
    if (x<0) x=0;
    if (y<0) y=0;
    if (x+width>screen.width) x=screen.width-width;
    if (y+height>screen.height) y=screen.height-height;
  
    // done
    return new Rectangle(x,y,width,height);
  }
  
  /**
   * Create a temporary key
   */
  protected String getTemporaryKey() {
    return "_"+temporaryKeyCounter++;
  }

  /**
   * Recall Keys
   */
  protected String[] recallKeys() {
    return (String[])key2framedlg.keySet().toArray(new String[0]);
  }

  /**
   * Recall frame/dialog
   */
  protected Object recall(String key) {
    // no key - no result
    if (key==null) 
      return null;
    // look it up
    return key2framedlg.get(key);
  }

  /**
   * Forget about frame/dialog, stash away bounds
   */
  protected void closeNotify(String key, Rectangle bounds, boolean maximized) {
    // no key - no action
    if (key==null) 
      return;
    // forget frame/dialog
    key2framedlg.remove(key);
    // temporary key? nothing to stash away
    if (key.startsWith("_")) 
      return;
    // keep bounds
    if (bounds!=null&&!maximized)
      registry.put(key, bounds);
    registry.put(key+".maximized", maximized);
    // done
  }
  
  /**
   * @see genj.window.WindowManager#closeAll()
   */
  public void closeAll() {

    // loop through keys    
    String[] keys = recallKeys();
    for (int k=0; k<keys.length; k++) {
      close(keys[k]);
    }
    
    // done
  }
  
  /**
   * An action listener that closes windows
    */
  private class Close implements ActionListener {
    
    private String key;
    
    private Close(String key) {
      this.key = key;
    }
    public void actionPerformed(ActionEvent e) {
      close(key);
    }
  } //Close
  
  /**
   * A patched up JOptionPane
   */
  protected class Content extends JOptionPane {
    
    /** constructor */
    protected Content(int messageType, JComponent content, Action[] actions) {
      super(new JLabel(),messageType, JOptionPane.DEFAULT_OPTION, null, new String[0] );
      
      // wrap content in a JPanel - the OptionPaneUI has some code that
      // depends on this to stretch it :(
      JPanel wrapper = new JPanel(new BorderLayout());
      wrapper.add(BorderLayout.CENTER, content);
      setMessage(wrapper);

      // create our action buttons
      Option[] options = new Option[actions.length];
      for (int i=0;i<actions.length;i++)
        options[i] = new Option(actions[i]);
      setOptions(options);
      
      // set defalut?
      if (options.length>0) 
        setInitialValue(options[0]);
      
      // done
    }

    /** patch up layout - don't allow too small */
    public void doLayout() {
      // let super do its thing
      super.doLayout();
      // check minimum size
      Container container = getTopLevelAncestor();
      Dimension minimumSize = container.getMinimumSize();
      Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
      minimumSize.width = Math.min(screen.width/2, minimumSize.width);
      minimumSize.height = Math.min(screen.height/2, minimumSize.height);
      Dimension size        = container.getSize();
      if (size.width < minimumSize.width || size.height < minimumSize.height) {
        Dimension newSize = new Dimension(Math.max(minimumSize.width,  size.width),
                                          Math.max(minimumSize.height, size.height));
        container.setSize(newSize);
      }
      // checked
    }
    
    /** an option in our option-pane */
    private class Option extends JButton implements ActionListener {
      
      /** constructor */
      private Option(Action action) {
        super(action);
        addActionListener(this);
      }
      
      /** trigger */
      public void actionPerformed(ActionEvent e) {
        setValue(getAction());
      }
      
    } //Action2Button
    
  } // Content 
  
} //AbstractWindowManager
