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
package gj.model.impl;

import gj.awt.geom.Path;
import gj.model.Arc;
import gj.model.Node;

/**
 * @see gj.model.MutableArc
 */
public class ArcImpl implements Arc {
  
  /** starting Node */
  private NodeImpl start;

  /** ending Node */
  private NodeImpl end;
  
  /** path */
  private Path path;
  
  /**
   * Constructor
   */
  /*package*/ ArcImpl(NodeImpl start, NodeImpl end, Path path) {
    this.start = start;
    this.end = end;
    this.path = path;
  }
  
  /**
   * Disconnects
   */
  /*package*/ void disconnect() {
    start.removeArc(this);
    end  .removeArc(this);
  }

  /**
   * String represenation
   */
  public String toString() {
    return start.toString() + ">" + end.toString();
  }
  
  /**
   * @see Arc#getPath()
   */
  public Path getPath() {
    return path;
  }
  
  /**
   * @see Arc#getStart()
   */
  public Node getStart() {
    return start;
  }

  /**
   * @see Arc#getEnd()
   */
  public Node getEnd() {
    return end;
  }

}
