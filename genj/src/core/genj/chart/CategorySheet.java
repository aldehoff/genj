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

import java.util.Arrays;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;

/**
 * A datasheet that can be shown in a chart
 */
public class CategorySheet {

  /*package*/ String[] series, cats;
  /*package*/ float[][] data;
  
  /**
   * Constructor
   */
  public CategorySheet(String[] series, String[] cats) {
    this.series = series;
    this.cats = cats;
    data = new float[series.length][cats.length];
  }
  
  /**
   * Cell Access
   */
  public float get(int serie, int cat) {
    if (serie>=series.length||cat>=cats.length)
      throw new IllegalArgumentException("No such cell");
    return data[serie][cat];
  }
  
  /**
   * Cell Access
   */
  public void set(int serie, int cat, float val) {
    if (serie>=series.length||cat>=cats.length)
      throw new IllegalArgumentException("No such cell");
    data[serie][cat] = val;
  }
  
  /**
   * Cell Access
   */
  public void inc(int serie, int cat) {
    set(serie, cat, get(serie, cat)+1);
  }
  
  /**
   * Cell Access
   */
  public void dec(int serie, int cat) {
    set(serie, cat, get(serie, cat)-1);
  }

  /**
   * Wrap into something JFreeChart can use
   */
  public CategoryDataset wrap() {
    return new Wrapper();
  }
  
  /** 
   * Wrapper for jfreechart Category DataSet
   */
  public class Wrapper extends AbstractDataset implements CategoryDataset {

    /** row for index */
    public Comparable getRowKey(int pos) {
      return series[pos];
    }

    /** index for row */
    public int getRowIndex(Comparable row) {
      for (int i=0; i<series.length; i++) 
        if (series[i].equals(row)) return i;
      throw new IllegalArgumentException();
    }

    /** all row keys  */
    public List getRowKeys() {
      return Arrays.asList(series);
    }

    /** column for index */
    public Comparable getColumnKey(int pos) {
      return cats[pos];
    }

    /** index for column */
    public int getColumnIndex(Comparable col) {
      for (int i=0; i<cats.length; i++) 
        if (cats[i].equals(col)) return i;
      throw new IllegalArgumentException();
    }

    /** all columns */
    public List getColumnKeys() {
      return Arrays.asList(cats);
    }

    /** value for col, row */
    public Number getValue(Comparable row, Comparable col) {
      return getValue(getRowIndex(row), getColumnIndex(col));
    }

    /** value for col, row */
    public Number getValue(int row, int col) {
      return new Float(data[row][col]);
    }
    
    /** #rows */
    public int getRowCount() {
      return series.length;
    }

    /** #cols */
    public int getColumnCount() {
      return cats.length;
    }
    
  } //Wrapper
  
} //CategorySheet

