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

/**
 * A Contour describes an area in a latitude/longitude space
 * which extends from a north to south with a west and east
 * extension. An example is
 * <pre>
 *     lon*gi*tude
 *   l     +-+
 *   a     | |          North
 *   t   +-+ +---+        A
 *   i   |       |  West <+> East
 *   t +-+       |        V
 *   u |         |      South
 *   d +---------+
 *   e
 * </pre>
 * A data representation for this example is
 * <pre>
 *     lon*gi*tude
 *   l .....0.....
 *   a    -1 +1         North
 *   t .....1.....        A
 *   i  -2      +3  West <+> East
 *   t .....2.....        V
 *   u-3        +3      South
 *   d .....3.....
 *   e
 * </pre>
 */
/*package*/ class Contour {
  
  protected final static int 
    WEST = 0,
    EAST = 1;
    
  protected double 
    north,
    south,
    west ,
    east ;
    
  private double
    dlat = 0,
    dlon = 0;    
    
  private double[][] data = null;
  private int     [] size = null;
    
  /**
   * Constructor
   */    
  /*package*/ Contour(double n, double w, double e, double s) {
    north = n;
    west  = w;
    east  = e;
    south = s;
  }
  
  /**
   * Merge
   */    
  /*package*/ static Contour merge(List contours) {
    
    // validity check?
    if (contours.size==0)
      throw new IllegalArgumentException("zero-length list n/a");
      
    // performance improvement ? 
    // - one-size list is trivial
    // - two-size list with [0]==[1] is trivial
    if (contours.size==1||(contours.size==2&&contours.items[0]==contours.items[1]))
      return contours.items[0];
      
    // create a new result
    Contour result = new Contour(Double.NaN, Double.MAX_VALUE, -Double.MAX_VALUE, Double.NaN);

    // prepare some data
    int demand = 0;
    for (int c=0;c<contours.size;c++) {
      if (contours.items[c].size==null) demand+=2;
      else demand+=Math.max(contours.items[c].size[EAST],contours.items[c].size[WEST]);
    }
    result.data = new double[2][demand];
    result.size = new int[2];
    
    // take east
    east: for (int c=contours.size-1;c>=0;c--) {
      Iterator it = contours.items[c].getIterator(EAST);
      if (c==contours.size-1) result.north = it.north;
      else while (it.south<=result.south) if (!it.next()) continue east;
      do {
        result.east = Math.max(result.east,it.longitude);
        result.data[EAST][result.size[EAST]++] = it.longitude;
        result.data[EAST][result.size[EAST]++] = it.south;
        result.south = it.south;
      } while (it.next());
    }
    result.size[EAST]--;
    
    // take west
    west: for (int c=0;c<contours.size;c++) {
      Iterator it = contours.items[c].getIterator(WEST);
      if (c==0) result.north = it.north;
      else while (it.south<=result.south) if (!it.next()) continue west;
      do {
        result.west = Math.min(result.west,it.longitude);
        result.data[WEST][result.size[WEST]++] = it.longitude;
        result.data[WEST][result.size[WEST]++] = it.south;
        result.south = it.south;
      } while (it.next());
    }
    result.size[WEST]--;
    
    // done
    return result;
  }
  
  /**
   * Translate
   */
  /*package*/ void translate(double dlat, double dlon) {
    north += dlat;
    south += dlat;
    west  += dlon;
    east  += dlon;
    this.dlat += dlat;
    this.dlon += dlon;
  }

  /**
   * Helper to get an iterator
   */
  /*package*/ Iterator getIterator(int side) {
    return new Iterator(this,side);
  }
    
  /**
   * ContourIterator
   */
  /*package*/ static class Iterator {
    
    private Contour contour;
    private int i,side;
    
    /*package*/ double
      north,
      longitude,
      south;
    
    /**
     * Constructor
     */
    /*package*/ Iterator(Contour c, int s) {
      
      contour = c;
      side = s;
      
      if (contour.size==null) {
        // simplest case - no data!
        north = contour.north;
        longitude = s==WEST ? contour.west : contour.east;
        south = contour.south;
      } else {
        // grab first from data
        south = contour.north;
        i = 0;
        next();
      }
    }

    /**
     * Set to next
     */
    /*package*/ boolean next() {
      
      // out of data?
      if ((contour.size==null)||(i>=contour.size[side]))
        return false;
        
      // move forward
      this.north = this.south;
      this.longitude = contour.dlon + contour.data[side][i++];
      if (i==contour.size[side])
        this.south = contour.south;
      else
        this.south = contour.dlat + contour.data[side][i++];

      // done
      return true;      
    }
      
  } //ContourIterator

  /** 
   * List
   */
  /*package*/ static class List {
    
    /** the items */
    /*package*/ Contour[] items;
    
    /** the size */
    /*package*/ int size;
    
    /**
     * Constructor
     */
    /*package*/ List(int maxSize) {
      items = new Contour[maxSize];
      size = 0;
    }
    
    /**
     * add
     */
    /*package*/ List add(Contour c) {
      items[size++] = c;
      return this;
    }
    
    /**
     * add
     */
    /*package*/ List add(Contour[] cs) {
      System.arraycopy(cs,0,items,size,cs.length);
      size+=cs.length;
      return this;
    }
    
  } //ChildList


}

