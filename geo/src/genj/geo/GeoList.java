/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2005 Nils Meier <nils@meiers.net>
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
package genj.geo;

import genj.util.ActionDelegate;
import genj.util.swing.ButtonHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * A list of locations and opertions on them
 */
/*package*/ class GeoList extends JPanel {
  
  private static final String
    TXT_LOCATION = GeoView.RESOURCES.getString("location"),
    TXT_CHANGE = GeoView.RESOURCES.getString("location.change"),
    TXT_LATLON = GeoView.RESOURCES.getString("location.latlon"),
    TXT_UNKNOWN = GeoView.RESOURCES.getString("location.unknown");
  
  /** model */
  private GeoModel model;

  /** wrapped table */
  private JTable table; 
  
  /**
   * Constructor
   */
  public GeoList(GeoModel model) {
    
    // remember 
    this.model = model;

    // create some components
    table = new JTable(new TableModel());

    Update update = new Update();
    table.getSelectionModel().addListSelectionListener(update);
    
    // layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(table));
    add(BorderLayout.SOUTH, new ButtonHelper().create(update));
    
    setPreferredSize(new Dimension(160,64));
    
    // done
  }

  /**
   * Component lifecycle - we're needed
   */
  public void addNotify() {
    super.addNotify();
    // setup hooked-up model now
    table.setModel(new TableModel(model));
  }
  
  /**
   * Component lifecycle - we're not needed anymore
   */
  public void removeNotify() {
    super.removeNotify();
    // setup empty model 
    table.setModel(new TableModel());
  }
  
  /**
   * Selection access
   */
  public List getSelectedLocations() {
    
    List list = new ArrayList();
    int[] rows = table.getSelectedRows();
    for (int i=0;i<rows.length;i++)
      list.add(table.getValueAt(rows[i], 0));

    return list;
  }
  
  /**
   * Selection access
   */
  public void setSelectedLocations(Set set) {
    
    ListSelectionModel selection = table.getSelectionModel();
    
    // collect selected rows
    selection.setValueIsAdjusting(true);
    selection.clearSelection();
    try {
      TableModel tmodel = (TableModel)table.getModel();
      for (int i=0, j=tmodel.getRowCount(); i<j; i++) {
        GeoLocation l = tmodel.getLocationAt(i);
        if (set.contains(l)) 
          selection.addSelectionInterval(i, i);
      }
    } finally {
      selection.setValueIsAdjusting(false);
    }
    
    // done
  }
  
  /**
   * Add a listener
   */
  public void addListSelectionListener(ListSelectionListener l) {
    table.getSelectionModel().addListSelectionListener(l);
  }

  /**
   * Remove a listener
   */
  public void removeListSelectionListener(ListSelectionListener l) {
    table.getSelectionModel().removeListSelectionListener(l);
  }
  
  /**
   * An action for updating a location
   */
  private class Update extends ActionDelegate implements ListSelectionListener {
    private Update() {
      setText(TXT_CHANGE);
      setEnabled(false);
    }
    public void valueChanged(ListSelectionEvent e) {
      setEnabled(table.getSelectedRowCount()==1);
    }
    protected void execute() {
    }
  }

  /**
   * A table grid wrapper for locations shown in a JTable
   */
  private static class TableModel extends AbstractTableModel implements GeoModelListener {

    private GeoModel model;
    private List locations = new ArrayList();
    
    /** constructor */
    public TableModel() {
    }

    /** constructor */
    public TableModel(GeoModel model) {
      this.model = model;
    }

    /** column header info */
    public String getColumnName(int col) {
      switch (col) {
        default: case 0:
          return TXT_LOCATION;
        case 1:
          return TXT_LATLON;
      }
    }
    
    /** lifecycle - listeners */
    public void addTableModelListener(TableModelListener l) {
      super.addTableModelListener(l);
      // start working?
      if (model!=null&&getTableModelListeners().length==1) {
        model.addGeoModelListener(this);
        locations.clear();
        locations.addAll(model.getLocations());
        Collections.sort(locations);
      }
    }
    
    /** lifecycle - listeners */
    public void removeTableModelListener(TableModelListener l) {
      super.removeTableModelListener(l);
      // stop working?
      if (model!=null&&getTableModelListeners().length==0) {
        model.removeGeoModelListener(this);
        locations.clear();
      }
    }
    
    /** colums */
    public int getColumnCount() {
      return 2;
    }

    /** number of rows = locations */
    public int getRowCount() {
      return locations.size();
    }
    
    public GeoLocation getLocationAt(int row) {
      return (GeoLocation)locations.get(row);
    }

    public Object getValueAt(int row, int col) {
      // location for row
      GeoLocation location = getLocationAt(row);
      // check column
      switch (col) {
        default: case 0:
          return location;
        case 1:
          if (!location.isValid())
            return TXT_UNKNOWN;
          String coord = GeoLocation.getString(location.getCoordinate());
          if (location.getMatches()>1) coord += "?";
          return coord;
      }
      // done
    }

    /** callback - geo model event */
    public void locationAdded(GeoLocation location) {
      fireTableDataChanged();
      
      locations.add(location);
      Collections.sort(locations);
    }

    /** callback - geo model event */
    public void locationUpdated(GeoLocation location) {
      fireTableRowsUpdated(0, getRowCount()-1);
    }

    /** callback - geo model event */
    public void locationRemoved(GeoLocation location) {
      locations.remove(location);
      fireTableDataChanged();
    }

  } //TableModel

}
