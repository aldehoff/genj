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
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import genj.util.*;

/**
 * Abstract base type for all GEDCOM properties
 */
public abstract class Property {

  private static final Property NO_KNOWN_PROPERTIES[] = {};
  private static final String   NO_KNOWN_SUB_TAGS  [] = {};
  private static final String   EVT_SUB_TAGS       [] = new String[]{ "DATE", "PLAC", "ADDR", "AGNC", "TYPE" };
  private static final String   IAT_SUB_TAGS       [] = new String[]{ "ADDR", "AGNC", "DATE", "PLAC", "TYPE" };

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

  private static Hashtable tag2defs;

  private static MetaDefinition unknownDefinition =
    new MetaDefinition(null,null,new String[]{});

  protected static final MetaDefinition metaDefs[] = {

    // FAM Record
    new MetaDefinition("FAM","Fam",new String[]{"MARR","CHIL","HUSB","WIFE","NOTE","ANUL","CENS","DIV","DIVF","ENGA","MARB","MARC","MARL","MARS"}) ,
    new MetaDefinition("CHIL","Child") ,
    new MetaDefinition("HUSB","Husband") ,
    new MetaDefinition("WIFE","Wife") ,

    // INDI Record
    new MetaDefinition("INDI","Indi",new String[]{"NAME","SEX","BIRT","DEAT","FAMS","FAMC","RESI","GRAD","OCCU","OBJE","NOTE","CAST","DSCR","EDUC","IDNO","NATI","NCHI","PROP","RELI","SSN","TITL","CHR","BURI","CREM","ADOP","BAPM","BAPL","BARM","BASM","BLES","CHRA","CONF","FCOM","ORDN","NATU","EMIG","IMMI","CENS","PROB","WILL","RETI"}) ,
    new MetaDefinition("NAME","Name") ,
    new MetaDefinition("SEX" ,"Sex") ,
    new MetaDefinition("FAMC" ,"FamilyChild") ,
    new MetaDefinition("FAMS" ,"FamilySpouse") ,

    // OBJE Record
    new MetaDefinition("OBJE","Indi") ,
    new MetaDefinition("BLOB","Blob") ,

    // NOTE Record
    new MetaDefinition("NOTE","Note", new String[]{"DATE"}) ,

    // SUBM Record
    new MetaDefinition("SUBM","Submitter",new String[]{"NAME","ADDR"}),

    // INDI Attributes
    new MetaDefinition("CAST","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("DSCR","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("EDUC","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("IDNO","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("NATI","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("NCHI","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("OCCU","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("PROP","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("RELI","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("RESI","IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("SSN" ,"IndividualAttribute",IAT_SUB_TAGS) ,
    new MetaDefinition("TITL","IndividualAttribute",IAT_SUB_TAGS) ,

    // INDI Events
    new MetaDefinition("BIRT","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CHR" ,"Event",EVT_SUB_TAGS) ,
    new MetaDefinition("DEAT","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BURI","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CREM","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("ADOP","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BAPM","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BAPL","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BARM","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BASM","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("BLES","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CHRA","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CONF","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("FCOM","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("ORDN","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("NATU","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("EMIG","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("IMMI","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CENS","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("PROB","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("WILL","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("GRAD","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("RETI","Event",EVT_SUB_TAGS) ,

    // FAM Events
    new MetaDefinition("ANUL","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("CENS","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("DIV" ,"Event",EVT_SUB_TAGS) ,
    new MetaDefinition("DIVF","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("ENGA","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("MARR","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("MARB","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("MARC","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("MARL","Event",EVT_SUB_TAGS) ,
    new MetaDefinition("MARS","Event",EVT_SUB_TAGS) ,

    // Individual Attributes
    new MetaDefinition("ADDR","Address",new String[]{ "CITY", "POST" }) ,
    new MetaDefinition("CONT","Continuation") ,
    new MetaDefinition("DATE","Date") ,

    new MetaDefinition("AGE" ,"Age") ,
    new MetaDefinition("CAUS","Cause") ,
    new MetaDefinition("CITY","City") ,
    new MetaDefinition("PLAC","Place",new String[]{ "CONT" }) ,
    new MetaDefinition("POST","PostalCode") ,

    // Generic Attributes
    new MetaDefinition("AGNC","GenericAttribute") ,
    new MetaDefinition("TYPE","GenericAttribute") ,
    new MetaDefinition("TITL","GenericAttribute") ,
    new MetaDefinition("FORM","GenericAttribute") ,

    // Medai
    new MetaDefinition("OBJE","Media"           ,new String[]{"TITL","FORM", "BLOB", "FILE"}) ,
    new MetaDefinition("FILE","File"            ),

    // Source
    new MetaDefinition("SOUR","Source"    ,new String[]{"TITL","AUTH", "ABBR", "PUBL"}) ,
    new MetaDefinition("AUTH","GenericAttribute") ,
    new MetaDefinition("ABBR","GenericAttribute") ,
    new MetaDefinition("PUBL","GenericAttribute") ,

    // Adding this line seems like the right this to do, but, if I do it, then
    // my test file does not load. It dies in PropertyRepository.link() in
    // the new() or the adding of the foriegnXRef. -jch
    //    new MetaDefinition("REPO","Repository"    ,new String[]{"NAME"}) ,
    new MetaDefinition("REPO","GenericAttribute") ,
  };

  /**
   * Initializer
   */
  static {

    // Create a Hashtable for lookups
    tag2defs = new Hashtable(metaDefs.length);

    // .. fill it up with property classes
    for (int i=0;i<metaDefs.length;i++) {
      MetaDefinition metaDef = metaDefs[i];
      tag2defs.put(metaDef.getTag(),metaDef);
    }

    // .. done

  }

  /**
   * Interface for an Iterator that enables the interested to
   * iterate through multiple lines of data
   */
  public interface LineIterator {
    /**
     * Returns wether this iterator has more lines
     */
    public boolean hasMoreValues();
    /**
     * Returns the next line of this iterator
     */
    public String getNextValue() throws NoSuchElementException;
    // EOC
  }

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
  public void addDefaultProperties() {
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
    getMetaDefinition(getTag()).addSubTag(prop.getTag());

    // Done
  }

  /**
   * Calculates a property's standard proxy from given TagPath
   */
  public static String calcDefaultProxy(TagPath path) {

    // Find class for tag
    Class c = getMetaDefinition(path.getLast()).getPropertyClass();

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
   * @return -1 this < property <BR>
   *          0 this = property <BR>
   *          1 this > property
   */
  public int compareTo(Property p) {
    return getValue().compareTo(p.getValue());
  }

  /**
   * Create a property object from a TAG
   */
  public static Property createInstance(String tag, boolean subProps) {
    Property result = createInstance(tag, "");
    if (subProps) {
      result.addDefaultProperties();
    }
    return result;
  }

  /**
   * Create a property object from a TAG, VALUE (can do preferred
   * sub-properties, too)
   */
  public static Property createInstance(String tag,String value) {

    // Find class for tag
    Class c = getMetaDefinition(tag).getPropertyClass();

    // Instantiate Property object
    Constructor constructor = null;
    Object      object = null;

    try {
      // .. get constructor of property
      Object parms[] = { tag, value };
      Class  parmclasses[] = { parms[0].getClass() , parms[1].getClass()};
      constructor = c.getConstructor(parmclasses);

      // .. get object
      object = constructor.newInstance(parms);

      // Done
      return (Property)object;

    } catch (Exception e) {
      Debug.log(Debug.ERROR, Property.class,e);
    }

    // Error means unknown
    return new PropertyUnknown(tag,value);
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
   * Returns the default image which is associated with this property.
   */
  public ImgIcon getDefaultImage() {
    return Images.get(getTag());
  }

  /**
   * Returns the default image which is associated with this property.
   */
  public static ImgIcon getDefaultImage(String tag) {
    return Images.get(tag);
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
  public ImgIcon getImage(boolean checkValid) {
    if (checkValid&&(!isValid())) {
      // Maybe we have a special error for this one?
      ImgIcon result = Images.get(getTag()+".err", false);
      if (result!=null) {
        return result;
      }
      // Otherwise we use the error one
      return Images.get("X");
    }
    // Normal tag to image
    return Images.get(getTag());
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
   * Returns some explanationary information about this property.
   */
  public String getInfo() {

    // Find Info that matches tag
    String tag = getTag();

    String name = Gedcom.getResources().getString(tag+".name");
    String info = Gedcom.getResources().getString(tag+".info");

    return name+":\n"+info;
  }

  /**
   * Returns a list of (sub-)properties which are known
   */
  public final Property[] getKnownProperties() {

    // Find the definition for this property
    MetaDefinition metaDef = (MetaDefinition)tag2defs.get(getTag());

    // 2001 03 05 user-added properties do not have a def.
    if (metaDef==null) {
      return NO_KNOWN_PROPERTIES;
    }

    Vector tags = metaDef.getSubTags();

    // Build a result
    Property result[] = new Property[tags.size()];
    int p = 0;
    Enumeration e = tags.elements();
    while (e.hasMoreElements()) {
      result[p++] = createInstance(
        (String)e.nextElement(),
        false
      );
    }

    // Done
    return result;

  }

  /**
   * Returns a LineIterator which can be used to iterate through
   * several lines of data in case of isMultiLine equals true;
   * Should be implemented by properties with several lines.
   * Returns null when single line only
   */
  public LineIterator getLineIterator() {
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
  public Vector getProperties(Class type) {
    
    Vector props = new Vector(3);
    
    internalGetProperties(props, type);
    
    return props;
  }
  
  /** 
   * Fills list of properties with properties of given type
   */
  private void internalGetProperties(Vector props, Class type) {
    if (getClass().equals(type)) {
      props.addElement(this);
    }
    for (int c=0;c<getNoOfProperties();c++) {
      getProperty(c).internalGetProperties(props, type);
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

    String next;

    while (true) {
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
      next = path.getNext();

      // Ignore 1st in case of entity
      if (!((this instanceof Entity)&&(getTag().equals(next)))) {
        break;
      }
    }

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
   * Looks up a metaDef for given tag
   */
  private static MetaDefinition getMetaDefinition(String tag) {
    MetaDefinition metaDef = (MetaDefinition)tag2defs.get(tag);
    if (metaDef==null) {
      metaDef = unknownDefinition;
    }
    return metaDef;
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

        getGedcom().setUnsavedChanges(true);
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
    /*
    if (!(this instanceof Entity))
      return getTag()+" "+getValue();
    Entity e = (Entity)this;
    return "@"+e.getId()+"@ "+getTag()+" "+getValue();
    */
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
   * Adds a note to this property
   */
  /*package*/ void addNote(Note note) {

    // New PropertyNote
    PropertyNote pn = new PropertyNote();
    addProperty(pn);

    // Put a foreign xref in the note
    PropertyForeignXRef fxref = new PropertyForeignXRef(pn);
    note.addForeignXRef(fxref);

    // ... and point
    pn.setTarget(fxref);
  }

  /**
   * Adds a multimedia to this property
   */
  /*package*/ void addMedia(Media media) {

    // New PropertyMedia
    PropertyMedia pm = new PropertyMedia();
    addProperty(pm);

    // Put a foreign xref in the note
    PropertyForeignXRef fxref = new PropertyForeignXRef(pm);
    media.addForeignXRef(fxref);

    // ... and point
    pm.setTarget(fxref);
  }

  /**
   * Adds a source to this property
   */
  /*package*/ void addSource(Source source) {

    // New PropertySource
    PropertySource pm = new PropertySource();
    addProperty(pm);

    // Put a foreign xref in the note
    PropertyForeignXRef fxref = new PropertyForeignXRef(pm);
    source.addForeignXRef(fxref);

    // ... and point
    pm.setTarget(fxref);
  }


  /**
   * Adds a repository to this property
   */
  /*package*/ void addRepository(Repository repository) {

    // New PropertyRepository
    PropertyRepository pm = new PropertyRepository();
    addProperty(pm);

    // Put a foreign xref in the note
    PropertyForeignXRef fxref = new PropertyForeignXRef(pm);
    repository.addForeignXRef(fxref);

    // ... and point
    pm.setTarget(fxref);
  }

  /**
   * Helper that returns an empty String and not null
   */
  protected String emptyNotNull(String text) {
    if (text==null) {
      return "";
    }
    return text;
  }
  
  /**
   * Helper that tests an string for null or empty
   */
  public static boolean isEmptyOrNull(String txt) {
    if (txt==null) {
      return true;
    }
    if (txt.trim().length()==0) {
      return true;
    }
    return false;
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
}

