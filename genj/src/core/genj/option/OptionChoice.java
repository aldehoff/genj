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
package genj.option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An option - choose one or many from a group of values
 */
public class OptionChoice extends Option {

  /** the values */
  private Object[] values;
  
  /** the selection */
  private Set selection = new HashSet();
  
  /** single selection */
  private boolean isSingleSelection;

  /**
   * Constructor 
   */
  public OptionChoice(String name, Object[] vaLues, Object[] seLection, boolean singleSelect) {
    super(name);
    isSingleSelection = singleSelect;
    values = vaLues;
    if (isSingleSelection)
    selection.addAll(Arrays.asList(seLection));
  }
  
  /**
   * Convert a value into a name
   */
  public String getName(Object value) {
    return value.toString();
  }
  
  /**
   * Accessor - values
   */
  public Object[] getValues() {
    return values;
  }

  /**
   * Accessor - selection
   */
  public boolean isSelected(Object value) {
    return selection.contains(value);
  }

  /**
   * Accessor - selection
   */
  public void setSelected(Object value) {
    selection.add(value);
  }
  
} //OptionChoice