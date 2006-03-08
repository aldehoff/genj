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
package genj.edit.beans;

import genj.gedcom.Property;
import genj.util.Registry;
import genj.view.ViewManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A factory for cached PropertyBeans
 */
public class BeanFactory {
  
  private final static Class[] beanTypes = {
    EntityBean.class,
    PlaceBean.class, // before choice!
    AgeBean.class,
    ChoiceBean.class,
    DateBean.class,
    EventBean.class,
    FileBean.class,
    MLEBean.class,
    NameBean.class,
    SexBean.class,
    XRefBean.class,
    SimpleValueBean.class // last!
  };
  
  /** registry used for all beans */
  private Registry registry;
  
  /** manager reference */
  private ViewManager viewManager;
  
  /** cached instances */
  private final static List cache = new LinkedList();
  
  /** map a 'proxy' to a resolved type */
  private static Map proxy2type = new HashMap();
  
  /**
   * Constructor
   */
  public BeanFactory(ViewManager viewManager, Registry registry) {
    this.viewManager = viewManager;
    this.registry = registry;
  }

  /**
   * Returns a cached property bean of given type name
   */
  public PropertyBean get(String type, Property prop) {
    
    // grab a bean
    PropertyBean bean = getBeanOfType(type);
    
    // set its value
    bean.setProperty(prop);
    
    // done
    return bean;
  }
  
  /**
   * Returns a cached property bean suitable to let the user edit given property
   */
  public PropertyBean get(Property prop) {

    // grab a bean
    PropertyBean bean = getBeanFor(prop);
    
    // set its value
    bean.setProperty(prop);
    
    // done
    return bean;
  }
  
  /**
   * Try to lookup a recycled bean
   */
  private synchronized PropertyBean getBeanOfType(String type) {
    
    try {
      Class beanType = Class.forName(type);
      
      // look into cache
      for (ListIterator it=cache.listIterator(); it.hasNext(); ) {
        PropertyBean bean = (PropertyBean)it.next();
        if (bean.getClass()==beanType) {
          it.remove();
          return bean;
        }
      }
      // create new instance
      PropertyBean bean = (PropertyBean)beanType.newInstance();
      bean.initialize(viewManager, registry);
      return bean;
      
    } catch (Throwable t) {
    }
    
    return new SimpleValueBean();
  }
  
  /**
   * Try to lookup a recycled bean
   */
  private synchronized PropertyBean getBeanFor(Property prop) {
    // look into cache
    for (ListIterator it=cache.listIterator(); it.hasNext(); ) {
      PropertyBean bean = (PropertyBean)it.next();
      if (bean.accepts(prop)) {
        it.remove();
        return bean;
      }
    }
    // create new instances
    try {
      for (int i=0;i<beanTypes.length;i++) {
        PropertyBean bean = (PropertyBean)beanTypes[i].newInstance();
        if (bean.accepts(prop)) {
          bean.initialize(viewManager, registry);
          return bean;
        }
      }
    } catch (Throwable t) {
    }
    return new SimpleValueBean();
  }
  
  /**
   * Recycle a bean
   */
  public synchronized void recycle(PropertyBean bean) {
    cache.add(bean);
  }
  
}
