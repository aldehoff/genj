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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The abstract base type of all options
 */
public abstract class Option {
  
  /** a name that can be presented to the user */
  private String name;
  
  /**
   * Constructor
   */
  public Option(String naMe) {
    name = naMe;
  }
  
  /**
   * Accessor - name of this option
   */
  public String getName() {
    return name;
  }
  
  /**
   * Get options in member variables of instance
   */
  public static Option[] getOptions(Object instance) {
    
    // prepare result
    List result = new ArrayList();
    
    // loop over fields of instance
    Field[] fields = instance.getClass().getFields();
    for (int f=0;f<fields.length;f++) {
      Field field = fields[f];
      // grab all Option fields
      if (Option.class.isAssignableFrom(field.getType())) try {
        result.add(field.get(instance));
      } catch (Throwable t) {
      }
    }
    
    // done
    return (Option[])result.toArray(new Option[result.size()]);
  }
  
  /**
   * Calculates a text representation
   */
  protected abstract String toText();

} //Option