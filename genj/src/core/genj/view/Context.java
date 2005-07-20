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
import genj.util.ActionDelegate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class Context {
  
  private ViewManager manager;
  private Gedcom gedcom;
  private Set entities = new HashSet();
  private Set properties = new HashSet();
  private List actions = new ArrayList();
  private Class entityType = null;
  
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
    if (e.getGedcom()!=gedcom)
      throw new IllegalArgumentException("entity's gedcom can't be different");
    if (entityType!=null&&entityType!=e.getClass())
      throw new IllegalArgumentException("can't mix and match entity types");
    entityType = e.getClass();
    entities.add(e);
  }
  
  /**
   * Add an property
   */
  public void addProperty(Property p) {
    if (p.getGedcom()!=gedcom)
      throw new IllegalArgumentException("entity's gedcom can't be different");
    properties.add(p);
    entities.add(p.getEntity());
  }
  
  /**
   * Add an action
   */
  public Context addAction(ActionDelegate action) {
    actions.add(action);
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
   * Accessor - all entities if available
   */
  public Entity[] getEntities() {
    if (entities.isEmpty())
      return new Entity[0];
    Entity[] result = (Entity[])Array.newInstance(entityType, entities.size());
    entities.toArray(result);
    return result;
  }

  /**
   * Accessor - the SINGLE entity if available
   */
  public Entity getEntity() {
    // only for a singleton context
    if (entities.size()!=1)
      return null;
    // check entity valid
    Entity e = (Entity)entities.iterator().next();
    if (!gedcom.contains(e)) {
      e = null;
      entities.clear();
    }
    // done
    return e;
  }
  
  /**
   * Accessor - the SINGLE property if available
   */
  public Property getProperty() {
    // only for a singleton context
    if (properties.size()!=1)
      return null;
    // check prop valid
    Property p = (Property)properties.iterator().next();
    Entity e = p.getEntity();
    if (e==null||!gedcom.contains(e)) {
      p = null;
      properties.clear();
      entities.remove(e);
    }
    // done
    return p;
  }

  /**
   * Accessor
   */
  public ViewManager getManager() {
    return manager;
  }
  
} //Context
