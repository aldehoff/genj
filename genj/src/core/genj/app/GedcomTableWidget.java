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
package genj.app;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.SortableTableHeader;
import genj.view.ViewManager;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A component displaying a list of Gedcoms
 */
/*package*/ class GedcomTableWidget extends JTable {
  
  /** default column widths */
  private static final int defaultWidths[] = {
    96, 24, 24, 24, 24, 24, 24, 24
  };

  /** a registry */
  private Registry registry;
  
  /** a model */
  private Model model;
  
  /**
   * Constructor
   */
  public GedcomTableWidget(ViewManager mgr, Registry reGistry) {
 
    // change the header to ours    
    setTableHeader(new SortableTableHeader());
    
    // Prepare a model
    model = new Model();
    registry = reGistry;
    
    // Prepare a column model
    TableColumnModel cm = new DefaultTableColumnModel();
    for (int h=0; h<Gedcom.ENTITIES.length+1; h++) {
      TableColumn col = new TableColumn(h);
      if (h==0) 
        col.setHeaderValue(Resources.get(this).getString("cc.column_header.name"));
      else
        col.setHeaderValue(Gedcom.getEntityImage(Gedcom.ENTITIES[h-1]));
      
      col.setPreferredWidth(defaultWidths[h]);
      cm.addColumn(col);
    }
    setModel(model);
    setColumnModel(cm);

    // change looks    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getTableHeader().setReorderingAllowed(false);
    
    // grab the preferred columns
    int[] widths = registry.get("columns",new int[0]);
    for (int c=0, max=getColumnModel().getColumnCount(); c<widths.length&&c<max; c++) {
      getColumnModel().getColumn(c).setPreferredWidth(widths[c]);
    }    

    // done
  }
  
  /**
   * Select a gedcom
   */
  public void setSelection(Gedcom gedcom) {
    int row = model.getRowFor(gedcom);
    if (row>=0)
      getSelectionModel().setSelectionInterval(row,row);
  }

  /**
   * Return gedcom at given position 
   */  
  public Gedcom getGedcomAt(Point pos) {
    int row = rowAtPoint(pos);
    return row<0 ? null : model.getGedcom(row);
  }

  /**
   * Hooking into the tear-down process to store our
   * settings in (set) registry
   */
  public void removeNotify() {
    // remember our layout
    int[] widths = new int[getColumnModel().getColumnCount()];
    for (int c=0; c<widths.length; c++) {
      widths[c] = getColumnModel().getColumn(c).getWidth();
    }
    registry.put("columns", widths);
    // continue
    super.removeNotify();
  }
  
  /**
   * Remove gedcom by name
   */
  public boolean removesGedcom(String name) {
    for (int i=0; i<model.getRowCount(); i++) {
      Gedcom gedcom = model.getGedcom(i);
      if (gedcom.getName().equals(name)) {
        model.removeGedcom(gedcom);
        return true;
      }
    }
    return false;
  }
  
  /**
   * Check for gedcom with name
   */
  public Gedcom getGedcom(String name) {
    for (int i=0; i<model.getRowCount(); i++) {
      Gedcom gedcom = model.getGedcom(i);
      if (gedcom.getName().equals(name))
        return gedcom;
    }
    return null;
  }
  
  /**
   * Accessor for model
   */
  public List getAllGedcoms() {
    return model.getAllGedcoms();
  }
  
  /**
   * The selected gedcom
   */
  public Gedcom getSelectedGedcom() {
    return model.getSelectedGedcom();
  }
  
  /**
   * Add a gedcom
   */
  public void addGedcom(Gedcom gedcom) {
    model.addGedcom(gedcom);
    List gs = model.getAllGedcoms();
    getSelectionModel().setSelectionInterval(gs.size()-1,gs.size()-1);
  }

  /**
   * Removes a gedcom
   */
  public void removeGedcom(Gedcom gedcom) {
    model.removeGedcom(gedcom);
  }

  /**
   * A model keeping track of a bunch of Gedcoms
   */
  private class Model extends AbstractTableModel implements GedcomListener {
    
    /** the Gedcoms we know about */
    private List gedcoms = new ArrayList(10);
    
    /**
     * Selected Gedcom
     */
    public Gedcom getSelectedGedcom() {
      int row = getSelectedRow();
      if (row==-1) return null;
      return getGedcom(row);
    }
    
    /**
     * Gedcom by row
     */
    public Gedcom getGedcom(int row) {
      return (Gedcom)gedcoms.get(row);
    }
  
    /**
     * All Gedcoms
     */
    public List getAllGedcoms() {
      return gedcoms;
    }
    
    /**
     * Add a gedcom
     */
    public void addGedcom(Gedcom gedcom) {
      gedcoms.add(gedcom);
      gedcom.addGedcomListener(this);
      fireTableDataChanged();
    }

    /**
     * Removes a gedcom
     */
    public void removeGedcom(Gedcom gedcom) {
      gedcoms.remove(gedcom);
      gedcom.removeGedcomListener(this);
      fireTableDataChanged();
    }
    
    /**
     * Gedcom 2 row
     */
    public int getRowFor(Gedcom gedcom) {
      return gedcoms.indexOf(gedcom);
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return Gedcom.ENTITIES.length+1;
    }
  
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
      return gedcoms.size();
    }
  
    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
      Gedcom gedcom = getGedcom(row);
      if (col==0) return gedcom.getName() + (gedcom.hasUnsavedChanges() ? "*" : "" );
      return new Integer(gedcom.getEntities(Gedcom.ENTITIES[col-1]).size());
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class getColumnClass(int col) {
      return col==0 ? String.class : Integer.class;
    }

  
    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Transaction change) {
      int i = getRowFor(change.getGedcom());
      if (i>=0) fireTableRowsUpdated(i,i);
    }

  } // Model

} //GedcomTableWidget
