/**
 * This GenJ Plugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package sample.tracker;

import genj.app.Workbench;
import genj.app.WorkbenchListener;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.util.Resources;
import genj.util.Trackable;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import genj.util.swing.ImageIcon;
import genj.view.ActionProvider;
import genj.view.View;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A sample GenJ plugin that tracks all changes to all gedcom files and adds an update counter to each changed entity
 */
public class TrackerPlugin implements WorkbenchListener, ActionProvider {
  
  final static ImageIcon IMG = new ImageIcon(TrackerPlugin.class, "/Tracker.gif");
  final static Resources RESOURCES = Resources.get(TrackerPlugin.class);
  final static Logger LOG = Logger.getLogger("sample.tracker");
  
  private boolean active = true;
  private GedcomTracker tracker = new GedcomTracker();
  private Workbench workbench;
  
  /**
   * Constructor
   */
  public TrackerPlugin(Workbench workbench) {
    
    this.workbench = workbench;
    
    workbench.addWorkbenchListener(this);
    
  }
  
  /** helper for logging text */ 
  private void log(String msg) {
    LOG.info("Tracker"+msg);
    
    TrackerView view = (TrackerView)workbench.getView(TrackerViewFactory.class);
    if (view==null)
      view = (TrackerView)workbench.openView(TrackerViewFactory.class);
    
    view.add(msg);
  }
  
  /**
   * Enable/Disable
   */
  private class EnableDisable extends Action2 {
    public EnableDisable() {
      setText();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      active = !active;
      setText();
      log("Writing TRAcs is "+(active?"enabled":"disabled"));
    }
    private void setText() {
      setText(RESOURCES.getString(active ? "action.disable" : "action.enable"));
    }
  }
  
  /**
   * Our little about dialog action
   */
  private class About extends Action2 {
    About() {
      setText(RESOURCES.getString("action.about"));
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      String text = RESOURCES.getString("info.txt", RESOURCES.getString((active?"info.active":"info.inactive")));
      DialogHelper.openDialog("Tracker", DialogHelper.INFORMATION_MESSAGE, text, Action2.okOnly(), DialogHelper.getComponent(e));
    }
  } //About
    
  /**
   * Our gedcom listener
   */
  private class GedcomTracker implements GedcomListener, GedcomMetaListener { 

    private TagPath PATH = new TagPath(".:TRAC");
    private Set<Entity> touchedEntities = new HashSet<Entity>();

    /** 
     * notification that an entity has been added
     * 
     * NOTE: this is a notification only and it's not allowed to make changes to the 
     * underlying gedcom structure at this point!
     * If Gedcom changes require subsequent changes performed by a plugin then this has 
     * to be deferred until the GedcomLifecycleListener-callback signals AFTER_UNIT_OF_WORK
     * 
     * @see GedcomListener#gedcomEntityAdded(Gedcom, Entity)
     */
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      log("Entity "+entity+" added to "+gedcom.getName());
      touchedEntities.add(entity);
    }
  
    /** 
     * notification that an entity has been deleted
     * 
     * NOTE: this is a notification only and it's not allowed to make changes to the 
     * underlying gedcom structure at this point!
     * If Gedcom changes require subsequent changes performed by a plugin then this has 
     * to be deferred until the GedcomLifecycleListener-callback signals AFTER_UNIT_OF_WORK
     * 
     * @see GedcomListener#gedcomEntityDeleted(Gedcom, Entity)
     */
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      log("Entity "+entity+" deleted from "+gedcom.getName());
      touchedEntities.remove(entity);
    }
  
    /** 
     * notification that a property has been added 
     * 
     * NOTE: this is a notification only and it's not allowed to make changes to the 
     * underlying gedcom structure at this point!
     * If Gedcom changes require subsequent changes performed by a plugin then this has 
     * to be deferred until the GedcomLifecycleListener-callback signals AFTER_UNIT_OF_WORK
     * 
     * @see GedcomListener#gedcomPropertyAdded(Gedcom, Property, int, Property)
     */
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      log("Property "+added.getTag()+" (value "+added.getDisplayValue()+") added to "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }
  
    /** 
     * notification that a property has been changed
     * 
     * NOTE: this is a notification only and it's not allowed to make changes to the 
     * underlying gedcom structure at this point!
     * If Gedcom changes require subsequent changes performed by a plugin then this has 
     * to be deferred until the GedcomLifecycleListener-callback signals AFTER_UNIT_OF_WORK
     * 
     * @see GedcomListener#gedcomPropertyChanged(Gedcom, Property)
     */
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      log("Property "+property.getTag()+" changed to "+property.getDisplayValue()+" in "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }
  
    /** 
     * notification that a property has been deleted
     * 
     * NOTE: this is a notification only and it's not allowed to make changes to the 
     * underlying gedcom structure at this point!
     * If Gedcom changes require subsequent changes performed by a plugin then this has 
     * to be deferred until the GedcomLifecycleListener-callback signals AFTER_UNIT_OF_WORK
     * 
     * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int, Property)
     */
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      log("Property "+deleted.getTag()+" deleted from "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }

    @Override
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
      
      // So we were not allowed to make changes to the underlying gedcom information
      // during the gedcomlistener callbacks - no problem: we kept track of entities
      // touched and now we'll update a counter for all touched entities ***after the 
      // unit of work has done its part**  (this is still before the write lock is released)
      
      // The result should look like this
      // 0 @..@ INDI
      // 1 TRAC n
      //
      for (Entity entity : touchedEntities) {
        int value;
        try {
          value = Integer.parseInt(entity.getValue(PATH, "0"))+1;
        } catch (NumberFormatException e) {
          value = 1;
        }
        entity.setValue(PATH, Integer.toString(value));
      }
      
    }

    @Override
    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void gedcomHeaderChanged(Gedcom gedcom) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
      // start fresh next time
      touchedEntities.clear();
    }
    
  } //GedcomTracker

  public void commitRequested(Workbench workbench) {
    // TODO Auto-generated method stub
  }


  public void gedcomClosed(Workbench workbench, Gedcom gedcom) {
    gedcom.removeGedcomListener(tracker);
    log(RESOURCES.getString("log.detached", gedcom.getName()));
  }



  public void gedcomOpened(Workbench workbench, Gedcom gedcom) {
    
    gedcom.addGedcomListener(tracker);
    log(RESOURCES.getString("log.attached", gedcom.getName()));
  }



  public void processStarted(Workbench workbench, Trackable process) {
    // TODO Auto-generated method stub
    
  }



  public void processStopped(Workbench workbench, Trackable process) {
    // TODO Auto-generated method stub
    
  }



  public void selectionChanged(Workbench workbench, Context context, boolean isActionPerformed) {
    // TODO Auto-generated method stub
    
  }



  public void viewClosed(Workbench workbench, View view) {
    // TODO Auto-generated method stub
    
  }



  public void viewOpened(Workbench workbench, View view) {
    // TODO Auto-generated method stub
    
  }



  public void viewRestored(Workbench workbench, View view) {
    // TODO Auto-generated method stub
    
  }



  public boolean workbenchClosing(Workbench workbench) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<Action2> createActions(Context context, Purpose purpose) {
    
    List<Action2> actions = new ArrayList<Action2>();
    
    switch (purpose) {
      case CONTEXT:
        actions.add(new Action2(RESOURCES.getString("action.remove"), false));
        break;
      case MENU:
        actions.add(new ActionProvider.EditActionGroup().add(new EnableDisable()));
        actions.add(new ActionProvider.HelpActionGroup().add(new About()));
        break;
    }
    
    return actions;
  }
  
} //TrackerPlugin
