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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property implements Comparable {

  /** static strings */
  protected final static String 
    UNSUPPORTED_TAG = "Unsupported Tag";
  
  private static final Pattern FORMAT_PATTERN = Pattern.compile("\\{(.*?)\\$(.)(.*?)\\}");
    
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
   * changed after the value has changed.
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
  /*package*/ Transaction getTransaction() {
    return parent==null ? null : parent.getTransaction();
  }
  
  /**
   * Associates a file with this property
   * @return success or not
   */
  public boolean addFile(File file) {
    // FILE not allowed here? 
    if (!getMetaProperty().allows("FILE")) {
      // OBJE neither? 
      if (!getMetaProperty().allows("OBJE")) 
        return false;
      // let new OBJE handle this
      return addProperty("OBJE", "").addFile(file);
    }
    // need to add it?
    List pfiles = getProperties(PropertyFile.class);
    PropertyFile pfile;
    if (pfiles.isEmpty()) {
      pfile = (PropertyFile)addProperty("FILE", "");
    } else {
      pfile = (PropertyFile)pfiles.get(0);
    }
    // keep it
    return pfile.addFile(file);
  }
  
  /**
   * Adds a sub-property to this property 
   */
  public Property addProperty(String tag, String value) {
    return addProperty(tag, value, true);
  }
  
  /**
   * Adds a sub-property to this property
   */
  public Property addProperty(String tag, String value, boolean place) {
    return addProperty(getMetaProperty().getNested(tag, true).create(value), place);
  }
  
  /**
   * Adds a sub-property to this property
   */
  public Property addProperty(String tag, String value, int pos) {
    return addProperty(getMetaProperty().getNested(tag, true).create(value), pos);
  }
  
  /**
   * Adds a sub-property to this property
   * @param prop new property to add
   */
  /*package*/ Property addProperty(Property prop) {
    return addProperty(prop, true);
  }

  /**
   * Adds another property to this property
   * @param prop new property to add
   * @param place whether to place the sub-property according to grammar
   */
  /*package*/ Property addProperty(Property prop, boolean place) {

    // check grammar for placement if applicable
    int pos = -1;
    
    if (place&&getNoOfProperties()>0&&getEntity()!=null) {

      MetaProperty meta = getMetaProperty();
      
      pos = 0;
      int index = meta.getNestedIndex(prop.getTag());
      for (;pos<getNoOfProperties();pos++) {
        if (meta.getNestedIndex(getProperty(pos).getTag())>index)
          break;
      }
    }
    
    // add property
    return addProperty(prop, pos);
    
  }
  
  /**
   * Adds another property to this property
   */
  /*package*/ Property addProperty(Property child, int pos) {

    // position valid?
    if (pos>=0&&pos<children.size())
      children.add(pos, child);
    else {
      children.add(child);
      pos = children.size()-1;
    }

	  // tell to added
	  child.addNotify(this);
    if (isTransient) child.isTransient = true;
	
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
   * Removes all properties
   */
  public void delProperties() {
    Property[] cs = (Property[])children.toArray(new Property[children.size()]);
    for (int c = 0; c < cs.length; c++) {
      delProperty(cs[c]);
    }
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
    return parent!=null ? parent.getGedcom() : null;
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
   * Return a containgin property of given type in the hierarchy of parents 
   */
  public Property getContaining(Class type) {
    Property prop = this;
    while (prop!=null) {
      if (type.isAssignableFrom(prop.getClass())) 
        return prop;
      prop = prop.getParent();
    }
    return null;
  }
  
  
  /**
   * Returns the property this property belongs to
   */
  public Property getParent() {
    return parent;
  }
  
  /**
   * Returns the path from this to containing property
   */
  public TagPath getPathToContaining(Property rparent) {    
    Stack result = new Stack();
    getPathToContaining(rparent, result);
    return new TagPath(result);
  }
  
  /**
   * Returns the path from this to a nested property
   */
  public TagPath getPathToNested(Property nested) {    
    Stack result = new Stack();
    nested.getPathToContaining(this, result);
    return new TagPath(result);
  }
  
  private void getPathToContaining(Property containing, Stack result) {
    result.push(getTag());
    if (containing==this)
      return;
    if (parent==null)
      throw new IllegalArgumentException("couldn't find containing "+containing);
    parent.getPathToContaining(containing, result);
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
   * Test for (recursive) containment
   */
  public boolean contains(Property prop) {
    for (int c = 0; c < children.size(); c++) {
      Property child = (Property)children.get(c);
      if (child==prop||child.contains(prop))
        return true;
    }
    return false;
  }

  /**
   * Test for (recursive) containment
   */
  public boolean isContained(Property in) {
    Property parent = getParent();
    if (parent==in) return true;
    return parent==null ? false : parent.isContained(in);
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
    // do the change
    List old = new ArrayList(children);
    children.clear();
    children.addAll(set);
	  // remember change
	  Transaction tx = getTransaction();
	  if (tx!=null) {
	    Change change = new Change.PropertyShuffle(this, old);
	    tx.get(Transaction.PROPERTIES_MODIFIED).add(this);
	    tx.addChange(change);
	    
			// propagate
      propagateChange(change);

	  }
    // done
  }
  
  /**
   * Returns this property's properties (all children)
   */
  public Property[] getProperties() {
    return toArray(children);
  }
  
  /**
   * Returns property's properties by criteria
   * @param tag  regular expression pattern of tag to match
   * @param value regular expression pattern of value to match
   * @return matching properties
   */
  public List findProperties(Pattern tag, Pattern value) {
    // create result
    List result = new ArrayList();
    // check argument
    if (value==null) value = Pattern.compile(".*");
    // recurse
    findPropertiesRecursively(result, tag, value, true);
    // done
    return result;
  }
  
  private void findPropertiesRecursively(Collection result, Pattern tag, Pattern value, boolean recursively) {
    // check current
    if (tag.matcher(getTag()).matches() && value.matcher(getValue()).matches() ) 
      result.add(this);
    // recurse into properties
    for (int i=0, j=getNoOfProperties(); i<j ; i++) {
      if (recursively) getProperty(i).findPropertiesRecursively(result, tag, value, recursively);
    }
    // done
  }
  
  /**
   * Returns this property's properties by tag (only valid properties are considered)
   */
  public Property[] getProperties(String tag) {
    return getProperties(tag, true);
  }
  
  /**
   * Returns this property's properties by tag 
   */
  public Property[] getProperties(String tag, boolean validOnly) {
    ArrayList result = new ArrayList(getNoOfProperties());
    for (int i=0, j = getNoOfProperties(); i<j ; i++) {
      Property prop = getProperty(i);
      if (prop.getTag().equals(tag)&&(!validOnly||prop.isValid()))
        result.add(prop);
    }
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
   * Returns one of this property's properties by path
   */
  public Property getPropertyByPath(String path) {
    // 20050822 I've added this convenient getter to make it
    // easier for beginners to use the API - no need to bother
    // them with creating a TagPath first. This is not as performant
    // as doing so and reusing the same path.
    return getProperty(new TagPath(path));
  }
  
  /**
   * Returns one of this property's properties by path
   */
  public Property getProperty(TagPath path) {

    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
       return keep(prop, false);
      }
    };
    
    path.iterate(this, visitor);
    
    return visitor.getProperty();
    
  }
  
  /**
   * Returns this property's properties by path
   */
  public Property[] getProperties(TagPath path) {

    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
       return keep(prop, true);
      }
    };
    
    path.iterate(this, visitor);
    
    return visitor.getProperties();
  }

//  private static Property getPropertyRecursively(Property prop, TagPath path, int pos, List listAll, boolean checkPropsTagFirst) {
//    
//    while (true) {
//
//      // traversed path?
//      if (pos==path.length()) {
//        if (listAll!=null)
//          listAll.add(prop);
//        return prop;
//      }
//      
//      // a '..'?
//      if (path.equals(pos, "..")) {
//        Property parent = prop.getParent();
//        // no parent?
//        if (parent==null)
//          return null;
//        // continue with parent
//        prop = parent;
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//      
//      // a '.'?
//      if (path.equals(pos, ".")) {
//        // continue with self
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//
//      // a '*'?
//      if (path.equals(pos, "*")) {
//        // check out target
//        if (!(prop instanceof PropertyXRef))
//          return null;
//        prop = ((PropertyXRef)prop).getTarget();
//        if (prop==null)
//          return null;
//        // continue with prop
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//
//      // still have to match prop's tag?
//      if (checkPropsTagFirst) {
//        if (!path.equals(pos, prop.getTag()))
//          return null;
//        // go with prop then
//        pos++;
//        checkPropsTagFirst = false;
//        continue;
//      }
//      
//      // Search for appropriate tag in children
//      for (int i=0;i<prop.getNoOfProperties();i++) {
//
//        Property ith = prop.getProperty(i);
//
//        // tag is good?
//        if (path.equals(pos, ith.getTag())) {
//          // find all or select one specific based on (tag, selector)?
//          if (listAll!=null) {
//            getPropertyRecursively(ith, path, pos+1, listAll, false);
//          } else {
//            return getPropertyRecursively(ith, path, pos+1, null, false);
//          }
//        }
//      }
//      
//      // no recursion left
//      return null;
//          
//    }
//    
//  }

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
   * Returns a property value of a child property. This is
   * a convenient method to access a child-property without having
   * to check for null before calling its getValue()
   */
  public String getPropertyValue(String tag) {
    Property child = getProperty(tag);
    return child!=null ? child.getValue() : "";
  }

  /**
   * Returns a user-readable property value of a child property. This is
   * a convenient method to access a child-property without having
   * to check for null before calling its getDisplayValue()
   */
  public String getPropertyDisplayValue(String tag) {
    Property child = getProperty(tag);
    return child!=null ? child.getDisplayValue() : "";
  }

  /**
   * The default toString() returns the display value of this property
   */
  public String toString() {
    return getDisplayValue();
  }
  
  /**
   * Set a value at given path
   */
  public Property setValue(final TagPath path, final String value) {
    
    PropertyVisitor visitor = new PropertyVisitor() {
      protected boolean leaf(Property prop) {
        prop.setValue(value);
        return keep(prop, false);
      }
      protected boolean recursion(Property parent,String child) {
        if (parent.getProperty(child, false)==null)
          parent.addProperty(child, "");
        return true;
      }
    };
    
    path.iterate(this, visitor);
    
    return visitor.getProperty();
    
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
    return compare(this.getDisplayValue(), ((Property)that).getDisplayValue() );
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
    MetaProperty[] subs = getNestedMetaProperties(MetaProperty.FILTER_DEFAULT); 
    for (int s=0; s<subs.length; s++) {
      if (getProperty(subs[s].getTag())==null)
        addProperty(subs[s].create("")).addDefaultProperties();
    }

    // done    
    return this;
  }
  
  /**
   * Resolve meta property
   */
  public MetaProperty getMetaProperty() {
    if (meta==null)
      meta = Grammar.getMeta(getPath());    
    return meta;
  }

  /**
   * Resolve meta properties
   * @param filter one/many of QUERY_ALL, QUERY_VALID_TRUE, QUERY_SYSTEM_FALSE, QUERY_FOLLOW_LINK
   */
  public MetaProperty[] getNestedMetaProperties(int filter) {
    return getMetaProperty().getAllNested(filter);
  }

  /**
   * Convert collection of properties into array
   */
  public static Property[] toArray(Collection ps) {
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

  /**
   * Generate a string representation based on given template.
   * @see Property#format(String, PrivacyPolicy)
   */
  public String format(String format) {
    return format(format, PrivacyPolicy.PUBLIC);
  }
  
  /**
   * Generate a string representation based on given template.
   * <pre>
   *   {$t} property tag (doesn't count as matched)
   *   {$T} property name(doesn't count as matched)
   *   {$D} date as fully localized string
   *   {$y} year 
   *   {$p} place (city)
   *   {$P} place (all jurisdictions)
   *   {$v} value
   *   {$V} display value
   * </pre>
   * @param format as described
   * @param policy applied privacy policy
   * @return formatted string if at least one marker matched, "" otherwise
   */
  public String format(String format, PrivacyPolicy policy) {
  
    // match format given
    Matcher matcher = FORMAT_PATTERN.matcher(format);
    // prepare running parameters
    StringBuffer result = new StringBuffer(format.length()+20);
    int masked = 0;
    int matches = 0;
    int cursor = 0;
    // go through all matches in format
    while (matcher.find()) {
      // grab prefix
      result.append(format.substring(cursor, matcher.start()));
      // analyze next {...$x...}
      String prefix = matcher.group(1);
      char marker = format.charAt(matcher.start(2));
      String suffix = matcher.group(3);
      // translate marker into a value
      Property prop;
      String value;
      switch (marker) {
        case 'D' : { prop = getProperty("DATE"); value = (prop instanceof PropertyDate)&&prop.isValid() ? prop.getDisplayValue() : ""; break; }
        case 'y': { prop = getProperty("DATE"); value = (prop instanceof PropertyDate)&&prop.isValid() ? Integer.toString(((PropertyDate)prop).getStart().getYear()) : ""; break; }
        case 'p': { prop = getProperty("PLAC"); value = (prop instanceof PropertyPlace) ? ((PropertyPlace)prop).getCity() : ""; if (value==null) value=""; break; }
        case 'P': { prop = getProperty("PLAC"); value = (prop instanceof PropertyPlace) ? prop.getDisplayValue() : ""; break;}
        case 'v': { prop = this; value = getDisplayValue(); break; }
        case 'V': { prop = this; value = getValue(); break; }
        case 't': { prop = null; value = getTag(); break; }
        case 'T': { prop = null; value = Gedcom.getName(getTag()); break; }
        default:
          throw new IllegalArgumentException("unknown formatting marker "+marker);
      }
      // check property against policy if applicable
      if (prop!=null && policy.isPrivate(prop)) {
        // we didn't have a mask yet or the prefix is not empty? use mask
        value = (masked++==0||prefix.trim().length()>0)  ? Options.getInstance().maskPrivate : "";
      }
      // append if value is good
      if (value.length()>0) {
        result.append(prefix);
        result.append(value);
        result.append(suffix);
        if (prop!=null) matches++;
      }
      // continue
      cursor = matcher.end();
    }
    
    // append the rest
    result.append(format.substring(cursor));
    
    // got anything at all?
    return matches>0 ? result.toString() : "";
  }
  
} //Property

