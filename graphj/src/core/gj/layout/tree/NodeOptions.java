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

  public final static int
    NORTH = 0,
    EAST  = 1,
    SOUTH = 2,
    WEST  = 3;
    
  /**
   * Padding of a node
   */
  public double getPadding(Node node, int dir);
  
  /**
   * Latitude of a node
   */
  public double getLatitude(Node node, double min, double max);
  
  /**
   * Longitude of a node
   */
  public double getLongitude(Node node, double minc, double maxc, double mint, double maxt);
    
} //NodeOptions
