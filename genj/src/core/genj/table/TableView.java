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
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import genj.renderer.PropertyProxy;
import genj.util.ActionDelegate;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ButtonHelper;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.SortableTableHeader;
import genj.view.CurrentSupport;
import genj.view.EntityPopupSupport;
import genj.view.ToolBarSupport;
import genj.view.ViewManager;
import genj.gedcom.*;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends JPanel implements ToolBarSupport, CurrentSupport, EntityPopupSupport {
  
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
  
  /** the gedcom listener we're using */
  private GedcomListener listener;
  
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
    table.setDefaultRenderer(Object.class, new PropertyTableCellRenderer());
    table.getSelectionModel().addListSelectionListener((ListSelectionListener)new ActionRowSelected().as(ListSelectionListener.class));
    
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    // start listening to Gedcom
    gedcom.addListener(new GedcomListener() {
      /** @see genj.gedcom.GedcomListener#handleChange(Change) */
      public void handleChange(Change change) {
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
   * @see genj.view.CurrentSupport#setCurrentEntity(Entity)
   */
  public void setCurrentEntity(Entity entity) {
    // a type that we're interested in?
    if (entity.getType()!=tableModel.getType()) return;
    // already selected?
    int row = table.getSelectionModel().getLeadSelectionIndex();
    if (row>=0 && tableModel.getEntity(row)==entity) return;
    // change selection
    row = tableModel.getRow(entity);
    table.scrollRectToVisible(table.getCellRect(row,0,true));
    table.getSelectionModel().setSelectionInterval(row,row);
    // done
  }

  /**
   * @see genj.view.CurrentSupport#setCurrentProperty(Property)
   */
  public void setCurrentProperty(Property property) {
    // ignored
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    // create buttons for mode switch
    ButtonHelper bh = new ButtonHelper();
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      bar.add(bh.create(new ActionChangeType(t)));
    }
    // done
  }
  
  /**
   * @see genj.view.EntityPopupSupport#getEntityPopupContainer()
   */
  public JComponent getEntityPopupContainer() {
    return table;
  }

  /**
   * @see genj.view.EntityPopupSupport#getEntityAt(Point)
   */
  public Entity getEntityAt(Point pos) {
    int row = table.rowAtPoint(pos);
    if (row<0) return null;
    table.getSelectionModel().setSelectionInterval(row, row);
    return tableModel.getEntity(row);
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
    for (int t=0; t<Gedcom.NUM_TYPES; t++) {
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
    for (int t=0; t<Gedcom.NUM_TYPES; t++) {
      String tag = Gedcom.getTagFor(t);
      registry.put(tag+".paths", tableModel.getPaths(t));
      registry.put(tag+".widths", tableModel.getWidths(t));
    }
    // Done
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
  private class ActionRowSelected extends ActionDelegate {
    /** run */
    public void execute() {
      int i = table.getSelectedRow();
      if (i<0) return;
      Entity e = tableModel.getEntity(i);
      ViewManager.getInstance().setCurrentEntity(e);
    }
  } //ActionMode
  
  /**
   * Renderer for properties in cells
   */
  private class PropertyTableCellRenderer extends HeadlessLabel implements TableCellRenderer {
    /** current property */
    private Property prop;
    /** attributes */
    private boolean isSelected;
    /**
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
      // there's a property here
      prop = (Property)value;
      // and some status
      isSelected = selected;
      // ready
      return this;
    }
    /**
     * @see javax.swing.JComponent#paintComponent(Graphics)
     */
    protected void paintComponent(Graphics g) {
      // our bounds
      Rectangle bounds = getBounds();
      bounds.x=0; bounds.y=0;
      // background?
      if (isSelected) {
        g.setColor(table.getSelectionBackground());
        g.fillRect(0,0,bounds.width,bounds.height);
        g.setColor(table.getSelectionForeground());
      } else {
        g.setColor(table.getForeground());
      }
      g.setFont(table.getFont());
      // no prop and we're done
      if (prop==null) return;
      // get the proxy
      PropertyProxy proxy = PropertyProxy.get(prop.getProxy());
      // let it render
      proxy.render(g, bounds, prop, proxy.PREFER_DEFAULT);
      // done
    }
  } //PropertyTableCellRenderer
    
  /**
   * @see java.awt.Component#removeNotify()
   */
  public void removeNotify() {
    // save state
    saveProperties();
    // delegate
    super.removeNotify();
    // destruct model
    tableModel.destructor();
  }

} //TableView
