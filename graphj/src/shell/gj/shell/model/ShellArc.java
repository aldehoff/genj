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
package gj.shell.model;

import gj.awt.geom.Path;
import gj.model.Node;
import gj.util.ArcHelper;

import java.awt.geom.Point2D;

/**
 * Arc for Shell
 */
public class ShellArc implements gj.model.Arc {
  
  private Path path = new Path();
  
  private ShellNode start, end;
  
  private ShellGraph graph;
  
  /**
   * Constructor
   */
  protected ShellArc(ShellGraph grAph, ShellNode from, ShellNode to) {
    graph = grAph;
    start = from;
    end   = to;
    ArcHelper.update(this);
  }
  
  /**
   * Translate
   */
  protected void translate(Point2D delta) {
    path.translate(delta);
  }
  
  /**
   * Remove it
   */
  public void delete() {
    graph.removeArc(this);
    start.removeArc(this);
    end.removeArc(this);
  }
  
  /**
   * @see gj.model.Arc#getStart()
   */
  public Node getStart() {
    return start;
  }

  /**
   * @see gj.model.Arc#getEnd()
   */
  public Node getEnd() {
    return end;
  }

  /**
   * @see gj.model.Arc#getPath()
   */
  public Path getPath() {
    return path;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return start+">"+end;
  }

} //ShellArc