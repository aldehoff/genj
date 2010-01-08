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
package genj.search;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.TagPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background search worker
 */
/*package*/ class Worker {
  
  /** max # hits */
  private final static int MAX_HITS = 255;
  
  /** one listener */
  private WorkerListener listener;
  
  /** current search state */
  private Gedcom gedcom;
  private TagPath tagPath;
  private Matcher matcher;
  private Set<Entity> entities = new HashSet<Entity>();
  private List<Hit> hits = new ArrayList<Hit>(MAX_HITS);
  private int hitCount = 0;
  
  /** thread */
  private Thread thread;
  private AtomicBoolean lock = new AtomicBoolean(false);
  private long lastFlush;
  
  /*package*/ Worker(WorkerListener listener) {
    this.listener = listener;
  }
  
  /** cancel current ongoing search */
  /*package*/ void stop() {

    synchronized (lock) {
      try {
        lock.set(false);
        if (thread!=null) thread.interrupt();
      } catch (Throwable t) {
      }
    }
    
    // done
  }

  /** start search */
  /*package*/ void start(Gedcom gedcom, TagPath path, String value, boolean regexp) {
    
    // sync up
    synchronized (lock) {
      
      // bail if already running
      if (thread!=null)
        throw new IllegalStateException("can't start while running");

      
      // prepare matcher & path
      this.gedcom = gedcom;
      this.matcher = getMatcher(value, regexp);
      this.tagPath = null;
      this.hits.clear();
      this.entities.clear();
      this.hitCount = 0;
      
      lock.set(true);

      thread = new Thread(new Runnable() {
        public void run() {
          try {
            Worker.this.listener.started();
            search(Worker.this.gedcom);
            flush();
          } catch (Throwable t) {
            Logger.getLogger("genj.search").log(Level.FINE, "worker bailed", t);
          } finally {
            synchronized (lock) {
              thread = null;
              lock.set(false);
              lock.notifyAll();
            }
            Worker.this.listener.stopped();
          }
        }
      });
      thread.setDaemon(true);
      thread.start();
    }
    
    // done
  }
  
  /** search in gedcom (not on EDT) */
  private void search(Gedcom gedcom) {
    for (int t=0; t<Gedcom.ENTITIES.length && hitCount<MAX_HITS; t++) {
      for (Entity entity : gedcom.getEntities(Gedcom.ENTITIES[t])) {
        
        // next
        search(entity, entity, 0);

        // still going?
        if (!lock.get())
          return;
      }
    }
  }

  private void flush() {
    // still more data to report?
    if (!hits.isEmpty()) {
      listener.more(Collections.unmodifiableList(hits));
      hits.clear();
    }
  }
  
  /** search property (not on EDT) */
  private void search(Entity entity, Property prop, int pathIndex) {
    // got a path?
    boolean searchThis = true;
    if (tagPath!=null) {
      // break if we don't match path
      if (pathIndex<tagPath.length()&&!tagPath.get(pathIndex).equals(prop.getTag())) 
        return;
      // search this if path is consumed 
      searchThis = pathIndex>=tagPath.length()-1;
    }
    // parse all but transients
    if (searchThis&&!prop.isTransient()) {
      // check entity's id
      if (entity==prop)
        search(entity, entity, entity.getId(), true);
      // check prop's value
      search(entity, prop, prop.getDisplayValue(), false);
    }
    // check subs
    int n = prop.getNoOfProperties();
    for (int i=0;i<n;i++) {
      search(entity, prop.getProperty(i), pathIndex+1);
    }
    // done
  }

  /** search property's value */
  private void search(Entity entity, Property prop, String value, boolean isID) {
    // look for matches
    Matcher.Match[] matches = matcher.match(value);
    if (matches.length==0)
      return;
    // too many?
    if (hitCount>=MAX_HITS)
      return;
    // keep entity
    entities.add(entity);
    // create a hit
    Hit hit = new Hit(prop, value, matches, entities.size(), isID);
    // keep it
    hits.add(hit);
    hitCount++;
    // sync every 500ms
    long now = System.currentTimeMillis();
    if (now-lastFlush>500) 
      flush();
    lastFlush = now;
    // done
  }

  /**
   * Returns a matcher for given pattern and regex flag
   */
  private Matcher getMatcher(String pattern, boolean regex) {

    Matcher result = regex ? (Matcher)new RegExMatcher() : (Matcher)new SimpleMatcher();
    
    // init
    result.init(pattern);
    
    // done
    return result;
  }

}
