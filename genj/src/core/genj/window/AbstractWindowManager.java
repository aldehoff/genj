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
import genj.util.GridBagHelper;
import genj.util.Registry;
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextAreaWidget;
import genj.util.swing.TextFieldWidget;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Abstract base type for WindowManagers
 */
public abstract class AbstractWindowManager implements WindowManager {

  /** a counter for temporary keys */
  private int temporaryKeyCounter = 0;  

  /** a mapping between key to framedlg */
  private Map key2framedlg = new HashMap();

  /**
   * @see genj.window.WindowManager#openFrame(java.lang.String, java.lang.String, javax.swing.ImageIcon, javax.swing.JComponent, java.lang.String)
   */
  public String openFrame(String key, String title, ImageIcon image, JComponent content, String option) {
    // key is necessary
    if (key==null) key = getTemporaryKey();
    // create option
    final String close = key;
    JPanel south = new JPanel();
    new ButtonHelper().setContainer(south).create(
      new ActionDelegate() {
        protected void execute() {
          close(close);
        }
      }.setText(option)
    );
    // create new content with one option
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(BorderLayout.CENTER, content);
    panel.add(BorderLayout.SOUTH , south  );
    // delegate
    return openFrame(key, title, image, panel, null, null, null);
  }

  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.lang.String, java.lang.String[], javax.swing.JComponent)
   */
  public int openDialog(String key, String title, Icon img, String txt, ActionDelegate[] options, Component owner) {

    // create a textpane for the txt
    TextAreaWidget text = new TextAreaWidget("", 4, 20);
    text.setLineWrap(true);
    text.setWrapStyleWord(true);
    text.setText(txt);
    text.setEditable(false);    
    text.setCaretPosition(0);
    text.setFocusable(false);

    // wrap in reasonable sized scroll
    JScrollPane content = new JScrollPane(text);
      
    // delegate
    return openDialog(key, title, img, content, options, owner);
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.awt.Dimension, javax.swing.JComponent[], java.lang.String[], javax.swing.JComponent)
   */
  public int openDialog(String key, String title, Icon image, JComponent[] content, ActionDelegate[] options, Component owner) {
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
    return openDialog(key, title, image, box, options, owner);
  }

  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.lang.String, java.lang.String, javax.swing.JComponent)
   */
  public String openDialog(String key, String title, Icon img, String txt, String value, Component owner) {

    // prepare text field and label
    TextFieldWidget tf = new TextFieldWidget(value, 24);
    JLabel lb = new JLabel(txt);
    
    // delegate
    int rc = openDialog(key, title, img, new JComponent[]{ lb, tf}, CloseWindow.OKandCANCEL(), owner);
    
    // analyze
    return rc==0?tf.getText().trim():null;
  }

  /**
   * Helper for assembling dialog content
   */
  protected void assembleDialogContent(JRootPane root, Container container, Icon image, JComponent content, ActionDelegate[] actions) {

    // assemble buttons for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    ButtonHelper bh = new ButtonHelper().setContainer(buttons);
    for (int a=0; a<actions.length; a++) {
      AbstractButton b = bh.create(actions[a]);
      // set default - sadly JRootpane only accepts a JButton
      if (a==0&&b instanceof JButton) root.setDefaultButton((JButton)b);	
    }
    
    // prepare an icon
    JLabel icon = new JLabel(image);
    icon.setVerticalAlignment(SwingConstants.TOP);
    
    // prepare panel
    //
    // +-----+---------+
    // |     |         |
    // |     | content |
    // | img |         |
    // |     +---------+
    // |     | buttons |
    // +-----+---------+
    //
    Insets insets = new Insets(8,8,8,8);
    GridBagHelper gh = new GridBagHelper(container);
    gh.add(icon   , 0, 0, 1, 2, 0, insets);
    gh.add(content, 1, 0, 1, 1, gh.GROWFILL_BOTH, insets);
    gh.add(buttons, 1, 1, 1, 1, 0);

    // done
  }
  
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
    if (key==null) return null;
    // look it up
    return key2framedlg.get(key);
  }

  /**
   * Remember frame/dialog, recall bounds from registry
   */
  protected Rectangle remember(String key, Object framedlg, Registry registry) {
    // no key - no action
    if (key==null) return null;
    // remember frame/dialog
    key2framedlg.put(key, framedlg);
    // temporary key? nothing to recall
    if (key.startsWith("_")) return null;
    // recall!
    return registry.get(key, (Rectangle)null);
  }
  
  /**
   * Forget about frame/dialog, stash away bounds
   */
  protected void forget(String key, Rectangle bounds, Registry registry) {
    // no key - no action
    if (key==null) return;
    // forget frame/dialog
    key2framedlg.remove(key);
    // temporary key? nothing to stash away
    if (key.startsWith("_")) return;
    // keep bounds
    if (bounds!=null)
      registry.put(key, bounds);
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
  
} //AbstractWindowManager
