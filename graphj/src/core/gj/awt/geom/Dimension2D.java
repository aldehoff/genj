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

import java.awt.geom.Rectangle2D;

/**
 * Dimension2D - the way it should have been
 */
public abstract class Dimension2D extends java.awt.geom.Dimension2D {
  
  public static class Double extends Dimension2D {

    /** the vals */    
    public double w,h;

    /**
     * Constructor
     */
    public Double() {
    }
  
    /**
     * Constructor
     */
    public Double(double width, double height) {
      setSize(width,height);
    }
  
    /**
     * @see Dimension2D#getHeight()
     */
    public double getHeight() {
      return h;
    }
  
    /**
     * @see Dimension2D#getWidth()
     */
    public double getWidth() {
      return w;
    }
  
    /**
     * @see Dimension2D#setSize(double, double)
     */
    public void setSize(double width, double height) {
      w=width;
      h=height;
    }

    /**
     * @see Dimension2D#setSize(double, double)
     */
    public void setSize(Rectangle2D r) {
      setSize(r.getWidth(),r.getHeight());
    }
    
    /**
     * Test representation
     */
    public String toString() {
      return "("+w+','+h+')';
    }
  }
    
}
