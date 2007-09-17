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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.SortableTableModel;
import genj.view.ContextProvider;
import genj.view.ContextSelectionEvent;
import genj.view.ViewContext;
import genj.view.ViewManager;
import genj.window.WindowBroadcastEvent;
import genj.window.WindowBroadcastListener;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import spin.Spin;

/**
 * A component displaying a list of Gedcoms
 */
/*package*/ class GedcomTableWidget extends JTable implements ContextProvider, WindowBroadcastListener {
  
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

      col.setWidth(defaultWidths[h]);
      col.setPreferredWidth(defaultWidths[h]);
      cm.addColumn(col);
    }
    setModel(new SortableTableModel(model, getTableHeader()));
    setColumnModel(cm);

    // change looks    
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getTableHeader().setReorderingAllowed(false);
    
    // grab the preferred columns
    int[] widths = registry.get("columns",new int[0]);
    for (int c=0, max=getColumnModel().getColumnCount(); c<widths.length&&c<max; c++) {
      TableColumn col = getColumnModel().getColumn(c);
      col.setPreferredWidth(widths[c]);
      col.setWidth(widths[c]);
    }    
    
    // add motion listener for tooltips
    getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        int col = getColumnModel().getColumnIndexAtX(e.getX());
        String tip = col<=0||col>Gedcom.ENTITIES.length ? null : Gedcom.getName(Gedcom.ENTITIES[col-1]);
        getTableHeader().setToolTipText(tip);
      }
    });

    // done
  }
  
  public Dimension getPreferredScrollableViewportSize() {
    return new Dimension(Math.max(128, getColumnModel().getTotalColumnWidth()), Math.max(4, getModel().getRowCount())*getRowHeight());
  }
  
  /**
   * ContextProvider - callback
   */
  public ViewContext getContext() {
    int row = getSelectedRow();
    return row<0 ? null : new ViewContext(model.getGedcom(row));
  }

  /**
   * A windows broadcast message
   */
  public boolean handleBroadcastEvent(WindowBroadcastEvent event) {
    
    ContextSelectionEvent cse = ContextSelectionEvent.narrow(event);
    if (cse!=null) {
      int row = model.getRowFor(cse.getContext().getGedcom());
      if (row>=0)
        getSelectionModel().setSelectionInterval(row,row);
    }
    
    return true;
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
    int row = model.getRowFor(gedcom);
    getSelectionModel().setSelectionInterval(row,row);
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
  private class Model extends AbstractTableModel implements GedcomMetaListener {
    
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
      Collections.sort(gedcoms);
      gedcom.addGedcomListener((GedcomListener)Spin.over(this));
      fireTableDataChanged();
    }

    /**
     * Removes a gedcom
     */
    public void removeGedcom(Gedcom gedcom) {
      gedcoms.remove(gedcom);
      gedcom.removeGedcomListener((GedcomListener)Spin.over(this));
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
      if (col==0) return gedcom.getName() + (gedcom.hasChanged() ? "*" : "" );
      return new Integer(gedcom.getEntities(Gedcom.ENTITIES[col-1]).size());
    }
    
    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class getColumnClass(int col) {
      return col==0 ? String.class : Integer.class;
    }

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property prop) {
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }
    
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
      int i = getRowFor(gedcom);
      if (i>=0) fireTableRowsUpdated(i,i);
    }

  } // Model

} //GedcomTableWidget
