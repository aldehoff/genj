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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all Gedcom changes
 */
public abstract class Change {

  /** affected */
  protected Entity entity;

  /**
   * Constructor
   */
  /*package*/ Change(Entity entity) {
    this.entity = entity;
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
   * Change affecting a single property change
   */  
  public static class PropertyValue extends Change {
    
    private String old;
    private Property changed;
    
    PropertyValue(Property changed, String old) {
      super(changed.getEntity());
      this.changed = changed;
      this.old = old;
    }
    
    void undo() {
      changed.setValue(old);
    }
    
    public Property getChanged() {
      return changed;
    }
    
    public String toString() {
      return "Changed "+changed.getPath()+" (old: "+old+")";
    }
    
    public boolean equals(Object o) {
      PropertyValue that = (PropertyValue)o;
      return this.changed==that.changed && this.old.equals(that.old);
    }
  } //PropertyModified
  
  /**
   * Structure change
   */
  public static abstract class PropertyStructure extends Change {
    
    protected Property root;
    
    protected PropertyStructure(Property root) {
      super(root.getEntity());
      this.root = root;
    }
    
    public Property getRoot() {
      return root;
    }
   
  } //Structure

  /**
   * Shuffled Properties
   */
  public static class PropertyShuffle extends PropertyStructure {
    
    private List children;
    
    PropertyShuffle(Property owner, List childrenBeforeShuffle) {
      super(owner);
      children = new ArrayList(childrenBeforeShuffle);
    }
    
    void undo() {
      root.setProperties(children);
    }
    
    public String toString() {
      return "Shuffled "+children+" of "+root.getPath();
    }
    
    public boolean equals(Object o) {
      PropertyShuffle that = (PropertyShuffle)o;
      return this.root==that.root && this.children.equals(that.children);
    }
  } //Shuffle
  
  /**
   * Added Property
   */  
  public static class PropertyAdd extends PropertyStructure {

    private int      pos;
    private Property added;
    
    PropertyAdd(Property owner, int pos, Property added) {
      super(owner);
      this.added = added;
      this.pos = pos;
    }
    
    void undo() {
      root.delProperty(added);
    }
    
    public Property getAdded() {
      return added;
    }
    
    public String toString() {
      return "Added "+added.getPath()+" (pos: "+pos+")";
    }
    
    public boolean equals(Object o) {
      PropertyAdd that = (PropertyAdd)o;
      return this.root==that.root && this.pos==that.pos && this.added==that.added;
    }
    
  } //PropertyAdded
  
  /**
   * Removed Property
   */  
  public static class PropertyDel extends PropertyStructure {
    
    private Property removed;
    private int      pos;
    
    PropertyDel(Property owner, int pos, Property removed) {
      super(owner);
      this.removed = removed;
      this.pos = pos;
    }
    
    void undo() {
      root.addProperty(removed, pos);
    }
    
    public Property getRemoved() {
      return removed;
    }

    public boolean equals(Object o) {
      PropertyDel that = (PropertyDel)o;
      return this.root==that.root && this.pos==that.pos && this.removed==that.removed;
    }
    
    public String toString() {
      return "Removed "+removed.getTag()+" from "+root.getPath()+" (pos: "+pos+")";
    }
  } //PropertyRemoved
  
  /**
   * Added Entity
   */  
  public static class EntityAdd extends Change {
    
    Gedcom gedcom;
    
    EntityAdd(Entity newEntity) {
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
  public static class EntityDel extends Change {

    Gedcom gedcom;
    
    EntityDel(Entity oldEntity) {
      super(oldEntity);
      gedcom = oldEntity.getGedcom();
    }
    
    void undo() {
      try {
        gedcom.addEntity(entity);
      } catch (GedcomException e) {
        // shouldn't happen
      }
    }

    public String toString() {
      return "Removed "+entity.getTag();
    }
  } //PropertyAdded
  
  
} //Change
