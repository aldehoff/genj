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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An option is simply a wrapped public field of a type 
 * with meta-information (JavaBean 'light')
 */
public class Option {
  
  /** option is for instance */
  private Object instance;
  
  /** a name that can be presented to the user */
  private String name;
  
  /** field */
  private Field field;
  
  /**
   * Constructor
   */
  public Option(Object inStance, String naMe, Field fiEld) {
    instance = inStance;
    name = naMe;
    field = fiEld;
  }
  
  /**
   * Accessor - name of this option
   */
  public String getName() {
    return name;
  }
  
  /**
   * Accessor - current value of this option
   */
  public Object getValue() {
    try {
      return field.get(instance);
    } catch (Throwable t) {
      // shouldn't happen
      throw new RuntimeException();
    }
  }
  
  /**
   * Get options for given instance - supported are
   * int, boolean and String
   */
  public static Option[] getOptions(Object instance, Properties field2name) {
    
    // prepare result
    List result = new ArrayList();
    
    // loop over fields of instance
    Field[] fields = instance.getClass().getFields();
    
    for (int f=0;f<fields.length;f++) {
      
      Field field = fields[f];
      Class type = field.getType();
      
      // test access
      int mod = field.getModifiers();
      
      if (Modifier.isFinal(mod) || !Modifier.isPublic(mod) || Modifier.isStatic(mod)) 
        continue;
        
      // calc name
      String name = field.getName();
      String s = field2name.getProperty(name);
      if (s!=null) name = s;
      
      // int, boolean, String?
      if (String.class.isAssignableFrom(type) ||
          Integer.TYPE.isAssignableFrom(type) ||
          Boolean.TYPE.isAssignableFrom(type))
        result.add(new Option(instance, name, field));

      // next
    }
    
    // done
    return (Option[])result.toArray(new Option[result.size()]);
  }
  

} //Option