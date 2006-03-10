/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.view;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.util.swing.Action2;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class Context {
  
  private ViewManager manager;
  private Gedcom gedcom;
  private List entities = new ArrayList();
  private List properties = new ArrayList();
  private List actions = new ArrayList();
  private Class entityType = null;
  private Class propertyType = null;
  
  /**
   * Constructor
   */
  public Context(Gedcom ged) {
    if (ged==null)
      throw new IllegalArgumentException("gedcom for context can't be null");
    gedcom = ged;
  }
  
  /**
   * Constructor
   */
  public Context(Property prop) {
    this(prop.getGedcom());
    addProperty(prop);
  }
  
  /**
   * Constructor
   */
  public Context(Entity entity) {
    this(entity.getGedcom());
    addEntity(entity);
  }
  
  /**
   * Add an entity
   */
  public void addEntity(Entity e) {
    // keep as property 
    addProperty(e);
  }
  
  /**
   * Add a property
   */
  public void addProperty(Property p) {
    // check gedcom
    if (p.getGedcom()!=gedcom)
      throw new IllegalArgumentException("properties' gedcom can't be different");
    // keep track of entity/types we contain
    Entity e = p.getEntity();
    entities.remove(e);
    if (entityType!=null&&entityType!=e.getClass())
      entityType = Entity.class;
    else 
      entityType = e.getClass();
    entities.add(e);
    // keep track of property types we contain
    properties.remove(p);
    if (propertyType!=null&&propertyType!=p.getClass())
      propertyType = Property.class;
    else 
      propertyType = p.getClass();
    // keep it
    properties.add(p);
  }
  
  /**
   * Add properties
   */
  public void addProperties(Collection ps) {
    for (Iterator it = ps.iterator();it.hasNext();) {
      addProperty((Property)it.next());
    }
  }
  
  /**
   * Add an action
   */
  public Context addAction(Action2 action) {
    actions.add(action);
    return this;
  }
  
  /**
   * Add actions
   */
  public Context addActions(Action2.Group group) {
    actions.add(group);
    return this;
  }
  
  /**
   * Access to actions
   */
  public List getActions() {
    return Collections.unmodifiableList(actions);
  }
  
  /**
   * Connect to manager
   */
  /*package*/ void setManager(ViewManager set) {
    manager = set;
  }
  
  /**
   * Accessor
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * Accessor - last entity selected
   */
  public Entity getEntity() {
    Entity[] es = getEntities();
    return es.length>0 ? es[es.length-1] : null;
  }
  
  /**
   * Accessor - last property selected
   */
  public Property getProperty() {
    Property[] ps = getProperties();
    return ps.length>0 ? ps[ps.length-1] : null;
  }
  
  /**
   * Accessor - all entities
   */
  public Entity[] getEntities() {
    // nothing there?
    if (entities.isEmpty())
      return new Entity[0];
    // check for still valid entities
    for (ListIterator it = entities.listIterator(); it.hasNext(); ) {
      if (!gedcom.contains((Entity)it.next()))
        it.remove();
    }
    Entity[] result = (Entity[])Array.newInstance(entityType, entities.size());
    entities.toArray(result);
    return result;
  }

  /**
   * Accessor - properties
   */
  public Property[] getProperties() {
    // nothing there?
    if (properties.isEmpty())
      return new Property[0];
    // check for still valid properties
    for (ListIterator it = properties.listIterator(); it.hasNext(); ) {
      Property p = (Property)it.next();
      Entity e = p.getEntity();
      if (e==null||!gedcom.contains(e))
        it.remove();
    }
    Property[] result = (Property[])Array.newInstance(propertyType, properties.size());
    properties.toArray(result);
    return result;
  }

  /**
   * Accessor
   */
  public ViewManager getManager() {
    return manager;
  }
  
} //Context
