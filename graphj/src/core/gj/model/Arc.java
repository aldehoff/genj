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
package gj.model;

import gj.awt.geom.Path;

/**
 * An Arc connects two points and contains a Path
 */
public interface Arc {

  /**
   * the start of the Arc - immutable
   */
  public Node getStart();
  
  /**
   * the end of the arc - immutable
   */
  public Node getEnd();

  /**
   * the contained path - mutable
   */
  public Path getPath();
    
}
