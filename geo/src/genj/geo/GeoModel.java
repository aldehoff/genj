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
  private Map prop2location = new HashMap();

  /**
   * Constructor
   */
  public GeoModel(Gedcom gedcom) {
    // keep 
    this.gedcom = gedcom;
  }
  
  /**
   * Accessor - locations
   */
  public Collection getLocations() {
    
    return prop2location.values();
  }
  
  /**
   * callback - gedcom change 
   */
  public void handleChange(Transaction tx) {
    // clear location for modified props
    addProperties(tx.get(Transaction.PROPERTIES_MODIFIED));
    // take on added props
    addProperties(tx.get(Transaction.PROPERTIES_ADDED));
    // remove deleted props
    delProperties(tx.get(Transaction.PROPERTIES_DELETED));
    // done
  }

  /**
   * Remove events
   */
  private void delProperties(Collection props) {
    for (Iterator dels=props.iterator(); dels.hasNext(); ) {
      Property prop = (Property)dels.next();
      while (prop!=null) {
        if (prop instanceof PropertyEvent) {
          prop2location.remove(prop);
          break;
        }
        prop = prop.getParent();
      }
    }
    // done
  }
  
  /**
   * Add location
   */
  private void addLocation(Property prop) {
    try {
      // check if part of event (recursive parent lookup)
      while (prop!=null) {
        if (prop instanceof PropertyEvent) {
          GeoLocation location = new GeoLocation(prop) {
            protected void set(float lat,float lon) {
              super.set(lat, lon);
              
              // FIXME need to go tell GeoModelListeners
              //System.out.println(this);
            }
          };
          prop2location.put(prop, location);
          LOCATOR.add(location);
          return;
        }
        prop = prop.getParent();
      }
    } catch (IllegalArgumentException i) {
    }
  }
  
  /**
   * Add events
   */
  private void addProperties(Collection props) {
    for (Iterator adds=props.iterator(); adds.hasNext(); ) 
      addLocation((Property)adds.next());
  }
  
  /**
   * Add events of entities
   */
  private void addEntities(Collection entities) {
    for (Iterator it = entities.iterator(); it.hasNext(); ) {
      Property entity = (Property)it.next();
      addProperties(entity.getProperties(PropertyEvent.class));
    }
  }
  
  /**
   * reset all data and start over
   */
  private void reset() {
    prop2location.clear();
    // collect events from indis and fams
    addEntities(gedcom.getEntities(Gedcom.INDI));
    addEntities(gedcom.getEntities(Gedcom.FAM));
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
      t.setPriority(Thread.MIN_PRIORITY);
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
        locations.add(location);
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
      
      // done
    }
  } // Locator
  
} //GeoModel
