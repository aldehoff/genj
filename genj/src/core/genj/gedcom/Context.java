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
package genj.gedcom;

import genj.util.swing.ImageIcon;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class Context {
  
  private Gedcom gedcom;
  private List entities = new ArrayList();
  private List properties = new ArrayList();
  private List actions = new ArrayList();
  private Class entityType = null;
  private Class propertyType = null;
  private ImageIcon  img = null;
  private String txt = null;
  
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
   * Add entities
   */
  public void addEntities(Entity[] es) {
    for (int i = 0; i < es.length; i++) 
      addEntity(es[i]);
  }
  
  /**
   * Remove entities
   */
  public void removeEntities(Collection rem) {
    entities.removeAll(rem);
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
  public void addProperties(Property[] ps) {
    for (int i = 0; i < ps.length; i++) 
      addProperty(ps[i]);
  }
  
  /**
   * Remove properties
   */
  public void removeProperties(Collection rem) {
    properties.removeAll(rem);
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
  public String getText() {
    return txt;
  }
  
  /** 
   * Accessor
   */
  public Context setText(String text) {
    txt = text;
    return this;
  }
  
  /** 
   * Accessor
   */
  public ImageIcon getImage() {
    // an override?
    if (img!=null)
      return img;
    // check prop
    if (properties.size()==1)
      return ((Property)properties.get(0)).getImage(false);
    // check entity
    if (entities.size()==1)
      return ((Entity)entities.get(0)).getImage(false);
    // fallback
    return Gedcom.getImage();
  }
  
  /** 
   * Accessor
   */
  public Context setImage(ImageIcon set) {
    img = set;
    return this;
  }

  /**
   * Add given context to this context
   */
  public void addContext(Context context) {
    if (context.getGedcom()!=getGedcom())
      throw new IllegalArgumentException();
    addProperties(context.getProperties());
    addEntities(context.getEntities());
  }
  
} //Context
