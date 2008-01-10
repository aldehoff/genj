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
 * A Tree like a Graph contains Nodes and Arcs. A Tree also
 * <il> 
 *  <li>contains a designated root (unless empty)
 *  <li>is acyclic so for a given edge[x,z] there is no 
 *      path{edge[x,a]>edge[a,b]>edge[b,.]>...>edge[.,y]>edge[y,z]}
 *  <li>here is a spanning Tree so for any verteces x,z there is a
 *      path{edge[x,a]>edge[a,b]>edge[b,.]>...>edge[.,y]>edge[y,z]}
 * </il>
 */
public interface Tree extends Graph {

  /**
   * Access to the root of a tree
   */
  public Object getRoot();
    
} //Tree
 