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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.SortableTableHeader;
import genj.view.ToolBarSupport;
import genj.gedcom.*;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends JPanel implements ToolBarSupport {
  
  /** a static set of resources */
  /*package*/ final static Resources resources = new Resources("genj.table");
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the registry we keep */
  private Registry registry;
  
  /** the table we're using */
  private JTable table;
  
  /** the table model we're using */
  private EntityTableModel tableModel;
  
  /**
   * Constructor
   */
  public TableView(Gedcom gedcom, Registry registry, Frame frame) {
    
    // keep some stuff
    this.gedcom = gedcom;
    this.registry = registry;
    
    // create the underlying model
    tableModel = new EntityTableModel(gedcom);

    // read properties
    loadProperties();
    
    // create our table
    table = new JTable(tableModel, tableModel.createTableColumnModel(640));
    table.setTableHeader(new SortableTableHeader());
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);
    table.getTableHeader().setReorderingAllowed(false);
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    // listen to selections
    table.getSelectionModel().addListSelectionListener((ListSelectionListener)new ActionSelect().as(ListSelectionListener.class));
    
    gedcom.addListener(new GedcomListener() {
      /** @see genj.gedcom.GedcomListener#handleChange(Change) */
      public void handleChange(Change change) {
      }
      /** @see genj.gedcom.GedcomListener#handleClose(Gedcom) */
      public void handleClose(Gedcom which) {
      }
      /** @see genj.gedcom.GedcomListener#handleSelection(Selection) */
      public void handleSelection(Selection selection) {
        Entity entity = selection.getEntity();
        // a type that we're interested in?
        if (entity.getType()!=tableModel.getType()) return;
        // change selection
        EntityList es = TableView.this.gedcom.getEntities(entity.getType());
        for (int e=0; e<es.getSize(); e++) {
          if (es.get(e)==entity) {
            table.getSelectionModel().setSelectionInterval(e,e);
            if (table.getSelectionModel().getMinSelectionIndex()!=e)
              table.scrollRectToVisible(table.getCellRect(e,0,true));
            return;
          }
        }
        // done
      }
    });

    // done
  }
  
  /**
   * Accessor - the gedcom we're focussed on
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - the paths we're using for given type
   */
  public TagPath[] getPaths(int type) {
    return tableModel.getPaths(type);
  }
  
  /**
   * Accessor - the paths we're using for given type
   */
  public void setPaths(int type, TagPath[] set) {
    tableModel.setPaths(type,set);
    table.setColumnModel(tableModel.createTableColumnModel(getWidth()));
  }
  
  /**
   * Returns the type of entities to look at
   */
  public int getType() {
    return tableModel.getType();
  }

  /**
   * Sets the type of entities to look at
   */
  public void setType(int type) {
    // grab the column widths as they are right now
    grabColumnWidths();
    // set the new type
    tableModel.setType(type);
    table.setColumnModel(tableModel.createTableColumnModel(getWidth()));
    // done
  }
  
  /**
   * Grab current column widths
   */
  private void grabColumnWidths() {
    // grab column widths
    TableColumnModel columns = table.getColumnModel();
    int[] widths = new int[columns.getColumnCount()];
    for (int c=0; c<columns.getColumnCount(); c++) {
      widths[c] = columns.getColumn(c).getWidth();
    }
    tableModel.setWidths(tableModel.getType(),widths);
    // done
  }

  /**
   * Read properties from registry
   */
  private void loadProperties() {

    // get current filter
    tableModel.setType(registry.get("type", Gedcom.INDIVIDUALS));
    
    // get paths&widths
    for (int t=0; t<Gedcom.LAST_ETYPE; t++) {
      String tag = Gedcom.getTagFor(t);
      String[] ps = registry.get(tag+".paths" , (String[])null);
      if (ps!=null) tableModel.setPaths(t, ps);
      int[]    ws = registry.get(tag+".widths", (int[]   )null);
      if (ws!=null) tableModel.setWidths(t,ws);
    }
    // Done
  }
  
  /**
   * Write properties from registry
   */
  private void saveProperties() {
    // grab the column widths as they are right now
    grabColumnWidths();
    // save current type
    registry.put("type",tableModel.getType());
    // save paths&widths
    for (int t=0; t<Gedcom.LAST_ETYPE; t++) {
      String tag = Gedcom.getTagFor(t);
      registry.put(tag+".paths", tableModel.getPaths(t));
      registry.put(tag+".widths", tableModel.getWidths(t));
    }
    // Done
  }  
  
  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    // create buttons for mode switch
    ButtonHelper bh = new ButtonHelper();
    for (int t=0;t<Gedcom.LAST_ETYPE;t++) {
      bar.add(bh.create(new ActionChangeType(t)));
    }
    // done
  }

  /**
   * Notification when table isn't used anymore
   */
  public void removeNotify() {
    saveProperties();
    super.removeNotify();
  }

  /**
   * Action - flip view to entity type
   */
  private class ActionChangeType extends ActionDelegate {
    /** the type this action triggers */
    private int type;
    /** constructor */
    ActionChangeType(int t) {
      type = t;
      setTip(resources.getString("mode.tip", Gedcom.getNameFor(type,true)));
      setImage(Property.getDefaultImage(Gedcom.getTagFor(type)));
    }
    /** run */
    public void execute() {
      setType(type);
    }
  } //ActionMode

  /**
   * Action - selection occured
   */
  private class ActionSelect extends ActionDelegate {
    /** run */
    public void execute() {
      int i = table.getSelectedRow();
      if (i<0) return;
      Entity e = tableModel.getEntity(i);
      gedcom.fireEntitySelected(null, e, false);
    }
  } //ActionMode
  
  
} //TableView
