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
import genj.util.swing.ButtonHelper;
import genj.util.swing.TextFieldWidget;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * Abstract base type for WindowManagers
 */
public abstract class AbstractWindowManager implements WindowManager {

  /**
   * convenient shortcut
   */
  public int openDialog(String key, String title, Icon img, String txt, String[] options, JComponent owner) {

    JComponent content;
    
//    if (txt.indexOf("\n")>=0) {  
      JTextPane text = new JTextPane();
      text.setText(txt);
      text.setEditable(false);
      
      content = new JScrollPane(text) {
        public Dimension getPreferredSize() {
          return new Dimension(240,80);
        }
      };
      
//    } else {
//      
//      content = new JLabel(txt);
//    }
    
    return openDialog(key, title, img, null, content, options, owner, null, null);
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.awt.Dimension, javax.swing.JComponent, java.lang.String[], javax.swing.JComponent)
   */
  public int openDialog(String key, String title, Icon image, Dimension dimension, JComponent content, String[] options, JComponent owner) {
    return openDialog(key, title, image, dimension, content, options, owner, null, null);
  }
  
  /**
   * @see genj.window.WindowManager#openDialog(java.lang.String, java.lang.String, javax.swing.Icon, java.lang.String, java.lang.String, javax.swing.JComponent)
   */
  public String openDialog(String key, String title, Icon img, String txt, String value, JComponent owner) {
    
    TextFieldWidget tf = new TextFieldWidget(value, 24);
    Box box = new Box(BoxLayout.Y_AXIS);
    box.add(new JLabel(txt));
    box.add(tf);

    int rc = openDialog(key, title, img, null, box, OPTIONS_OK_CANCEL, owner);      
    
    return rc==0?tf.getText().trim():null;
  }

  /**
   * Helper for assembling dialog content
   */
  protected void assembleDialogContent(Container container, Icon image, JComponent content, ActionDelegate[] actions) {
    
    // assemble buttons for actions
    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
    new ButtonHelper().setContainer(buttons).create(actions);
    
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
    gh.add(content, 1, 0, 1, 1, gh.GROW_BOTH|gh.FILL_BOTH, insets);
    gh.add(buttons, 1, 1, 1, 1, 0);

    // done  
  }

} //AbstractWindowManager
