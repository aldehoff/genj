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

import genj.gedcom.Gedcom;
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
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A global Almanac for all kinds of historic events
 */
public class Almanac {
  
  /** language we use for events */
  private final static String LANG = Locale.getDefault().getLanguage();
  
  /** listeners */
  private List listeners = new ArrayList(10);
  
  /** singleton */
  private static Almanac instance;
  
  /** events */
  private List events = new ArrayList();
  
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
    // load what we can find async
    new Thread(new Runnable() {
      public void run() {
        new CDayLoader().load();
        new AlmanacLoader().load();
  	    Debug.log(Debug.INFO, Almanac.this, "Loaded "+events.size()+" events");
      }
    }).start();
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
   * Accessor - events by point in time
   */
  public Iterator getEvents(PointInTime when, int days, Set cats) throws GedcomException {
    return new Range(when, days, cats);
  }
  
  /**
   * Accessor - a range of events
   */
  public Iterator getEvents(int startYear, int endYear, Set cats) {
    return new Range(startYear, endYear, cats);
  }
  
  /**
   * A loader for almanac files
   */  
  private abstract class Loader {
    
		/**
		 * async load
		 */
		protected void load() {
		  
		  // look into dir
		  File[] files;
		  
		  File dir = getDirectory();
		  if (!dir.exists()||!dir.isDirectory())
		    files = new File[0];
		  else
		    files = dir.listFiles();
		  
		  if (files.length==0) {
		    Debug.log(Debug.INFO, Almanac.this, "Found no file(s) in "+dir.getAbsoluteFile());
		    return;
		  }
		  
		  // load each one
		  for (int f = 0; f < files.length; f++) {
		    File file = files[f];
		    if (filter(file)) {
    	    Debug.log(Debug.INFO, Almanac.this, "Loading "+file.getAbsoluteFile());
    	    try {
	          load(file);
	        } catch (IOException e) {
	    	    Debug.log(Debug.WARNING, Almanac.this, "IO Problem reading "+file.getAbsoluteFile(), e);
	        }
		    }
      }
		  
		  // done
		}    
		
		/**
		 * load one file
		 */
		protected void load(File file) throws IOException {
		  
		  // read its lines
		  BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), getCharset()));
		  for (String line = in.readLine(); line!=null; line = in.readLine())  {
		    
		    try {
			    Event event = load(line);
			    if (event!=null) {
			      // Search for correct index
			      int index = Collections.binarySearch(events, event);
			      if (index < 0) 
			        index = -index-1;
					  // keep
			      synchronized (events) {
			        events.add(index, event);
			      }
			    }
		    } catch (Throwable t) {
		    }
		  
		    // next
		  }
		    
		  // notify about changes
      fireStateChanged(); 
		  
		  // done
		}
		
		/**
		 * load one line and create an Event (or null)
		 */
		protected abstract Event load(String line) throws GedcomException ;
		
		/**
		 * check a suffix for applicability
		 */
		protected abstract boolean filter(File file);

		/**
		 * resolve directory to look for files in
		 */
    protected abstract File getDirectory();
    
    /**
     * resolve charset
     */
    protected abstract Charset getCharset();
    
  } //Loader
 
  /**
	 * This class adds support for the ALMANAC style event repository
	 * (our own invention)
	 */
  private class AlmanacLoader extends Loader {
    /** only .almanac */
    protected boolean filter(File file) {
      return file.getName().toLowerCase().endsWith(".almanac");
    }
    /** look into ./contrib/almanac */
    protected File getDirectory() {
      return new File("./contrib/almanac");
    }
    /** create an event */
    protected Event load(String line) throws GedcomException {
      // comment?
      if (line.startsWith("#"))
        return null;
      // break it by ';'
      StringTokenizer cols = new StringTokenizer(line, ";", true);
      // #1 date YYYYMMDD
      String date = cols.nextToken().trim();cols.nextToken();
      if (date.length()<4)
        return null;
      int year  = Integer.parseInt(date.substring(0, 4));
      int month = date.length()>=6 ? Integer.parseInt(date.substring(4, 6)) : 0;
      int day   = date.length()>=8 ? Integer.parseInt(date.substring(6, 8)) : 0;
      PointInTime time = new PointInTime(day-1, month-1, year);
      if (!time.isValid())
        return null;
      // #2 date
      String date2 = cols.nextToken();
      if (!date2.equals(";")) {
        cols.nextToken();
      }
      // #3 country
      String country = cols.nextToken().trim();
      if (!country.equals(";")) {
        cols.nextToken();
      }
      // #4 significance
      int sig = Integer.parseInt(cols.nextToken());
      cols.nextToken();
      // #5 type
      List cats = new ArrayList();
      String type = cols.nextToken().trim();
      for (int c=0;c<type.length();c++) 
        cats.add(getCategory(type.substring(c,c+1)));
      if (cats.isEmpty())
        return null;
      cols.nextToken();
      // #6 and following description
      String desc = null;
      while (cols.hasMoreTokens()) {
        String translation = cols.nextToken().trim();
        if (translation.equals(";"))
          continue;
        int i = translation.indexOf('=');
        if (i<0)
          continue;
        String lang = translation.substring(0,i);
        if (desc==null||LANG.equals(lang)) 
          desc = translation.substring(i+1);
      }
      // got a description?
      if (desc==null)
        return null;
      // done
      return new Event(cats, time, desc);
    }
    /** charset */
    protected Charset getCharset() {
		  return Charset.forName("UTF-8");
    }
  } //AlmanacLoader
  
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
  private class CDayLoader extends Loader {
    
    private final String DIR = "./contrib/cday";
    
    private final String[] SUFFIXES = {
      ".own", ".all", ".jan", ".feb", ".mar", ".apr", ".may", ".jun", ".jul", ".oct", ".sep", ".nov", ".dec" 
    };
    
    /** our directory */
    protected File getDirectory() {
      return new File(DIR);
    }
    
    /** filter files */
    protected boolean filter(File file) {
		  // check suffixes
		  String name = file.getName().toLowerCase();
		  for (int s = 0; s < SUFFIXES.length; s++) {
        if (name.endsWith(SUFFIXES[s]))
          return true;
      }
		  // not good
		  return false;
    }
    
    /** create an event */
    protected Event load(String line) throws GedcomException {

		  // check format (B|S)MMDDYYYY some event
		  if (line.length()<11)
		    return null;
		  
		  // grab category
      String c = line.substring(0,1);
		  
		  // check date
		  int day, month, year;
		  month = Integer.parseInt(line.substring(1, 3));
		  day   = Integer.parseInt(line.substring(3, 5));
		  year  = Integer.parseInt(line.substring(5, 9));
		  
		  // check text
		  String text = line.substring(10).trim();
		  if (text.length()==0)
		    return null;
		  if (c.toLowerCase().startsWith("b"))
		    text = Gedcom.getName("BIRT") + ": " + text;
		  
		  // lookup category
		  List cats = Collections.singletonList(getCategory(c));

      // create event
		  return new Event(cats, new PointInTime(day-1, month-1, year), text); 
    }
    
    /** charset */
    protected Charset getCharset() {
		  return Charset.forName("ISO-8859-1");
    }
    
  } //CDAY
  
  /**
   * An iterator over a range of events
   */
  private class Range implements Iterator {
    
    private int start, end;
    
    private int endYear;
    
    private long origin = -1;
    private long originDelta;
    
    private Event next;

    private Set categories;
    
    /**
     * Constructor
     */
    Range(PointInTime when, int days, Set cats) throws GedcomException {

      endYear = when.getYear()+1;
      
	    // convert to julian day
	    origin = when.getJulianDay();
	    originDelta = days;
	    
	    // init
	    init(when.getYear()-1, cats);
  	    
      // done
    }
    
    /**
     * Constructor
     */
    Range(int startYear, int endYear, Set cats) {
      
      this.endYear = endYear;

      // init
      init(startYear, cats);
    }
    
    private void init(int startYear, Set cats) {
      
      categories = cats;
      
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
	        // here's the next
		      next = (Event)events.get(start++);
		      // good category?
		      if (categories!=null&&!next.isCategory(categories)) 
	          continue;
	        // it's still in year range?
		      if (next.getTime().getYear()>endYear) 
		        return end();
		      // check against origin?
		      if (origin>0) {
		        long delta = next.getJulian() - origin;
		        if (delta>originDelta) 
			        return end();
		        if (delta<-originDelta)
		          continue;
		      }
		      // found next
	        return true;
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
