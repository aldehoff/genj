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

import genj.util.Resources;
import genj.util.swing.ImageIcon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

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
  
  /** whether we're transient or not */
  protected boolean isTransient = false;

  /** whether we're private or not */
  private boolean isPrivate = false;
  
  /** resources */
  protected final static Resources resources = Gedcom.resources;

  /** a localized label */
  public final static String LABEL = resources.getString("prop");

  /** the meta information */
  private MetaProperty meta = null;

  /**
   * Lifecycle - callback when being added to parent.
   * This is called by the parent of the property after
   * it has been added.
   */
  /*package*/ void addNotify(Property parent) {

    // remember parent
    this.parent=parent;

    // propage to still active children
    Property[] props = getProperties();
    for (int i=0,j=props.length;i<j;i++) {
      Property child = (Property)props[i];
      child.addNotify(this);
    }

    // remember being added
    Transaction tx = getTransaction();
    if (tx!=null)
      tx.get(Transaction.PROPERTIES_ADDED).add(this);

  }

  /**
   * Lifecycle - callback when being removed from parent.
   * This is called by the parent of the property after
   * it has been removed.
   */
  /*package*/ void delNotify(Property oldParent) {
  
    // reset meta
    meta = null;
    
    // tell children first (they might have to do some cleanup themselves)
    Property[] props = getProperties();
    for (int i=props.length-1;i>=0;i--) {
      props[i].delNotify(this);
    }
    
    // remember being deleted
    Transaction tx = getTransaction();
    if (tx!=null)
      tx.get(Transaction.PROPERTIES_DELETED).add(this);
      
    // reset parent
    parent = null;
    
    // continue
  }
  
  /**
   * This called by the property in process of being
   * changed before the value has changed.
   */
  /*package*/ void propagateChange(String old) {
    
    // remember being modified
    Transaction tx = getTransaction();
    if (tx!=null) {
      Change change = new Change.PropertyValue(this, old);
      tx.get(Transaction.PROPERTIES_MODIFIED).add(this);
      tx.addChange(change);
      
      // propagate change
      propagateChange(change);
    }
    
  }
  
  /**
   * Propagate something has changed to the
   * parent hierarchy
   */
  /*package*/ void propagateChange(Change change) {
    // tell it to parent
    if (parent!=null)
      parent.propagateChange(change);
  }
  
  /**
   * get current transaction
   */
  protected Transaction getTransaction() {
    return parent==null ? null : parent.getTransaction();
  }
  
  /**
   * Adds a sub-property to this property
   * @param prop new property to add
   */
  public Property addProperty(Property prop) {
    return addProperty(prop, true);
  }

  /**
   * Adds another property to this property
   * @param prop new property to add
   * @param place whether to place the sub-property according to grammar
   */
  public Property addProperty(Property prop, boolean place) {

    // check grammar for placement if applicable
    int pos = -1;
    
    if (place&&getNoOfProperties()>0&&getEntity()!=null) {

      MetaProperty meta = getMetaProperty();
      
      pos = 0;
      int index = meta.getIndex(prop.getTag());
      for (;pos<getNoOfProperties();pos++) {
        if (meta.getIndex(getProperty(pos).getTag())>index)
          break;
      }
    }
    
    // add property
    return addProperty(prop, pos);
    
  }
  
  /**
   * Adds another property to this property
   */
  public Property addProperty(Property child, int pos) {

// This will break the blueprint editor
//    // check against meta of child
//    if (child.meta!=null && getMetaProperty().get(child.getTag(), false) != child.getMetaProperty())
//      throw new IllegalArgumentException("illegal use of property "+child.getTag()+" in "+getPath());

    // position valid?
    if (pos>=0&&pos<children.size())
      children.add(pos, child);
    else {
      children.add(child);
      pos = children.size()-1;
    }

	  // tell to added
	  child.addNotify(this);
	
	  // remember change
	  Transaction tx = getTransaction();
	  if (tx!=null) {
	    Change change = new Change.PropertyAdd(this, pos, child);
	    tx.get(Transaction.PROPERTIES_MODIFIED).add(this);
	    tx.addChange(change);
	    
			// propagate
			propagateChange(change);

	  }
	
    // Done
    return child;
  }
  
  /**
   * Removes a property by looking in the property's properties
   */
  public void delProperty(Property deletee) {

    // find position (throw outofbounds if n/a)
    int pos = 0;
    for (;;pos++) {
      if (children.get(pos)==deletee)
        break;
    }

    // do it
    delProperty(pos);
    
  }

  /**
   * Removes a property by position
   */
  public void delProperty(int pos) {

    // range check
    if (pos<0||pos>=children.size())
      throw new IndexOutOfBoundsException();

    // remove   
    Property removed = (Property)children.remove(pos);

	  // tell to removed
	  removed.delNotify(this);
	
	  // remember change
	  Transaction tx = getTransaction();
	  if (tx!=null) {
	    Change change = new Change.PropertyDel(this, pos, removed);
	    tx.get(Transaction.PROPERTIES_MODIFIED).add(this);
	    tx.addChange(change);
	    
			// tell it to parent
			propagateChange(change);
	  
	  }
	
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
      if (image==null) 
        image = getMetaProperty().getImage(); 
      return image;
    }
    
    // not valid
    if (imageErr==null) 
      imageErr = getMetaProperty().getImage("err"); 
      
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

    Stack stack = new Stack();

    // build path start with this
    String tag = getTag();
    if (tag==null)
      throw new IllegalArgumentException("encountered getTag()==null");
    stack.push(tag);
    
    // loop through parents
    Property parent = getParent();
    while (parent!=null) {
      tag = parent.getTag();
      if (tag==null)
        throw new IllegalArgumentException("encountered getTag()==null");
      stack.push(tag);
      parent = parent.getParent();
    }

    // done
    return new TagPath(stack);
    
  }

  /**
   * Test properties
   */
  public boolean isProperties(List props) {
    return children.containsAll(props);
  }
  
  /**
   * Allows to change the order of the properties contained. This
   * method will throw an IllegalArgumentException if 
   * ! set.contains(getProperties()) && getProperties.contains(set)
   */
  public void setProperties(List set) {
    // check mutual inclusion
    if (!(children.containsAll(set)&&set.containsAll(children)))
      throw new IllegalArgumentException("change of properties not allowed");
	  // remember change
	  Transaction tx = getTransaction();
	  if (tx!=null) {
	    Change change = new Change.PropertyShuffle(this, children);
	    tx.get(Transaction.PROPERTIES_MODIFIED).add(this);
	    tx.addChange(change);
	    
			// propagate
      propagateChange(change);

	  }
    // do the change
    children.clear();
    children.addAll(set);
    // done
  }
  
  /**
   * Returns this property's properties (all children)
   */
  public Property[] getProperties() {
    return toArray(children);
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
  public Property[] getProperties(TagPath path) {

    // Gather 'em
    List result = new ArrayList(children.size());
    getPropertiesRecursively(path, 0, result);

    // done
    return toArray(result);
  }

  private void getPropertiesRecursively(TagPath path, int pos, List fill) {

    // Correct here ?
    if (!path.equals(pos, getTag())) 
      return;

    // Me the last one?
    if (pos==path.length()-1) {
      fill.add(this);
      return;
    }

    // Search in properties
    for (int i=0;i<children.size();i++) 
      getProperty(i).getPropertiesRecursively(path,pos+1,fill);

    // done
  }
  
  /**
   * Returns a sub-property position
   */
  public int getPropertyPosition(Property prop) {
    for (int i=0;i<children.size();i++) {
      if (children.get(i)==prop)
        return i;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns this property's nth property
   * kenmraz: added checks since I was able
   * to get an indexOutOfBounds error when DnDing
   * to the end of list of children.  
   * nmeier: remove check again to force valid
   * index argument - fixed DnD code to provide
   * correct parameter
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
    return getPropertyRecursively(this, path, 0, true);
  }
  
  private static Property getPropertyRecursively(Property prop, TagPath path, int pos,  boolean checkPropsTagFirst) {
    
    while (true) {

      // still tags available in path?
      if (pos==path.length())
        return prop;
      String tag = path.get(pos);
    
      // a '..'?
      if (tag.equals("..")) {
        Property parent = prop.getParent();
        // no parent?
        if (parent==null)
          return null;
        // continue with parent
        prop = parent;
        pos++;
        checkPropsTagFirst = false;
        continue;
      }
      
      // a '.'?
      if (tag.equals(".")) {
        // continue with self
        pos++;
        checkPropsTagFirst = false;
        continue;
      }

      // a '*'?
      if (tag.equals("*")) {
        // check out target
        if (!(prop instanceof PropertyXRef))
          return null;
        prop = ((PropertyXRef)prop).getTarget();
        if (prop==null)
          return null;
        // continue with prop
        pos++;
        checkPropsTagFirst = false;
        continue;
      }

      // still have to match prop's tag?
      if (checkPropsTagFirst) {
        if (!tag.equals(prop.getTag()))
          return null;
        // go with prop then
        pos++;
        checkPropsTagFirst = false;
        continue;
      }
      
      // Search for appropriate tag in children
      for (int i=0,n=0;i<prop.getNoOfProperties();i++) {

        Property ith = prop.getProperty(i);

        // tag is good?
        if (path.equals(pos, ith.getTag())) {
          // selector good?
          if (path.equals(pos, n))
            return getPropertyRecursively(ith, path, pos+1, false);
          // inc selector
          n++;
        }
      }
      
      // no recursion left
      return null;
          
    }
    
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
   * Initializes this poperty giving it a chance to inspect
   * support for tag and value, eventually returning a 
   * different type that is better suited for the SPECIFIC
   * combination.
   */
  /*package*/ Property init(MetaProperty meta, String value) throws GedcomException {
    // remember meta
    this.meta = meta;
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
   * Returns the value of this property as string (this is a Gedcom compliant value)
   */
  abstract public String getValue();
  
  /**
   * Returns a user-readable property value
   */
  public String getDisplayValue() {
    return getValue();
  }

  /**
   * The default toString() returns the display value of this property
   */
  public String toString() {
    return getDisplayValue();
  }
  
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
   * Compares this property to another property
   * @return -1 this &lt; property <BR>
   *          0 this = property <BR>
   *          1 this &gt; property
   */
  public int compareTo(Object that) {
    // safety check
    if (!(that instanceof Property)) 
      throw new ClassCastException("compareTo("+that+")");
    // no gedcom available?
    return compare(this.getValue(), ((Property)that).getValue() );
  }
  
  /**
   * Compare to string gedcom language aware
   */
  protected int compare(String s1, String s2) {
    
    // I was pondering the notion of keeping cached CollationKeys
    // for faster recurring comparisons but apparently that's not
    // always faster and the keys have a respectable size themselves
    // which leads me to believe a simple Collator.compare() is
    // the better compromise here (even for sort algs)
    
    // gedcom and its collator available?
    Gedcom ged = getGedcom();
    if (ged!=null)
      return ged.getCollator().compare(s1,s2);
      
    // fallback to simple compare
    return s1.compareTo(s2);
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
   * Adds default properties to this property
   */
  public final Property addDefaultProperties() {
    
    // only if parent set
    if (getEntity()==null) throw new IllegalArgumentException("addDefaultProperties() while getEntity()==null!");
    
    // loop
    MetaProperty[] subs = getSubMetaProperties(MetaProperty.FILTER_DEFAULT); 
    for (int s=0; s<subs.length; s++) {
      if (getProperty(subs[s].getTag())==null)
        addProperty(subs[s].create(EMPTY_STRING)).addDefaultProperties();
    }

    // done    
    return this;
  }
  
  /**
   * Resolve meta property
   */
  public MetaProperty getMetaProperty() {
    if (meta==null)
      meta = MetaProperty.get(getPath());    
    return meta;
  }

  /**
   * Resolve meta properties
   * @param filter one/many of QUERY_ALL, QUERY_VALID_TRUE, QUERY_SYSTEM_FALSE, QUERY_FOLLOW_LINK
   */
  public MetaProperty[] getSubMetaProperties(int filter) {
    return getMetaProperty().getSubs(filter);
  }

  /**
   * Convert collection of properties into array
   */
  protected static Property[] toArray(Collection ps) {
    return (Property[])ps.toArray(new Property[ps.size()]);
  }
  
  /**
   * Accessor - private
   */
  public boolean isPrivate() {
    return isPrivate;
  }
  
  /**
   * Accessor - secret is private and unknown password
   */
  public boolean isSecret() {
    return isPrivate && getGedcom().getPassword()==Gedcom.PASSWORD_UNKNOWN;
  }
  
  /**
   * Accessor - private
   */
  public void setPrivate(boolean set, boolean recursively) {
    
    // change state
    if (recursively) {
      for (int c=0;c<getNoOfProperties();c++) {
        Property child = getProperty(c);
        child.setPrivate(set, recursively);
      }
    }
    isPrivate = set;
    
    // bookkeeping
    propagateChange(getValue());
    
    // done
  }

  /**
   * Resolves end-user information about this property - by
   * default whatever is in the language resource files
   * @return name and info or null
   */
  public String getPropertyInfo() {
    return getMetaProperty().getInfo();
  }
  
  /**
   * Resolves end-user information about this property - by
   * default whatever is in the language resource files
   * @return name
   */
  public String getPropertyName() {
    return Gedcom.getName(getTag());
  }

} //Property

