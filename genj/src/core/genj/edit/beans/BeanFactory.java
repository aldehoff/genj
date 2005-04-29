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
import genj.view.ViewManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A factory for cached PropertyBeans
 */
public class BeanFactory {
  
  /** manager reference */
  private ViewManager viewManager;
  
  /** cached instances */
  private final static Map proxy2instances = new HashMap();
  
  /** map a 'proxy' to a resolved type */
  private static Map proxy2type = new HashMap();
  
  /**
   * Constructor
   */
  public BeanFactory(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  /**
   * Returns a cached property bean suitable to let the user edit given property
   */
  public PropertyBean get(Property prop) {
    return get(prop.getProxy());
  }
  
  /**
   * Returns a cached property bean for given 'name' (genj.edit.beans.{PROXY}Bean)
   */
  public PropertyBean get(String proxy) {
    PropertyBean result = null;
    // maybe we can use a cached version?
    synchronized (proxy2instances) {
      List instances = (List)proxy2instances.get(proxy);
      if (instances!=null&&!instances.isEmpty())
        return (PropertyBean)instances.remove(instances.size()-1);
    }
    // lookup a type for that proxy key
    Class type = (Class)proxy2type.get(proxy);
    if (type==null) {
      try {
        type = Class.forName( "genj.edit.beans." + proxy + "Bean");
        result = (PropertyBean)type.newInstance();
      } catch (Throwable t) {
        type = SimpleValueBean.class;
      }
      proxy2type.put(proxy, type);
    }
    // instantiate if still necessary
    if (result==null) try {
      result = (PropertyBean)type.newInstance();
    } catch (Throwable t) {
      result = new SimpleValueBean();
    }
    // initialize it
    result.initialize(proxy, this, viewManager);
    // done
    return result;
  }
  
  /**
   * Recycle a bean
   */
  /*package*/ void recycle(String proxy, PropertyBean bean) {
    synchronized (proxy2instances) {
      List instances = (List)proxy2instances.get(proxy);
      if (instances==null) {
        instances = new ArrayList();
        proxy2instances.put(proxy, instances);
      }
      instances.add(bean);
    }
    // done
  }
  
}
