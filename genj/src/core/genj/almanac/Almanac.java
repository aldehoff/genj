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
package genj.almanac;

import genj.gedcom.GedcomException;
import genj.gedcom.time.PointInTime;
import genj.util.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class adds support for a CDAY style event repository with
 * entries one per line. This code respects births (B) and event (S)
 * with a date-format of MMDDYYYY.
 * <code>
 *  B01011919 J. D. Salinger, author of 'Catcher in the Rye'.
 *  S04121961 Cosmonaut Yuri Alexeyevich Gagarin becomes first man in orbit.
 * </code>
 * The files considered as input have to reside in ./contrib/cday and end in
 * <code>
 *  .own
 *  .all
 *  .jan, .feb, .mar, .apr, .may, .jun, .jul, .oct, .sep, .nov, .dec
 * </code>
 * @see http://cday.sourceforge.net
 */
public class Almanac {
  
  private final static String DIR = "./contrib/cday";
  
  private final static String[] SUFFIXES = {
    ".own", ".all", ".jan", ".feb", ".mar", ".apr", ".may", ".jun", ".jul", ".oct", ".sep", ".nov", ".dec" 
  };
  
  /** listeners */
  private List listeners = new ArrayList(10);
  
  /** singleton */
  private static Almanac instance;
  
  /** events */
  private List events = new ArrayList();
  
  /** libraries */
  private List libraries = new ArrayList();
  
  /** categories */
  private List categories = new ArrayList();
  
  /** 
   * Singleton Accessor 
   */
  public static Almanac getInstance() {
    if (instance==null)
      instance = new Almanac();
    return instance;
  }
  
  /**
   * Constructor
   */
  private Almanac() {
    // load what we can find
    new Thread(new Loader()).start();
    // done for now
  }
  
  /**
   * Add a change listener
   */
  public void addChangeListener(ChangeListener l) {
    listeners.add(l);
  }
  
  /**
   * Remove a change listener
   */
  public void removeChangeListener(ChangeListener l) {
    listeners.remove(l);
  }
  
  /**
   * Update listeners
   */
  protected void fireStateChanged() {
    ChangeEvent e = new ChangeEvent(this);
    ChangeListener[] ls = (ChangeListener[])listeners.toArray(new ChangeListener[listeners.size()]);
    for (int l = 0; l < ls.length; l++) {
      ls[l].stateChanged(e);
    }
  }
  
  /**
   * Accessor - categories
   */
  public List getCategories() {
    return categories;
  }
  
  /**
   * Accessor - category
   */
  public Category getCategory(String key) {
    for (int c=0; c<categories.size(); c++) {
      Category cat = (Category)categories.get(c);
      if (cat.getKey().equals(key))
        return cat;
    }
    Category cat = new Category(key);
    categories.add(cat);
    return cat;
  }
  
  /**
   * Accessor - libraries
   */
  public List getLibraries() {
    return libraries;
  }
  
  /**
   * Accessor - events by point in time
   */
  public Iterator getEvents(PointInTime when, int days) throws GedcomException {
    return new Range(when, days);
  }
  
  /**
   * Accessor - a range of events
   */
  public Iterator getEvents(int startYear, int endYear) {
    return new Range(startYear, endYear);
  }
  
  /**
   * A loader for cday files in ./cday
   */  
  private class Loader implements Runnable {
    
		/**
		 * async load
		 */
		public void run() {
		  
		  // prepare the charset for loading
		  Charset charset = Charset.forName("ISO-8859-1");

		  // look into dir
		  File[] files;
		  
		  File dir = new File(DIR);
		  if (!dir.exists()||!dir.isDirectory())
		    files = new File[0];
		  else
		    files = dir.listFiles();
		  
	    Debug.log(Debug.INFO, Almanac.this, "Found "+files.length+" CDay file(s) in "+dir.getAbsoluteFile());
		  if (files.length==0) 
		    return;
		  
		  // load each one
		  for (int f = 0; f < files.length; f++) {
		    File file = files[f];
		    if (isGoodSuffix(file)) {
    	    Debug.log(Debug.INFO, Almanac.this, "Loading "+file.getAbsoluteFile());
    	    try {
	          load(file, charset);
	        } catch (IOException e) {
	    	    Debug.log(Debug.WARNING, Almanac.this, "IO Problem reading "+file.getAbsoluteFile(), e);
	        }
		    }
      }
		  
	    Debug.log(Debug.INFO, Almanac.this, "Loaded "+events.size()+" events");
      
		  // done
		}    
		
		/**
		 * check a suffix for applicability
		 */
		private boolean isGoodSuffix(File file) {
		  
		  // check suffixes
		  String name = file.getName().toLowerCase();
		  for (int s = 0; s < SUFFIXES.length; s++) {
        if (name.endsWith(SUFFIXES[s]))
          return true;
      }
		  
		  // not good
		  return false;
		}
		
		/**
		 * load one file
		 */
		private void load(File file, Charset charset) throws IOException {
		  
		  String lib = file.getName();
      libraries.add(lib);
		  
		  // read its lines
		  BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
		  for (String line = in.readLine(); line!=null; line = in.readLine()) 
		    load(lib, line);
		    
		  // notify about changes
      fireStateChanged(); 
		  
		  // done
		}
		
		/**
		 * load one line
		 */
		private boolean load(String lib, String line) {
		  
		  // check format (B|S)MMDDYYYY some event
		  if (line.length()<11)
		    return false;
		  
		  // grab category
      String c = line.substring(0,1);
		  
		  // check date
		  int day, month, year;
		  try {
			  month = Integer.parseInt(line.substring(1, 3));
			  day   = Integer.parseInt(line.substring(3, 5));
			  year  = Integer.parseInt(line.substring(5, 9));
		  } catch (NumberFormatException e) {
		    return false;
		  }
		  
		  // check text
		  String text = line.substring(10).trim();
		  if (text.length()==0)
		    return false;

      // create event
      Event event;
      try {
		    event = new Event(lib, getCategory(c), new PointInTime(day-1, month-1, year), text); 
		  } catch (GedcomException e) {
		    return false;
		  }
        
      // Search for correct index
      int index = Collections.binarySearch(events, event);
      if (index < 0) 
        index = -index-1;
      
		  // instantiate
      synchronized (events) {
        events.add(index, event);
      }
      
		  // done
		  return true;
		}
		
  } //Loader
  
  /**
   * An iterator over a range of events
   */
  private class Range implements Iterator {
    
    private int start, end;
    
    private int endYear;
    
    private long origin = -1;
    private long originDelta;
    
    private Object next;
    
    /**
     * Constructor
     */
    Range(PointInTime when, int days) throws GedcomException {

      endYear = when.getYear();
      
	    // convert to julian day
	    origin = when.getJulianDay();
	    originDelta = days;
  	    
	    synchronized (events) {
	      end = events.size();
	      start = getStartIndex(endYear);
        hasNext();
	    }
	    
      // done
    }
    
    /**
     * Constructor
     */
    Range(int startYear, int endYear) {
      
      this.endYear = endYear;
      
	    synchronized (events) {
	      end = events.size();
	      start = getStartIndex(startYear);
        hasNext();
	    }
    }
    
    /**
     * end
     */
    boolean end() {
      next = null;
      start = end;
      return false;
    }
    
    /**
     * set next
     */
    private boolean setNext(Object event) {
      next = event;
      return true;
    }
    
    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      // one waiting?
      if (next!=null)
        return true;
      // sync'up
      synchronized (events) {
        // events changed?
        if (events.size()!=end)
          return end();
	      // check Event
	      while (true) {
	        // reached the end?
	        if (start==end)
	          return end();
	        // it's still in year range?
		      Event e = (Event)events.get(start++);
		      if (e.getTime().getYear()>endYear) 
		        return end();
		      // check against origin?
		      if (origin<0) {
		        return setNext(e);
		      } else {
		        long delta = e.getJulian() - origin;
		        if (delta>originDelta) 
			        return end();
		        if (delta>-originDelta)
			        return setNext(e);
		      }
	      }
      }
    }
    
    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
      if (next==null&&!hasNext())
        throw new IllegalArgumentException("no next");
      Object result = next;
      next = null;
      return result;
    }
    
    /**
     * n/a
     * @see java.util.Iterator#remove()
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    /**
     * find start index of given year in events (log n)
     */
  	private int getStartIndex(int year) {
  	  if (events.isEmpty())
  	    return 0;
      return getStartIndex(year, 0, events.size()-1);
    }
    private int getStartIndex(int year, int start, int end) {
      
      // no range?
      if (end==start)
        return start;

      int pivot = (start + end)/2;

      int y = ((Event)events.get(pivot)).getTime().getYear();
      if (y<year)
        return getStartIndex(year, pivot+1, end);
      return getStartIndex(year, start, pivot);
    }
    
  } //Range
  
} //CDay
