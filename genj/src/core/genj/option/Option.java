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
  protected Object instance;
  
  /** a name that can be presented to the user */
  protected String name;
  
  /** field */
  protected Field field;
  
  /**
   * Constructor
   */
  protected Option(Object inStance, String naMe, Field fiEld) {
    // remember
    instance = inStance;
    name = naMe;
    field = fiEld;
    // done
  }

  /**
   * Accessor - a unique key for this option
   */ 
  public String getKey() {
    return field.getName();
  }
  
  /**
   * Accessor - type of this option
   */ 
  private Class getType() {
    return field.getType();
  }
  
  /**
   * Accessor - (boxed) type of this option
   */ 
  private Class getNonPrimitiveType() {
    Class type = getType();
    if (type == boolean.class) return Boolean.class;         
    if (type == byte.class) return Byte.class;         
    if (type == char.class) return Character.class;         
    if (type == short.class) return Short.class;         
    if (type == int.class) return Integer.class;         
    if (type == long.class) return Long.class;         
    if (type == float.class) return Float.class;         
    if (type == double.class) return Double.class;                     
    return type;
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
    } catch (IllegalAccessException e) {
      // shouldn't happen if instantiated through getOptions()
      return null;
    }
  }
  
  /**
   * Accessor - current value of this option
   */
  public void setValue(Object value) {
    try {
      // type clash?
      Class type = getNonPrimitiveType();
      if (value.getClass()!=type) { 
        value = type.getConstructor(new Class[]{value.getClass()})
          .newInstance(new Object[]{ value });
      }
      // set it
      field.set(instance, value);
    } catch (Throwable t) {
      // not much we can do about that - ignored
    }
  }
  
  /**
   * Get option for given instance, name and field
   */
  private static Option getOption(Object instance, String name, Field field) {
    
    // maybe multiple choice?
    if (Integer.TYPE.isAssignableFrom(field.getType())) {
      try {
        Object[] choices = (Object[])instance.getClass().getField(field.getName()+"s").get(instance);
        return new MultipleChoiceOption(instance, name, field, choices);
      } catch (Exception e) {
      }
    }
    
    // Simple value option
    return new Option(instance, name, field); 
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

      // int, boolean, String?
      if (!String.class.isAssignableFrom(type) &&
          !Integer.TYPE.isAssignableFrom(type) &&
          !Boolean.TYPE.isAssignableFrom(type) )
        continue;
        
      // calc name
      String name = field.getName();
      String s = field2name.getProperty(name);
      if (s!=null) name = s;

      // keep      
      result.add(getOption(instance, name, field));

      // next
    }
    
    // done
    return (Option[])result.toArray(new Option[result.size()]);
  }
  

} //Option