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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * An iterator for looping over Arcs from a source
 */
public class ArcIterator {
  
  /** the arcs we've covered already */
  private List arcs;
  
  /** the nodes we've visited already */
  private List visited;
  
  /** the arcs */
  private ListIterator it;
  
  /** the source (fixed) and destination */
  public Node source, dest;
  
  /** the current arc */
  public Arc arc;
  
  /** the current's meta information */
  public boolean isLoop;
  public boolean isFirst;
  
  /**
   * Constructor
   */
  public ArcIterator(Node sOurce) {
    source = sOurce;
    arcs = source.getArcs();
    visited = new ArrayList(arcs.size());
    it = arcs.listIterator();
  }
  
  /**
   * Returns the next
   */
  public boolean next() {
    
    // no more?
    if (!it.hasNext()) return false;
  
    // the next
    arc = (Arc)it.next();
    isFirst = true;
    
    // not twice (loop) -> ignore
    if (arcs.indexOf(arc)<it.previousIndex()) return next();
    
    // .. arc could be dup of existing -> second or more
    dest = ModelHelper.getOther(arc, source);
    isFirst = !visited.contains(dest);
    if (isFirst) visited.add(dest);
    
    // loop?
    isLoop = source==dest;

    // done
    return true;
  }
  
} //ArcIterator
