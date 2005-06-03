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

import genj.util.ChangeSupport;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * A widget for dynamically searching for database information about locations
 */
public class QueryWidget extends JPanel {

  private static final String
    TXT_LOCATION = GeoView.RESOURCES.getString("location"),
    TXT_LATLON = GeoView.RESOURCES.getString("location.latlon");
  
  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout(
      "<col>" +
      "<row><label/><city wx=\"1\"/></row>" +
      "<row><label/><tlj wx=\"1\"/></row>" +
      "<row><label/><country wx=\"1\"/></row>" +
      "<row><hits wx=\"1\" wy=\"1\"/></row>" +
      "</col>"
      );
  
  /** our match model */
  private Model model;
  
  /** components */
  private TextFieldWidget city;
  private ChoiceWidget tlj;
  private ChoiceWidget country;
  private JTable hits;
  
  /**
   * Constructor
   */
  public QueryWidget(GeoLocation location) {
    super(LAYOUT.copy());
    
    // prepare our Match Model
    model = new Model();
    
    // prepare our components
    city = new TextFieldWidget(location.getCity());
    tlj  = new ChoiceWidget();
    country = new ChoiceWidget();
    hits = new JTable(model);
    
    add(new JLabel("City")); add(city);
    add(new JLabel("Top-Level Jurisdiction")); add(tlj);
    add(new JLabel("Country")); add(country);
    add(new JScrollPane(hits));
    
    // listen to changes
    ChangeSupport cs = new ChangeSupport(this);
    city.addChangeListener(cs);
    tlj.addChangeListener(cs);
    country.addChangeListener(cs);
    cs.addChangeListener(model);
    
    // done
  }
  
  public void addNotify() {
    // continue
    super.addNotify();
    // start async querying
    model.start();
  }
  
  public void removeNotify() {
    model.stop();
    // continue
    super.removeNotify();
  }
  
  /**
   * Our asynchronous model
   */
  private class Model extends AbstractTableModel implements Runnable, ChangeListener {
    
    private Thread thread = null;
    private boolean running = false;
    private GeoLocation query = null;
    private List locations = new ArrayList();
    
    /** table callback - column name */
    public String getColumnName(int col) {
      switch (col) {
      default: case 0: return TXT_LOCATION;
      case 1: return TXT_LATLON;
    }
    }
    
    /** table callback - columns */
    public int getColumnCount() {
      return 2;
    }
    
    /** table callback - rows */
    public int getRowCount() {
      return locations.size();
    }
    
    /** table callback - cell  */
    public Object getValueAt(int row, int col) {
      GeoLocation loc = (GeoLocation)locations.get(row);
      switch (col) {
        default: case 0: return loc.toString();
        case 1: return loc.getCoordinateAsString();
      }
    }

    /** a user initiated ui change - grab new location to look for */
    public synchronized void stateChanged(ChangeEvent e) {
      String sCity = city.getText().trim();
      if (sCity.length()==0) return;
      query = new GeoLocation(sCity, null, null);
    }
    
    /** our async thread */
    public void run() {
      // loop while running
      while (running) {
        // wait for something to do
        GeoLocation todo;
        synchronized (this) { 
          try { this.wait(250); } catch (InterruptedException ie) {} 
          todo = query;
          query = null;
        }
        // a task?
        if (running&&todo!=null) {
          GeoLocation[] found  = GeoService.getInstance().query(todo);
          synchronized (this) {
            locations = Arrays.asList(found);
            fireTableDataChanged();
          }
        }
      }
      // exit
    }
    
    /** start our thread */
    private synchronized void start() {
      stop();
      running = true;
      thread = new Thread(this);
      thread.start();
      // trigger some lookup
      stateChanged(null);
    }
    
    /** stop our thread */
    private void stop() {
      running = false;
      synchronized (this) {
        notify();
      }
      while (thread!=null) try {  thread.join(); thread = null; } catch (InterruptedException ie) {} 
      
    }
    
  } //Query
  
}
