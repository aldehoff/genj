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

import java.util.Vector;
import java.util.Enumeration;

/**
 * Class for encapsulating a set of Entities
 */
public class EntityList implements Cloneable {

  private Vector vector;

  /**
   * Constructor
   */
  public EntityList() {
    this(new Vector());
  }

  /**
   * Constructor
   */
  public EntityList(Entity entity) {
    this();
    add(entity);
  }

  /**
   * Constructor
   */
  public EntityList(Vector vector) {
    this.vector = vector;
  }

  /**
   * Constructor with pre-occupied number of entities
   */
  public EntityList(int initialCapacity) {
    vector = new Vector(initialCapacity);
  }

  /**
   * Constructor with number of pre-occupied entities
   * and number of persons to be incremented during growth
   */
  public EntityList(int initialCapacity, int capacityIncrement) {
    vector = new Vector(initialCapacity, capacityIncrement);
  }

  /**
   * Adds another entity to this set
   */
  EntityList add(Entity entity) {
    vector.addElement(entity);
    return this;
  }

  /**
   * Adds other entities to this set
   */
  EntityList add(EntityList entities) {
    vector.ensureCapacity(getSize()+entities.getSize());
    for (int i=0;i<entities.getSize();i++) {
      vector.addElement(entities.get(i));
    }
    return this;
  }

  /**
   * Clones this set
   */
  public Object clone() {

    EntityList result=null;
    try {
      result = (EntityList)super.clone();
      result.vector = (Vector)vector.clone();
    } catch (CloneNotSupportedException e) {
    }

    return result;
  }

  /**
   * Checks wether an entity belongs to this set
   * @param entity the Entity to be looked for
   * @return true or false
   */
  public boolean contains(Entity entity) {
    if (entity==null) {
      return false;
    }
    return vector.contains(entity);
  }

  /**
   * Copies kept Entities into Vector
   */
  public void copyInto(Vector v) {
    Enumeration e = vector.elements();
    while (e.hasMoreElements())
    v.addElement(e.nextElement());
  }

  /**
   * Removes an entity
   */
  EntityList del(int which) {
    vector.removeElementAt(which);
    return this;
  }

  /**
   * Removes an entity
   */
  EntityList del(Entity which) {
    vector.removeElement(which);
    return this;
  }

  /**
   * Returns one of the entities in this set
   */
  public Entity get(int which) {
    return ((Entity)vector.elementAt(which));
  }

  /**
   * Returns entity as family
   */
  public Fam getFam(int which) {
    return ((Fam)get(which));
  }

  /**
   * Returns entity as individual
   */
  public Indi getIndi(int which) {
    return ((Indi)get(which));
  }

  /**
   * Returns entity as media
   */
  public Media getMedia(int which) {
    return ((Media)get(which));
  }

  /**
   * Returns entity as note
   */
  public Note getNote(int which) {
    return ((Note)get(which));
  }

  /**
   * Returns the number of entities in this set
   */
  public int getSize() {
    return(vector.size());
  }

  /**
   * Returns the elements as an array
   */
  public Entity[] toArray() {
    Entity[] result = new Entity[vector.size()];
    Enumeration es = vector.elements();
    for (int e=0;es.hasMoreElements();e++) {
      result[e] = (Entity) es.nextElement();
    }
    return result;
  }
}

