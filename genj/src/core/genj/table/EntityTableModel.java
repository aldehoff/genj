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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.swing.ImageIcon;
import genj.util.swing.SortableTableHeader;
import genj.view.Context;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

/**
 * Our model of entites shown in a table
 */
/*package*/ class EntityTableModel extends AbstractTableModel implements GedcomListener, SortableTableHeader.SortableTableModel {
  
  /** the gedcom we're looking at */
  private Gedcom gedcom;
  
  /** the modes we know about */  
  private Map modes = new HashMap();
    {
      modes.put(Gedcom.INDI, new Mode(Gedcom.INDI, new String[]{"INDI","INDI:NAME","INDI:SEX","INDI:BIRT:DATE","INDI:BIRT:PLAC","INDI:FAMS", "INDI:FAMC", "INDI:OBJE:FILE"}));
      modes.put(Gedcom.FAM , new Mode(Gedcom.FAM , new String[]{"FAM" ,"FAM:MARR:DATE","FAM:MARR:PLAC", "FAM:HUSB", "FAM:WIFE", "FAM:CHIL" }));
      modes.put(Gedcom.OBJE, new Mode(Gedcom.OBJE, new String[]{"OBJE","OBJE:TITL"}));
      modes.put(Gedcom.NOTE, new Mode(Gedcom.NOTE, new String[]{"NOTE","NOTE:NOTE"}));
      modes.put(Gedcom.SOUR, new Mode(Gedcom.SOUR, new String[]{"SOUR","SOUR:TITL", "SOUR:TEXT"}));
      modes.put(Gedcom.SUBM, new Mode(Gedcom.SUBM, new String[]{"SUBM","SUBM:NAME" }));
      modes.put(Gedcom.REPO, new Mode(Gedcom.REPO, new String[]{"REPO","REPO:NAME", "REPO:NOTE"}));
    };
  
  /** the current mode */
  private Mode mode;
  
  /** the rows */
  private Row[] rows;
  
  /**
   * Set the current gedcom
   */
  public void setGedcom(Gedcom set) {
    // old?
    if (gedcom!=null) {
      gedcom.removeGedcomListener(this);
      gedcom = null;
    }
    // init empty rows of mode INDI
    if (mode==null)
      mode = getMode(Gedcom.INDI);
    rows = new Row[0];
    // new?
    if (set!=null) {
      gedcom = set;
      gedcom.addGedcomListener(this);
      setMode(mode.tag);
    }
    // done
  }

  /**
   * Returns current mode
   */
  /*package*/ Mode getMode() {
    return mode;
  }
  
  /**
   * Returns a mode for given tag
   */
  /*package*/ Mode getMode(String tag) {
    // known mode?
    Mode mode = (Mode)modes.get(tag); 
    if (mode==null) {
      mode = new Mode(tag, new String[0]);
      modes.put(tag, mode);
    }
    return mode;
  }
  
  /**
   * Sets the entity type we're looking at
   */
  /*package*/ void setMode(String tag) {
    // look it up
    Mode set = getMode(tag);
    // already?
//    if (mode==set)
//      return;
    mode = set;
    // build rows
    prepareRows();
    // and sort
    sortRows();
    // propagate
    fireTableStructureChanged();
  }

  /**
   * Returns the entity type we're looking at
   */
  /*package*/ String getType() {
    return mode.tag;
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
   * Returns the context at given row/col
   */
  /*package*/ Context getContext(int row, int col) {
    Row theRow = rows[row];
    return new Context(
      gedcom,
      theRow.getEntity(),
      theRow.getProperty(col)
    );
  }
  
  /**
   * Returns the row,col for given entity,prop
   */
  /*package*/ Point getRowCol(Entity e, Property p) {
    
    int row=-1,col=-1;
    
    // find row
    for (int r=0; r<rows.length; r++) {
      if (rows[r].getEntity()==e) {
        row = r;
        
        // find col
        if (p!=null) {
          Property[] cols = rows[r].getColumns();
          for (int c=0; c<cols.length; c++) {
            if (cols[c]==p) {
              col = c;
              break;
            }
          }
        }
        
        // stop lookin
        break;
      }
    }
    
    return new Point(col, row);
  }
  
  /**
   * Prepares the data grid
   */
  private void prepareRows() {
    // anything to do?
    if (gedcom==null) {
      rows = new Row[0];
      return;
    }
    // grab entities
    Collection es = gedcom.getEntities(mode.tag);
    // build rows
    rows = new Row[es.size()];
    Iterator it=es.iterator();
    for (int r=0;it.hasNext();r++) {
      rows[r] = new Row((Entity)it.next(), mode.paths);
    }
    // done
  }

  /**
   * Sorts the rows
   */
  private void sortRows() {
    Arrays.sort(rows, mode);
  }
  
  /**
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  public String getColumnName(int col) {
    return Gedcom.getName(mode.paths[col].getLast());
  }
  
  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return mode.paths.length;
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
    return mode.sortColumn;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#isAscending()
   */
  public boolean isAscending() {
    return mode.sortOrder==1;
  }

  /**
   * @see genj.util.swing.SortableTableHeader.SortableTableModel#setSortedColumn(int)
   */
  public void setSortedColumn(int col, boolean ascending) {
    // propagate
    mode.setSort(new Point(col, ascending?1:-1));
  }

  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Transaction tx) {
    // a drastic tx (entities added/deleted) ? 
    if (!(tx.get(Transaction.ENTITIES_ADDED).isEmpty()&&tx.get(Transaction.ENTITIES_DELETED).isEmpty())) {
      // rebuild!
      prepareRows();
      sortRows();
      fireTableDataChanged();
      // done
      return;
    }
    // any rows to update?
    Set emods = tx.get(Transaction.ENTITIES_MODIFIED);
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
   * A mode is a configuration for a set of entities
   */
  /*package*/ class Mode implements Comparator {
    
    // FIXME widths shouldn't be part of the mode but handled in tableview as part of 'view'
    
    /** attributes */
    private ImageIcon image;
    private String tag;
    private String[] defaults;
    private TagPath[] paths;
    private int[] widths;
    private int sortColumn, sortOrder;
    /** constructor */
    private Mode(String t, String[] d) {
      // remember
      tag      = t;
      defaults = d;
      paths    = TagPath.toArray(defaults);
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
    
    /**
     * Get paths 
     */
    /*package*/ TagPath[] getPaths() {
      return paths;
    }
  
    /**
     * Sets paths 
     */
    /*package*/ void setPaths(TagPath[] set) {
      paths = set;
      if (mode==this) {
        prepareRows();
        sortColumn = -1;
        fireTableStructureChanged();
      }
    }
    
    /**
     * Sets withs
     */
    /*package*/ void setWidths(int[] set) {
      widths = set;
    }
    
    /**
     * Gets withs 
     */
    /*package*/ int[] getWidths() {
      return widths;
    }
    /**
     * Set sort
     */
    /*package*/ void setSort(Point columnAndDir) {
      sortColumn = columnAndDir.x;
      sortOrder = columnAndDir.y;
      if (mode==this) {
        sortRows();
        fireTableDataChanged();
      }
    }
    /** 
     * Get sort
     */
    /*package*/ Point getSort() {
      return new Point(sortColumn, sortOrder);
    }
  } //Mode
  
} //TableModel
