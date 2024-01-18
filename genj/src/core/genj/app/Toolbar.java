package genj.app;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomListenerAdapter;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.swing.Action2;
import genj.util.swing.ToolbarWidget;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;
import genj.view.View;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;

import spin.Spin;

/**
 * our toolbar
 */
/*package*/ class Toolbar extends ToolbarWidget implements WorkbenchListener, ActionProvider {
  
  private final static Resources RES = Resources.get(Toolbar.class);
  private final static Registry REGISTRY = Registry.get(Toolbar.class);
  private final static Logger LOG = Logger.getLogger("genj.app");

  private Workbench workbench;
  private HistoryWidget history;
  
  private List<Action> actions = new CopyOnWriteArrayList<Action>();
  
  private GedcomListener callback = new GedcomListenerAdapter() {
    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      setup(gedcom);
    }
  };
  private Gedcom gedcom;
  
  /**
   * Constructor
   */
  /*package*/ Toolbar(Workbench workbench) {
    this.workbench = workbench;
    workbench.addWorkbenchListener(this);
    history = new HistoryWidget(workbench);
    setFloatable(false);
    setup(null);

    setVisible(REGISTRY.get("visible", true));
  }
  
  private void remove(Action action) {
    
    if (gedcom!=null&&action instanceof GedcomListener)
      gedcom.removeGedcomListener((GedcomListener)Spin.over(action));
    
    if (action instanceof WorkbenchListener)
      workbench.removeWorkbenchListener((WorkbenchListener)action);
    
    actions.remove(action);
  }
  
  private void setup(Gedcom gedcom) {
   
    // tear down
    if (this.gedcom!=null)
      this.gedcom.removeGedcomListener(callback);
    
    for (Action action : actions) 
      remove(action);
    removeAll();
    
    // keep
    this.gedcom = gedcom;
      
    // defaults
    add(workbench.new ActionNew());
    add(workbench.new ActionOpen());
    add(workbench.new ActionSave(false));
    
    // let providers speak
    if (gedcom!=null) {
      Action2.Group actions = new Action2.Group("ignore");
      addSeparator();
      for (ActionProvider provider : workbench.getProviders(ActionProvider.class)) {
        actions.clear();
        provider.createActions(workbench.getContext(), Purpose.TOOLBAR, actions);
        for (Action2 action : actions) {
          if (action instanceof Action2.Group)
            LOG.warning("ActionProvider "+provider+" returned a group for toolbar");
          else {
            if (action instanceof ActionProvider.SeparatorAction)
              addSeparator();
            else {
              add(action);
            }
          }
        }
      }
    }
    
    if (this.gedcom!=null)
      this.gedcom.addGedcomListener(callback);
    
    // add history
    add(history);
    
    // done
  }
  
  @Override
  public JButton add(Action action) {
    // remember
    actions.add(action);
    // no mnemonic (e.g. alt-o triggering Open action), no text
    action.putValue(Action.MNEMONIC_KEY, null);
    action.putValue(Action.NAME, null);
    // connection
    if (gedcom!=null&&action instanceof GedcomListener)
      gedcom.addGedcomListener((GedcomListener)Spin.over(action));
    if (action instanceof WorkbenchListener)
      workbench.addWorkbenchListener((WorkbenchListener)action);
    
    // super stuff
    return super.add(action);
  }

  public void commitRequested(Workbench workbench) {
  }

  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    setup(null);
  }

  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    setup(gedcom);
  }

  public void processStarted(Workbench workbench, Trackable process) {
  }

  public void processStopped(Workbench workbench, Trackable process) {
  }

  public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    setup(context.getGedcom());
  }

  public void viewClosed(Workbench workbench, View view) {
  }
  
  public void viewRestored(Workbench workbench, View view) {
  }

  public void viewOpened(Workbench workbench, View view) {
  }

  public void workbenchClosing(Workbench workbench) {
  }
  
  public void createActions(Context context, Purpose purpose, Group into) {
    if (purpose==Purpose.MENU) {
      Action2.Group views = new ActionProvider.ViewActionGroup();
      views.add(new Visible());
      into.add(views);
    }
  }
  
  private class Visible extends Action2 {

    /** constructor */
    protected Visible() {
      setText(RES, "toolbar");
      setSelected(REGISTRY.get("visible", true));
    }

    /** run */
    public void actionPerformed(ActionEvent event) {
      boolean set = !REGISTRY.get("visible", true);
      REGISTRY.put("visible", set);
      setVisible(set);
    }
    
  }

}
