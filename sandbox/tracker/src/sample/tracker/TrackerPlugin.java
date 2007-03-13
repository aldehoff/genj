/**
 * This GenJ Plugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package sample.tracker;

import genj.app.AfterCloseGedcom;
import genj.app.AfterOpenGedcom;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomLifecycleEvent;
import genj.gedcom.GedcomLifecycleListener;
import genj.gedcom.GedcomListener;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.plugin.ExtensionPoint;
import genj.plugin.Plugin;
import genj.plugin.PluginManager;
import genj.util.swing.Action2;
import genj.util.swing.ImageIcon;
import genj.view.BeforeShowContext;
import genj.window.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A sample plugin that tracks all changes to all gedcom files and adds an update counter to each changed entity
 */
public class TrackerPlugin implements Plugin {
  
  private PluginManager manager;
  private JTextArea text = new JTextArea(40,10);
  private Map gedcom2tracker = new HashMap();
  
  /**
   * our constructor plugin-wise
   * @see Plugin#initPlugin(PluginManager)
   */
  public void initPlugin(PluginManager manager) {
    this.manager = manager;
    text.setEditable(false);
    manager.getWindowManager().openWindow("tracker", "Tracker", new ImageIcon(this, "/Tracker.gif"), new JScrollPane(text), null, null);
  }
  
  
  /**
   * Our change to enrich an extension point
   * @see genj.plugin.Plugin#extend(genj.plugin.ExtensionPoint)
   */
  public void extend(ExtensionPoint ep) {
    
    if (ep instanceof AfterOpenGedcom) {
      // attach to gedcom
      Gedcom gedcom = ((AfterOpenGedcom)ep).getGedcom();
      GedcomTracker tracker = new GedcomTracker();
      gedcom.addLifecycleListener(tracker);
      gedcom.addGedcomListener(tracker);
      gedcom2tracker.put(gedcom, tracker);
      log("Tracker attached to "+gedcom.getName());
      // done
      return;
    }

    if (ep instanceof AfterCloseGedcom) {
      // detach from gedcom
      Gedcom gedcom = ((AfterCloseGedcom)ep).getGedcom();
      GedcomTracker tracker = (GedcomTracker)gedcom2tracker.get(gedcom);
      gedcom.removeLifecycleListener(tracker);
      gedcom.removeGedcomListener(tracker);
      log("Tracker detached from "+gedcom.getName());
    }
    
    if (ep instanceof BeforeShowContext) {
      ((BeforeShowContext)ep).getContext().addAction("Tracker", new About());
      
    }
    
  }

  /** helper for logging text */ 
  private void log(String msg) {
    // log a text message to our output area
    try {
      Document doc = text.getDocument();
      doc.insertString(doc.getLength(), msg, null);
      doc.insertString(doc.getLength(), "\n", null);
    } catch (BadLocationException e) {
      // can't happen
    }
  }
  
  /**
   * Our little about dialog action
   */
  private class About extends Action2 {
    About() {
      setText("About");
    }
    protected void execute() {
      manager.getWindowManager().openDialog("tracker.about", "Tracker", WindowManager.INFORMATION_MESSAGE, "Tracker 1.0", Action2.okOnly(), null);
    }
  } //About
    
  /**
   * Our gedcom listener
   */
  private class GedcomTracker implements GedcomListener, GedcomLifecycleListener { 

    private TagPath PATH = new TagPath(".:TRAC");
    private Set touchedEntities = new HashSet();

    public void handleLifecycleEvent(GedcomLifecycleEvent event) {
      
      // we'll update a counter for all touched entities after the unit of work has done its part 
      // but before the write lock is released piggy backing on the editor's unit of work
      // (this would be a good place to update some other (e.g. sosa) indexing scheme
      // The result should look like this
      // 0 @..@ INDI
      // 1 TRAC n
      //
      if (event.getId()==GedcomLifecycleEvent.AFTER_UNIT_OF_WORK) {
        
        List list = new ArrayList(touchedEntities);
        for (Iterator it = list.iterator(); it.hasNext();) {
          Entity entity = (Entity) it.next();
          int value;
          try {
            value = Integer.parseInt(entity.getValue(PATH, "0"))+1;
          } catch (NumberFormatException e) {
            value = 1;
          }
          entity.setValue(PATH, Integer.toString(value));
        }
      }
      
      
      // we reset our tracking state after the write lock has been released
      if (event.getId()==GedcomLifecycleEvent.WRITE_LOCK_RELEASED) {
        touchedEntities.clear();
      }
      
      // done
    }
  
    /** 
     * notification that an entity has been added 
     * @see GedcomListener#gedcomEntityAdded(Gedcom, Entity)
     */
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      log("Entity "+entity+" added to "+gedcom.getName());
      touchedEntities.add(entity);
    }
  
    /** 
     * notification that an entity has been deleted
     * @see GedcomListener#gedcomEntityDeleted(Gedcom, Entity)
     */
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      log("Entity "+entity+" deleted from "+gedcom.getName());
      touchedEntities.remove(entity);
    }
  
    /** 
     * notification that a property has been added 
     * @see GedcomListener#gedcomPropertyAdded(Gedcom, Property, int, Property)
     */
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      log("Property "+added.getTag()+" added to "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }
  
    /** 
     * notification that a property has been changed
     * @see GedcomListener#gedcomPropertyChanged(Gedcom, Property)
     */
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      log("Property "+property.getTag()+" changed to "+property.getDisplayValue()+" in "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }
  
    /** 
     * notification that a property has been deleted
     * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int, Property)
     */
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      log("Property "+deleted.getTag()+" deleted from "+property.getEntity()+" in "+gedcom.getName());
      touchedEntities.add(property.getEntity());
    }

  } //GedcomTracker
  
} //TrackerPlugin
