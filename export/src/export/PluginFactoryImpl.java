/**
 * GenJ Plugin Template
 */
package export;

import genj.app.PluginFactory;
import genj.app.Workbench;

/**
 * Plugin Factory
 */
public class PluginFactoryImpl implements PluginFactory {

  public Object createPlugin(Workbench workbench) {
    return new Plugin();
  }

}
