/**
 * This source file is part of a GenJ Plugin and copyright of the respective authors.
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genj.plugin.sosa;

import genj.app.PluginFactory;
import genj.app.Workbench;

/**
 * The factory for this plugin 
 */
public class SosaPluginFactory implements PluginFactory {

  public Object createPlugin(Workbench workbench) {
    return new SosaPlugin(workbench);
  }

}
