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
package genj.renderer;

import genj.option.Option;
import genj.option.OptionProvider;
import genj.option.OptionUI;
import genj.option.OptionsWidget;
import genj.util.Registry;

import java.util.Collections;
import java.util.List;

/**
 * Blueprint/Renderer Options
 */
public class Options implements OptionProvider {

  /**
   * Access to our options (one)
   */
  public List getOptions() {
    return Collections.singletonList(new Mgr());
  }
  
  /**
   * Our one option
   */
  private class Mgr extends Option {

    public String getName() {
      return "blueprints";
    }

    public void restore(Registry registry) {
      
      // read old style blueprints if available
      //   views.blueprints.INDI=foo bar
      //   views.blueprints.INDI.foo=...
      //   views.blueprints.INDI.bar=...
      // new is the current view (options)
      //   options.blueprints.INDI=foo bar
      //   options.blueprints.INDI.foo=...
      //   options.blueprints.INDI.bar=...
      Registry root = registry.getRoot();
      if (root.get("views.blueprints.INDI", (String)null)!=null) 
        registry = new Registry(root, "views");
      
      // continue
      BlueprintManager.getInstance().read(registry);
      
      // continue old leftovers
      root.remove("views.blueprints.");
      root.remove("blueprints.");
    }

    public void persist(Registry registry) {
      BlueprintManager.getInstance().write(registry);
    }

    public OptionUI getUI(OptionsWidget widget) {
      return null;
    }

  } //Default

} //Options
