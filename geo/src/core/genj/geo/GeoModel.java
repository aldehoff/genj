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
import genj.gedcom.Transaction;
import genj.util.ActionDelegate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Geographic model wrapper for gedcom
 */
/*package*/ class GeoModel implements GedcomListener {
  
  /** state */
  private List listeners = new ArrayList();
  private Gedcom gedcom;
  private Set locations = new HashSet();
  private LinkedList resolvers = new LinkedList();
  
  /**
   * Constructor
   * @param gedcom the reference to gedcom object this model is for
   * @param map map this model is providing data for
   */
  public GeoModel(Gedcom gedcom) {
    
    // remember the critical part
    this.gedcom = gedcom;
    
    // done
  }
  
  /**
   * Accessor - gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Set a location's coordinates
   */
  public void setCoordinates(GeoLocation loc, Coordinate coord) {
    if (locations.contains(loc)) {
      loc.setCoordinate(coord);
      fireLocationUpdated(loc);
    }
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
    return Collections.unmodifiableCollection(locations);
  }
  
  /**
   * callback - gedcom change 
   */
  public void handleChange(Transaction tx) {
      // remove all locations for all modified entities
      List current = new ArrayList(locations);
      Set entities = tx.get(Transaction.ENTITIES_MODIFIED);
      for (Iterator locs = current.iterator(); locs.hasNext(); ) {
        GeoLocation loc = (GeoLocation)locs.next();
        loc.removeEntities(entities);
        if (loc.getNumProperties()==0) {
          locations.remove(loc);
          fireLocationRemoved(loc);
        }
      }
      
      // reparse entities changed
      Set added = GeoLocation.parseEntities(entities);
      for (Iterator locs = added.iterator(); locs.hasNext(); ) {
        GeoLocation loc = (GeoLocation)locs.next();
        locations.add(loc);
        fireLocationAdded(loc);
      }
      
      // resolve
      resolve(added);
      
    // done
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
   * Start a resolver
   */
  private void resolve(Collection todo) {
    synchronized (resolvers) {
      Resolver resolver = new Resolver(locations);
      if (resolvers.isEmpty())
        resolver.trigger();
      else
        resolvers.add(resolver);
    }
  }

  /**
   * A resolver for our locations
   */
  private class Resolver extends ActionDelegate {
    
    private ArrayList todo;

    /** constructor */
    private Resolver(Collection todo) {
      setAsync(ActionDelegate.ASYNC_SAME_INSTANCE);
      
      // make a private copy of todo
      this.todo = new ArrayList(todo);
    }

    /** async exec */
    protected void execute() {
      try { 
        GeoService.getInstance().match(gedcom, todo);
      } catch (Throwable t) {}
    }

    /** sync post-exec */
    protected void postExecute(boolean preExecuteResult) {
      // tell about it
      for (Iterator it=todo.iterator(); it.hasNext(); ) {
        GeoLocation loc = (GeoLocation)it.next();
        if (locations.contains(loc)) fireLocationUpdated(loc);
      }
      // start pending?
      synchronized (resolvers) {
        if (!resolvers.isEmpty())
          ((Resolver)resolvers.removeFirst()).trigger();
      }
    }
    
  } //Resolver
  
  /**
   * add a listener
   */
  public void addGeoModelListener(GeoModelListener l) {
    // the first one ?
    if (listeners.isEmpty()) {
      // grab everything again
      locations.clear();
      locations.addAll(GeoLocation.parseEntities(gedcom.getEntities(Gedcom.INDI)));
      // start a resolver
      resolve(locations);
      // attach
      gedcom.addGedcomListener(this);
    }
    // remember
    listeners.add(l);
    // done
  }
  
  /**
   * remove a listener
   */
  public void removeGeoModelListener(GeoModelListener l) {
    
    // bbye
    listeners.remove(l);
    
    // was this the last one?
    if (listeners.isEmpty()) {
      // clear our list of locations
      locations.clear();
      // detach
      gedcom.removeGedcomListener(this);
    }
  }
  
}
