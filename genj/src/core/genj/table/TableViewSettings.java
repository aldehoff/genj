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

import genj.gedcom.Gedcom;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.ActionDelegate;
import genj.util.GridBagHelper;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.view.Settings;
import genj.view.ViewManager;
import genj.view.widgets.PathListWidget;
import genj.view.widgets.PathTreeWidget;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class for providing ViewInfo information to a ViewEditor
 */
public class TableViewSettings extends JPanel implements Settings {

  /** components */
  private JComboBox       cTypes;
  private PathTreeWidget     pathTree;
  private PathListWidget     pathList;
  private TableView       table;
  private Resources       resources = Resources.get(this);

  /**
   * @see genj.view.Settings#init(genj.view.ViewManager)
   */
  public void init(ViewManager manager) {

    // Create!
    GridBagHelper gh = new GridBagHelper(this);

    // Chooseable type
    cTypes = new JComboBox();

    for (int i=0;i<Gedcom.ENTITIES.length;i++) {
      cTypes.addItem(Gedcom.getEntityName(Gedcom.ENTITIES[i],true));
    }
    cTypes.addActionListener(new ActionChooseEntity());

    // Tree of TagPaths
    pathTree = new PathTreeWidget();

    PathTreeWidget.Listener plistener = new PathTreeWidget.Listener() {
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
    pathTree.addListener(plistener);

    // List of TagPaths
    pathList = new PathListWidget();

    // Up/Down of ordering
    ButtonHelper bh = new ButtonHelper().setResources(resources).setInsets(0);
    AbstractButton bUp   = bh.create(new ActionUpDown(true));
    AbstractButton bDown = bh.create(new ActionUpDown(false));
    
    // Layout
    gh.add(new JLabel(resources.getString("info.entities"))  ,0,1,1,1);
    gh.add(cTypes                  ,1,1,2,1,gh.GROWFILL_HORIZONTAL);

    gh.add(new JLabel(resources.getString("info.columns"))   ,0,2,1,1);
    gh.add(pathTree                ,1,2,2,2,gh.GROWFILL_BOTH);

    gh.add(new JLabel(resources.getString("info.order"))  ,0,4,1,1);
    gh.add(bUp                                            ,0,5,1,1,gh.FILL_HORIZONTAL);
    gh.add(bDown                                          ,0,6,1,1,gh.FILL_HORIZONTAL);
    gh.add(pathList                                       ,1,4,2,4,gh.GROWFILL_BOTH);

    // Done
  }
  
  /**
   * @see genj.view.Settings#setView(javax.swing.JComponent, genj.view.ViewManager)
   */
  public void setView(JComponent view) {
    // remember
    table = (TableView)view;
    // done
  }


  /**
   * Tells the ViewInfo to apply made changes
   */
  public void apply() {
    // Write columns by TagPaths
    String tag = Gedcom.ENTITIES[cTypes.getSelectedIndex()];
    TagPath[] paths = pathList.getPaths();
    table.setPaths(tag, paths);
    // Done
  }

  /**
   * Tells the ViewInfo to reset made changes
   */
  public void reset() {

    // Reflect columns by TagPaths
    String tag = Gedcom.ENTITIES[cTypes.getSelectedIndex()];
    
    TagPath[] selectedPaths = table.getPaths(tag);
    TagPath[] usedPaths     = MetaProperty.getPaths(tag, Property.class);

    pathTree.setPaths(usedPaths, selectedPaths);
    pathList.setPaths(selectedPaths);

    // Done
  }
  
  /**
   * @see genj.view.Settings#getEditor()
   */
  public JComponent getEditor() {
    return this;
  }

  /**
   * Action - ActionChooseEntity
   */
  private class ActionChooseEntity extends ActionDelegate {
    /** constructor */
    /** run */
    public void execute() {
      if (table==null) return;
      table.setType(Gedcom.ENTITIES[cTypes.getSelectedIndex()]);
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
