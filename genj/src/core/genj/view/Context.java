/**
 * 
 */
package genj.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.ActionDelegate;

import java.util.ArrayList;
import java.util.List;

/**
 * A context
 */  
public class Context {
  
  private ViewManager manager;
  private Gedcom gedcom;
  private Entity entity;
  private Property property;
  private ArrayList actions = new ArrayList();

  /**
   * Constructor
   */
  public Context(Gedcom ged) {
    gedcom = ged;
  }
  
  /**
   * Constructor
   */
  public Context(Property prop) {
    this(prop.getGedcom(), prop.getEntity(), prop);
  }
  
  /**
   * Constructor
   */
  public Context(Entity entity) {
    this(entity.getGedcom(), entity, entity);
  }
  
  /**
   * Constructor
   */
  public Context(Gedcom ged, Entity ent, Property prop) {
    
    // property?
    if (prop!=null) {
      property = prop;
      entity = property.getEntity();
      gedcom = entity.getGedcom();
    } else {
      // entity?
      if (ent!=null) {
        entity = ent;
        gedcom = entity.getGedcom();
      } else {
        // gedcom
        gedcom = ged;
      }
      property = entity;
    }
    
    // done
  }
  
  /**
   * Connect to manager
   */
  /*package*/ void setManager(ViewManager set) {
    manager = set;
  }
  
  /**
   * valid context?
   */
  public boolean isValid() {
    return gedcom!=null && (entity!=null||property==null) && (entity==null||gedcom.contains(entity));
  }
  
  /**
   * Accessor
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor
   */
  public Entity getEntity() {
    return entity;
  }
  
  /**
   * Accessor
   */
  public Property getProperty() {
    return property;
  }

  /**
   * Add actions
   */
  public void addActions(List add) {
    actions.addAll(add);
  }
  
  /**
   * Add action
   */
  public void addAction(ActionDelegate add) {
    actions.add(add);
  }
  
  /**
   * Accessor
   */
  public List getActions() {
    return actions;
  }
  
  /**
   * Accessor
   */
  public ViewManager getManager() {
    return manager;
  }
  
  public boolean equals(Object o) {
    Context that = (Context)o;
    return this.gedcom == that.gedcom && this.entity == that.entity && this.property == that.property;
  }
  
} //Context
