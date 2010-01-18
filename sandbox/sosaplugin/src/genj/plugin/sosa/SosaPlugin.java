/**
 * This GenJ SosaPlugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
//il faut transmettre les instances des autres menus actions dans le setChangeMenuaction class
package genj.plugin.sosa;

import genj.app.Workbench;
import genj.app.WorkbenchListener;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.View;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Indexation of individuals
 */
public class SosaPlugin implements WorkbenchListener, ActionProvider {

  private final static ImageIcon IMG = new ImageIcon(SosaPlugin.class, "/Sosa.gif");
  private final static Resources RESOURCES = Resources.get(SosaPlugin.class);
  private final static Logger LOG = Logger.getLogger("genj.plugin.sosa");
  private final static Registry REGISTRY = Registry.get(SosaPlugin.class);

  private Map<Gedcom, Indexation> gedcom2indexation = new HashMap<Gedcom, Indexation>();

  private Workbench workbench;

  /**
   * Constructor
   */
  public SosaPlugin(Workbench workbench) {
    this.workbench = workbench;
    workbench.addWorkbenchListener(this);
  }

  public void commitRequested(Workbench workbench) {
    // we have nothing to commit
  }

  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    // store indexer state
    Indexation index = gedcom2indexation.remove(gedcom);
    REGISTRY.put(gedcom.getName(), index.isEnabled());
    // detach from gedcom
    gedcom.removeGedcomListener(index);
  }

  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    
    // init/restore indexer
    Indexation index = new SosaIndexation();
    index.setEnabled(REGISTRY.get(gedcom.getName(), false));
    
    gedcom2indexation.put(gedcom, index);
    
    // attach to gedcom
    gedcom.addGedcomListener(index);
  }

  public void processStarted(Workbench workbench, Trackable process) {
    // don't care
  }

  public void processStopped(Workbench workbench, Trackable process) {
    // don't care
  }

  public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    // don't care
  }

  public void viewClosed(Workbench workbench, View view) {
    // don't care
  }

  public void viewOpened(Workbench workbench, View view) {
    // don't care
  }

  public void viewRestored(Workbench workbench, View view) {
    // don't care
  }

  public boolean workbenchClosing(Workbench workbench) {
    // fine with me
    return true;
  }

  public void createActions(Context context, Purpose purpose, Group result) {
    
    if (purpose == Purpose.MENU && context.getGedcom()!=null) {
      
      // we might not have been told that the file is opened before we're asked for actions so we check for that
      Indexation engine = gedcom2indexation.get(context.getGedcom());
      if (engine==null)
        return;
      
      Action2.Group tools = new ActionProvider.ToolsActionGroup();
      result.add(tools);

      Action2.Group sosa = new Action2.Group("Sosa Plugin");
      tools.add(sosa);

      Root root = new Root(engine, context);
      Index index = new Index(engine, context);
      Enable enable = new Enable(engine, root, index);

      sosa.add(root);
      sosa.add(index);
      sosa.add(enable);

    }
  }
  
  private class Root extends Action2 {
    public Root(Indexation engine, Context context) {
      setText("Set Root");
      setEnabled(engine.isEnabled() && context.getEntities().size()==1 && (context.getEntity() instanceof Indi));
    }
  }

  private class Enable extends Action2 {
    private Indexation index;
    private Action2[] actions;
    public Enable(Indexation index, Action2... actions) {
      this.index = index;
      this.actions = actions;
      setText(index.isEnabled() ? "Disable" : "Enable");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      boolean set = !index.isEnabled();
      index.setEnabled(set);
      setText(set ? "Disable" : "Enable");
      for (Action2 a : actions)
        a.setEnabled(set);
    }
  }

  
  private class Index extends Action2 {
    public Index(Indexation engine, Context context) {
      setText("Recalculate Index");
      setEnabled(engine.isEnabled() && engine.getRoot()!=null);
    }
  }
}