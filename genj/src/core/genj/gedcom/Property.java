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
    
  /** query flags */
  public static final int
    QUERY_ALL          = 0,
    QUERY_VALID_TRUE   = 1,
    QUERY_SYSTEM_FALSE = 2,
    QUERY_FOLLOW_LINK  = 4;

  /** parent of this property */
  private Property parent=null;
  
  /** children of this property */
  private List children = new ArrayList();
  
  /** images */
  protected ImageIcon image, imageErr;
  
  /** whether we're transient or not */
  protected boolean isTransient = false;

  /** whether we're private or not */
  protected boolean isPrivate = false;

  /**
   * Lifecycle - callback when being added to parent
   */
  /*package*/ void addNotify(Property parent) {

    // remember parent
    this.parent=parent;

    // propagate
    changeNotify(this, Change.PADD);

  }

  /**
   * Lifecycle - callback when being remove from parent
   */
  /*package*/ void delNotify() {

    // propagate
    changeNotify(this, Change.PDEL);

    // delete all properties - to avoid an endless
    // loop we first make up our mind which ones
    // to delete - that's it - anything added beyond
    // this point will be ignored
    Property[] props = getProperties();
    for (int i=0,j=props.length;i<j;i++)
      delProperty((Property)props[i]);
    
    // forget parent
    parent = null;

    // Done
  }
  
  /**
   * Lifecycle - callback expected for changes being made 
   */
  /*package*/ void modNotify() {
    // tell it to parent
    if (parent!=null)
      parent.changeNotify(this, Change.PMOD);
    // done      
  }
  
  /**
   * Lifecycle - callback when property changed. Is propagated
   * 'up' the owner chain
   * @param status Change.PMOD || Change.PDEL || Change.PADD 
   */
  /*package*/ void changeNotify(Property prop, int status) {
    // tell it to parent
    if (parent!=null)
      parent.changeNotify(prop, status);
    // done      
  }
  
  /**
   * Adds another property to this property
   * @param prop new property to add
   */
  public Property addProperty(Property prop) {
    // Remember
    children.add(prop);
    // Notify
    prop.addNotify(this);
    // Done
    return prop;
  }

  /**
   * Removes a property by looking in the property's properties
   * list and eventually calling delProperty recursively
   */
  public boolean delProperty(Property which) {

    // Look for first class properties
    if (children.remove(which)) {
      which.delNotify();
      return true;
    }

    // Look for second class properties
    for (int i=0;i<children.size();i++) {
      if (getProperty(i).delProperty(which)) 
        return true;
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
   * Calculates the number of properties this property has.
   */
  public int getNoOfProperties() {
    return children.size();
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
   * Returns this property's properties (all children)
   */
  public Property[] getProperties() {
    return toArray(children);
  }
  
  /**
   * Returns this property's properties adhering criteria
   */
  public Property[] getProperties(int criteria) {
    List result = new ArrayList(children.size());
    for (int i=0;i<children.size();i++) {
      Property child = (Property)children.get(i);
      if (!child.is(criteria))
        continue;
      result.add(child);
    }
    return toArray(result);
  }  

  /**
   * Returns this property's properties by tag
   * (only valid children are considered)
   */
  public Property[] getProperties(String tag) {
    return getProperties(tag, QUERY_VALID_TRUE);
  }

  /**
   * Returns this property's properties by tag
   */
  public Property[] getProperties(String tag, int qfilter) {
    
    // safety check
    if (tag.indexOf(':')>0) throw new IllegalArgumentException("Path not allowed");
    
    // loop children
    ArrayList result = new ArrayList();
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      // filter
      if (!child.getTag().equals(tag))
        continue;
      // 20031027 changed from is() to child.is()
      if (!child.is(qfilter))
        continue;
      // hit!        
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
  public Property[] getProperties(TagPath path, int qfilter) {

    // Gather 'em
    List result = new ArrayList(children.size());
    getPropertiesRecursively(path, 0, result, qfilter);

    // done
    return toArray(result);
  }

  private List getPropertiesRecursively(TagPath path, int pos, List fill, int qfilter) {

    // Correct here ?
    if (!path.get(pos).equals(getTag())) return fill;

    // Me the last one?
    if (pos==path.length()-1) {
      
      if (is(qfilter)) 
        fill.add(this);
        
      // .. done
      return fill;
    }

    // Search in properties
    for (int i=0;i<children.size();i++) {
      getProperty(i).getPropertiesRecursively(path,pos+1,fill,qfilter);
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
    return getProperty(path, QUERY_VALID_TRUE);
  }
  
  /**
   * Returns this property's property by path
   */
  public Property getProperty(TagPath path, int qfilter) {
    return getPropertyRecursively(path, 0, path.get(0), qfilter);
  }
  
  private Property getPropertyRecursively(TagPath path, int pos, String tag, int qfilter) {

    // go up parent chain for all coming '..'s
    if (tag.equals("..")) {
      Property p = this;
      do {
        // get parent
        p = p.getParent();
        if (p==null)
          return null;
        // skip tag (end?)
        pos ++;
        if (pos==path.length())
          break;
        // next
        tag = path.get(pos);
      } while (tag.equals(".."));
      
      // ask p to continue
      return p.getPropertyRecursively(path, pos-1, ".", qfilter); 
    }
    
    // Correct here - either '.' or matching tag?
    if (!(tag.equals(".")||tag.equals(getTag()))) 
      return null;

    // test filter
    if (!is(qfilter))
      return null;
    
    // path traversed? (it's me)
    if (pos==path.length()-1) 
      return this;
      
    // follow a link? (this probably should be into PropertyXref.class - override)
    if ((qfilter&QUERY_FOLLOW_LINK)!=0&&this instanceof PropertyXRef) {
      Property p = ((PropertyXRef)this).getReferencedEntity();
      if (p!=null) p = p.getPropertyRecursively(path, pos+1, path.get(pos+1), qfilter);
      if (p!=null)
        return p;
    }
    
    // Search in properties
    for (int i=0;i<getNoOfProperties();i++) {
      Property p = getProperty(i).getPropertyRecursively(path, pos+1, path.get(pos+1), qfilter);
      if (p!=null) 
        return p;
    }
    
    // not found
    return null;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "SimpleValue";
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public abstract String getTag();

  /**
   * Sets this property's tag
   */
  /*package*/ Property init(String tag, String value) throws GedcomException {
    // assuming concrete sub-type handles tag - keep value
    setValue(value);
    // we stay around
    return this;
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
   * Swap two childen 
   */
  public void swapProperties(Property childA, Property childB) {

    int 
      a = children.indexOf(childA),
      b = children.indexOf(childB);

    // safety check
    if (a<0||b<0)
      throw new IllegalArgumentException();
      
    // move it
      children.set(a, childB);
      children.set(b, childA);

    // tell about it
    changeNotify(childA, Change.PDEL);
    changeNotify(childA, Change.PADD);
    changeNotify(childB, Change.PADD);
    changeNotify(childB, Change.PDEL);

    // done
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
    return isTransient;
  }

  /**
   * A read-only attribute that can be honoured by the UI
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Marking a property as system can be honoured by the UI (don't show)
   */
  public boolean isSystem() {
    if (parent==null)
      return false;
    return parent.isSystem();
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
  
  /**
   * test for given criteria
   */
  private final boolean is(int criteria) {
    
    if ((criteria&QUERY_VALID_TRUE)!=0&&!isValid())
      return false;
      
    if ((criteria&QUERY_SYSTEM_FALSE)!=0&&isSystem())
      return false;
    
    return true;
  }

  /**
   * Accessor - private
   */
  public boolean isPrivate() {
    return isPrivate;
  }
  
  /**
   * Accessor - private
   */
  public void setPrivate(boolean set) {
    isPrivate = set;
    modNotify();
  }
  
} //Property

