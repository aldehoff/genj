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

import gj.model.Factory;
import gj.model.Graph;

import java.awt.geom.Rectangle2D;

/**
 * GraphFactory - a Tree
 */
public class EmptyFactory extends AbstractGraphFactory {

  /**
   * @see gj.model.factory.AbstractGraphFactory#create(gj.model.Factory, java.awt.geom.Rectangle2D)
   */
  public Graph create(Factory factory, Rectangle2D bounds) {
    return factory.createGraph();
  }

} //EmptyFactory
