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

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.swing.ImageIcon;
import genj.util.swing.SortableTableHeader;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Our model of entites shown in a table
 */
/*package*/ class EntityTableModel extends AbstractTableModel implements GedcomListener, SortableTableHeader.SortableTableModel {
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the modes we know about */  
  private Filter[] filters = new Filter[]{
      new Filter(Gedcom.INDIVIDUALS , new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}),
      new Filter(Gedcom.FAMILIES    , new String[]{"FAM" ,"FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }),
      new Filter(Gedcom.MULTIMEDIAS , new String[]{"OBJE","OBJE:TITL"}),
      new Filter(Gedcom.NOTES       , new String[]{"NOTE","NOTE:NOTE"}),
      new Filter(Gedcom.SOURCES     , new String[]{"SOUR","SOUR:TITL", "SOUR:TEXT"}),
      new Filter(Gedcom.SUBMITTERS  , new String[]{"SUBM","SUBM:NAME" }),
      new Filter(Gedcom.REPOSITORIES, new String[]{"REPO","REPO:NAME", "REPO:NOTE"})
    };
  
  /** the current mode */
  private Filter filter;
  
  /** the rows */
  private Row[] rows;
  
  /**
   * Constructor
   */
  /*pacakge*/ EntityTableModel(Gedcom gedcom) {
    // remember
    this.gedcom=gedcom;
    // set starting type
    setType(Gedcom.INDIVIDUALS);
    // start listening
    gedcom.addListener(this);
    // done
  }

  /**
   * Destructor
   */
  /*package*/ void destructor() {
    // stop listening
    gedcom.removeListener(this);
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
    prepareRows();
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
    // build data
    prepareRows();
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
    return rows[row].getEntity();
  }
  
  /**
   * Returns the property at given row/col
   */
  /*package*/ Property getProperty(int row, int col) {
    return rows[row].getColumns()[col];
  }
  
  /**
   * Returns the row for given entity
   */
  /*package*/ int getRow(Entity e) {          
    for (int r=0; r<rows.length; r++) {
      if (rows[r].getEntity()==e) return r;
    }
    return -1;
  }
  
  /**
   * Prepares the data grid
   */
  private void prepareRows() {
    // grab entities
    List es = gedcom.getEntities(filter.type);
    // build rows
    rows = new Row[es.size()];
    for (int r=0; r<rows.length; r++) {
      rows[r] = new Row((Entity)es.get(r), filter.paths);
    }
    // sort
    sortRows();
    // done
  }

  /**
   * Sorts the rows
   */
  private void sortRows() {
    Arrays.sort(rows, filter);
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
    return rows.length;
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    return rows[rowIndex].getColumns()[columnIndex];
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#getSortedColumn()
   */
  public int getSortedColumn() {
    return filter.sortColumn;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#isAscending()
   */
  public boolean isAscending() {
    return filter.sortOrder==1;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#setSortedColumn(int)
   */
  public void setSortedColumn(int col, boolean ascending) {
    // remember
    filter.sortColumn = col;
    filter.sortOrder  = ascending?1:-1;
    // sort
    sortRows();
    // notify
    fireTableDataChanged();
  }

  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {
    // a drastic change (entities added/deleted) ? 
    if (!(change.getChanges(Change.EADD).isEmpty()&&change.getChanges(Change.EDEL).isEmpty())) {
      // rebuild!
      prepareRows();
      fireTableDataChanged();
      // done
      return;
    }
    // any rows to update?
    Set emods = change.getChanges(change.EMOD);
    int 
     first = -1,
     last  = -1; 
    for (int r=0;r<rows.length;r++) {
      Row row = rows[r];
      if (emods.contains(row.getEntity())) {
        row.invalidate();
        if (first<0) first = r;
        last = r; 
      }
        
    }
    if (first>=0)
      fireTableRowsUpdated(first, last);
    // done 
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
      col.setHeaderValue(paths[c]);
      col.setPreferredWidth(widths.length>c&&widths[c]>0?widths[c]:total/filter.paths.length);
      columns.addColumn(col);
    }
    // done
    return columns;
  }

  /**
   * A Filter filters the entities we have in the model
   */
  private class Filter implements Comparator {
    /** attributes */
    ImageIcon image;
    int type;
    String[] defaults;
    TagPath[] paths;
    int[] widths;
    int sortColumn, sortOrder;
    /** constructor */
    Filter(int t, String[] d) {
      // remember
      type     = t;
      defaults = d;
      paths    = calcPaths(defaults);
      widths   = new int[paths.length];
    }
    /**
     * @see java.util.Comparator#compare(Object, Object)
     */
    public int compare(Object o1, Object o2) {
      // only if column is fine
      if (sortColumn<0||sortColumn>=paths.length) 
        return 0;
      // here's the rows
      Property[] 
        row1 = ((Row)o1).getColumns(),
        row2 = ((Row)o2).getColumns();
      // null?
      if (row1[sortColumn]==row2[sortColumn]) 
        return  0;
      if (row1[sortColumn]==null) 
        return -1 * sortOrder;
      if (row2[sortColumn]==null) 
        return  1 * sortOrder;
      // let property decide
      return row1[sortColumn].compareTo(row2[sortColumn]) * sortOrder;
    }
  } //Filter
  
} //TableModel
