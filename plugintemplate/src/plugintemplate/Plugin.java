/**
 * GenJ Plugin Template
 */
package plugintemplate;

import java.awt.event.ActionEvent;

import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;

/**
 * Plugin logic
 */
public class Plugin extends WorkbenchAdapter implements ActionProvider {
  
  private final static Resources RESOURCES = Resources.get(Plugin.class);

  public void createActions(Context context, Purpose purpose, Group into) {
    switch (purpose) {
      case CONTEXT:
        // none
        break;
      case MENU:
        into.add(new PluginAction());
        break;
      case TOOLBAR:
        // none
        break;
    }
  }
  
  @Override
  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    // do something with opened file - e.g. listen to it
  }
  
  @Override
  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    // cleanup after closed file - e.g. stop listen to it
  }
  
  
  /**
   * An action we plug-into the workbench
   */
  private class PluginAction extends Action2 {
    
    public PluginAction() {
      setText(RESOURCES.getString("menu"));
      setImage(new ImageIcon(this, "plugin.png"));
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
      // show a simple hello dialog with an ok button only
      DialogHelper.openDialog(
          RESOURCES.getString("title"), 
          DialogHelper.INFORMATION_MESSAGE, 
          RESOURCES.getString("message"), 
          Action2.okOnly(), 
          DialogHelper.getComponent(event));
    }
    
  }

}
