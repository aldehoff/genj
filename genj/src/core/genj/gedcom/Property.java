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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property implements Comparable {

  /** empty string */
  protected final static String EMPTY_STRING = "";

  /** parent of this property */
  protected Property parent=null;
  
  /** children of this property */
  protected List childs = new ArrayList();
  
  /** images */
  protected ImageIcon image, imageErr;

  /**
   * Method for notifying being added to another property
   */
  public void addNotify(Property parent) {
    this.parent=parent;
    noteAddedProperty();
  }

  /**
   * Adds another property to this property
   * @param prop new property to add
   */
  public final Property addProperty(Property prop) {
    // Remember
    childs.add(prop);
    // Notify
    prop.addNotify(this);
    // Done
    return prop;
  }

  /**
   * Compares this property to another property
   * @return -1 this &lt; property <BR>
   *          0 this = property <BR>
   *          1 this &gt; property
   */
  public int compareTo(Object o) {
    // safety check
    if (!(o instanceof Property)) throw new ClassCastException("compareTo("+o+")");
    // entity?
    if (this instanceof Entity) {
      try {
        return getComparableId(((Entity)this).getId()) - getComparableId(((Entity)o).getId()); 
      } catch (NumberFormatException e) {
      }
    }
    return getValue().compareTo(((Property)o).getValue());
  }
  
  /**
   * Helper that creates an int (comparable) out of a entity ID
   */
  private int getComparableId(String id) throws NumberFormatException {
    
    int 
      start = 0,
      end   = id.length()-1;
      
    while (start<=end&&!Character.isDigit(id.charAt(start))) start++;
    while (end>=start&&!Character.isDigit(id.charAt(end))) end--;

    if (end<start) throw new NumberFormatException();
         
    return Integer.parseInt(id.substring(start, end+1));
  }

  /**
   * Method for notifying being removed from parent property
   */
  public void delNotify() {

    // Remember it
    noteDeletedProperty();

    // Say it to properties
    delProperties((Property[])childs.toArray(new Property[childs.size()]));

    // Done
  }

  /**
   * Removes an array of properties
   */
  public void delProperties(Property[] which) {
    // single swoop remove
    childs.removeAll(Arrays.asList(which));
    // notify
    for (int i=0;i<which.length;i++) {
      which[i].delNotify();
    }
    // done
  }

  /**
   * Removes a property by looking in the property's properties
   * list and eventually calling delProperty recursively
   */
  public boolean delProperty(Property which) {

    // Look for first class properties
    if (childs.contains(which)) {
      childs.remove(which);
      which.delNotify();
      return true;
    }

    // Look for second class properties
    for (int i=0;i<childs.size();i++) {
      if (getProperty(i).delProperty(which)) return true;
    }

    // Not found
    return false;
  }

  /**
   * Returns a warning string that describes what happens when this
   * property would be deleted
   * @return warning as <code>String</code>, <code>null</code> when no warning
   */
  public String getDeleteVeto() {
    return null;
  }

  /**
   * Calculates the max. depth of properties this property has.
   */
  public int getDepthOfProperties() {
    // recursive search
    int result = 0;
    for (int i=0;i<childs.size();i++) {
      result = Math.max( result, getProperty(i).getDepthOfProperties()+1 );
    }
    // Done
    return result;
  }

  /**
   * Returns the entity this property belongs to - simply looking up
   */
  public Entity getEntity() {
    return parent==null ? null : parent.getEntity();
  }

  /**
   * Returns the gedcom this property belongs to
   */
  public Gedcom getGedcom() {

    // Entity ?
    Entity entity = getEntity();
    if (entity==null) {
      return null;
    }

    // Ask Entity
    return entity.getGedcom();

  }
  
  /**
   * Returns the image which is associated with this property.
   */
  public ImageIcon getImage(boolean checkValid) {
    
    // valid or not ?
    if (!checkValid||isValid()) {
      if (image==null) image = MetaProperty.get(this).getImage(); 
      return image;
    }
    
    // not valid
    if (imageErr==null) imageErr = MetaProperty.get(this).getImage("err"); 
    return imageErr;
  }

  /**
   * Returns the index of given property - or 0 when not found.
   * @param prop Property to look for
   */
  public int getIndexOf(Property prop) {

    // Look through properties
    for (int i=0;i<getNoOfProperties();i++) {
      if (getProperty(i)==prop) {
        return i;
      }
    }

    // Not found
    return 0;
  }

  /**
   * Returns the next sibling of this property
   * @return property beside or null
   */
  public Property getNextSibling() {

    // No parent ?
    if (parent==null) {
      return null;
    }

    // Wich index is this one ?
    int index = parent.getIndexOf(this);
    if (index==-1) {
      return null;
    }

    // Me the last ?
    if (index==parent.getNoOfProperties()-1) {
      return null;
    }

    // Return next sibling
    return parent.getProperty(index+1);
  }

  /**
   * Calculates the number of properties this property has.
   */
  public int getNoOfProperties() {
    return childs.size();
  }

  /**
   * Calculates the number of properties this property has.
   * When recursive is true, sub-properties are counted recursively, too.
   * When valid is true, only valid sub-properties are counted.
   */
  public int getNoOfProperties(boolean recursive, boolean validOnly) {

    // recursive
    int result = 0;
    for (int i=0;i<childs.size();i++) {
      Property child = getProperty(i); 
      if (child.isValid() || !validOnly)
        result ++;
      if (recursive)
        result += child.getNoOfProperties(true,validOnly);
    }

    return result;
  }

  /**
   * Returns the property this property belongs to
   */
  public Property getParent() {
    return parent;
  }
  
  /**
   * Returns the path to this property
   */
  public TagPath getPath() {
    return new TagPath(getEntity().getPathTo(this));
  }

  /**
   * Returns path of properties to specified property
   */
  public Property[] getPathTo(Property prop) {

    // Create a linked list
    LinkedList result = new LinkedList();
    
    // look for it
    getPathToRecursively(result, prop);
    
    // Done
    return (Property[])result.toArray(new Property[result.size()]);
  }

  /**
   * Recursive getPathTo
   */
  private void getPathToRecursively(LinkedList path, Property prop) {
    
    // is it me?
    if (prop==this) {
      path.add(this);
      return;
    }
    
    // maybe it's one of my children
    for (int i=0;i<getNoOfProperties();i++) {
      getProperty(i).getPathToRecursively(path, prop);
      if (path.size()>0) {
        // .. add myself
        path.addFirst(this);
        break;
      } 
    }

    // not found
  }
  
  /**
   * Returns the previous sibling of this property
   * @return property beside or null
   */
  public Property getPreviousSibling() {

    // No parent ?
    if (parent==null) {
      return null;
    }

    // Wich index is this one ?
    int index = parent.getIndexOf(this);
    if (index==-1) {
      return null;
    }

    // Me the first ?
    if (index==0) {
      return null;
    }

    // Return previous sibling
    return parent.getProperty(index-1);
  }

  /**
   * Returns this property's properties which are of given type
   */
  public List getProperties(Class type) {
    List props = new ArrayList(10);
    getPropertiesRecursively(props, type);
    return props;
  }
  
  private void getPropertiesRecursively(List props, Class type) {
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      if (type.isAssignableFrom(child.getClass())) {
        props.add(child);
      }
      child.getPropertiesRecursively(props, type);
    }
  }

  /**
   * Returns this property's properties by path
   */
  public Property[] getProperties(TagPath path, boolean validOnly) {

    // Gather 'em
    List result = new ArrayList(childs.size());
    getPropertiesRecursively(path, 0, result, validOnly);

    // done
    return (Property[])result.toArray(new Property[result.size()]);
  }

  private List getPropertiesRecursively(TagPath path, int pos, List fill, boolean validOnly) {

    // Correct here ?
    if (!path.get(pos).equals(getTag())) return fill;

    // Me the last one?
    if (pos==path.length()-1) {
      // .. only when valid
      if ( (!validOnly) || (isValid()) ) 
        fill.add(this);
      // .. done
      return fill;
    }

    // Search in properties
    for (int i=0;i<childs.size();i++) {
      getProperty(i).getPropertiesRecursively(path,pos+1,fill,validOnly);
    }

    // done
    return fill;
  }

  /**
   * Returns this property's nth property
   */
  public Property getProperty(int n) {
    return (Property)childs.get(n);
  }

  /**
   * Returns this property's property by path
   */
  public Property getProperty(String path) {
    return getProperty(path, true);
  }

  /**
   * Returns this property's property by path
   * (only valid children are considered)
   */
  public Property getProperty(String path, boolean validOnly) {
    // maybe a simple tag?
    if (!TagPath.isPath(path)) {
      for (int c=0;c<getNoOfProperties();c++) {
        Property child = getProperty(c);
        if (!child.getTag().equals(path)) continue;
        if (validOnly&&!child.isValid()) continue;
        return child;
      }
      return null;
    }
    // go all the way
    return getProperty(new TagPath(path), validOnly);
  }
  
  /**
   * Returns this property's property by path
   */
  public Property getProperty(TagPath path, boolean validOnly) {

    // if we're an entity then we check the tag and skip
    if (this instanceof Entity && !getTag().equals(path.get(0))) return null;

    // look for it
    return getPropertyRecursively(path, 0, validOnly);
  }
  
  private Property getPropertyRecursively(TagPath path, int pos, boolean validOnly) {

    // Correct here ?
    if (!path.get(pos).equals(getTag())) return null;

    // Me the last one?
    if (pos==path.length()-1) 
      return !validOnly || isValid() ? this : null;

    // Search in properties
    for (int i=0;i<getNoOfProperties();i++) {
      Property p = getProperty(i).getPropertyRecursively(path, pos+1, validOnly);
      if (p!=null) return p;
    }

    // not found
    return null;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Unknown";
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public abstract String getTag();

  /**
   * Sets this property's tag
   */
  public abstract void setTag(String tag) throws GedcomException ;

  /**
   * Returns the value of this property as string.
   */
  abstract public String getValue();

  /**
   * Sets this property's value as string.
   */
  public abstract void setValue(String value);

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    return true;
  }

  /**
   * Moves a property amongst its siblings
   */
  public void move(int move) {
    
    // Look for position amongst siblings
    int pos = 0;
    while (parent.getProperty(pos)!=this) pos++;
    
    // check lower/upper boundary
    move = Math.min(1,Math.max(-1, move));
    if (move<0&&pos==0) return;
    if (move>0&&pos==parent.getNoOfProperties()-1) return;
    
    // move it
    Property sibling = parent.getProperty(pos+move);
    parent.childs.set(pos+move, this);
    parent.childs.set(pos, sibling);

    sibling.noteDeletedProperty();    
    sibling.noteAddedProperty  ();    
    noteDeletedProperty();    
    noteAddedProperty  ();    

    // done
  }

  /**
   * Notify Gedcom that this property has been added
   */
  protected void noteAddedProperty() {
    Gedcom gedcom = getGedcom();
    if (gedcom!=null) {
      gedcom.noteAddedProperty(this);
    }
  }

  /**
   * Notify Gedcom that this property has been deleted
   */
  protected void noteDeletedProperty() {
    Gedcom gedcom = getGedcom();
    if (gedcom!=null) {
      gedcom.noteDeletedProperty(this);
    }
  }

  /**
   * Notify Gedcom that this property has been changed
   */
  protected void noteModifiedProperty() {
    Gedcom gedcom = getGedcom();
    if (gedcom!=null) {
      gedcom.noteModifiedProperty(this);
    }
  }

  /**
   * The default toString returns the value of this property
   * NM 19990715 introduced to allow access to a property on a
   *             more abstract level than getValue()
   * NM 20020221 changed to return value only
   */
  public String toString() {
    return getTag()+' '+getValue();
  }
  
  /**
   * Whether this property is transient and therfor shouldn't
   * - act as a template
   * - be saved
   */
  public boolean isTransient() {
    return false;
  }

  /**
   * Adds default properties to this property
   */
  public final Property addDefaultProperties() {
    
    // only if parent set
    if (getEntity()==null) throw new IllegalArgumentException("entity is null!");
    
    // loop
    MetaProperty[] subs = getMetaProperties(MetaProperty.FILTER_DEFAULT); 
    for (int s=0; s<subs.length; s++) {
      if (getProperty(subs[s].getTag())==null)
        addProperty(subs[s].create(EMPTY_STRING)).addDefaultProperties();
    }

    // done    
    return this;
  }

  /**
   * Resolve meta properties
   * @param attr comma separated list of attributes to filter
   */
  public MetaProperty[] getMetaProperties(int filter) {
    return MetaProperty.get(this).getSubs(filter);
  }

//  /**
//   * Set of sub-properties 
//   */
//  private class PropertySet {
//    
//    /** the elements */
//    private Vector vector = new Vector();
//
//    /**
//     * Adds another property to this set
//     * @prop the property to add to this list
//     */
//    protected void add(Property prop) {
//      vector.addElement(prop);
//      prop.addNotify(Property.this);
//    }
//
//    /**
//     * Property in list?
//     */
//    protected boolean contains(Property property) {
//      return vector.contains(property);
//    }
//  
//    /**
//     * Removes a property
//     */
//    protected void delete(Property which) {
//      vector.removeElement(which);
//      which.delNotify();
//    }
//
//    /**
//     * Removes all properties
//     */
//    protected void deleteAll() {
//      Enumeration e = ((Vector)vector.clone()).elements();
//      vector.removeAllElements();
//      while (e.hasMoreElements())
//        ((Property)e.nextElement()).delNotify();
//    }
//
//    /**
//     * Returns one of the properties in this set by index
//     */
//    public Property get(int which) {
//      return ((Property)vector.elementAt(which));
//    }
//
//    /**
//     * Returns one of the properties in this set by tag
//     */
//    public Property get(String tag) {
//      Property p;
//      for (int i=0;i<getSize();i++) {
//        p = get(i);
//        if (p.getTag().equals(tag)) return p;
//      }
//      return null;
//    }
//
//    /**
//     * Returns the number of properties in this set
//     */
//    public int getSize() {
//      return vector.size();
//    }
//  
//    /**
//     * Swaps place of properties given by index
//     */
//    public void swap(int i, int j) {
//      Object o = vector.elementAt(i);
//      vector.setElementAt(vector.elementAt(j),i);
//      vector.setElementAt(o,j);
//    }          
//    
//  }

} //Property

