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
   * Padding of a node
   */
  public Padding getPadding(Node node);
  
  /**
   * Alignment of a node
   */
  public Alignment getAlignment(Node node);
  
  /**
   * Padding of a node
   */
  public class Padding {
    public double north, south, west, east;
  } //Padding
  
  /**
   * Alignment of a node
   */
  public class Alignment {
    public double lat, lon;
  } //Alignment
  
} //NodeOptions
