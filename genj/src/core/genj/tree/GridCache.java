/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.tree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * GridCache - caching information in a 2d grid */
public class GridCache {
  
  /** an empty list */
  private final static List EMPTY = new ArrayList(0);

  /** the grids */
  private Object[][] grid;
 
  /** the system we're spanning */
  private Rectangle2D system;

  /** the resolution we're using */
  private double resolution;
  
  /**
   * Constructor
   */
  public GridCache(Rectangle2D syStem, double resolUtion) {
    // remember
    system = syStem;
    resolution = resolUtion;
    // calc rows/cols
    int 
      cols = (int)Math.ceil(system.getWidth ()/resolution),
      rows = (int)Math.ceil(system.getHeight()/resolution);
    // init grids
    grid = new Object[rows][cols];
    // done
  }
  
  /**
   * Adds an Object to the grid
   */
  public void put(Object object, Point2D p) {
    // safety check
    if (!system.contains(p)) 
      throw new IllegalArgumentException("Object outside system");
    // calc row/col
    int
      col = (int)Math.floor((p.getX()-system.getMinX())/resolution),
      row = (int)Math.floor((p.getY()-system.getMinY())/resolution);
    // keep it
    Object old = grid[row][col]; 
    if (old==null) {
      // keep as simple entry if possible
      grid[row][col] = object;
    } else {
      // add to existing list or create new list
      if (old instanceof List) {
        ((List)old).add(object);
      } else {
        List l = new ArrayList(8);
        l.add(old);
        l.add(object);
        grid[row][col] = l;
      }
    }
    // done
  }

  /**
   * Gets objects by coordinate
   */
  public List get(Rectangle2D range) {
    
    // safety check
    range = system.createIntersection(range);
    // prepare a result
    List result = new ArrayList(10);
    
    // calc row/col
    int
      col = (int)Math.floor((range.getMinX()-system.getMinX())/resolution),
      row = (int)Math.floor((range.getMinY()-system.getMinY())/resolution),
      ecol = (int)Math.ceil((range.getMaxX()-system.getMinX())/resolution),
      erow = (int)Math.ceil((range.getMaxY()-system.getMinY())/resolution);

    // grab the ones we can find
    for (; row<erow; row++) {
      for (; col<ecol; col++) {
        Object o = grid[row][col];
        if (o==null) continue;
        if (o instanceof List) result.addAll((List)o);
        else result.add(o);
      }
    }
    
    // done
    return result;
  }
    
} //GridCache
