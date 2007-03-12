/**
 * This GenJ Plugin Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package sample.tracker;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.plugin.Plugin;
import genj.plugin.PluginEvent;
import genj.plugin.GedcomLifecycleEvent;
import genj.plugin.PluginManager;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A sample plugin that tracks all changes to all gedcom files
 */
public class TrackerPlugin implements Plugin, GedcomMetaListener {
  
  private JTextArea text = new JTextArea(40,10);
  
  /**
   * our constructor plugin-wise
   * @see Plugin#initPlugin(PluginManager)
   */
  public void initPlugin(PluginManager manager) {
    text.setEditable(false);
    manager.getWindowManager().openWindow("tracker", "Tracker", null, new JScrollPane(text), null, null);
  }
  
  /**
   * event for us
   */
  public void handlePluginEvent(PluginEvent event) {
    if (event instanceof GedcomLifecycleEvent)
      handlePluginGedcomEvent((GedcomLifecycleEvent)event);
  }

  /**
   * event for us
   */
  public void handlePluginGedcomEvent(GedcomLifecycleEvent event) {
    Gedcom gedcom = event.getGedcom();
    switch (event.getId()) {
    case GedcomLifecycleEvent.AFTER_GEDCOM_LOADED:
      // attach to gedcom
      gedcom.addGedcomListener(this);
      log("Tracker attached to "+gedcom.getName());
      break;
    case GedcomLifecycleEvent.BEFORE_GEDCOM_CLOSED:
    case GedcomLifecycleEvent.BEFORE_GEDCOM_SAVED:
      // could do some stuff to the gedcom object before it's gone
      break;
    case GedcomLifecycleEvent.AFTER_GEDCOM_CLOSED:
      // detach from gedcom
      gedcom.removeGedcomListener(this);
      log("Tracker detached from "+gedcom.getName());
      break;
    }
  }

  /** 
   * notification that a gedcom unit of work is going to start
   * @see GedcomMetaListener#gedcomBeforeUnitOfWork(Gedcom)
   */
  public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    // probably not too interesting
  }

  /** 
   * notification that a gedcom unit of work has finished 
   * @see GedcomMetaListener#gedcomAfterUnitOfWork(Gedcom)
   */
  public void gedcomAfterUnitOfWork(Gedcom gedcom) {      
    // we could do some after unit of work adjustments at this point if we like - e.g. update a sosa index
  }

  /** 
   * notification that the gedcom header has changed 
   * @see GedcomMetaListener#gedcomHeaderChanged(Gedcom)
   */
  public void gedcomHeaderChanged(Gedcom gedcom) {
    // just for info
  }

  /** 
   * notification that a gedcom write lock has been acquired 
   * @see GedcomMetaListener#gedcomWriteLockAcquired(Gedcom)
   */
  public void gedcomWriteLockAcquired(Gedcom gedcom) {
    // just for info
  }

  /** 
   * notification that a gedcom write lock has been released
   * @see GedcomMetaListener#gedcomWriteLockReleased(Gedcom)
   */
  public void gedcomWriteLockReleased(Gedcom gedcom) {
    // just for info
  }

  /** 
   * notification that an entity has been added 
   * @see GedcomListener#gedcomEntityAdded(Gedcom, Entity)
   */
  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
    log("Entity "+entity+" added to "+gedcom.getName());
  }

  /** 
   * notification that an entity has been deleted
   * @see GedcomListener#gedcomEntityDeleted(Gedcom, Entity)
   */
  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
    log("Entity "+entity+" deleted from "+gedcom.getName());
  }

  /** 
   * notification that a property has been added 
   * @see GedcomListener#gedcomPropertyAdded(Gedcom, Property, int, Property)
   */
  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
    log("Property "+added.getTag()+" added to "+property.getEntity()+" in "+gedcom.getName());
  }

  /** 
   * notification that a property has been changed
   * @see GedcomListener#gedcomPropertyChanged(Gedcom, Property)
   */
  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
    log("Property "+property.getTag()+" changed to "+property.getDisplayValue()+" in "+property.getEntity()+" in "+gedcom.getName());
  }

  /** 
   * notification that a property has been deleted
   * @see GedcomListener#gedcomPropertyDeleted(Gedcom, Property, int, Property)
   */
  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
    log("Property "+deleted.getTag()+" deleted from "+property.getEntity()+" in "+gedcom.getName());
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
    
} //TrackerPlugin
