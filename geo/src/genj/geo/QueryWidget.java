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
import genj.util.Resources;
import genj.util.swing.ChoiceWidget;
import genj.util.swing.NestedBlockLayout;
import genj.util.swing.TextFieldWidget;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

/**
 * A widget for dynamically searching for database information about locations
 */
public class QueryWidget extends JPanel {
  
  private final static Resources RESOURCES = Resources.get(QueryWidget.class);

  private static final String
    TXT_LOCATION = RESOURCES.getString("location"),
    TXT_LATLON = RESOURCES.getString("location.latlon"),
    TXT_QUERYING = RESOURCES.getString("query.querying");
  
  private final static NestedBlockLayout LAYOUT = new NestedBlockLayout(
      "<col>" +
      "<row><label/></row>" +
      "<row><label/><city wx=\"1\"/></row>" +
      "<row><label/><tlj wx=\"1\"/></row>" +
      "<row><label/><country wx=\"1\"/></row>" +
      "<row><label/></row>" +
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
  private JLabel status;
  
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
    tlj.setEditable(false);
    tlj.setEnabled(false);
    
    country = new ChoiceWidget();
    List countries = new ArrayList();
    countries.add(null);
    countries.addAll(Arrays.asList(Country.getAllCountries()));
    country.setEditable(false);
    country.setValues(countries);
    country.setSelectedItem(location.getCountry());
    hits = new JTable(model);
    status = new JLabel();
    
    add(new JLabel(RESOURCES.getString("query.instruction"))); 
    add(new JLabel(RESOURCES.getString("query.city"))); add(city);
    add(new JLabel(RESOURCES.getString("query.tlj"))); add(tlj);
    add(new JLabel(RESOURCES.getString("query.country"))); add(country);
    add(status);
    add(new JScrollPane(hits));
    
    // listen to changes
    ChangeSupport cs = new ChangeSupport(this);
    city.addChangeListener(cs);
    tlj.addChangeListener(cs);
    country.addChangeListener(cs);
    
    final Timer timer = new Timer(500, new ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent e) {
        String sCity = city.getText().trim();
        if (sCity.length()<2) return;
        model.setLocation(new GeoLocation(sCity, (Jurisdiction)tlj.getSelectedItem(), (Country)country.getSelectedItem()));
      }
    });
    timer.setRepeats(false);
    timer.start();
    
    cs.addChangeListener( new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // country choice?
        if (e.getSource()==country) {
          Country c = (Country)country.getSelectedItem();
          List jurisdictions = new ArrayList();
          if (c!=null) {
            jurisdictions.add(null);
            jurisdictions.addAll(Jurisdiction.getAll(c));
          }
          tlj.setSelectedItem(null);
          tlj.setValues(jurisdictions);
          tlj.setEnabled(!jurisdictions.isEmpty());
        }
        // restart query time
        timer.restart();
      }
    });
    
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
  private class Model extends AbstractTableModel implements Runnable {
    
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
    
    /** set current location to query */
    public void setLocation(GeoLocation set) {
      synchronized (this) {
        query = set;
        notify();
      }
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
          synchronized (this) {
            locations = Collections.EMPTY_LIST;
            fireTableDataChanged();
            status.setText(TXT_QUERYING);
          }
          GeoLocation[] found  = GeoService.getInstance().query(todo);
          synchronized (this) {
            locations = Arrays.asList(found);
            fireTableDataChanged();
            status.setText(RESOURCES.getString("query.matches", String.valueOf(found.length)));
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
    }
    
    /** stop our thread */
    private void stop() {
      running = false;
      synchronized (this) {
        notify();
        if (thread!=null) thread.interrupt();
      }
      //while (thread!=null) try {  thread.join(); thread = null; } catch (InterruptedException ie) {} 
      
    }
    
  } //Query
  
}
