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

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A datasheet that can be shown in a chart
 */
public class XYSheet {

  /** series keys to points */
  private Map series2points = new HashMap();
  
  /**
   * size of series
   */
  /*package*/ int getSize(String series) {
    return ((List)series2points.get(series)).size();
  }
  
  /**
   * value for series
   */
  /*package*/ Point getValue(String series, int i) {
    return (Point)((List)series2points.get(series)).get(i);
  }
  
  /**
   * Series
   */
  /*package*/ Set getSeries() {
    return series2points.keySet();
  }
  
  /**
   * Increment a value
   */
  public void inc(String series, int x) {
    // grab current list of values for series
    List points = (List)series2points.get(series);
    if (points==null) {
      points = new ArrayList(10);
      series2points.put(series, points);
    }
    // find point
    Point point = getPoint(points, x);
    // increase
    point.y ++;
    // done
  }
  
  /**
   * Get a series point by x
   */
  private Point getPoint(List points, int x) {
    
    // look at existing points
    for (int i=0;i<points.size();i++) {
      Point p = (Point)points.get(i);
      
      // found it?
      if (p.x==x) 
        return p;
      
      // past insertion point?
      if (p.x>x) {
        p = new Point(x,0);
        points.add(i, p);
        return p;
      }
      
      // try next
    }
    
    // append new
    Point p = new Point(x,0);
    points.add(p);
    return p;
  }
  
  /**
   * simple text representation
   */
  public String toString() {
    StringBuffer result = new StringBuffer();
    Iterator it = series2points.keySet().iterator();
    while (it.hasNext()) {
      String series = (String)it.next();
      result.append(series);
      result.append(" : ");
      result.append(series2points.get(series));
      result.append("\n");
    }
    return result.toString();
  }
  
  /**
   * Wrap into something JFreeChart can use
   */
  public XYDataset wrap() {
    return new Wrapper();
  }
  
  /**
   * A wrapper for Java Free Chart's XY DataSheet
   */
  private class Wrapper extends AbstractXYDataset {
    
    /** wrapped */
    private XYSheet sheet;
    
    /** series */
    private List series;
    
    /**
     * Constructor
     */
    private Wrapper() {
      series = new ArrayList(getSeries());
    }

    /**
     * # of series
     */
    public int getSeriesCount() {
      return series.size();
    }

    /**
     * series by index 
     */
    public String getSeriesName(int i) {
      return (String)series.get(i);
    }

    /**
     * # of items in series
     */
    public int getItemCount(int series) {
      return getSize(getSeriesName(series));
    }

    /**
     * item x for seriex
     */
    public Number getX(int series, int item) {
      return new Integer(getValue(getSeriesName(series), item).x);
    }

    /**
     * item y for seriex
     */
    public Number getY(int series, int item) {
      return new Integer(getValue(getSeriesName(series), item).y);
    }

  } //Wrapper
  
} //DataSheet

