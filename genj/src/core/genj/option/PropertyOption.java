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

import genj.util.Registry;
import genj.util.swing.TextFieldWidget;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * An option based on a simple accessible value
 */
public abstract class PropertyOption extends Option {

  /** option is for instance */
  protected Object instance;
  
  /** property */
  protected String property;

  /** type */
  protected Class type;

  /**
   * Get options for given instance - supported are
   * int, boolean and String
   */
  public static List introspect(Object instance) {
    
    // prepare result
    List result = new ArrayList();
    Set beanattrs = new HashSet();

    // loop over bean properties of instance
    try {
      BeanInfo info = Introspector.getBeanInfo(instance.getClass());
      PropertyDescriptor[] properties = info.getPropertyDescriptors();    
      for (int p=0; p<properties.length; p++) {
        
        PropertyDescriptor property = properties[p];
        
        try {
          // has to have getter & setter
          if (property.getReadMethod()==null||property.getWriteMethod()==null)
            continue;
            
          // int, boolean, String?
          if (!PropertyOption.isSupportedArgument(property.getPropertyType()))
            continue;
            
          // try a read
          property.getReadMethod().invoke(instance, null);
            
          // create and keep the option
          result.add(BeanPropertyOption.create(instance, property));
          
          // remember name
          beanattrs.add(property.getName());
        } catch (Throwable t) {
        }
      }
    } catch (IntrospectionException e) {
    }

    // loop over fields of instance
    Field[] fields = instance.getClass().getFields();
    for (int f=0;f<fields.length;f++) {
      
      Field field = fields[f];
      Class type = field.getType();

      // won't address name of property again
      if (beanattrs.contains(field.getName()))
        continue;

      // has to be public, non-static, non-final      
      int mod = field.getModifiers();
      if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) 
        continue;
      try {
        field.get(instance);
      } catch (Throwable t) {
        continue;
      }

      // int, boolean, String?
      if (!PropertyOption.isSupportedArgument(type))
        continue;

      // create and keep the option
      result.add(FieldOption.create(instance, field));

      // next
    }
    
    // done
    return result;
  }

  /**
   * Constructor
   */
  protected PropertyOption(Object instance, String property, Class type) {
    this.instance = instance;
    this.property = property;
    this.type     = type;
  }

  /**
   * Accessor - a unique key for this option 
   */ 
  public String getProperty() {
    return property;
  }
  
  /**
   * Accessor - name of this option
   */
  public String getName() {
    // can localize?
    if (instance instanceof OptionMetaInfo)
      return ((OptionMetaInfo)instance).getLocalizedName(this);
    // key
    return getProperty();
  }

  /**
   * Restore option values from registry
   */
  public void restore(Registry registry) {
    String value = registry.get(instance.getClass().getName() + '.' + getProperty(), (String)null);
    if (value!=null) 
      setValue(value);
  }
  
  /**
   * Persist option values to registry
   */
  public void persist(Registry registry) {
    Object value = getValue();
    if (value!=null) 
      registry.put(instance.getClass().getName() + '.' + getProperty(), value.toString());
  }

  /**
   * Provider a UI for this option
   */  
  public OptionUI getUI(OptionsWidget widget) {
    // a boolean?
    if (type==Boolean.TYPE)
      return new BooleanUI();
    // all else
    return new SimpleUI();
  }
  
  /**
   * Accessor - current value of this option
   */
  protected final Object getValue() {
    try {
      // get it
      return getValueImpl();
    } catch (Throwable t) {
      return null;
    }
  }

  /**
   * Accessor - implementation
   */
  protected abstract Object getValueImpl() throws Throwable;
  
  /**
   * Accessor - current value of this option
   */
  protected final void setValue(Object value) {
    try {
      // type clash?
      Class boxed = box(type);
      if (value!=null&&value.getClass()!=boxed) { 
        value = boxed.getConstructor(new Class[]{value.getClass()})
          .newInstance(new Object[]{ value });
      }
      // set it
      setValueImpl(value);
    } catch (Throwable t) {
      // not much we can do about that - ignored
    }
  }

  /**
   * Accessor - implementation
   */
  protected abstract void setValueImpl(Object value) throws Throwable;
  
  /**
   * Accessor - (boxed) type of this option
   */ 
  private static Class box(Class type) {
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
   * Test for supported option types
   */
  private static boolean isSupportedArgument(Class type) {
    return
      String.class.isAssignableFrom(type) ||
      Integer.TYPE.isAssignableFrom(type) ||
      Boolean.TYPE.isAssignableFrom(type);
  }

  /**
   * A UI for a boolean 
   */
  private class BooleanUI extends JCheckBox implements OptionUI {
    /** constructor */
    private BooleanUI() {
      setOpaque(false);
      setHorizontalAlignment(JCheckBox.LEFT);
      Boolean value = (Boolean)getValue();
      if (value.booleanValue())
        setSelected(true);
    }
    /** no text ui */
    public String getTextRepresentation() {
      return null;
    }
    /** component */
    public JComponent getComponentRepresentation() {
      return this;
    }
    /** commit */
    public void endRepresentation() {
      setValue(isSelected()?Boolean.TRUE : Boolean.FALSE);
    }
  } //BooleanUI

  /**
   * A UI for text, numbers, etc.
   */
  private class SimpleUI extends TextFieldWidget implements OptionUI {
    /** constructor */
    private SimpleUI() {
      Object value = getValue();
      setText(value!=null?value.toString():"");
      setSelectAllOnFocus(true);
      setColumns(12);
    }
    /** no text ui */
    public String getTextRepresentation() {
      return getText();
    }
    /** component */
    public JComponent getComponentRepresentation() {
      return this;
    }
    /** commit */
    public void endRepresentation() {
      setValue(getText());
    }
  } //BooleanUI

  /**
   * A field Option
   */
  private static class FieldOption extends PropertyOption {

    /** field */
    protected Field field;

    /** factory */
    protected static Option create(final Object instance, Field field) {
      // create one
      PropertyOption result = new FieldOption(instance, field);
      // is it an Integer field with matching multiple choice field?
      if (field.getType()==Integer.TYPE) try {
        final Field choices = instance.getClass().getField(field.getName()+"s");
        if (choices.getType().isArray())
          // wrap in multiple choice 
          return new MultipleChoiceOption(result) {
            public Object[] getChoicesImpl() throws Throwable {
              return (Object[])choices.get(instance);
            }
          };
      } catch (Throwable t) {
      }
      // done
      return result;
    }

    /** Constructor */
    private FieldOption(Object instance, Field field) {  
      super(instance, field.getName(), field.getType());
      this.field = field;
    }

    /** accessor */    
    protected Object getValueImpl() throws Throwable {
      return field.get(instance);
    }
    
    /** accessor */
    protected void setValueImpl(Object value) throws Throwable {
      field.set(instance, value);
    }
    
  } //Field

  /**
   * A bean property Option
   */
  private static class BeanPropertyOption extends PropertyOption {

    /** descriptor */
    PropertyDescriptor descriptor;
    
    /** factory */
    protected static Option create(final Object instance, PropertyDescriptor descriptor) {
      // create one
      PropertyOption result = new BeanPropertyOption(instance, descriptor);
      // is it an Integer field with matching multiple choice field?
      if (descriptor.getPropertyType()==Integer.TYPE) try {
        final Method choices = instance.getClass().getMethod(descriptor.getReadMethod().getName()+"s", null);
        if (choices.getReturnType().isArray())
          // wrap in multiple choice 
          return new MultipleChoiceOption(result) {
            public Object[] getChoicesImpl() throws Throwable {
              return (Object[])choices.invoke(instance, null);
            }
          };
      } catch (Throwable t) {
      }
      // done
      return result;
    }

    /** Constructor */
    private BeanPropertyOption(Object instance, PropertyDescriptor property) {
      super(instance, property.getName(), property.getPropertyType()); 
      this.descriptor = property;
    }
    
    /** accessor */    
    protected Object getValueImpl() throws Throwable {
      return descriptor.getReadMethod().invoke(instance, null);
    }
    
    /** accessor */
    protected void setValueImpl(Object value) throws Throwable {
      descriptor.getWriteMethod().invoke(instance, new Object[]{value} );
    }
    
  } //BeanProperty
 
} //ValueOption
