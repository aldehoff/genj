/**
 * GraphJ
 * 
 * Copyright (C) 2002 Nils Meier
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package gj.util;

import gj.model.Arc;
import gj.model.Node;
import java.util.Iterator;
import java.util.List;

/**
 * An iterator for looping over Arcs
 */
public class ArcIterator {
  
  /** the arcs we've covered already */
  private Arc[] done;
  
  /** the arcs */
  private Iterator arcs;
  
  /** the current arc */
  public Arc arc = null;
  
  /** the current's meta information */
  public int i;
  public boolean isLoop;
  public boolean isFirst;
  
  /** index */
  private int index = 0;
  
  /**
   * Constructor
   */
  public ArcIterator(List arcs) {
    done = new Arc[arcs.size()];
    this.arcs = arcs.iterator();
  }
  
  /**
   * Constructor
   */
  public ArcIterator(Node node) {
    this(node.getArcs());
  }
  
  /**
   * helper - isDup(a,b) if a.start==b.start && a.end=b.end (or reversed)
   */
  public final boolean isDup(Arc other) {
    if ((arc==null)||(other==null)) return false;
    if ((arc.getStart()==other.getStart())&&(arc.getEnd()==other.getEnd  ())) return true;
    if ((arc.getStart()==other.getEnd  ())&&(arc.getEnd()==other.getStart())) return true;
    return false;    
  }
  
  /**
   * Returns the next
   */
  public boolean next() {
    
    // anything more?
    if (!arcs.hasNext())
      return false;
  
    // the next
    arc = (Arc)arcs.next();
    i    = 0;
    
    // analyze the arcs we've iterated over already
    for (int a=0;a<index;a++) {
      // .. arc could be same twice (loop) -> ignore
      if (done[a]==arc) return next();
      // .. arc could be dup of existing
      if (isDup(done[a])) { i++; }
    }
    done[index++]=arc;
    
    // flags
    isFirst = i==0;
    isLoop = arc.getStart()==arc.getEnd();

    // done
    return true;
  }
  
}
