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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyEvent;

/**
 * A model that wraps the Gedcom information in a timeline fashion
 */
/*package*/ class Model {

  /** the gedcom we're looking at */
  /*package*/ Gedcom gedcom;
  
  /** limits */
  /*package*/ double 
    max = Double.NaN,
    min = Double.NaN;

  /** a filter for events that we're interested in */
  private Set filter;
  
  /** default filter */
  private final static String[] DEFAULT_FILTER = { "BIRT", "MARR", "RESI", "EMIG" };
    
  /** our levels */
  /*package*/List layers;
  
  /** time per event */
  private double timePevent = 1/2;
  
  /**
   * Constructor
   */
  /*package*/ Model(Gedcom gedcom, double timePevent) {
    
    // remember
    this.gedcom = gedcom;
    this.timePevent = timePevent;
    
    // setup default filter
    filter = new HashSet();
    for (int f=0; f<DEFAULT_FILTER.length; f++) {
      filter.add(DEFAULT_FILTER[f]);
    }
    
    // gather events for the 1st time
    createEvents();
    
    // done
  }
  
  /**
   * change time per event
   */
  /*package*/ void setTimePerEvent(double set) {
    timePevent = set;
    createEvents();
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
    
    // recheck trailing events in layers
    checkTrailingEvents();
    
    // nothing found -> fallback to this year
    if (layers.size()==0) {
      min = Calendar.getInstance().get(Calendar.YEAR);
      max = min+(double)11/12;
    }
    
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
    PropertyDate date = pe.getDate();
    if (date==null) return;
    // get start (has to be valid) and end
    PropertyDate.PointInTime 
      start = date.getStart(),
      end   = date.getEnd();
    if (!start.isValid()) return;
    if (!end  .isValid()) end = start;
    // create the Event
    Event e = new Event(pe, wrap(start), wrap(end));
    // remember min and max
    min = Math.min(Math.floor(e.from), min);
    max = Math.max(Math.ceil (e.to  ), max);
    // keep the event
    insertEvent(e);
    // done
  }
  
  /**
   * Insert the Event into one of our layers
   */
  private final void insertEvent(Event e) {
    
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
      if (candidate.to<event.from-timePevent) {
        events.previous();
        events.add(candidate);
        return true;
      }
      // overlap?
      if (candidate.from<event.to+timePevent) 
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
   * Check trailing events - we don't want them closer
   * than timePerEvent*2 to the previous one
   */
  private final void checkTrailingEvents() {
    // loop layers
    for (int l=0;l<layers.size();l++) {
      LinkedList layer = (LinkedList)layers.get(l);
      Event event = (Event)layer.getLast();
    }
    // done
  }
  
  /**
   * An event in our model
   */
  /*package*/ class Event {
    /** state */
    /*package*/ double from, to;
    /*package*/ PropertyEvent prop;
    /*package*/ String tag;
    /** 
     * Constructor
     */
    Event(PropertyEvent propEvent, double start, double end) {
      // remember
      prop = propEvent;
      from  = start;
      to  = end;
      // calculate tag
      Entity e = propEvent.getEntity();
      tag = e instanceof Indi ? tag((Indi)e) : tag((Fam)e);
      tag = tag + '(' + propEvent.getDate() + ')';
      // done
    }
    /**
     * calculate the tag
     */
    private final String tag(Indi indi) {
      return indi.getName();
    }
    /**
     * calculate the tag
     */
    private final String tag(Fam fam) {
      Indi 
        husband = fam.getHusband(),
        wife = fam.getWife();
      if (husband!=null&&wife!=null) return husband.getName() + " and " + wife.getName();
      if (husband!=null) return husband.getName();
      if (wife!=null) return wife.getName();
      return "@"+fam.getId()+"@";
    }
  } //Event
  
} //TimelineModel 
