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

import java.util.ArrayList;
import java.util.List;

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
  private String       value ;

  /**
   * Empty Constructor
   */
  public PropertyXRef() {
    this(null);
  }

  /**
   * Constructor with reference
   * @param target reference of property this property links to
   */
  public PropertyXRef(PropertyXRef target) {
    // keep
    setTarget(target);
    if (target==null) value=EMPTY_STRING;
    // done    
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
      return value == null ? EMPTY_STRING : value;
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
    return EMPTY_STRING;
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
    // 20030106 No target - not valid unless overridden
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

    // Remember change
    modNotify();

    // Do it
    this.target=target;
    this.value =null  ;

    // Done
  }
  

  /**
   * Returns this reference's target
   * @return target or null
   */  
  public Property getTarget() {
    return target;
  }

  /**
   * Sets this property's value as string.
   */
  public void setValue(String vAlue) {

    modNotify();

    // referenced entity exists ?
    unlink();

    // remember value
    value = vAlue.replace('@',' ').trim();

    // done
  }
  
  /**
   * @see genj.gedcom.Property#setTag(java.lang.String)
   */
  /*package*/ Property init(String tag, String value) throws GedcomException {
    assume(getTag().equals(tag), UNSUPPORTED_TAG);
    return super.init(tag,value);
  }


  /**
   * Helper that unlinks reference to other entity.
   * removes links from other entities to this property's entity, too.
   */
  private void unlink() {

    // Referenced entities ?
    if (target==null)
      return;

    // keep referenced id
    value = getValue();

    // Forget referenced entity
    PropertyXRef t = target;
    target = null;

    // ... delete back referencing property in referenced entity
    t.getEntity().delProperty(t);

    // ... should be unlinked vice-versa now
  }

  /**
   * This property as a verbose string
   */
  public String toString() {
    Entity e = getReferencedEntity();
    if (e==null) {
      return super.toString();
    }
    return e.toString();
  }

  /**
   * The expected referenced type
   */
  public abstract String[] getTargetTypes();

  /**
   * @see genj.gedcom.Property#getDeleteVeto()
   */
  public String getDeleteVeto() {
    if (getReferencedEntity()==null) 
      return null;
    return resources.getString("prop.xref.veto");
  }

  /**
   * Return all entities that are connected to the given entity through
   * a PropertyXRef
   */
  public static Entity[] getReferences(Entity ent) {
    List result = new ArrayList(10);
    // loop through pxrefs
    List ps = ent.getProperties(PropertyXRef.class);
    for (int p=0; p<ps.size(); p++) {
    	PropertyXRef px = (PropertyXRef)ps.get(p);
      Property target = px.getTarget(); 
      if (target!=null) result.add(target.getEntity());
    }
    // done
    return (Entity[])result.toArray(new Entity[result.size()]);
  }

  /**
   * overriden to make sure we always look for sub-meta-properties
   * with FILTER_XREF
   * @see genj.gedcom.Property#getMetaProperties(int)
   */
  public MetaProperty[] getSubMetaProperties(int filter) {
    return super.getSubMetaProperties(MetaProperty.FILTER_XREF|filter);
  }
  
  /**
   * Final impl for image of xrefs
   * @see genj.gedcom.Property#getImage(boolean)
   */
  public ImageIcon getImage(boolean checkValid) {
    return overlay(super.getImage(false));
  }
  
  /**
   * Overlay image with current status
   */
  protected ImageIcon overlay(ImageIcon img) {
    ImageIcon overlay = target!=null?MetaProperty.IMG_LINK:MetaProperty.IMG_ERROR;
    return img.getOverLayed(overlay);
  }
  
  /**
   * Patched to now allow private - would require that
   * the opposite link is made private, too
   * @see genj.gedcom.Property#setPrivate(boolean, boolean)
   */
  public void setPrivate(boolean set, boolean recursively) {
    // ignored
  }


} //PropertyXRef
