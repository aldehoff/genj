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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Abstract base type for WindowManagers
 */
public abstract class AbstractWindowManager implements WindowManager {

  /**
   * convenient shortcut
   */
  public int openDialog(String key, Icon img, String txt, String[] options, JComponent owner) {

    JComponent content;
    
//    if (txt.indexOf("\n")>=0) {  
      JTextPane text = new JTextPane();
      text.setText(txt);
      text.setEditable(false);
      
      content = new JScrollPane(text) {
        public Dimension getPreferredSize() {
          return new Dimension(240,160);
        }
      };
      
//    } else {
//      
//      content = new JLabel(txt);
//    }
    
    return openDialog(key, null, img, null, content, options, owner, null, null);
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
    icon.setBorder(new EmptyBorder(8,8,8,8));
    
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
    GridBagHelper gh = new GridBagHelper(container);
    gh.add(icon   , 0, 0, 1, 2);
    gh.add(content, 1, 0, 1, 1);
    gh.add(buttons, 1, 1, 1, 1);

    // done  
  }

} //AbstractWindowManager
