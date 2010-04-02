/**
 * This source file is part of a GenJ Plugin and copyright of the respective authors.
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package genj.plugin.sosa;

import genj.app.Workbench;
import genj.app.WorkbenchAdapter;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.UnitOfWork;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.util.swing.Action2.Group;
import genj.view.ActionProvider;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Index of individuals
 */
public class SosaPlugin extends WorkbenchAdapter implements ActionProvider {

  private final static ImageIcon IMG = new ImageIcon(SosaPlugin.class, "/Sosa.gif");
  private final static Resources RESOURCES = Resources.get(SosaPlugin.class);
  private final static Logger LOG = Logger.getLogger("genj.plugin.sosa");
  private final static Registry REGISTRY = Registry.get(SosaPlugin.class);

  private Map<Gedcom, Index> gedcom2indexation = new HashMap<Gedcom, Index>();

  private Workbench workbench;

  /**
   * Constructor
   */
  public SosaPlugin(Workbench workbench) {
    this.workbench = workbench;
    workbench.addWorkbenchListener(this);
  }

  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    // store indexer state
    Index index = gedcom2indexation.remove(gedcom);
    Indi root = index.getRoot();
    index.setRoot(null);
    REGISTRY.put(gedcom.getName()+".on", index.isEnabled());
    REGISTRY.put(gedcom.getName()+".root", root!=null ? root.getId() : "");
    // detach from gedcom
    gedcom.removeGedcomListener(index);
  }

  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    
    // init/restore indexer
    Index index = new SosaIndex();
    
    Entity root = gedcom.getEntity(Gedcom.INDI, REGISTRY.get(gedcom.getName()+".root", "noroot"));
    if (root instanceof Indi)
      index.setRoot((Indi)root);
    index.setEnabled(REGISTRY.get(gedcom.getName()+".on", false));
    
    gedcom2indexation.put(gedcom, index);
    
    // attach to gedcom
    gedcom.addGedcomListener(index);
  }

  public void createActions(Context context, Purpose purpose, Group result) {
    
    if (purpose == Purpose.MENU && context.getGedcom()!=null) {
      
      // we might not have been told that the file is opened before we're asked for actions so we check for that
      Index engine = gedcom2indexation.get(context.getGedcom());
      if (engine==null)
        return;
      
      Action2.Group tools = new ActionProvider.ToolsActionGroup();
      result.add(tools);

      Action2.Group sosa = new Action2.Group(RESOURCES.getString("plugin"));
      tools.add(sosa);

      sosa.add(new Root(engine, context));
      sosa.add(new Reindex(engine, context));
      sosa.add(new Maintain(engine));
      sosa.add(new Remove(engine,context.getGedcom()));

    }
  }
  
  private class Root extends Action2 {
    private Indi root;
    private Index index;
    public Root(Index index, Context context) {
      this.index = index;
      boolean enabled = index.isEnabled(); 
      if (context.getEntities().size()==1 && (context.getEntity() instanceof Indi)) {
        setText(RESOURCES.getString("action.root.indi", context.getEntity()));
        root = (Indi)context.getEntity();
      } else {
        setText(RESOURCES.getString("action.root"));
        enabled = false;
      }
      setEnabled(enabled);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      
      // double-check if there's already a root set
      if (index.getRoot()!=null&&0!=DialogHelper.openDialog(
          "Sosa", 
          DialogHelper.QUESTION_MESSAGE, 
          "Are you sure you want to move index-root from "+index.getRoot()+" to "+root+"?", 
          Action2.okCancel(), workbench)) 
        return;
      
      // do it
      index.setRoot(root);
    }
  }

  private class Maintain extends Action2 {
    private Index index;
    public Maintain(Index index) {
      this.index = index;
      setText(RESOURCES.getString("action.maintain"));
      setSelected(index.isEnabled());
      this.index = index;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      boolean set = !index.isEnabled();
      index.setEnabled(set);
    }
  }

  
  private class Reindex extends Action2 implements UnitOfWork {
    private Index engine;
    public Reindex(Index engine, Context context) {
      this.engine = engine;
      setText(RESOURCES.getString("action.reindex"));
      setEnabled(engine.getRoot()!=null);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      if (engine.getRoot()==null)
        DialogHelper.openDialog("Sosa", DialogHelper.INFORMATION_MESSAGE, "Please select a Sosa root first", Action2.okOnly(), workbench);
      else
        engine.getRoot().getGedcom().doMuteUnitOfWork(this);
    }
    public void perform(Gedcom gedcom) throws GedcomException {
      engine.reindex();
    }
  }
  
  private class Remove extends Action2 implements UnitOfWork {
    private Index engine;
    private Gedcom gedcom;
    public Remove(Index engine, Gedcom gedcom) {
      this.engine = engine;
      setText(RESOURCES.getString("action.remove"));
      this.gedcom = gedcom;
      setEnabled(gedcom!=null);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      gedcom.doMuteUnitOfWork(this);
    }
    public void perform(Gedcom gedcom) throws GedcomException {
      engine.remove(gedcom);
    }
  }
}