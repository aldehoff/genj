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
package genj.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A color set is a logical group of a color and named sub-colors
 */
public class ColorSet {

  /** main key */
  private String key;
  
  /** colors */
  private List keys = new ArrayList(10);
  private Map key2color = new HashMap(10);
  
  /** resources for translation */
  private Resources resources = null;
  
  /** registry to read/write from/to */
  private Registry registry = null;
  
  /**
   * Constructor
   * @param k the key of this set
   * @param n the name of this set (localized)
   * @param c the base color (background) of this set
   */
  public ColorSet(String k, Color c, Resources resrces, Registry regstry) {
    // remember
    key = k;
    resources = resrces;
    registry = regstry;
    // add zero color
    add(k, c);
    // done
  }
  
  /** 
   * Adds a color
   */
  public ColorSet add(String k, Color c) {
    // try to get from registry
    c = registry.get( "color."+key+'.'+k, new Color(c.getRGB()));
    // remember
    keys.add(k);
    key2color.put(k, c);
    // done
    return this;
  }
  
  /**
   * The number of colors
   */
  public int getSize() {
    return keys.size();
  }
  
  /**
   * Returns a name
   */
  public String getName(int i) {
    String k = "color."+key+"."+keys.get(i).toString();
    return resources.getString(k, k);
  }
  
  /**
   * Returns a color
   */
  public Color getColor(int i) {
    return (Color)key2color.get(keys.get(i));
  }

  /**
   * Sets a color
   */
  public void setColor(int i, Color c) {
    // remember
    key2color.put(keys.get(i), new Color(c.getRGB()));
    // update registry
    registry.put( "color."+key+'.'+keys.get(i), c);
    // done
  }

  /**
   * Returns a color
   */
  public Color getColor(String key) {
    return (Color)key2color.get(key);
  }
  
  /**
   * Substitutes colors
   */
  public void substitute(Map map) {
    // check-colors?
    for (int i=0; i<keys.size(); i++) {
      Color color = getColor(i);
      Color subst = (Color)map.get(color);
      if (subst!=null) {
        setColor(i, subst);
      }
    }
    // done
  }
  
  /**
   * String representation
   */
  public String toString() {
    return getName(0);
  }

} //ColorSet
