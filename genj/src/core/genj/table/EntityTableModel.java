package genj.table;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.EntityList;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.Selection;
import genj.gedcom.TagPath;
import genj.util.ImgIcon;
import genj.util.swing.SortableTableHeader;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Our model of entites shown in a table
 */
/*package*/ class EntityTableModel extends AbstractTableModel implements GedcomListener, SortableTableHeader.SortableTableModel {
  
  /** the sorted column */
  private int sortedColumn = -1;
  
  /** whether sorting is ascending/descending */
  private boolean sortedAscending = true;
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the modes we know about */  
  private Filter[] filters = new Filter[]{
      new Filter(Gedcom.INDIVIDUALS , new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}),
      new Filter(Gedcom.FAMILIES    , new String[]{"FAM","FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }),
      new Filter(Gedcom.MULTIMEDIAS , new String[]{"OBJE","OBJE:FILE"}),
      new Filter(Gedcom.NOTES       , new String[]{"NOTE"}),
      new Filter(Gedcom.SOURCES     , new String[]{"SOUR"}),
      new Filter(Gedcom.SUBMITTERS  , new String[]{"SUBM"}),
      new Filter(Gedcom.REPOSITORIES, new String[]{"REPO"})
    };
  
  /** the current mode */
  private Filter filter;
  
  /** the list of entities we're looking at right now */
  private Entity[] entities;
  
  /**
   * Constructor
   */
  /*pacakge*/ EntityTableModel(Gedcom gedcom) {
    // remember
    this.gedcom=gedcom;
    // listen
    gedcom.addListener(this);
    // set starting type
    setType(Gedcom.INDIVIDUALS);
    // done
  }

  /**
   * Get paths for given type
   */
  /*package*/ TagPath[] getPaths(int type) {
    return filters[type].paths;
  }
  
  /**
   * Sets paths for given type
   */
  /*package*/ void setPaths(int type, String[] paths) {
    setPaths(type, calcPaths(paths));
  }
  
  /**
   * Sets paths for given type
   */
  /*package*/ void setPaths(int type, TagPath[] paths) {
    filters[type].paths = paths;
    fireTableStructureChanged();
  }
  
  /**
   * Sets withs for given type
   */
  /*package*/ void setWidths(int type, int[] widths) {
    filters[type].widths = widths;
  }
  
  /**
   * Gets withs for given type
   */
  /*package*/ int[] getWidths(int type) {
    return filters[type].widths;
  }
  
  /**
   * Sets the entity type we're looking at
   */
  /*package*/ void setType(int entity) {
    // already?
    if (filters[entity]==filter) return;
    // remember
    filter = filters[entity];
    // grab entities
    entities = gedcom.getEntities(filter.type).toArray();
    // no sorting
    sortedColumn = -1;
    // propagate
    fireTableStructureChanged();
  }

  /**
   * Returns the entity type we're looking at
   */
  /*package*/ int getType() {
    return filter.type;
  }
  
  /**
   * Returns the entity at given row index
   */
  /*package*/ Entity getEntity(int row) {
    return entities[row];
  }
  
  /**
   * Helper to convert Strings to TagPaths
   */
  private TagPath[] calcPaths(String[] paths) {
    TagPath[] result = new TagPath[paths.length];
    for (int i=0; i<result.length; i++) {
      result[i] = new TagPath(paths[i]);
    }
    return result;
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
    return entities.length;
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    Entity e = entities[rowIndex];
    filter.paths[columnIndex].setToFirst();
    return e.getProperty().getProperty(filter.paths[columnIndex], false);
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#getSortedColumn()
   */
  public int getSortedColumn() {
    return sortedColumn;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#isAscending()
   */
  public boolean isAscending() {
    return sortedAscending;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#setSortedColumn(int)
   */
  public void setSortedColumn(int col) {
    sortedColumn = col;
    sortedAscending = !sortedAscending;
    System.out.println(sortedAscending);
    fireTableDataChanged();
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
    // ignored
  }

  /**
   * Helper that creates a new ColumnModel
   */
  /*package*/ TableColumnModel createTableColumnModel(int total) {
    TagPath[] paths = getPaths(filter.type);
    int[] widths = getWidths(filter.type);
    // create and fill
    TableColumnModel columns = new DefaultTableColumnModel();
    for (int c=0; c<filter.paths.length; c++) {
      TableColumn col = new TableColumn(c);
      col.setHeaderValue(paths[c].getLast());
      col.setPreferredWidth(widths.length>c&&widths[c]>0?widths[c]:total/filter.paths.length);
      columns.addColumn(col);
    }
    // done
    return columns;
  }

  
  /**
   * A Filter filters the entities we have in the model
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
      paths    = calcPaths(defaults);
      widths   = new int[paths.length];
    }
  } //Filter
  
  /** 
   * A wrapper for an entity in this model and its cached properties
   */
  private class Entry {
    /** attributes */
    Entity e;
    Property ps[];
    /** constructor */
    Entry(Entity e) {
      this.e = e;
    }
  } //Entry

} //TableModel
