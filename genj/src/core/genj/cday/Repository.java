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
package genj.cday;

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
import java.util.List;

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
public class Repository {
  
  private final static String DIR = "./contrib/cday";
  
  private final static String[] SUFFIXES = {
    ".own", ".all", ".jan", ".feb", ".mar", ".apr", ".may", ".jun", ".jul", ".oct", ".sep", ".nov", ".dec" 
  };
  
  /** singleton */
  private static Repository instance;
  
  /** events */
  private List events = new ArrayList();
  
  /** 
   * Singleton Accessor 
   */
  public static Repository getInstance() {
    if (instance==null)
      instance = new Repository();
    return instance;
  }
  
  /**
   * Constructor
   */
  private Repository() {
    // load what we can find
    new Thread(new Loader()).start();
    // done for now
  }
  
  /**
   * Accessor - event by year
   */
  public Event getEvent(PointInTime when) {
    
    Event result = null;
    
    try {
      
	    // convert to julian day
	    long julian = when.getJulianDay();
	    
	    // loop over events
	    long delta = -1;
	    
	    synchronized (events) {
	      for (int i=0,j=events.size(); i<j; i++) {
	        Event event = (Event)events.get(i);
	        PointInTime pit = event.getTime();
	        // check year
	        if (pit.getYear()<when.getYear())
	          continue;
	        if (pit.getYear()>when.getYear())
	          break;
	        // calculate delta
	        long d = pit.getJulianDay()-julian;
	        if (d<0) d=-d;
	        if (delta<0||d<delta) {
	          delta = d;
	          result = event;
	        }
	      }
	    }
	    
    } catch (GedcomException e) {
    }
    
    // none found
    return result;
  }
  
  /**
   * Accessor - the list of events
   */
  public List getEvents() {
    return Collections.unmodifiableList(events);
  }

  /**
   * Accessor - the list of events
   */
  public void setEvents(List set) {
    synchronized (events) {
      // replace
      events.clear();
      events.addAll(set);
	    // sort them
      Collections.sort(events);
    }
  }

  /** debug */
  public static void main(String[] args) {
    getInstance();
  }
  
  /**
   * A loader for cday files in ./cday
   */  
  private class Loader implements Runnable {
    
    private List result = new ArrayList(1000);
    
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
		  
	    Debug.log(Debug.INFO, Repository.this, "Found "+files.length+" CDay file(s) in "+dir.getAbsoluteFile());
		  if (files.length==0) 
		    return;
		  
		  // load each one
		  for (int f = 0; f < files.length; f++) {
		    File file = files[f];
		    if (isGoodSuffix(file)) {
    	    Debug.log(Debug.INFO, Repository.this, "Loading "+file.getAbsoluteFile());
    	    try {
	          load(file, charset);
	        } catch (IOException e) {
	    	    Debug.log(Debug.WARNING, Repository.this, "IO Problem reading "+file.getAbsoluteFile(), e);
	        }
		    }
      }
		  
	    Debug.log(Debug.INFO, Repository.this, "Loaded "+result.size()+" events");

	    // tell about it
	    setEvents(result);
	    
	    Debug.flush();
	    
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
		  
		  // read its lines
		  BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
		  for (String line = in.readLine(); line!=null; line = in.readLine())
		    load(line);
		  
		  // done
		}
		
		/**
		 * load one line
		 */
		private boolean load(String line) {
		  
		  // check format (B|S)MMDDYYYY some event
		  if (line.length()<11)
		    return false;
		  
		  // check prefix
		  boolean birthday;
		  switch (line.charAt(0)) {
		    case 'B': birthday = true; break;
		    case 'S': birthday = false; break;
		    default : return false;
		  }
		  
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

		  // instantiate
		  try {
		    result.add(new Event(birthday, new PointInTime(day-1, month-1, year), text)); 
		  } catch (GedcomException e) {
		    return false;
		  }
		  
		  // done
		  return true;
		}
		
  } //Loader
  
} //CDay
