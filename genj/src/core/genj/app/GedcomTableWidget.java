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

import genj.gedcom.Change;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.util.Registry;
import genj.util.swing.SortableTableHeader;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * A component displaying a list of Gedcoms
 */
public class GedcomTableWidget extends JTable {
  
  /** default column headers */
  private static final Object headers[] = {
    App.resources.getString("cc.column_header.name"),
    Gedcom.getImage(Gedcom.INDIVIDUALS),
    Gedcom.getImage(Gedcom.FAMILIES),
    Gedcom.getImage(Gedcom.MULTIMEDIAS),
    Gedcom.getImage(Gedcom.NOTES),
    Gedcom.getImage(Gedcom.SOURCES),
    Gedcom.getImage(Gedcom.SUBMITTERS),
    Gedcom.getImage(Gedcom.REPOSITORIES),
  };

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
  public GedcomTableWidget() {
 
    // change the header to ours    
    setTableHeader(new SortableTableHeader());
    
    // Prepare a model
    model = new Model();
    
    // Prepare a column model
    TableColumnModel cm = new DefaultTableColumnModel();
    for (int h=0; h<headers.length; h++) {
      TableColumn col = new TableColumn(h);
      col.setHeaderValue(headers[h]);
      col.setPreferredWidth(defaultWidths[h]);
      cm.addColumn(col);
    }
    setModel(model);
    setColumnModel(cm);

    // change looks    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getTableHeader().setReorderingAllowed(false);
    
    // done
  }
  
  /**
   * Tells us where to look for stored information
   */
  public void setRegistry(Registry registry) {
    // remember
    this.registry=registry;
    // grab the preferred columns
    int[] widths = registry.get("columns",new int[0]);
    for (int c=0; c<widths.length; c++) {
      getColumnModel().getColumn(c).setPreferredWidth(widths[c]);
    }    
    // done
  }
  
  /**
   * Hooking into the tear-down process to store our
   * settings in (set) registry
   */
  public void removeNotify() {
    // remember our layout
    if (registry!=null) {
      int[] widths = new int[headers.length];
      for (int c=0; c<widths.length; c++) {
        widths[c] = getColumnModel().getColumn(c).getWidth();
      }
      registry.put("columns", widths);
    }
    // continue
    super.removeNotify();
  }
  
  /**
   * Accessor for model
   */
  public Vector getAllGedcoms() {
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
    Vector gs = model.getAllGedcoms();
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
  public class Model extends AbstractTableModel implements GedcomListener {
    
    /** the Gedcoms we know about */
    private Vector gedcoms = new Vector(10);
    
    /**
     * Selected Gedcom
     */
    public Gedcom getSelectedGedcom() {
      int row = getSelectedRow();
      if (row==-1) return null;
      return (Gedcom)gedcoms.elementAt(row);
    }
  
    /**
     * All Gedcoms
     */
    public Vector getAllGedcoms() {
      return gedcoms;
    }
  
    /**
     * Add a gedcom
     */
    public void addGedcom(Gedcom gedcom) {
      gedcoms.addElement(gedcom);
      gedcom.addListener(this);
      fireTableDataChanged();
    }

    /**
     * Removes a gedcom
     */
    public void removeGedcom(Gedcom gedcom) {
      gedcoms.removeElement(gedcom);
      gedcom.removeListener(this);
      fireTableDataChanged();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
      return headers.length;
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
      Gedcom gedcom = (Gedcom)gedcoms.elementAt(row);
      if (col==0) return gedcom.getName();
      return ""+gedcom.getEntities(col-1).size();
    }
  
    /**
     * @see genj.gedcom.GedcomListener#handleChange(Change)
     */
    public void handleChange(Change change) {
      if (change.isChanged(change.EADD)||change.isChanged(change.EDEL)) {
        int row = getSelectedRow();
        fireTableDataChanged();
        if (row>=0) getSelectionModel().setSelectionInterval(row,row);
      }
    }

  } // Model

} //GedcomTableWidget
