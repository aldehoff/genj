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

import java.util.*;

/**
 * Gedcom Property : ABC
 * Class for encapsulating a property that describes a Reference to an entity
 * @author  Nils Meier
 * @version 0.1 04/29/98
 */
public abstract class PropertyXRef extends Property {

  /** the target property that this xref references */
  private PropertyXRef target;

  /** the value for a broken xref */
  protected String       value ;

  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertyXRef(PropertyXRef target) {

    setTarget(target);
    
    if (target==null) value="";
    
  }

  /**
   * Constructor with Tag,Value parameters
   * @param tag property's tag
   * @param value property's value
   */
  public PropertyXRef(String tag, String value) {
    setValue(value);
  }

  /**
   * Method for notifying being removed from another parent
   */
  public void delNotify() {

    // Make sure the referenced entity doesn't reference back anymore
    unlink();

    // Let it through
    super.delNotify();
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   * @return proxy's logical name
   */
  public String getProxy() {
    return "XRef";
  }

  /**
   * Returns the name of the proxy-object which knows properties looked
   * up by TagPath
   * @return proxy's logical name
   */
  public static String getProxy(TagPath path) {
    return "XRef";
  }

  /**
   * Returns the entity this reference points to
   * @return entity this property links to
   */
  public Entity getReferencedEntity() {
    if (target==null) {
      return null;
    }
    return target.getEntity();
  }

  /**
   * Returns the id of the referenced entity
   * @return referenced entity's id
   */
  public String getReferencedId() {
    if (target==null) {
      return value == null ? "" : value;
    }
    return target.getEntity().getId();
  }

  /**
   * Returns the value of this property as string.
   * @return value of this property as <code>String</code>
   */
  public String getValue() {
    if (target!=null) {
      return "@"+target.getEntity().getId()+"@";
    }
    if ((value!=null)&&(value.length()>0)) {
      // NM 20020221 a non linked value without @' might look better
      //return "@"+value+"@";
      return value;
    }
    return "";
  }

  /**
   * Returns <b>true</b> if this property is valid
   * @return <code>boolean</code> true or false
   */
  public boolean isValid() {
    // Valid target?
    if (target!=null) {
      return true;
    }
    // Empty value?
    if ((value==null)||(value.length()==0)) {
      // for example : a NOTE entity - no reference value -
      // contains data -> valid!
      return true;
    }
    return false;
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public abstract void link() throws GedcomException;

  /**
   * Sets the entity's property this reference points to
   * @param target referenced entity's property
   */
  protected void setTarget(PropertyXRef target) {

    // Did we get an entity that we want to link to?
    if (target instanceof Entity) {
      // .. create a 'substitute' foreign x-ref 
      PropertyForeignXRef fx = new PropertyForeignXRef(this);
      ((Entity)target).addForeignXRef(fx);
      target = fx;
    }

    // Remember change
    noteModifiedProperty();

    // Do it
    this.target=target;
    this.value =null  ;

    // Done
  }

  /**
   * Sets this property's value as string.
   */
  public boolean setValue(String value) {

    noteModifiedProperty();

    // referenced entity exists ?
    unlink();

    // remember value
    this.value=value.replace('@',' ').trim();

    // done
    return true;
  }

  /**
   * Helper that unlinks reference to other entity.
   * removes links from other entities to this property's entity, too.
   */
  private void unlink() {

    // Referenced entities ?
    if (target==null) {
      return;
    }

    // Forget referenced entity
    PropertyXRef t = target;
    target = null;

    // ... delete back referencing property in referenced entity
    t.getEntity().getProperty().delProperty(t);

    // ... should be unlinked vice-versa now
  }

  /**
   * This property as a verbose string
   */
  public String toString() {
    Entity e = getReferencedEntity();
    if (e==null) {
      return "";
    }
    return e.toString();
  }

  /**
   * The expected referenced type
   */
  public abstract int getExpectedReferencedType();

}
