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
package gj.model.factory;

import gj.awt.geom.ShapeHelper;
import gj.model.MutableGraph;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphFactory - a Tree
 */
public class EmptyFactory extends AbstractFactory {

  /**
   * @see Factory#create(MutableGraph, Shape)
   */
  public void create(MutableGraph graph, Shape nodeShape) {
  }
  
  /**
   * @see IGraphFactory#getName()
   */
  public String getName() {
    return "Empty";
  }

}
