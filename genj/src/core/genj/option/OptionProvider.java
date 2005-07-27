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
 * A service that can provide options
 */
public abstract class OptionProvider {

  /** all known options */
  private static List options;

  /**
   * Accessor - options
   */
  public abstract List getOptions();

  /**
   * Persist all options from all OptionProviders to registry
   */
  public static void persistAll(Registry registry) {
    
    registry = new Registry(registry, "options");
  
    // loop over all options
    Iterator it = getAllOptions(null).iterator();
    while (it.hasNext()) try {
      ((Option)it.next()).persist(registry);
    } catch (Throwable t) {
    }
    
    // done
    
  }
  
  /**
   * Static Accessor - explicitly set options to consider
   */
  public static void setOptionProviders(String[] providers) {
    
    // collect    
    options = new ArrayList(32);
    for (int i=0;i<providers.length;i++) { 
      try {
        // one provider at a time
        OptionProvider provider = (OptionProvider)Class.forName(providers[i]).newInstance();
        // grab its options
        List os = provider.getOptions();
        // keep em
        options.addAll(os);
      } catch (Throwable t) {
      }
    }
    
    // done
  }
  
  /**
   * Static Accessor - all options available from OptionProviders
   */
  public static List getAllOptions() {  
    return getAllOptions(null);
  }
  public static List getAllOptions(Registry restoreFrom) {  
    
    // known?
    if (options!=null)
      return options;    
  
    // collect    
    options = new ArrayList(32);
    if (restoreFrom!=null) 
      restoreFrom = new Registry(restoreFrom, "options");
  
    // prepare options
    Iterator providers = Service.providers(OptionProvider.class);
    while (providers.hasNext()) {
      // one provider at a time
      OptionProvider provider = (OptionProvider)providers.next();
      // grab its options
      List os = provider.getOptions();
      options.addAll(os);
      // restore their value
      if (restoreFrom!=null) {
        for (Iterator it=os.iterator(); it.hasNext(); ) {
          try {
            ((Option)it.next()).restore(restoreFrom);
          } catch (Throwable t) {}
        }
      }
      // next provider
    }
    
    // done
    return options;
  }

} //OptionProvider
