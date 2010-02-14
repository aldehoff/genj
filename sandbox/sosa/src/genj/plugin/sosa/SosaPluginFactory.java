package genj.plugin.sosa;

import genj.app.PluginFactory;
import genj.app.Workbench;

/**
 * the factory for this plugin 
 */
public class SosaPluginFactory implements PluginFactory {

  public Object createPlugin(Workbench workbench) {
    return new SosaPlugin(workbench);
  }

}
