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
package gj.layout;

import gj.model.Graph;

/**
 * What a layout is all about - Implementors provide layout
 * functionality in <i>applyTo(Graph graph)</i>. 
 */
public interface Layout {

  /** 
   * Applies the layout to a given graph
   */
  public void layout(Graph graph) throws LayoutException;
  
}
