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
package gj.layout.hierarchical;

import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;
import gj.model.impl.MutableGraphImpl;
import gj.util.ArcIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * An acyclic Graph that will reverse arcs when addition
 * would lead to directed cycle
 */
/*package*/ class AcyclicGraph extends MutableGraphImpl {
  
  /** arcs that were reversed */
  private List reversed;
  
  /** 
   * Constructor
   */
  /*package*/ AcyclicGraph(Graph graph) {
    super(graph);
  }
  
  /**
   * We make sure that this graph stays acyclic by
   * reversing arcs where necessary
   * 
   * @see gj.model.MutableGraph#createArc(Node, Node, Path)
   */
  public Arc createArc(Node from, Node to, Path path) {
    Arc result;
    // reversed if there is a path from-to already
    if (isPath(to,from)) {
      result = super.createArc(to, from, path);
      getReversedArcs().add(result);
    } else {
      result = super.createArc(from, to, path);
    }
    return result;
  }

  /**
   * Checks whether there is a path from-to
   */
  private boolean isPath(Node from, Node to) {
    ArcIterator it = new DirectedArcIterator(from, false);
    while (it.next()) {
      // no need to check path-segment twice
      if (!it.isFirst) continue;
      if (it.isLoop) continue;
      // trivial case?
      if ((it.arc.getStart()==from)&&(it.arc.getEnd()==to)) return true;
      // recursive step!
      if (isPath(it.arc.getEnd(), to)) return true;
      // next
    }
    return false;
  }
  
  /**
   * Returns the arcs that were reversed to avoid
   * directed cycles
   */
  public List getReversedArcs() {
    if (reversed==null) {
      reversed = new ArrayList();
    }
    return reversed;
  }
  
  /**
   * @see gj.model.MutableGraph#removeArc(Arc)
   */
  public void removeArc(Arc arc) {
    
    // is it in our list of reversed arcs?
    reversed.remove(arc);
    
    // delegate to super
    super.removeArc(arc);
  }

}
 