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

      Action2.Group sosa = new Action2.Group(RESOURCES.getString("plugin"));
      tools.add(sosa);

      sosa.add(new Root(engine, context));
      sosa.add(new Index(engine, context));
      sosa.add(new Maintain(engine));
      sosa.add(new Remove(engine,context.getGedcom()));

    }
  }
  
  private class Root extends Action2 {
    public Root(Indexation engine, Context context) {
      boolean enabled = engine.isEnabled(); 
      if (context.getEntities().size()==1 && (context.getEntity() instanceof Indi)) {
        setText(RESOURCES.getString("action.root.indi", context.getEntity()));
      } else {
        setText(RESOURCES.getString("action.root"));
        enabled = false;
      }
      setEnabled(enabled);
    }
  }

  private class Maintain extends Action2 {
    private Indexation index;
    public Maintain(Indexation index) {
      this.index = index;
      setText(RESOURCES.getString("action.maintain"));
      setSelected(index.isEnabled());
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      boolean set = !index.isEnabled();
      index.setEnabled(set);
    }
  }

  
  private class Index extends Action2 {
    public Index(Indexation engine, Context context) {
      setText(RESOURCES.getString("action.reindex"));
      setEnabled(engine.getRoot()!=null);
    }
  }
  
  private class Remove extends Action2 {
    public Remove(Indexation engine, Gedcom gedcom) {
      setText(RESOURCES.getString("action.remove"));
    }
  }
}