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
package gj.ui;

import gj.layout.Layout;
import gj.model.Arc;
import gj.model.Graph;
import gj.model.Node;

/**
 * Interface to implementor knowing how to render a graph's layout information
 */
public interface LayoutRenderer {

  /**
   * Callback for rendering a Graph's layout information
   * @param graph the graph to render
   * @param layout the layout that has been applied to the graph (might be null)
   * @param graphics the GraphGraphics to use
   */
  public void render(Graph graph, Layout layout, UnitGraphics graphics);
    
} //LayoutRenderer
