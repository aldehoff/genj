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

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.Transaction;
import genj.util.Debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Geographic model wrapper for gedcom
 */
/*package*/ class GeoModel implements GedcomListener {
  
  /** one locator for all */
  private final static Locator LOCATOR = new Locator();
  
  /** state */
  private List listeners = new ArrayList();
  private Gedcom gedcom;
  private Map knownLocations = new HashMap();
  private Map unknownLocations = new HashMap();

  /**
   * Constructor
   * @param gedcom the reference to gedcom object this model is for
   * @param map map this model is providing data for
   */
  public GeoModel(Gedcom gedcom) {
    // keep 
    this.gedcom = gedcom;
  }
  
  /**
   * Accessor - locations
   */
  public synchronized List getKnownLocations() {
    return new ArrayList(knownLocations.keySet());
  }
  
  /**
   * callback - gedcom change 
   */
  public void handleChange(Transaction tx) {
// FIXME handle changes
//    // clear location for modified props
//    addProperties(tx.get(Transaction.PROPERTIES_MODIFIED));
    // take on added props
    parseProperties(tx.get(Transaction.PROPERTIES_ADDED));
    // remove deleted props
    delProperties(tx.get(Transaction.PROPERTIES_DELETED));
    // done
  }
  
  /**
   * Tell listeners about a found location
   */
  private void fireLocationUpdated(GeoLocation location) {
    GeoModelListener[] ls = (GeoModelListener[])listeners.toArray(new GeoModelListener[listeners.size()]);
    for (int i = 0; i < ls.length; i++) {
      ls[i].locationUpdated(location);
    }
  }

  /**
   * Remove events
   */
  private void delProperties(Collection props) {
    for (Iterator dels=props.iterator(); dels.hasNext(); ) {
      Property prop = (Property)dels.next();
      if (prop instanceof PropertyEvent) 
        removeLocation(prop);
    }
    // done
  }
  
  /**
   * Remove location
   */
  private synchronized void removeLocation(Property prop) {
// FIXME
//    for (Iterator it = knownLocations.iterator(); it.hasNext(); ) { 
//      GeoLocation location = (GeoLocation)it.next();
//      if (location.getProperty()==prop) {
//        it.remove();
//      }
//    }
//    for (ListIterator it = unknownLocations.listIterator(); it.hasNext(); ) { 
//      GeoLocation location = (GeoLocation)it.next();
//      if (location.getProperty()==prop)
//        it.remove();
//    }
  }
  
  /**
   * Add a new location
   */
  private void addLocation(PropertyEvent event) {
    
    // create a location for it
    GeoLocation location;
    try {
      location = new GeoLocation(event) {
        // override location set to keep track of locations that become known
        protected void set(double lat,double lon) {
          super.set(lat, lon);
          synchronized (GeoModel.this) {
            if (unknownLocations.remove(this)==this && isValid())
              knownLocations.put(this, this);
          }
          fireLocationUpdated(this);
        }
      };
    } catch (IllegalArgumentException e) {
      return;
    }
    
    // check if we have a location like that
    GeoLocation other;
    synchronized (this) {
      other = (GeoLocation)unknownLocations.get(location);
      if (other==null) other = (GeoLocation)knownLocations.get(location);
    }
    
    if (other!=null) {
      other.add(location);
      fireLocationUpdated(other);
      return;
    }
      
    // add this location as unknown and to-do
    synchronized (this) {
      unknownLocations.put(location, location);
      LOCATOR.add(location);
    }
    
    // done
  }
  
  /**
   * Check to find location worthy property
   */
  private void parseProperty(Property prop) {

    // check if part of event (recursive parent lookup)
    while (prop!=null) {
      if (prop instanceof PropertyEvent) {
        addLocation((PropertyEvent)prop);
        return;
      }
      prop = prop.getParent();
    }
    
    // didn't work out
  }
  
  /**
   * Add events
   */
  private void parseProperties(Collection props) {
    for (Iterator adds=props.iterator(); adds.hasNext(); ) 
      parseProperty((Property)adds.next());
  }
  
  /**
   * Add events of entities
   */
  private void parseEntities(Collection entities) {
    for (Iterator it = entities.iterator(); it.hasNext(); ) {
      Property entity = (Property)it.next();
      parseProperties(entity.getProperties(PropertyEvent.class));
    }
  }
  
  /**
   * reset all data and start over
   */
  private void reset() {
    knownLocations.clear();
    unknownLocations.clear();
    // collect events from indis and fams
    parseEntities(gedcom.getEntities(Gedcom.INDI));
    parseEntities(gedcom.getEntities(Gedcom.FAM));
    // done
  }
  
  /**
   * add a listener
   */
  public void addGeoModelListener(GeoModelListener l) {
    listeners.add(l);
    // start listening?
    if (listeners.size()==1) {
      reset();
      gedcom.addGedcomListener(this);
    }
  }
  
  /**
   * remove a listener
   */
  public void removeGeoModelListener(GeoModelListener l) {
    listeners.remove(l);
    // stoplistening?
    if (listeners.isEmpty())
    gedcom.removeGedcomListener(this);
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
          Debug.log(Debug.ERROR, this, t);
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
//        long t = System.currentTimeMillis();
        service.match(location);
//        System.out.println(System.currentTimeMillis()-t);
      }
      
      // done
    }
  } // Locator
  
} //GeoModel
