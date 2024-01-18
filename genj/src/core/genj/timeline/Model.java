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
package genj.timeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import genj.gedcom.Change;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyName;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model implements GedcomListener {

  /** the gedcom we're looking at */
  /*package*/ Gedcom gedcom;
  
  /** limits */
  /*package*/ double 
    max = Double.NaN,
    min = Double.NaN;

  /** a filter for events that we're interested in */
  private Set filter;
  
  /** default filter */
  /*package*/ final static Set DEFAULT_FILTER = new HashSet(Arrays.asList(new String[]{ "BIRT", "MARR", "RESI", "EMIG" }));
    
  /** our levels */
  /*package*/List layers;
  
  /** time per event */
  /*package*/ double 
    timeBeforeEvent = 0.5D,
    timeAfterEvent  = 2.0D;
  
  /** listeners */
  private List listeners = new ArrayList(1);
  
  /**
   * Constructor
   */
  /*package*/ Model(Gedcom gedcom, Set filter) {
    
    // remember
    this.gedcom = gedcom;
    this.filter = filter;    

    // done
  }
  
  /**
   * Add a listener
   */
  /*package*/ void addListener(Listener listener) {
    // keep it
    listeners.add(listener);
    // 1st listener?
    if (listeners.size()==1) {
      // start listening ourselves
      gedcom.addListener(this);
      // gather events
      createEvents();
    }
    // done
  }
  
  /**
   * Removes a listener
   */
  /*package*/ void removeListener(Listener listener) {
    // get rif of it
    listeners.remove(listener);
    // last listener?
    if (listeners.size()==0) {
      // stop listening ourselves
      gedcom.removeListener(this);
    }
    // done
  }
  
  /**
   * change time per event
   */
  /*package*/ void setTimePerEvent(double before, double after) {
    // already there?
    if (timeBeforeEvent==before&&timeAfterEvent==after) return;
    // remember
    timeBeforeEvent = before;
    timeAfterEvent = after;
    // layout the events we've got
    if (layers!=null) layoutEvents();
    // done
  }
  
  /**
   * Returns the filter - set of Tags we consider
   */
  public Set getFilter() {
    return Collections.unmodifiableSet(filter);
  }
  
  /**
   * Sets the filter - set of Tags we consider
   */
  public void setFilter(Set set) {
    filter.clear();
    filter.addAll(set);
    createEvents();
  }
  
  /**
   * @see genj.gedcom.GedcomListener#handleChange(Change)
   */
  public void handleChange(Change change) {
    // deleted or added entities/properties -> recreate
    if (change.isChanged(change.EDEL)||change.isChanged(change.EADD)||change.isChanged(change.PADD)||change.isChanged(change.PDEL)) {
      createEvents();
      return;
    }
    // changed properties -> scan for dates or names
    boolean changed = false;
    if (change.isChanged(change.PMOD)) {
      Iterator ps = change.getProperties(change.PMOD).iterator();
      while (ps.hasNext()) {
        Property p = (Property)ps.next();
        // a date -> lets recreate everything
        if (p instanceof PropertyDate) {
          createEvents();
          return;
        }
        // a name -> let's update all it's entities' events
        if (p instanceof PropertyName) {
          contentEvents(p.getEntity());
          changed = true;
        }
      }
    }
    // still here and a change has happened?
    if (changed) fireDataChanged();
    // done
  }

  /**
   * @see genj.gedcom.GedcomListener#handleSelection(Entity, boolean)
   */
  public void handleSelection(Entity entity, boolean emphasized) {
    // ignored
  }
  
  /**
   * Trigger callback - our structure has changed
   */
  private void fireStructureChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((Listener)listeners.get(l)).structureChanged();
    }
  }

  /**
   * Trigger callback - our data has changed
   */
  private void fireDataChanged() {
    for (int l=listeners.size()-1; l>=0; l--) {
      ((Listener)listeners.get(l)).dataChanged();
    }
  }
  
  /**
   * Retags events for given entity
   */
  private final void contentEvents(Entity entity) {
    // loop through layers
    for (int l=0; l<layers.size(); l++) {
      List layer = (List)layers.get(l);
      Iterator events = layer.iterator();
      while (events.hasNext()) {
        Event event = (Event)events.next();
        if (event.pe.getEntity()==entity) event.content();
      }
    }
    // done
  }

  /**
   * Layout events by using the existing set of events
   * and re-stacking them in layers
   */
  private final void layoutEvents() {
    // reset
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    // keep old and create some new space
    List old = layers;
    layers = new ArrayList(10);
    // loop through old
    for (int l=0; l<old.size(); l++) {
      List layer = (List)old.get(l);
      Iterator events = layer.iterator();
      while (events.hasNext()) {
        Event event = (Event)events.next();
        insertEvent(event);
      }
    }
    // extend time by before/after
    max += timeAfterEvent;
    min -= timeBeforeEvent;
    // trigger
    fireStructureChanged();
    // done
  }
  
  /**
   * Gather Events
   */
  private final void createEvents() {
    // reset
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    // prepare some space
    layers = new ArrayList(10);
    // look for events in INDIs and FAMs
    createEventsFrom(gedcom.getEntities(Gedcom.INDIVIDUALS));
    createEventsFrom(gedcom.getEntities(Gedcom.FAMILIES   ));
    // extend time by before/after
    max += timeAfterEvent;
    min -= timeBeforeEvent;
    // trigger
    fireStructureChanged();
    // done
  }
  
  /** 
   * Gather Events for given entities
   * @param es list of entities to find events in
   */
  private final void createEventsFrom(List es) {
    // loop through entities
    for (int i=0; i<es.size(); i++) {
      Entity e = (Entity)es.get(i);
      List ps = e.getProperty().getProperties(PropertyEvent.class);
      for (int j=0; j<ps.size(); j++) {
        PropertyEvent pe = (PropertyEvent)ps.get(j);
        if (filter.contains(pe.getTag())) createEventFrom(pe);
      }
    }
    // done
  }
  
  /** 
   * Gather Event for given PropertyEvent
   * @param pe property to use
   */
  private final void createEventFrom(PropertyEvent pe) {
    // we need a valid date for that event
    PropertyDate pd = pe.getDate();
    if (pd==null) return;
    // get start (has to be valid) and end
    PropertyDate.PointInTime 
      start = pd.getStart(),
      end   = pd.getEnd();
    if (!start.isValid()) return;
    if (!end  .isValid()) end = start;
    // create the Event
    Event e = new Event(pe, pd, wrap(start), wrap(end));
    // keep the event
    insertEvent(e);
    // done
  }
  
  /**
   * Insert the Event into one of our layers
   */
  private final void insertEvent(Event e) {
    
    // remember min and max
    min = Math.min(Math.floor(e.from), min);
    max = Math.max(Math.ceil (e.to  ), max);
    
    // find a level that suits us
    for (int l=0;l<layers.size();l++) {
      // try to insert in level
      List layer = (List)layers.get(l);
      if (insertEvent(e, layer)) return;
      // continue
    }
    
    // create a new layer
    List layer = new LinkedList();
    layers.add(layer);
    layer.add(e);
    
    // done
  }
  
  /**
   * Insert the Event into a layer
   * @return whether that was successfull
   */
  private final boolean insertEvent(Event candidate, List layer) {
    // loop through layer
    ListIterator events = layer.listIterator();
    do {
      Event event = (Event)events.next();
      // before?
      if (candidate.to+timeAfterEvent<event.from-timeBeforeEvent) {
        events.previous();
        events.add(candidate);
        return true;
      }
      // overlap?
      if (candidate.from-timeBeforeEvent<event.to+timeAfterEvent) 
        return false;
      // after?
    } while (events.hasNext());
    // after!
    events.add(candidate);
    return true;
  }
  
  /**
   * Helper transforming a point in time into a double value
   */
  private final double wrap(PropertyDate.PointInTime p) {
    return (double)p.getYear(0) + ((double)p.getMonth(1)-1)/12 + ((double)p.getDay(0)/12/31);
  }
  
  /**
   * An event in our model
   */
  /*package*/ class Event {
    /** state */
    /*package*/ double from, to;
    /*package*/ PropertyEvent pe;
    /*package*/ PropertyDate pd;
    /*package*/ String content;
    /** 
     * Constructor
     */
    Event(PropertyEvent propEvent, PropertyDate propDate, double start, double end) {
      // remember
      pe = propEvent;
      pd = propDate;
      from  = start;
      to  = end;
      // calculate content
      content();
      // done
    }
    /** 
     * calculate a content
     */
    private final void content() {
      Entity e = pe.getEntity();
      content = e instanceof Indi ? content((Indi)e) : content((Fam)e);
    }
    /**
     * calculate the content
     */
    private final String content(Indi indi) {
      return indi.getName();
    }
    /**
     * calculate the content
     */
    private final String content(Fam fam) {
      Indi 
        husband = fam.getHusband(),
        wife = fam.getWife();
      if (husband!=null&&wife!=null) return husband.getName() + "+" + wife.getName();
      if (husband!=null) return husband.getName();
      if (wife!=null) return wife.getName();
      return "@"+fam.getId()+"@";
    }
  } //Event
  
  /**
   * Interface for listeners
   */
  /*package*/ interface Listener {
    /**
     * callback for data changes
     */
    public void dataChanged();
    /**
     * callback for structure (and data) changes
     */
    public void structureChanged();
  } //ModelListener
  
} //TimelineModel 
