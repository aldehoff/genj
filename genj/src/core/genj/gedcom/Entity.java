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
 * Abstract base type for all Entities
 */
public class Entity extends Property {
  
  /** the containing gedcom */
  private Gedcom gedcom;
  
  /** the id */
  private String id;
  
  /** the tag */
  private String tag;
  
  /** just in case someone's using a value */
  private String value;
  
  /**
   * Lifecycle - callback when being added to Gedcom
   */
  /*package*/ void addNotify(Gedcom ged) {
    
    // remember
    gedcom = ged;

    // note
    Transaction tx = gedcom.getTransaction();
    if (tx!=null) {
      tx.get(Transaction.ENTITIES_ADDED).add(this);
      tx.addChange(new Change.EntityAdded(this));
    }
    
    // propagate to property
    addNotify(null, tx);

    // done    
  }
  
  /**
   * Lifecycle - callback when being removed from Gedcom
   */
  /*package*/ void delNotify() {
    
    // propagate to property hierarchy
    Transaction tx = gedcom.getTransaction();
    delNotify(tx);

    // housekeeping
    if (tx!=null) {
      tx.get(Transaction.ENTITIES_DELETED).add(this);
      tx.addChange(new Change.EntityRemoved(this));
    }
    
    // forget gedcom
    gedcom = null;
    
    // done    
  }
  
  /**
   * Propagate changed property
   */
  /*package*/ void propagateChanged(Property changed) {

    // always tell to modified first
    Transaction tx = gedcom.getTransaction();

    changed.modNotify(tx);    
    
    if (tx==null)
      return;
      
    Change change = new Change.PropertyChanged(changed);
    
    tx.get(Transaction.ENTITIES_MODIFIED).add(this);
    tx.addChange(change);
    
    // Reflect change of property 
    PropertyChange.update(this, tx, change);
      
  }
  
  /**
   * Propagate added property
   */
  /*package*/ void propagateAdded(Property owner, int pos, Property added) {

    // always tell to added first
    Transaction tx = gedcom.getTransaction();

    added.addNotify(owner, tx);
    
    // more housekeeping?
    if (tx==null)
      return;

    Change change = new Change.PropertyAdded(owner, pos, added);
      
    tx.get(Transaction.ENTITIES_MODIFIED).add(this);
    tx.addChange(change);
    
    // Reflect change of property 
    PropertyChange.update(this, tx, change);
      
  }

  /**
   * Propagate removed property
   */
  /*package*/ void propagateRemoved(Property owner, int pos, Property removed) {

    // always tell to removed first
    Transaction tx = gedcom.getTransaction();

    removed.delNotify(tx);

    // more housekeeping?
    if (tx==null)
      return;

    Change change = new Change.PropertyRemoved(owner, pos, removed);
    
    tx.get(Transaction.ENTITIES_MODIFIED).add(this);
    tx.addChange(change);
    
    // Reflect change of property 
    PropertyChange.update(this, tx, change);

  }
  
  /**
   * Return the last change of this entity (might be null)
   */
  public PropertyChange getLastChange() {
    return (PropertyChange)getProperty("CHAN");
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }
  
  /**
   * @see genj.gedcom.Property#getEntity()
   */
  public Entity getEntity() {
    return this;
  }

  /**
   * Returns entity's id
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Initialize entity
   */
  /*package*/ void init(String setTag, String setId) {
    tag = setTag;
    id = setId;
  }
  
  /**
   * @see genj.gedcom.Property#toString()
   */
  public String toString() {
    return getTag() + ' ' + id;
  }

  /**
   * @see genj.gedcom.PropertyNote#getProxy()
   */
  public String getProxy() {
    return "Entity";
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return tag;
  }
  
  /**
   * @see genj.gedcom.Property#getValue()
   */
  public String getValue() {
    return value!=null?value:EMPTY_STRING;
  }
  
  /**
   * @see genj.gedcom.Property#setValue(java.lang.String)
   */
  public void setValue(String set) {
    value = set;
  }

  /**
   * @see genj.gedcom.Property#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    try {
      Entity other = (Entity)o;
      return getID() - other.getID(); 
    } catch (Throwable t) {
    }
    return super.compareTo(o);
  }

  /**
   * Returns a comparable id
   */
  private int getID() throws NumberFormatException {
    
    int 
      start = 0,
      end   = id.length()-1;
      
    while (start<=end&&!Character.isDigit(id.charAt(start))) start++;
    while (end>=start&&!Character.isDigit(id.charAt(end))) end--;

    if (end<start) throw new NumberFormatException();
         
    return Integer.parseInt(id.substring(start, end+1));
  }

} //Entity
