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
package genj.table;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import genj.util.GridBagHelper;
import genj.gedcom.*;
import genj.option.*;
import genj.app.*;
import genj.util.Resources;

/**
 * Class for providing ViewInfo information to a ViewEditor
 */
public class TableViewInfo extends JPanel implements ViewInfo {

  /** components */
  private JComboBox       cTypes;
  private TagPathTree     pathTree;
  private TagPathList     pathList;
  private TableView       table;

  /** currently viewed types */
  private int             eType = Gedcom.INDIVIDUALS;

  /**
   * Creates the visual parts of the editor
   */
  public TableViewInfo() {

    // Create!
    GridBagHelper helper = new GridBagHelper(this);

    // Prepare an action listener
    ActionListener alistener = new ActionListener() {
      // LCD
      /** notification about action */
      public void actionPerformed(ActionEvent e) {
        if (table==null) {
          return;
        }
        // Selection of Type?
        if (e.getSource()==cTypes) {
          eType = cTypes.getSelectedIndex();
          readFromTable();
          return;
        }
        // Buttons?
        if ("UP".equals(e.getActionCommand())) {
          pathList.up();
        }
        if ("DOWN".equals(e.getActionCommand())) {
          pathList.down();
        }
        // Done
      }
      // EOC
    };

    // Chooseable type
    cTypes = new JComboBox();

    for (int i=Gedcom.FIRST_ETYPE;i<=Gedcom.LAST_ETYPE;i++) {
      cTypes.addItem(Gedcom.getNameFor(i,true));
    }
    cTypes.addActionListener(alistener);

    // Tree of TagPaths
    pathTree = new TagPathTree();

    TagPathTreeListener plistener = new TagPathTreeListener() {
      // LCD
      /** selection notification */
      public void handleSelection(TagPath path, boolean on) {
        if (!on) {
          pathList.removePath(path);
        } else {
          pathList.addPath(path);
        }
      }
      // EOC
    };
    pathTree.addTagPathTreeListener(plistener);

    // List of TagPaths
    pathList = new TagPathList();

    // Up/Down of ordering
    JButton bUp   = ButtonHelper.createButton(TableView.resources.getString("info.up"),null,"UP",alistener,true,false);
    JButton bDown = ButtonHelper.createButton(TableView.resources.getString("info.down"),null,"DOWN",alistener,true,false);

    // Layout
    helper.add(new JLabel(TableView.resources.getString("info.entities"))  ,0,1,1,1);
    helper.add(cTypes                  ,1,1,2,1);

    helper.add(new JLabel(TableView.resources.getString("info.columns"))  ,0,2,1,1);
    helper.add(pathTree                ,1,2,2,1,helper.GROW_BOTH|helper.FILL_BOTH);

    helper.add(new JLabel(TableView.resources.getString("info.order"))  ,0,3,1,1);
    helper.add(bUp                     ,0,4,1,1);
    helper.add(bDown                   ,0,5,1,1);
    helper.add(pathList                ,1,3,2,4,helper.GROW_BOTH|helper.FILL_BOTH);

    // Done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    writeToTable();
  }

  /**
   * Has to return the component used to edit for editing
   */
  public Component getEditor() {
    return this;
  }

  /**
   * Reflect state of Table in our Editor
   */
  private void readFromTable() {

    // Clear reference to table for listeners
    TableView t = table;
    table = null;

    // Reflect shown type
    cTypes.setSelectedIndex(eType-Gedcom.FIRST_ETYPE);

    // Reflect columns by TagPaths
    TagPath[] selectedPaths = t.getTagPathsFor(eType);
    TagPath[] usedPaths     = TagPath.getUsedTagPaths(t.gedcom,eType);

    pathTree.setPaths(usedPaths);
    pathTree.setSelection(selectedPaths);

    pathList.setPaths(selectedPaths);

    // Show table again
    table = t;

    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {
    readFromTable();
  }

  /**
   * Initializes this ViewInfo with the given View
   */
  public void setView(Component comp) throws IllegalArgumentException {

    // Preset values
    if (!(comp instanceof TableView))
      throw new IllegalArgumentException();

    // Calculate Table component & starting type
    table = (TableView)comp;
    eType = table.getType();

    readFromTable();

    // Done
  }

  /**
   * Write changed state to Table
   */
  private void writeToTable() {

    // Write columns by TagPaths
    TagPath[] paths = pathList.getPaths();
    table.setTagPaths(eType,paths);

    // Done
  }
}
