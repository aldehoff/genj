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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
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
import genj.view.ToolBarSupport;
import genj.gedcom.*;

/**
 * Component for showing entities of a gedcom file in a tabular way
 */
public class TableView extends JPanel implements ToolBarSupport {
  
  /** a static set of resources */
  /*package*/ final static Resources resources = new Resources("genj.table");
  
  /** the gedcom we're looking at */
  /*package*/ Gedcom gedcom;
  
  /** the registry we keep */
  private Registry registry;
  
  /** the modes we know about */  
  private Filter[] filters;
  
  /** the table we're using */
  private JTable table;
  
  /** the table model we're using */
  private Model tableModel;
  
  /**
   * Constructor
   */
  public TableView(Gedcom gedcom, Registry registry, Frame frame) {
    
    // keep some stuff
    this.gedcom = gedcom;
    this.registry = registry;
    
    // init our modes
    filters = new Filter[]{
      new Filter(Gedcom.INDIVIDUALS , new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}),
      new Filter(Gedcom.FAMILIES    , new String[]{"FAM","FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }),
      new Filter(Gedcom.MULTIMEDIAS , new String[]{"OBJE","OBJE:FILE"}),
      new Filter(Gedcom.NOTES       , new String[]{"NOTE"}),
      new Filter(Gedcom.SOURCES     , new String[]{"SOUR"}),
      new Filter(Gedcom.SUBMITTERS  , new String[]{"SUBM"}),
      new Filter(Gedcom.REPOSITORIES, new String[]{"REPO"})
    };
    
    // read properties
    Filter f = loadProperties();
    
    // create our table
    tableModel = new Model(f);
    table = new JTable(tableModel, createTableColumnModel(f));
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateColumnsFromModel(false);
    table.getTableHeader().setReorderingAllowed(false);
    setLayout(new BorderLayout());
    add(new JScrollPane(table), BorderLayout.CENTER);
    
    // done
  }
  
  /**
   * Accessor - the paths we're using for given type
   */
  public TagPath[] getPaths(int type) {
    return filters[type].paths;
  }
  
  /**
   * Accessor - the paths we're using for given type
   */
  public void setPaths(int type, TagPath[] set) {
    filters[type].setPaths(set,null);
    tableModel.setFilter(tableModel.getFilter());
    table.setColumnModel(createTableColumnModel(filters[type]));
  }
  
  /**
   * Sets the type of entities to look at
   */
  public void setType(int type) {
    // grab current filter
    Filter filter = tableModel.getFilter();
    // grab column widths
    TableColumnModel columns = table.getColumnModel();
    int[] widths = new int[columns.getColumnCount()];
    for (int c=0; c<columns.getColumnCount(); c++) {
      widths[c] = columns.getColumn(c).getWidth();
    }
    filter.widths = widths;
    // set the new filter and tell it to the model
    tableModel.setFilter(filters[type]);
    table.setColumnModel(createTableColumnModel(filters[type]));
    // done
  }

  /**
   * Read properties from registry
   */
  private Filter loadProperties() {
    // get current filter
    Filter current = filters[registry.get("filter", Gedcom.INDIVIDUALS)];
    // get paths&widths
    for (int f=0; f<filters.length; f++) {
      Filter filter = filters[f];
      String tag = Gedcom.getTagFor(filter.type);
      String[] ps = registry.get(tag+".paths" , (String[])null);
      int[]    ws = registry.get(tag+".widths", (int[]   )null);
      if (ps!=null&&ws!=null) filter.setPaths(ps,ws);
    }

    // Done
    return current;
  }
  
  /**
   * Write properties from registry
   */
  private void saveProperties() {
    
    // (re)set filter (which commits column widths)
    setType(tableModel.getFilter().type);

    // save current filter
    registry.put("filter",tableModel.getFilter().type);
    
    // save paths&widths
    for (int f=0; f<filters.length; f++) {
      Filter filter = filters[f];
      String tag = Gedcom.getTagFor(filter.type);
      registry.put(tag+".paths", filter.paths);
      registry.put(tag+".widths", filter.widths);
    }

    // Done
  }  
  
  /**
   * Helper that creates a new ColumnModel
   */
  private TableColumnModel createTableColumnModel(Filter filter) {
    // create and fill
    TableColumnModel columns = new DefaultTableColumnModel();
    for (int c=0; c<filter.paths.length; c++) {
      TableColumn col = new TableColumn(c);
      col.setHeaderValue(filter.paths[c]);
      col.setPreferredWidth(filter.widths[c]);
      columns.addColumn(col);
    }
    // done
    return columns;
  }

  /**
   * Sets the type of entities to look at
   */
  public int getType() {
    return tableModel.getFilter().type;
  }

  /**
   * @see genj.view.ToolBarSupport#populate(JToolBar)
   */
  public void populate(JToolBar bar) {
    // create buttons for mode switch
    ButtonHelper bh = new ButtonHelper();
    for (int m=0;m<filters.length;m++) {
      bar.add(bh.create(new ActionChangeFilter(filters[m])));
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
   *
   */
  private class Filter {
    /** attributes */
    ImgIcon image;
    int type;
    String[] defaults;
    TagPath[] paths;
    int[] widths;
    /** constructor */
    Filter(int t, String[] d) {
      // remember
      type     = t;
      defaults = d;
      // init
      setPaths(defaults, null);
    }
    /** set paths to use */
    void setPaths(Object[] ps, int[] ws) {
      // build paths
      paths = new TagPath[ps.length];
      widths= new int[ps.length];
      for (int p=0;p<ps.length;p++) {
        paths [p] = new TagPath(ps[p].toString());
        widths[p] = ws != null ? ws[p] : 640/ps.length;
      }
      // done
    }
  } //Mode

  /**
   * Action - flip view to entity type
   */
  private class ActionChangeFilter extends ActionDelegate {
    /** the mode this action triggers */
    private Filter filter;
    /** constructor */
    ActionChangeFilter(Filter f) {
      filter=f;
      setTip(resources.getString("mode.tip", Gedcom.getNameFor(f.type,true)));
      setImage(Property.getDefaultImage(Gedcom.getTagFor(f.type)));
    }
    /** run */
    public void execute() {
      setType(filter.type);
    }
  } //ActionMode

  /**
   * Our model
   */
  private class Model extends AbstractTableModel implements GedcomListener {
    
    /** the current mode */
    private Filter filter;
    
    /**
     * Constructor
     */
    Model(Filter start) {
      filter = start;
    }
    
    /**
     * Sets the mode we're in
     */
    void setFilter(Filter set) {
      // remember
      filter = set;
      // propagate
      fireTableStructureChanged();
    }
  
    /**
     * Returns the mode we're in
     */
    Filter getFilter() {
      return filter;
    }
    
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return filter.paths.length;
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return gedcom.getEntities(filter.type).getSize();
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
      Entity e = gedcom.getEntities(filter.type).get(rowIndex);
      filter.paths[columnIndex].setToFirst();
      return e.getProperty().getProperty(filter.paths[columnIndex], false);
    }

    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {
      //if (change.isChanged(change.EADD)||change.isChanged(change.EDEL))
      fireTableDataChanged();
    }

    /**
     * @see genj.gedcom.GedcomListener#handleClose(Gedcom)
     */
    public void handleClose(Gedcom which) {
      // ignored
    }

    /**
     * @see genj.gedcom.GedcomListener#handleSelection(Selection)
     */
    public void handleSelection(Selection selection) {
      Entity e = selection.getEntity();
      // a type that we're interested in?
      if (e.getType()!=filter.type) return;
      // change selection
    }
    
  } //Model
  
} //TableView
