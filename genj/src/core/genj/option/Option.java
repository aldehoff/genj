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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sun.misc.Service;

/**
 * An option is simply a wrapped public field of a type 
 * with meta-information (JavaBean 'light')
 */
public abstract class Option {
  
  /**
   * Accessor - name of this option
   */
  public abstract String getName();
  
  /**
   * Restore option values from registry
   */
  public abstract void restore(Registry registry);
  
  /**
   * Persist option values to registry
   */
  public abstract void persist(Registry registry);
  
  /**
   * Create an editor
   */
  public abstract OptionUI getUI(OptionsWidget widget);
  
  /** all known options */
  private static List options;

  /**
   * Restore options values from registry
   */
  public static void restoreAll(Registry registry) {

    registry = new Registry(registry, "options");

    // loop over all options
    Iterator it = getAllOptions().iterator();
    while (it.hasNext()) try {
      ((Option)it.next()).restore(registry);
    } catch (Throwable t) {}
    
    // done
  }
  
  /**
   * Persist option values to registry
   */
  public static void persistAll(Registry registry) {
    
    registry = new Registry(registry, "options");

    // loop over all options
    Iterator it = getAllOptions().iterator();
    while (it.hasNext()) try {
      ((Option)it.next()).persist(registry);
    } catch (Throwable t) {
    }
    
    // done
    
  }
  
  /**
   * Static Accessor - all options available from OptionProviders
   */
  public static List getAllOptions() {  
    
    // known?
    if (options!=null)
      return options;    

    // collect    
    options = new ArrayList(32);

    // prepare options
    Iterator it = Service.providers(OptionProvider.class);
    while (it.hasNext()) {
      // one provider at a time
      OptionProvider provider = (OptionProvider)it.next();
      // one option at a time
      options.addAll(provider.getOptions());
    }

    // done
    return options;
  }
  
} //Option