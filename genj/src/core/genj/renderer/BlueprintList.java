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
package genj.renderer;

import genj.util.swing.ButtonHelper;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/** 
 * A list of editable BluePrints */
public class BlueprintList extends JSplitPane {
  
  /** we keep one editor */
  private BlueprintEditor editor = new BlueprintEditor();
  
  /** a list */
  private JList blueprints = new JList();
  
  /**
   * Constructor   */
  public BlueprintList() {
    // left section
    Box left = new Box(BoxLayout.Y_AXIS);
    left.add(new JScrollPane(blueprints));
    
    //ButtonHelper bh = new ButtonHelper().set
    JButton b = new JButton("Add");
    b.setMaximumSize(null);
    b.setAlignmentX(0.5F);
    left.add(b);
    left.add(new JButton("Remove"));
    // children
    setLeftComponent(left);
    setRightComponent(editor);
    // done    
  }

} //BluePrintList
