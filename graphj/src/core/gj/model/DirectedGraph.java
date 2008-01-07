/**
 * This file is part of GraphJ
 * 
 * Copyright (C) 2002-2004 Nils Meier
 * 
 * GraphJ is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * GraphJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphJ; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package gj.model;


/**
 * A directed graph (or Digraph)
 */
public interface DirectedGraph extends Graph {
  
  /**
   * @see DirectedGraph#getDirectPredecessors(Object)
   */
  int getNumDirectPredecessors(Object vertex);
  
  /**
   * An arc e = (x,y) is considered to be directed from x to y; y is called the head and x is called the 
   * tail of the arc; y is said to be a direct successor of x, and x is said to be a direct predecessor of y. 
   * If a path leads from x to y, then y is said to be a successor of x, and x is said to be a predecessor 
   * of y. The arc (y,x) is called the arc (x,y) inverted. 
   */
  public Iterable<?> getDirectPredecessors(Object vertex);
  
  /**
   * @see DirectedGraph#getDirectSuccessors(Object)
   */
  int getNumDirectSuccessors(Object vertex);
  
  /**
   * An arc e = (x,y) is considered to be directed from x to y; y is called the head and x is called the 
   * tail of the arc; y is said to be a direct successor of x, and x is said to be a direct predecessor of y. 
   * If a path leads from x to y, then y is said to be a successor of x, and x is said to be a predecessor 
   * of y. The arc (y,x) is called the arc (x,y) inverted. 
   */
  public Iterable<?> getDirectSuccessors(Object vertex);
  
  /**
   * Direction
   */
  public int getDirectionOfEdge(Object edge, Object vertex);
  
} //DirectedGraph
