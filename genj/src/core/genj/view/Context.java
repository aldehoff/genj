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
import genj.util.swing.ImageIcon;

import java.awt.Component;

import javax.swing.JComponent;

/**
 * A context represents a 'current context in Gedcom terms', a gedcom
 * an entity and a property
 */  
public class Context {
  
  private ViewManager manager;
  private Gedcom gedcom;
  private Entity entity;
  private Property property;
  private Component source;
  private boolean isPropagated = false;

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
    if (prop!=null && (prop instanceof Entity||prop.getParent()!=null) ) {
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
    }
    
    // done
  }
  
  /**
   * Whether the context has been propagated
   */
  /*package*/ boolean isPropagated() {
    return isPropagated;
  }
  
  /**
   * Whether the context has been propagated
   */
  /*package*/ void setPropagated() {
    isPropagated = true;
    source = null;
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
  public void setSource(Component set) {
    source = set;
  }
  
  /**
   * Accessor - try to identify a view this context came from
   */
  public JComponent getView() {
    // no source no view
    if (source==null)
      return null;
    // run up the chain
    while (true) {
      Component parent = source.getParent();
      if (parent==null)
        break;
      if (parent instanceof ViewContainer)
        return (JComponent)source;
      source = parent;
    }
    // none found
    return null;
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
   * Accessor
   */
  public ViewManager getManager() {
    return manager;
  }
  
  /**
   * Comparison
   */
  public boolean equals(Object o) {
    Context that = (Context)o;
    return that!=null && this.gedcom == that.gedcom && this.entity == that.entity && this.property == that.property;
  }
  
  /**
   * String representation
   */
  public String toString() {
    return gedcom+"/"+entity+"/"+property;
  }

  /**
   * An image representation
   */
  public ImageIcon getImage() {
    if (property!=null)
      return property.getImage(false);
    if (entity!=null)
      return entity.getImage(false);
    return Gedcom.getImage();
  }

} //Context
