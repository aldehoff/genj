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
package genj.chart;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A series of x,y values
 */
public class XYSeries {

  /** values */
  private LinkedList points = new LinkedList();
  
  /** name */
  private String name;
  
  /**
   * Constructor
   */
  public XYSeries(String name) {
    this.name = name;
  }
    
  /** 
   * Accessor - size
   */
  public int getSize() {
    return points.size();
  }
  
  /**
   * Accessor - point by index
   */
  private Point2D.Float getPointByIndex(int i) {
    return (Point2D.Float)points.get(i);
  }
    
  /**
   * Accessor - point by x
   */
  private Point2D.Float getPointForX(float x) {
    
    // look at existing points
    for (int i=0;i<points.size();i++) {
      
      Point2D.Float p = (Point2D.Float)points.get(i);
      
      // found it?
      if (p.getX()==x) 
        return p;
      
      // past insertion point?
      if (p.getX()>x) {
        p = new Point2D.Float(x,0);
        points.add(i, p);
        return p;
      }
      
      // try next
    }
    
    // append new
    Point2D.Float p = new Point2D.Float(x,0);
    points.add(p);
    return p;
  }
  
  /**
   * Increment a value by x
   */
  public void inc(float x) {
    // find point
    Point2D.Float point = getPointForX(x);
    // increase
    point.y++;
    // done
  }
  
  /**
   * Wrap into something JFreeChart can use
   */
  public static XYDataset toXYDataset(XYSeries[] series) {
    return new XYDatasetImpl(series);
  }
  
  /**
   * A wrapper for Java Free Chart's XY DataSheet
   */
  private static class XYDatasetImpl extends AbstractXYDataset {
    
    /** wrapped */
    private XYSeries[] series;
    
    /**
     * Constructor
     */
    private XYDatasetImpl(XYSeries[] series) {
      this.series = series;
    }
    
    /**
     * # of series
     */
    public int getSeriesCount() {
      return series.length;
    }

    /**
     * series by index 
     */
    public String getSeriesName(int s) {
      return series[s].name;
    }

    /**
     * # of items in series
     */
    public int getItemCount(int s) {
      return series[s].getSize();
    }

    /**
     * item x for seriex
     */
    public Number getX(int i, int item) {
      Point2D.Float p = series[i].getPointByIndex(item);
      return new Float(p.x);
    }

    /**
     * item y for seriex
     */
    public Number getY(int i, int item) {
      Point2D.Float p = series[i].getPointByIndex(item);
      return new Float(p.y);
    }

  } //Wrapper
  
  /**
   * A list for xy-series
   */
  public static class Collector extends java.util.ArrayList {
    
    /**
     * Accessor - a series by name
     */
    public XYSeries get(String name) {
      XYSeries result;
      for (int i = 0; i < size(); i++) {
        result = (XYSeries)get(i);
        if (result.name.equals(name))
          return result;
      }
      result = new XYSeries(name);
      add(result);
      return result;
    }
    
    /**
     * converter to array
     */
    public XYSeries[] toSeriesArray() {
      return (XYSeries[])super.toArray(new XYSeries[size()]);
    }
  
  } //List
  
} //XYSeries
