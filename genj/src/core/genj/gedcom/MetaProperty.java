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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Wrapper for a Property (could be read from XML file later)
 */
public class MetaProperty {
  
  /** meta.properties */
  private static Properties properties = loadProperties();
  
  /** instances */
  private static Map instances = new HashMap();
  
  /** events */
  private static Set events = new HashSet();
  
  /** unknown */
  private final static MetaProperty UNKNOWN = new MetaProperty("?","Unknown",null);

  /** globally loaded images */    
  private static Map images = new HashMap(); 

  /** Members */
  private String    theTag;
  private Class     theClass;
  private List      theSubs = new ArrayList(20);
  private boolean  isEvent;
  private ImageIcon theImage = null;
  private Map       theImages = new HashMap();

  /**
   * Constructor
   */
  private MetaProperty(String tag, String type, String subs) {
    // Remember data
    theTag = tag;
    
    // Figure out the class
    try {
      // easy for null
      if (type==null) theClass = PropertyUnknown.class;
      else theClass = Class.forName(Property.class.getName()+type);
    } catch (Throwable throwable) {
      Debug.log(Debug.WARNING, this, "meta.properties #"+tag+" points to non existing type Property"+type);
      theClass = PropertyUnknown.class;
    }
    
    // Initialize subTags
    if (subs!=null) {
      if (subs.startsWith("$")) subs = properties.getProperty(subs.substring(1));
      StringTokenizer tokens = new StringTokenizer(subs,",");
      while (tokens.hasMoreTokens()) theSubs.add(tokens.nextToken());
    }
    
    // event?
    isEvent = PropertyEvent.class.isAssignableFrom(theClass);
    
    // done
  }

  /**
   * notifies this meta of a property being added
   */
  public void addNotify(Property prop) {
    // won't keep transients
    if (prop.isTransient()) return;
    // won't keep xrefs
    if (prop instanceof PropertyXRef) return;
    // won't keep known
    if (theSubs.contains(prop.getTag())) return;
    // keep it
    theSubs.add(prop.getTag());
  }

  /**
   * The property's type
   */
  public Class getPropertyClass() {
    return theClass;
  }
  
  /**
   * Returns some explanationary information about the property.
   */
  public String getInfo() {

    // Find Info that matches tag
    String tag = getTag();

    String name = Gedcom.getResources().getString(tag+".name");
    String info = Gedcom.getResources().getString(tag+".info");

    return name+":\n"+info;
  }

  /**
   * The subs defined for the meta-definition
   */
  public MetaProperty[] getSubs(boolean defaultsOnly) {
    
    Set result = new LinkedHashSet(theSubs.size());
    for (int i=0; i<theSubs.size(); i++) {
      String sub = theSubs.get(i).toString();
      if (sub.startsWith("!")) {
        sub = sub.substring(1);
      } else {
        if (defaultsOnly) continue;
      } 
      result.add(get(sub));
    }
        
    return (MetaProperty[])result.toArray(new MetaProperty[result.size()]);
  }

  /**
   * The property's tag
   */
  public String getTag() {
    return theTag;
  }
  
  /**
   * The property's image
   */
  public ImageIcon getImage() {
    // got it already?
    if (theImage!=null) return theImage;
    // get it
    theImage = getImage(null);
    // done
    return theImage;
  }
  
  /**
   * The property's image
   */
  public ImageIcon getImage(String qualifier) {
    // know it?
    ImageIcon result = (ImageIcon)theImages.get(qualifier);
    if (result!=null) return result;
    // try to lookup/load
    try {
      // .. looks like SEX.img
      String key = getTag()+".img";
      // .. maybe like SEX.img.m
      if (qualifier!=null) key += '.'+qualifier;
      // .. img either name of .gif or variable $
      String img = properties.getProperty(key);
      if (img.startsWith("$")) img = properties.getProperty(img.substring(1));
      // .. check loaded images
      result = (ImageIcon)images.get(img);
      if (result==null) {
        result = new ImageIcon(getClass(), "images/"+img);
        images.put(img, result);
      }
      // .. have it
    } catch (Throwable t) {
      // .. fallback to UNKNOWN's image
      result = UNKNOWN.getImage(qualifier);
    }
    // remember
    theImages.put(qualifier, result);
    // done
    return result;
  }

  /**
   * Returns whether this definition represents an event or not
   */
  public boolean isEvent() {
    return isEvent;
  }

  /**
   * static property loader
   */
  private static Properties loadProperties() {
    
    // loading meta.properties
    Properties props = new Properties();
    try {
      props.load(MetaProperty.class.getResourceAsStream("meta.properties"));
    } catch(IOException e) {
      throw new Error("Couldn't load MetaDefinition's meta.properties");
    }
    
    // done
    return props;
  }
  
  /**
   * Resolve event instances
   */
  public static List getEvents() {
    List result = new ArrayList(events.size());
    Iterator it = events.iterator();
    while (it.hasNext()) {
      result.add(it.next());
    }
    return result;
  }
  
  /**
   * Resolve the MetaProperty for given property
   */
  public static MetaProperty get(Property p) {
    return get(p.getTag());
  }
  
  /**
   * Resolve the MetaProperty for given tag-path
   */
  public static MetaProperty get(TagPath p) {
    return get(p.getLast());
  }
  
  /**
   * Resolve MetaDefinition instance
   */
  public static MetaProperty get(String tag) {
    
    // do we have that information already?
    MetaProperty result = (MetaProperty)instances.get(tag);
    if (result!=null) 
      return result;
    
    // try to get the information we need
    String type = properties.getProperty(tag+".type");
    String subs = properties.getProperty(tag+".subs");

    // create it
    result = new MetaProperty(tag, type, subs);
    instances.put(tag, result);
    if (result.isEvent()) events.add(result);
    
    // done
    return result;
  }
  
  /**
   * Instantiate property
   */
  public Property instantiate(String value, boolean addDefaults) {
    
    // Instantiate Property object
    try {
      // .. get constructor of property
      Object parms[] = { getTag(), value };
      Class  parmclasses[] = { String.class , String.class };
      Constructor constructor = theClass.getConstructor(parmclasses);

      // .. get object
      Property result = (Property)constructor.newInstance(parms);
      
      // .. add defaults?
      if (addDefaults) result.addDefaultProperties();

      // Done
      return result;

    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this, t);
    }

    // Error means unknown
    return new PropertyUnknown(getTag(),value);
  }
  
  /**
   * Instantitate property
   */
  public static Property instantiate(String tag, String value, boolean addDefaults) {
    return get(tag).instantiate(value, addDefaults);
  }

} //MetaDefinition
