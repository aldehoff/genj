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

import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.swing.ButtonHelper;
import genj.gedcom.*;
import genj.option.*;
import genj.app.*;
import genj.util.Resources;

/**
 * Class for providing ViewInfo information to a ViewEditor
 */
public class TableViewSettings extends ViewSettingsWidget {

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
  public TableViewSettings(TableView table) {

    // remember
    this.table = table;
    this.eType = table.getType();

    // Create!
    GridBagHelper gh = new GridBagHelper(this);

    // Chooseable type
    cTypes = new JComboBox();

    for (int i=Gedcom.FIRST_ETYPE;i<=Gedcom.LAST_ETYPE;i++) {
      cTypes.addItem(Gedcom.getNameFor(i,true));
    }
    cTypes.addActionListener((ActionListener)new ActionChooseEntity().as(ActionListener.class));

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
    ButtonHelper bh = new ButtonHelper().setResources(TableView.resources).setInsets(0);
    JButton bUp   = bh.create(new ActionUpDown(true));
    JButton bDown = bh.create(new ActionUpDown(false));
    
    // Layout
    gh.add(new JLabel(TableView.resources.getString("info.entities"))  ,0,1,1,1);
    gh.add(cTypes                  ,1,1,2,1);

    gh.add(new JLabel(TableView.resources.getString("info.columns"))  ,0,2,1,1);
    gh.add(pathTree                ,1,2,2,1,gh.GROW_BOTH|gh.FILL_BOTH);

    gh.add(new JLabel(TableView.resources.getString("info.order"))  ,0,3,1,1);
    gh.add(bUp                     ,0,4,1,1);
    gh.add(bDown                   ,0,5,1,1);
    gh.add(pathList                ,1,3,2,4,gh.GROW_BOTH|gh.FILL_BOTH);

    // init data
    reset();

    // Done
  }

  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    // Write columns by TagPaths
    TagPath[] paths = pathList.getPaths();
    table.setTagPaths(eType,paths);

    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {

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
   * Action - ActionChooseEntity
   */
  private class ActionChooseEntity extends ActionDelegate {
    /** constructor */
    /** run */
    public void execute() {
      if (table==null) return;
      eType = cTypes.getSelectedIndex();
      reset();
    }
  } //ActionChooseEntity
  
  /**
   * Action - ActionUpDown
   */
  private class ActionUpDown extends ActionDelegate {
    /** up or down */
    private boolean up;
    /** constructor */
    protected ActionUpDown(boolean up) {
      this.up=up;
      if (up) setText("info.up");
      else setText("info.down");
    }
    /** run */
    public void execute() {
      if (up)
        pathList.up();
      else 
        pathList.down();
    }
  } //ActionUpDown
  
}
