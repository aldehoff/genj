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

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.Transaction;
import genj.util.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Geographic model wrapper for gedcom
 */
/*package*/ class GeoModel implements GedcomListener, GeoService.Listener {
  
  /** one locator for all */
  private final static Locator LOCATOR = new Locator();
  
  /** state */
  private List listeners = new ArrayList();
  private Gedcom gedcom;
  private Map locations = new HashMap();
  private Registry registry;
  
  /**
   * Constructor
   * @param gedcom the reference to gedcom object this model is for
   * @param map map this model is providing data for
   */
  public GeoModel(Gedcom gedcom) {
    // remember the critical part
    this.gedcom = gedcom;
    // load gedcom specific .geo file
    String name = gedcom.getName();
    if (name.endsWith(".ged")) name = name.substring(0, name.length()-".ged".length());
    name = name + ".geo";
    registry = Registry.lookup(name, gedcom.getOrigin());
    
    // done
  }
  
  /**
   * Accessor - gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - locations
   */
  public synchronized int getNumLocations() {
    return locations.size();
  }
  
  /**
   * Accessor - locations
   */
  public synchronized Collection getLocations() {
    return Collections.unmodifiableCollection(locations.keySet());
  }
  
  /**
   *callback - GeoService changes
   */
  public void handleGeoDataChange() {
    // update all locations
    synchronized (locations) {
      for (Iterator it = locations.keySet().iterator(); it.hasNext(); ) 
        LOCATOR.add((GeoLocation)it.next());
    }
  }
    
  /**
   * callback - gedcom change 
   */
  public void handleChange(Transaction tx) {
    // clear location for all modified entityes
    for (Iterator it = tx.get(Transaction.ENTITIES_MODIFIED).iterator(); it.hasNext(); ) {
      removeLocations((Entity)it.next());
    }
    // reparse entities changed
    parseEntities(tx.get(Transaction.ENTITIES_MODIFIED));
//    // remove deleted props
//    Change[] changes = tx.getChanges();
//    for (int i=0;i<changes.length;i++) {
//      Change change = changes[i];
//      if (change instanceof Change.PropertyDel) 
//        removeLocation(((Change.PropertyDel)change).getRoot());
//    }
    // done
  }
  
  /**
   * Fix a location permanently
   */
  protected void remember(GeoLocation loc, Coordinate coord) {
    // remember
    registry.put(loc.getJurisdictionsAsString(), coord.y + "," + coord.x);
    
    // tell it to location
    loc.set(coord.y, coord.x, 1);
  }
  
  /**
   * Tell listeners about a found location
   */
  private void fireLocationAdded(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationAdded(location);
    }
  }
  
  /**
   * Tell listeners about an updated location
   */
  private void fireLocationUpdated(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationUpdated(location);
    }
  }

  /**
   * Tell listeners about a removed location
   */
  private void fireLocationRemoved(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationRemoved(location);
    }
  }

  /**
   * Remove location
   */
  private synchronized void removeLocations(Entity entity) {

    // check locations - since removeAll might end up in locations
    // being removed we have to copy the key-set first
    Object[] locs = locations.keySet().toArray();
    for (int i=0;i<locs.length;i++) 
      ((GeoLocation)locs[i]).removeAll(entity);
    
    // done
  }
  
  /**
   * Add a new location for given property
   */
  private void addLocation(Property prop) {
    
    // create a location for it
    GeoLocation location;
    try {
      location = new GeoLocation(prop) {
        // override : keep track of location lat/lon
        protected void set(double lat, double lon, int matches) {
          super.set(lat, lon, matches);
          synchronized (GeoModel.this) {
            if (!locations.containsKey(this)) 
              return;
          }
          fireLocationUpdated(this);
        }
        // override : remove empty locations
        public void removeAll(Entity entity) {
          // let super do its thing
          super.removeAll(entity);
          // check if still necessary
          if (properties.isEmpty()) {
            synchronized (GeoModel.this) {
              locations.remove(this);
            }
            fireLocationRemoved(this);
          } else {
            fireLocationUpdated(this);
          }
          // done
        }
      };
    } catch (IllegalArgumentException e) {
      return;
    }
    
    // check if we have a location like that
    GeoLocation other;
    synchronized (this) {
      other = (GeoLocation)locations.get(location);
    }
    
    if (other!=null) {
      other.add(location);
      fireLocationUpdated(other);
      return;
    }
    
    // keep new location 
    synchronized (this) {
      
      locations.put(location, location);
      
      // something we can map through the registry?
      String latlon  = registry.get(location.getJurisdictionsAsString(), (String)null);
      if (latlon!=null) {
        int comma = latlon.indexOf(',');
        if (comma>0) 
          location.set( Double.parseDouble(latlon.substring(0, comma)), Double.parseDouble(latlon.substring(comma+1)), 1);
      }

      // add to to-do list
      if (!location.isValid())
        LOCATOR.add(location);
    }
    
    // tell about it
    fireLocationAdded(location);
    
    // done
  }
  
  /**
   * Add root properties PLAC or ADDR of entities
   */
  private void parseEntities(Collection entities) {
    
    for (Iterator it = entities.iterator(); it.hasNext(); ) {
      Property entity = (Property)it.next();
      for (int i=0, j=entity.getNoOfProperties(); i<j; i++) {
        Property prop = entity.getProperty(i);
        if (prop.getProperty("PLAC")!=null||prop.getProperty("ADDR")!=null)
          addLocation(prop);
      }
    }
  }
  
  /**
   * reset all data and start over
   */
  private void reset() {
    locations.clear();
    // collect events from indis and fams
    parseEntities(gedcom.getEntities(Gedcom.INDI));
    parseEntities(gedcom.getEntities(Gedcom.FAM));
    // done
  }
  
  /**
   * add a listener
   */
  public void addGeoModelListener(GeoModelListener l) {
    // start listening?
    if (listeners.isEmpty()) {
      reset();
      gedcom.addGedcomListener(this);
      GeoService.getInstance().addListener(this);
    }
    listeners.add(l);
  }
  
  /**
   * remove a listener
   */
  public void removeGeoModelListener(GeoModelListener l) {
    listeners.remove(l);
    // stoplistening?
    if (listeners.isEmpty()) {
      gedcom.removeGedcomListener(this);
      GeoService.getInstance().removeListener(this);
    }
  }
  
  /**
   * One global asynchronous locator
   */
  private static class Locator implements Runnable {
    
    private GeoService service = GeoService.getInstance();
    
    /** current 'job list' */
    private LinkedList locations = new LinkedList();
    
    /** constructor */
    private Locator() {
      // setup thread
      Thread t = new Thread(this);
      t.setPriority(Thread.NORM_PRIORITY-1);
      t.setDaemon(true);
      t.start();
      // ready
    }
    /** async */
    public void run() {
      
      while (true) {
        try {
          // wait 
          sleep();
          // locate places
          locate();
        } catch (Throwable t) {
          GeoView.LOG.log(Level.SEVERE, "unexpected throwable", t);
        }
      }
    }
    /** wait for a second/changes */
    private void sleep() {
      try {
        synchronized (locations) {
          if (locations.isEmpty())
            locations.wait();
        }
      } catch (InterruptedException e) {
      }
    }
    /** add to 'job-list' */
    private void add(GeoLocation location) {
      synchronized (locations) {
        locations.addLast(location);
        locations.notify();
      }
    }
    /** locate current locations */
    private void locate() {
      
      // loop until all locations are iterated
      while (true) {
        GeoLocation location;
        synchronized (locations) {
          if (locations.isEmpty())
            return;
          location = (GeoLocation)locations.removeFirst();
        }
        service.match(location);
     }
      
      // no more locations to match
    }
  } // Locator
  
}
