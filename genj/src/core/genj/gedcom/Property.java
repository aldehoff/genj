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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property implements Comparable {

  /** static strings */
  protected final static String 
    EMPTY_STRING = "",
    UNSUPPORTED_TAG = "Unsupported Tag";

  /** parent of this property */
  private Property parent=null;
  
  /** children of this property */
  private List children = new ArrayList();
  
  /** images */
  protected ImageIcon image, imageErr;

  /**
   * Lifecycle - callback when being added to parent
   */
  /*package*/ void addNotify(Property parent) {
    this.parent=parent;
    noteAddedProperty();
  }

  /**
   * Lifecycle - callback when being remove from parent
   */
  /*package*/ void delNotify() {

    // Remember it
    noteDeletedProperty();

    // Say it to properties
    delProperties(toArray(children));
    
    // forget parent
    parent = null;

    // Done
  }
  
  /**
   * Adds another property to this property
   * @param prop new property to add
   */
  public final Property addProperty(Property prop) {
    // Remember
    children.add(prop);
    // Notify
    prop.addNotify(this);
    // Done
    return prop;
  }

  /**
   * Removes an array of properties
   */
  public void delProperties(Property[] which) {
    // single swoop remove
    children.removeAll(Arrays.asList(which));
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
    if (children.contains(which)) {
      children.remove(which);
      which.delNotify();
      return true;
    }

    // Look for second class properties
    for (int i=0;i<children.size();i++) {
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
    for (int i=0;i<children.size();i++) {
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
   * Returns the index of given property or -1 when not found.
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
    return -1;
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
    return children.size();
  }

  /**
   * Calculates the number of properties this property has.
   * When recursive is true, sub-properties are counted recursively, too.
   * When valid is true, only valid sub-properties are counted.
   */
  public int getNoOfProperties(boolean recursive, boolean validOnly) {

    // recursive
    int result = 0;
    for (int i=0;i<children.size();i++) {
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
    return toArray(result);
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
   * Returns this property's properties (all children)
   */
  public Property[] getProperties() {
    return toArray(children);
  }

  /**
   * Returns this property's properties by tag
   * (only valid children are considered)
   */
  public Property[] getProperties(String tag) {
    return getProperties(tag, true);
  }

  /**
   * Returns this property's properties by tag
   */
  public Property[] getProperties(String tag, boolean validOnly) {
    // safety check
    if (tag.indexOf(':')>0) throw new IllegalArgumentException("Path not allowed");
    // loop children
    ArrayList result = new ArrayList();
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      if (child.getTag().equals(tag)&&(!validOnly||child.isValid()))
        result.add(child);  
    }
    // not found
    return toArray(result);
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
    List result = new ArrayList(children.size());
    getPropertiesRecursively(path, 0, result, validOnly);

    // done
    return toArray(result);
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
    for (int i=0;i<children.size();i++) {
      getProperty(i).getPropertiesRecursively(path,pos+1,fill,validOnly);
    }

    // done
    return fill;
  }

  /**
   * Returns this property's nth property
   */
  public Property getProperty(int n) {
    return (Property)children.get(n);
  }

  /**
   * Returns this property's property by tag
   * (only valid children are considered)
   */
  public Property getProperty(String tag) {
    return getProperty(tag, true);
  }

  /**
   * Returns this property's property by path
   */
  public Property getProperty(String tag, boolean validOnly) {
    // safety check
    if (tag.indexOf(':')>0) throw new IllegalArgumentException("Path not allowed");
    // loop children
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      if (!child.getTag().equals(tag)) continue;
      if (validOnly&&!child.isValid()) continue;
      return child;
    }
    // not found
    return null;
  }

  /**
   * Returns this property's property by path
   */
  public Property getProperty(TagPath path) {
    return getProperty(path, true);
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

    // Validity?
    if (validOnly && !isValid()) return null;
    
    // Me?
    if (pos==path.length()-1) 
      return this;

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
  /*package*/ void setTag(String tag) throws GedcomException {
  }
  
  /**
   * Assertion (this was assert once but that's deprecated with 1.4)
   */
  protected void assume(boolean condition, String explanation) throws GedcomException {
    if (!condition) throw new GedcomException(explanation);
  }

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
    parent.children.set(pos+move, this);
    parent.children.set(pos, sibling);

    sibling.noteDeletedProperty();    
    sibling.noteAddedProperty  ();    
    noteDeletedProperty();    
    noteAddedProperty  ();    

    // done
  }

  /**
   * Notify Gedcom that this property has been added
   */
  /*package*/ void noteAddedProperty() {
    Gedcom gedcom = getGedcom();
    if (gedcom!=null) {
      gedcom.noteAddedProperty(this);
    }
  }

  /**
   * Notify Gedcom that this property has been deleted
   */
  /*package*/ void noteDeletedProperty() {
    Gedcom gedcom = getGedcom();
    if (gedcom!=null) {
      gedcom.noteDeletedProperty(this);
    }
  }

  /**
   * Notify Gedcom that this property has been changed
   */
  /*package*/ void noteModifiedProperty() {
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
   * Compares this property to another property
   * @return -1 this &lt; property <BR>
   *          0 this = property <BR>
   *          1 this &gt; property
   */
  public int compareTo(Object o) {
    // safety check
    if (!(o instanceof Property)) throw new ClassCastException("compareTo("+o+")");
    return getValue().compareTo(((Property)o).getValue());
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

  /**
   * Convert collection of properties into array
   */
  protected static Property[] toArray(Collection ps) {
    return (Property[])ps.toArray(new Property[ps.size()]);
  }
  
} //Property

