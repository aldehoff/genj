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
package gj.layout.tree;

import gj.model.Node;

/**
 * Options for nodes
 */
public interface NodeOptions {

  /**
   * Callback - Padding of a node (n,w,e,s)
   */
  public int[] getPadding(Node node, Orientation o);
  
  /**
   * Callback - Calculate node's longitude above children
   * @param node the node that the callback is for
   * @param children the node's children
   * @param min the minimum longitude to keep the node and its shape above its children
   * @param max the maximum longitude to keep the node and its shape above its children
   */
  public int getLongitude(Node node, Branch[] children, Orientation o);
    
} //NodeOptions
