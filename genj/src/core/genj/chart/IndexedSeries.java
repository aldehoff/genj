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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.TableXYDataset;

/**
 * A series of category/value pairs
 */
public class IndexedSeries {

  /** name */
  private String name;
  
  /** values */
  private float[] values;
  
  /**
   * Constructor
   */
  public IndexedSeries(String name, int size) {
    this.name = name;
    values = new float[size];
  }
  
  /**
   * Access
   */
  public float get(int cat) {
    if (cat<0||cat>=values.length)
      throw new IllegalArgumentException("No such cell");
    return values[cat];
  }
  
  /**
   * Cell Access
   */
  public void set(int cat, float val) {
    if (cat<0||cat>=values.length)
      throw new IllegalArgumentException("No such cell");
    values[cat] = val;
  }
  
  /**
   * Cell Access
   */
  public void inc(int cat) {
    set(cat, get(cat)+1);
  }
  
  /**
   * Cell Access
   */
  public void dec(int cat) {
    set(cat, get(cat)-1);
  }
  
  /**
   * Convenient converter
   */
  public static IndexedSeries[] toArray(Collection c) {
    return (IndexedSeries[])c.toArray(new IndexedSeries[c.size()]);
  }

  /**
   * Wrap into something JFreeChart can use
   */
  public static CategoryDataset asCategoryDataset(IndexedSeries[] series, String[] categories) {
    return new CategoryDatasetImpl(series, categories);
  }
  
  /**
   * Wrap into something JFreeChart can use
   */
  public static TableXYDataset asTableXYDataset(IndexedSeries[] series, int rangeStart, int rangeEnd) {
    return new TableXYDatasetImpl(series, rangeStart, rangeEnd);
  }
  
  /** 
   * Wrapper for jfreechart TableXYDataSet
   */
  private static class TableXYDatasetImpl extends AbstractXYDataset implements TableXYDataset {
    
    /** series */
    private IndexedSeries[] series;
    
    /** range */
    private int rangeStart, rangeEnd;
    
    /**
     * Constructor
     */
    public TableXYDatasetImpl(IndexedSeries[] series, int rangeStart, int rangeEnd) {
      
      this.series = series;
      this.rangeStart = rangeStart;
      this.rangeEnd = rangeEnd;

      int len = rangeEnd-rangeStart+1;
      
      for (int i=0;i<series.length;i++) {
        if (series[i].values.length!=len)
          throw new IllegalArgumentException("series doesn't match "+len+" elements");
      }
    }
    
    /**
     * THE TableXYDataset requirement - one equal item count
     */
    public int getItemCount() {
      return rangeEnd-rangeStart+1;
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
      return getItemCount();
    }

    /**
     * item x for seriex
     */
    public Number getX(int s, int item) {
      return new Integer(rangeStart + item);
    }

    /**
     * item y for seriex
     */
    public Number getY(int s, int item) {
      return new Float(series[s].get(item));
    }

  } //TableXYDatasetImpl
  
  /** 
   * Wrapper for jfreechart Category DataSet
   */
  private static class CategoryDatasetImpl extends AbstractDataset implements CategoryDataset {
    
    /** series */
    private IndexedSeries[] series;
    
    /** categories */
    private String[] categories;
    
    /** constructor */
    private CategoryDatasetImpl(IndexedSeries[] series, String[] categories) {
      this.series = series;
      this.categories = categories;
      
      for (int i=0;i<series.length;i++) {
        if (series[i].values.length!=categories.length)
          throw new IllegalArgumentException("series doesn't match categories");
      }
    }

    /** row for index */
    public Comparable getRowKey(int row) {
      return series[row].name;
    }

    /** index for row */
    public int getRowIndex(Comparable row) {
      for (int i=0; i<series.length; i++) 
        if (series[i].name.equals(row)) 
          return i;
      throw new IllegalArgumentException();
    }

    /** all row keys  */
    public List getRowKeys() {
      ArrayList result = new ArrayList();
      for (int i=0;i<series.length;i++)
        result.add(series[i].name);
      return result;
    }

    /** column for index */
    public Comparable getColumnKey(int col) {
      return categories[col];
    }

    /** index for column */
    public int getColumnIndex(Comparable col) {
      for (int i=0; i<categories.length; i++) 
        if (categories[i].equals(col)) return i;
      throw new IllegalArgumentException();
    }

    /** all columns */
    public List getColumnKeys() {
      return Arrays.asList(categories);
    }

    /** value for col, row */
    public Number getValue(Comparable row, Comparable col) {
      return getValue(getRowIndex(row), getColumnIndex(col));
    }

    /** value for col, row */
    public Number getValue(int row, int col) {
      return new Float(series[row].get(col));
    }
    
    /** #rows */
    public int getRowCount() {
      return series.length;
    }

    /** #cols */
    public int getColumnCount() {
      return categories.length;
    }
    
  } //Wrapper
  
  
} //CategorySeries