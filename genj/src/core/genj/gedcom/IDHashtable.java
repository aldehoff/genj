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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Class for Hashtable of IDs
 */
public class IDHashtable {

  private Map hashtable;
  private List duplicates;
  
  /**
   * Constructor
   */
  public IDHashtable(int initialCapacity) {
    hashtable  = new HashMap(initialCapacity);
    duplicates = new ArrayList(16);
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
  public List getAll(String id) {

    // Calc object for id
    Object obj = hashtable.get(id);
    if (obj==null) {
      return Collections.EMPTY_LIST;
    }

    // .. ambiguous ?
    if (obj instanceof List) {
      return Collections.unmodifiableList((List)obj);
    }

    // .. o.k.
    List result = new ArrayList(1);
    result.add(obj);
    return result;
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
    if (obj instanceof List)
      throw new DuplicateIDException("Entity-ID "+id+" is ambiguous");

    // .. o.k.
    return (Entity)obj;
  }
    
  /**
   * Returns all duplicates in this Hashtable
   */
  public List getDuplicates() {
    return Collections.unmodifiableList(duplicates);
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
    if (obj instanceof List) {
      // .. one more in the list
      ((List)obj).add(entity);
    } else {
      // .. new list here
      List l = new ArrayList(2);
      l.add(obj);
      l.add(entity);
      hashtable.put(id,l);
    }

    // Remember as duplicate
    duplicates.add(entity);

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
    if (obj instanceof List) {

      // .. remove entity from vector
      List l = (List)obj;
      l.remove(entity);

      // .. one only of previous two left?
      if (l.size()==1) {
        hashtable.put(old,l.get(0));
      }

      // .. forget about obj as being a duplicate
      duplicates.remove(entity);

      // .. done
      return;
    }

    // Entity found
    hashtable.remove(old);

    // Done
  }            
  
} //IDHashtable
