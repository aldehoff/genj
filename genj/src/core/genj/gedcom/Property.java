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

import genj.util.Debug;
import genj.util.swing.ImageIcon;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property implements Comparable {

  private static final Property NO_KNOWN_PROPERTIES[] = {};
  private static final String   NO_KNOWN_SUB_TAGS  [] = {};

  /**
   * Common fields for every Property: parent and vector of child properties
   * Define tag and value in subclass, or hard-wire in getTag() and getValue()
   */
  protected Property  parent=null;
  protected PropertySet children=null;

  public static final int
    UP   = 1,
    DOWN = 2;

  public final static int
    NO_MULTI=1,
    MULTI_NEWLINE=2,
    MULTI_BLOCK=3;

  /**
   * Helper for getPathTo
   */
  private Vector _getPathTo(Property prop) {

    // Prop is me ?
    if (prop==this) {
      Vector result = new Vector(10);
      result.addElement(this);
      return result;
    }

    // One of my properties ?
    Vector result = null;
    for (int i=0;i<getNoOfProperties();i++) {
      // .. test it
      result = getProperty(i)._getPathTo(prop);
      if (result!=null)
      break;
      // .. next
    }

    // Found ? Add myself
    if (result!=null)
      result.addElement(this);

    // Done
    return result;
  }

  /**
   * Adds default properties to this property
   */
  public Property addDefaultProperties() {
    
    // ask for default sub-properties
    MetaProperty meta = MetaProperty.get(this);
    MetaProperty[] subs = meta.getSubs(true);
    
    // loop
    for (int s=0; s<subs.length; s++) {
      if (getProperty(subs[s].getTag())==null)
      	addProperty(subs[s].instantiate("", true));
    }
    
    return this;
  }

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
  public final void addProperty(Property prop) {

    // Make sure we have a children's list
    if (children==null) {
      children=new PropertySet();
    }

    // Remember
    children.add(prop);

    // We have to remember this as a new known (sub-)property
    MetaProperty.get(this).noteSub(prop);

    // Done
  }

  /**
   * Calculates a property's standard proxy from given TagPath
   */
  public static String calcDefaultProxy(TagPath path) {

    // Find class for tag
    Class c = MetaProperty.get(path).getPropertyClass();

    // Get proxy value
    Class  argtypes[] = { path.getClass() };
    Object arg     [] = { path            };
    try {
      Method method = c.getMethod("getProxy",argtypes);
      return (String)method.invoke(null,arg);
    } catch (Exception e) {
      Debug.log(Debug.WARNING, Property.class, e);
    }

    // Error means unknown
    return PropertyUnknown.getProxy(null);
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
   * Removes all properties
   */
  public final void delAllProperties() {
    if (children!=null) {
      children.deleteAll();
      children=null;
    }
  }

  /**
   * Method for notifying being removed from parent property
   */
  public void delNotify() {

    // Remember it
    noteDeletedProperty();

    // Say it to properties
    delAllProperties();

    // Done
  }

  /**
   * Removes an array of properties
   */
  public void delProperties(Property[] which) {
    for (int i=0;i<which.length;i++) {
      delProperty(which[i]);
    }
  }

  /**
   * Removes a property by looking in the property's properties
   * list and eventually calling delProperty recursively
   */
  public boolean delProperty(Property which) {

    // No properties ?
    if (children==null) {
      return false;
    }

    // Look for first class properties
    if (children.contains(which)) {
      children.delete(which);
      return true;
    }

    // Look for second class properties
    for (int i=0;i<children.getSize();i++) {
      if ( children.get(i).delProperty(which) ) {
        return true;
      }
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

    if (children==null) {
      return 0;
    }

    int result = 0;
    for (int i=0;i<children.getSize();i++) {
      result = Math.max( result, children.get(i).getDepthOfProperties()+1 );
    }

    // Done
    return result;
  }

  /**
   * Returns the entity this property belongs to
   */
  public Entity getEntity() {

    // Parent == Entity ?
    if (this instanceof Entity) {
      return (Entity)this;
    }

    // No Parent ?
    if (parent==null) {
      return null;
    }

    // Ask Parent
    return parent.getEntity();
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
    if (!checkValid||isValid())
      return MetaProperty.get(this).getImage();
    return MetaProperty.get(this).getImage("err");
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
   * Returns a LineIterator which can be used to iterate through
   * several lines of data in case of isMultiLine equals true;
   * Should be implemented by properties with several lines.
   * Returns null when single line only
   */
  public Enumeration getLineIterator() {
    return null;
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
    if (children==null) return 0;
    return children.getSize();
  }

  /**
   * Calculates the number of properties this property has.
   * When recursive is true, sub-properties are counted recursively, too.
   * When valid is true, only valid sub-properties are counted.
   */
  public int getNoOfProperties(boolean recursive, boolean validOnly) {

    if (children==null) return 0;

    int result = 0;
    for (int i=0;i<children.getSize();i++) {
      if (children.get(i).isValid() || (!validOnly))
      result ++;
      if (recursive)
      result += children.get(i).getNoOfProperties(true,validOnly);
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
    return new TagPath(getEntity().getProperty().getPathTo(this));
  }

  /**
   * Returns path of properties to specified property
   */
  public Property[] getPathTo(Property prop) {

    // Try to find vector
    Vector v = _getPathTo(prop);
    if (v==null) {
      return null;
    }

    // Transform to array
    Property[] result = new Property[v.size()];
    for (int i=0;i<result.length;i++) {
      result[result.length-1-i] = (Property)v.elementAt(i);
    }

    // Done
    return result;
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
    internalGetProperties(props, type);
    return props;
  }
  
  /** 
   * Fills list of properties with properties of given type
   */
  private void internalGetProperties(List props, Class type) {
    for (int c=0;c<getNoOfProperties();c++) {
      Property child = getProperty(c);
      if (type.isAssignableFrom(child.getClass())) {
        props.add(child);
      }
      child.internalGetProperties(props, type);
    }
  }

  /**
   * Returns this property's properties by path
   */
  public Property[] getProperties(TagPath path, boolean validOnly) {

    // No properties there ?
    if (children==null) {
      return new Property[0];
    }

    // Gather 'em
    Vector vresult = new Vector(children.getSize());
    getPropertiesInto(path,vresult,validOnly);

    Property[] result = new Property[vresult.size()];
    for (int i=0;i<result.length;i++) {
      result[i]=(Property)vresult.elementAt(i);
    }

    // Done
    return result;
  }

  /**
   * Helper for getProperties
   */
  private void getPropertiesInto(TagPath path,Vector fill, boolean validOnly) {

    // Correct here ?
    if (!path.getNext().equals(getTag())) {
      path.back();
      return;
    }

    // Me ?
    if (!path.hasMore()) {
      path.back();
      // .. only when valid
      if ( (validOnly) && (!isValid()) ) {
      return;
      }
      // .. add me
      fill.addElement(this);
      // .. done
      return;
    }

    // Does this one have properties ?
    if (children==null) {
      path.back();
      return;
    }

    // Search in properties
    Property p;
    for (int i=0;i<children.getSize();i++) {
      children.get(i).getPropertiesInto(path,fill,validOnly);
    }

    path.back();
  }

  /**
   * Returns this property's nth property
   */
  public Property getProperty(int n) {
    if (children==null) throw new IndexOutOfBoundsException("Index "+n+" doesn't work for empty set of properties");
    return children.get(n);
  }

  /**
   * Returns this property's property by path
   * (only valid children are considered
   */
  public Property getProperty(String path) {
    return getProperty(new TagPath(path), true);
  }
  
  /**
   * Returns this property's property by path
   */
  public Property getProperty(TagPath path, boolean validOnly) {

    // if we're an entity then we check the tag and skip
    if (this instanceof Entity) {
      if (!getTag().equals(path.getNext())) return null;
    }

    // Empty TagPath means us
    if (!path.hasMore()) {
      // .. validity?
      if ( (validOnly) && (!isValid()) ) {
        return null;
      }
      // .. we're o.k.
      return this;
    }

    // This is what we are looking for
    String next = path.getNext();

    // Loop through the children
    if (children!=null) {
      
      Property child;
      Property result;
      
      for (int i=0;i<children.getSize();i++) {
  
        child = children.get(i);
  
        // .. check that child
        if (child.getTag().equals(next)) {
          result = child.getProperty(path, validOnly);
          if (result != null) {
            return result;
          }
        }
  
        // .. we have to keep on looking
      }
      
    }

    // Getting here ... restore path for a sibling
    path.back();

    return null;
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public String getProxy() {
    return "Unknown";
  }

  /**
   * Returns the logical name of the proxy-object which knows this object
   */
  public static String getProxy(TagPath path) {
    return "Unknown";
  }

  /**
   * Returns the Gedcom-Tag of this property
   */
  public abstract String getTag();

  /**
   * Returns the value of this property as string.
   */
  abstract public String getValue();

  /**
   * Returns wether this property consists of several lines
   * @return NO_MULTI, MULTI_NEWLINE, MULTI_BLOCK
   * @see #getLineIterator()
   */
  public int isMultiLine() {
    return NO_MULTI;
  }

  /**
   * Returns <b>true</b> if this property is valid
   */
  public boolean isValid() {
    return true;
  }

  /**
   * Moves one of the (sub)properties up/down
   */
  public boolean moveProperty(Property which, int how) {

    // Does this one have properties ?
    if (children==null) {
      return false;
    }

    // Search in properties
    Property p;
    for (int i=0;i<children.getSize();i++) {
      p = children.get(i);
      // ... move it
      if (p==which) {

        int j = i+(how==UP?-1:1);
        try {
          children.swap(i,j);
          
          Property 
            pi = children.get(i),
            pj = children.get(j);
          getGedcom().noteDeletedProperty(pj);
          getGedcom().noteDeletedProperty(pi);
          getGedcom().noteAddedProperty(pj);
          getGedcom().noteAddedProperty(pi);
        } catch (Exception e) {
          return false;
        }
        return true;
      }
      // ... maybe in property ?
      if (p.moveProperty(which,how))
        return true;
      // ... try next
    }

    // Not moved ! (?)
    return false;
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
   * Sets this property's value as string.
   */
  public abstract boolean setValue(String newValue);

  /**
   * The default toString returns the value of this property
   * NM 19990715 introduced to allow access to a property on a
   *             more abstract level than getValue()
   * NM 20020221 changed to return value only
   */
  public String toString() {
    return getValue();
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
   * Finds first given text in this property (recursive)
   */
  public Property find(String text) {
    Vector result = find(text,false);
    return (result.size()>0) ? (Property)result.firstElement() : null;
  }

  /**
   * Finds given text in this property (recursive)
   */
  public Vector find(String text, boolean all) {

    // here's the result
    Vector result = new Vector(3);

    // go for it
    return find(result, text.toLowerCase(), all);

  }

  /**
   * Internal finder for given text in property (recursive)
   */
  private Vector find(Vector result, String text, boolean all) {

    // check just as a start
    if (getValue().toLowerCase().indexOf(text)>=0) {

      // remember
      result.addElement(this);

      // no more?
      if (!all) {
        return result;
      }
    }

    // check the children
    if (children!=null) {
      for (int i=0;i<children.getSize();i++) {
        children.get(i).find(result, text, all);
      }
    }

    // done
    return result;
  }

  /**
   * Set of sub-properties 
   */
  protected class PropertySet {
    
    /** the elements */
    private Vector vector = new Vector();

    /**
     * Adds another property to this set
     * @prop the property to add to this list
     */
    protected void add(Property prop) {
      vector.addElement(prop);
      prop.addNotify(Property.this);
    }

    /**
     * Property in list?
     */
    protected boolean contains(Property property) {
      return vector.contains(property);
    }
  
    /**
     * Removes a property
     */
    protected void delete(Property which) {
      vector.removeElement(which);
      which.delNotify();
    }

    /**
     * Removes all properties
     */
    protected void deleteAll() {
      Enumeration e = ((Vector)vector.clone()).elements();
      vector.removeAllElements();
      while (e.hasMoreElements())
        ((Property)e.nextElement()).delNotify();
    }

    /**
     * Returns one of the properties in this set by index
     */
    public Property get(int which) {
      return ((Property)vector.elementAt(which));
    }

    /**
     * Returns one of the properties in this set by tag
     */
    public Property get(String tag) {
      Property p;
      for (int i=0;i<getSize();i++) {
        p = get(i);
        if (p.getTag().equals(tag)) return p;
      }
      return null;
    }

    /**
     * Returns the number of properties in this set
     */
    public int getSize() {
      return vector.size();
    }
  
    /**
     * Swaps place of properties given by index
     */
    public void swap(int i, int j) {
      Object o = vector.elementAt(i);
      vector.setElementAt(vector.elementAt(j),i);
      vector.setElementAt(o,j);
    }          
    
  } // PropertySet
  
} //Property

