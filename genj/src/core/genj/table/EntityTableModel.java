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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
  private Map filters = new HashMap();
    {
      filters.put(Gedcom.INDI, new Filter(Gedcom.INDI, new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}));
      filters.put(Gedcom.FAM , new Filter(Gedcom.FAM , new String[]{"FAM" ,"FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }));
      filters.put(Gedcom.OBJE, new Filter(Gedcom.OBJE, new String[]{"OBJE","OBJE:TITL"}));
      filters.put(Gedcom.NOTE, new Filter(Gedcom.NOTE, new String[]{"NOTE","NOTE:NOTE"}));
      filters.put(Gedcom.SOUR, new Filter(Gedcom.SOUR, new String[]{"SOUR","SOUR:TITL", "SOUR:TEXT"}));
      filters.put(Gedcom.SUBM, new Filter(Gedcom.SUBM, new String[]{"SUBM","SUBM:NAME" }));
      filters.put(Gedcom.REPO, new Filter(Gedcom.REPO, new String[]{"REPO","REPO:NAME", "REPO:NOTE"}));
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
    setType(Gedcom.INDI);
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
  /*package*/ TagPath[] getPaths(String tag) {
    Filter f = (Filter)filters.get(tag);
    return f!=null ? f.paths : new TagPath[0];
  }
  
  /**
   * Sets paths for given type
   */
  /*package*/ void setPaths(String tag, String[] paths) {
    setPaths(tag, calcPaths(paths));
  }
  
  /**
   * Sets paths for given type
   */
  /*package*/ void setPaths(String tag, TagPath[] paths) {
    Filter f = (Filter)filters.get(tag);
    if (f!=null) f.paths = paths;
    prepareRows();
    fireTableStructureChanged();
  }
  
  /**
   * Sets withs for given type
   */
  /*package*/ void setWidths(String tag, int[] widths) {
    Filter f = (Filter)filters.get(tag);
    if (f!=null) f.widths = widths;
  }
  
  /**
   * Gets withs for given type
   */
  /*package*/ int[] getWidths(String tag) {
    Filter f = (Filter)filters.get(tag);
    return f!=null ? f.widths : new int[0];
  }
  
  /**
   * Sets the entity type we're looking at
   */
  /*package*/ void setType(String tag) {
    // known filter?
    Filter set = (Filter)filters.get(tag); 
    if (set==null)
      return;
    // already?
    if (set==filter) 
      return;
    // remember
    filter = set;
    // build data
    prepareRows();
    // propagate
    fireTableStructureChanged();
  }

  /**
   * Returns the entity type we're looking at
   */
  /*package*/ String getType() {
    return filter.tag;
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
    Collection es = gedcom.getEntities(filter.tag);
    // build rows
    rows = new Row[es.size()];
    Iterator it=es.iterator();
    for (int r=0;it.hasNext();r++) {
      rows[r] = new Row((Entity)it.next(), filter.paths);
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
    TagPath[] paths = getPaths(filter.tag);
    int[] widths = getWidths(filter.tag);
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
    String tag;
    String[] defaults;
    TagPath[] paths;
    int[] widths;
    int sortColumn, sortOrder;
    /** constructor */
    Filter(String t, String[] d) {
      // remember
      tag      = t;
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
