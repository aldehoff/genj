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
package gj.awt.geom;

import java.awt.geom.PathIterator;

/**
 * An interface wrapping constants needed for dealing with PathIterator elements
 */
public interface PathIteratorKnowHow {

  /** static values - segment type sizes */    
  public static final int[] SEG_SIZES = {2, 2, 4, 6, 0};
    
  /** static values - segment types */
  public static final byte SEG_MOVETO  = (byte) PathIterator.SEG_MOVETO;
  public static final byte SEG_LINETO  = (byte) PathIterator.SEG_LINETO;
  public static final byte SEG_QUADTO  = (byte) PathIterator.SEG_QUADTO;
  public static final byte SEG_CUBICTO = (byte) PathIterator.SEG_CUBICTO;
  public static final byte SEG_CLOSE   = (byte) PathIterator.SEG_CLOSE;

  /** static values - segment names */
  public final static String[] SEG_NAMES = {
    "moveto", "lineto", "quadto", "cubicto", "close",
  };

}

