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

/**
 * Abstract base class for all Gedcom changes
 */
public abstract class Change {

  /** affected */
  protected Entity entity;
  protected Property prop;

  /**
   * Constructor
   */
  /*package*/ Change(Entity entity) {
    this(entity, entity);
  }

  /**
   * Constructor
   */
  /*package*/ Change(Entity entity, Property prop) {
    this.entity = entity;
    this.prop = prop;
  }

  /**
   * Undo the change
   */
  /*package*/ abstract void undo();
  
  /**
   * Affected entity
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * Affected property
   */
  public Property getProperty() {
    return prop;
  }

  /**
   * Change affecting a single property change
   */  
  public static class PropertyChanged extends Change {
    
    private String old;
    
    PropertyChanged(Property propBeforeChange) {
      super(propBeforeChange.getEntity(), propBeforeChange);
      old = propBeforeChange.getValue();
    }
    
    void undo() {
      prop.setValue(old);
    }
    
    public String toString() {
      return "Changed "+prop.getPath()+" (old: "+old+")";
    }

    
  } //PropertyModified

  /**
   * Added Property
   */  
  public static class PropertyAdded extends Change {

    private Property owner;
    private int      pos;
    
    PropertyAdded(Property owner, int pos, Property added) {
      super(owner.getEntity(), added);
      this.owner = owner;
      this.pos = pos;
    }
    
    public Property getOwner() {
      return owner;
    }
    
    public int getPosition() {
      return pos;
    }
    
    void undo() {
      owner.delProperty(prop);
    }
    
    public String toString() {
      return "Added "+prop.getPath()+" (pos: "+pos+")";
    }
    
  } //PropertyAdded
  
  /**
   * Removed Property
   */  
  public static class PropertyRemoved extends Change {
    
    private Property owner;
    private int      pos;
    
    PropertyRemoved(Property owner, int pos, Property removed) {
      super(owner.getEntity(), removed);
      this.owner = owner;
      this.pos = pos;
    }
    
    public Property getOwner() {
      return owner;
    }
    
    public int getPosition() {
      return pos;
    }
    
    void undo() {
      owner.addProperty(prop, pos);
    }

    public String toString() {
      return "Removed "+prop.getTag()+" from "+owner.getPath()+" (pos: "+pos+")";
    }
  } //PropertyRemoved
  
  /**
   * Added Entity
   */  
  public static class EntityAdded extends Change {
    
    Gedcom gedcom;
    
    EntityAdded(Entity newEntity) {
      super(newEntity);
      gedcom = newEntity.getGedcom();
    }
    
    void undo() {
      gedcom.deleteEntity(entity);
    }

    public String toString() {
      return "Added "+entity.getTag();
    }
  } //PropertyAdded
  
  /**
   * Removed Entity
   */  
  public static class EntityRemoved extends Change {

    Gedcom gedcom;
    
    EntityRemoved(Entity oldEntity) {
      super(oldEntity);
      gedcom = oldEntity.getGedcom();
    }
    
    void undo() {
      gedcom.addEntity(entity);
    }

    public String toString() {
      return "Removed "+entity.getTag();
    }
  } //PropertyAdded
  
  
} //Change
