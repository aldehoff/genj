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

import java.util.Hashtable;
import java.util.Vector;

/**
 * Class for Hashtable of IDs
 */
public class IDHashtable {

  private Hashtable hashtable;
  private Vector    duplicates;

  /**
   * Constructor
   */
  public IDHashtable(int initialCapacity) {
    hashtable  = new Hashtable(initialCapacity);
    duplicates = new Vector(16);
  }

  /**
   * Returns existiance of key
   */
  public boolean contains(String id) {
    return hashtable.containsKey(id);
  }

  /**
   * Returns all entities by id
   */
  public EntityList getAll(String id) {

    // Calc object for id
    Object obj = hashtable.get(id);
    if (obj==null) {
      return new EntityList();
    }

    // .. ambiguous ?
    if (obj instanceof Vector) {
      return new EntityList((Vector)obj);
    }

    // .. o.k.
    return new EntityList((Entity)obj);
  }

  /**
   * Returns an entity by id
   */
  public Entity get(String id) throws DuplicateIDException {

    // Calc object for id
    Object obj = hashtable.get(id);
    if (obj==null) {
      return null;
    }

    // .. ambiguous ?
    if (obj instanceof Vector)
      throw new DuplicateIDException("Entity-ID "+id+" is ambiguous");

      // .. o.k.
      return (Entity)obj;
    }
    /**
     * Returns all duplicates in this Hashtable
     */
    public Entity[] getDuplicates() {
    Entity[] result = new Entity[duplicates.size()];
    for (int i=0;i<result.length;i++) {
      result[i]=(Entity)duplicates.elementAt(i);
    }
    return result;

  }

  /**
   * Returns wether this Hashtable has duplicate Entities (regarding ID)
   */
  public boolean hasDuplicates() {
    return (duplicates.size()>0);
  }

  /**
   * Adds an entity with id
   */
  public void put(String id, Entity entity)  {

    // Is the given id ambiguous ?
    Object obj = hashtable.get(id);
    if (obj==null) {

      // .. it's o.k.
      hashtable.put(id,entity);

      // .. done
      return;
    }

    // We have >two entities with same id
    // .. already more than one there?
    if (obj instanceof Vector) {

      // .. one more in the list
      ((Vector)obj).addElement(entity);

    } else {

      // .. new list here
      Vector v = new Vector(2);
      v.addElement(obj);
      v.addElement(entity);
      hashtable.put(id,v);

    }

    // Remember as duplicate
    duplicates.addElement(entity);

    // Done
  }

  /**
   * Removes an entity
   */
  public void remove(Entity entity) {

    // Get old entry in hashtable
    String old = entity.getId();
    Object obj = hashtable.get(old);

    // Vector of indis ?
    if (obj instanceof Vector) {

      // .. remove entity from vector
      Vector vec=(Vector)obj;
      vec.removeElement(entity);

      // .. one only of previous two left?
      if (vec.size()==1) {
      hashtable.put(old,vec.elementAt(0));
      }

      // .. forget about obj as being a duplicate
      duplicates.removeElement(entity);

      // .. done
      return;
    }

    // Entity found
    hashtable.remove(old);

    // Done
  }            
}
