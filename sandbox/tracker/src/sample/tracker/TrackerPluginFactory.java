package sample.tracker;

import genj.app.PluginFactory;
import genj.app.Workbench;

public class TrackerPluginFactory implements PluginFactory {

  public Object createPlugin(Workbench workbench) {
    return new TrackerPlugin(workbench);
  }
  

}
