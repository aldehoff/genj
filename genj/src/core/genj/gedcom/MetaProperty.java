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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for a Property
 */
public class MetaProperty implements Comparable {

  /** static - flags */
  public final static int
    FILTER_NOT_HIDDEN = 1, // only those that are not marked as hidden
    FILTER_DEFAULT    = 2, // only those that are marked default
    FILTER_XREF       = 4; // xref && not !xref
  
  /** static - loaded images */    
  private static Map name2images = new HashMap();
  
  /** static - images */
  public final static ImageIcon
    IMG_CUSTOM  = loadImage("Attribute.gif"),
    IMG_LINK    = loadImage("Association.gif"),
    IMG_UNKNOWN = loadImage("Question.gif"),
    IMG_ERROR   = loadImage("Error.gif"),
    IMG_PRIVATE = loadImage("Private.gif");
    
  /** tag */
  private String tag;
  
  /** cached - image */
  private ImageIcon image;
  
  /** cached - type */
  private Class type;

  /** cached - info */
  private String info;
  
  /** whether this has been instantiated */
  boolean isInstantiated = false;
  
  /** whether this is grammar conform */
  private boolean isGrammar;
  
  /** properties */
  private Map attrs;
  
  /** subs */
  private Map tag2nested = new HashMap();
  List nested = new ArrayList();

  /**
   * Constructor
   */
  /*package*/ MetaProperty(String tag, Map attributes, boolean grammar) {
    // remember tags&props
    this.tag = tag;
    this.attrs = attributes;
    this.isGrammar = grammar;
    // inherit from super if applicable
    String path = (String)attributes.get("super");
    if (path!=null) {
      MetaProperty supr = Grammar.getMeta(new TagPath(path));
      // subs
      tag2nested.putAll(supr.tag2nested);
      nested.addAll(supr.nested);
      // type & image
      if (getAttribute("type")==null)
        attributes.put("type", supr.getAttribute("type"));
      if (getAttribute("img")==null)
        attributes.put("img", supr.getAttribute("img"));
    }
    // done
  }
  
  /**
   * Add a sub
   */
  /*package*/ void addNested(MetaProperty sub) {
    // keep key->sub
    tag2nested.put(sub.tag, sub);
    // keep list (replace existing!)
    for (int i=0; i<nested.size(); i++) {
      MetaProperty other = (MetaProperty)nested.get(i);
      if (other.tag.equals(sub.tag)) {
        nested.set(i, sub);
        return;       
      }
    }
    nested.add(sub);
    // done
  }
  
  /**
   * Acessor - nested meta properties
   * This is package private to make callees go through
   * indvidual properties rather than accessing this directly.
   */
  /*package*/ MetaProperty[] getAllNested(int filter) {
    
    // Loop over subs
    List result = new ArrayList(nested.size());
    for (int s=0;s<nested.size();s++) {
      
      // .. next sub
      MetaProperty sub = (MetaProperty)nested.get(s);

      // default only?
      if ((filter&FILTER_DEFAULT)!=0) {
        String isDefault = sub.getAttribute("default");
        if (isDefault==null||"0".equals(isDefault))
        continue;
      }
        
      // hidden at all (a.k.a cardinality == 0)?
      if ((filter&FILTER_NOT_HIDDEN)!=0&&"0".equals(sub.getAttribute("cardinality")))
        continue;

      // xref && not !xref   (FILTER_XREF = 4)
      if ((filter&FILTER_XREF)!=0) {
        if ("0".equals(sub.getAttribute("xref")))
          continue;
      } else {
        if ("1".equals(sub.getAttribute("xref")))
          continue;
      }
        
      // .. keep
      result.add(sub);
    }
    // done
    return (MetaProperty[])result.toArray(new MetaProperty[result.size()]);
  }
  
  /**
   * Lookup an attribute
   */
  /*package*/ String getAttribute(String key) {
    return (String)attrs.get(key);
  }
  
  /**
   * A comparison based on tag name
   */
  public int compareTo(Object o) {
    MetaProperty other = (MetaProperty)o;
    return getTag().compareTo(other.getTag());
  }

  /**
   * Test
   */
  public boolean allows(String sub) {
    // has to be defined as sub with isGrammar==true
    MetaProperty meta = (MetaProperty)tag2nested.get(sub);
    return meta==null ? false : meta.isGrammar;
  }
  
  /**
   * Test
   */
  public boolean allows(String sub, Class type) {
    // has to be defined as sub with isGrammar==true
    MetaProperty meta = (MetaProperty)tag2nested.get(sub);
    return meta!=null && type.isAssignableFrom(meta.getType());
  }
  
  /**
   * Create an instance
   * FIXME this should not be public but called through Property
   */
  public Property create(String value) {

    // let's try to instantiate    
    Property result;
    
    try {
      result = (Property)getType().newInstance();
      result = result.init(this, value);
    } catch (Exception e) {
      // 20030530 catch exceptions only - during load
      // an outofmemoryerrror could happen here
      Debug.log(Debug.WARNING, this, e);
      result = new PropertySimpleValue(); 
      ((PropertySimpleValue)result).init(this, value);
    }
    
    // increate count
    isInstantiated = true;

    // done 
    return result;
  }
  
  /**
   * Accessor - image
   */
  public ImageIcon getImage() {
    if (image==null) {
      // check 'img' attribute
      String s = getAttribute("img");
      // unknown?
      if (s==null) 
        image = getTag().startsWith("_") ? IMG_CUSTOM : IMG_UNKNOWN;
      else  // load it
        image = loadImage(s);
    }
    return image;
  }

  /**
   * Accessor - image
   */
  public ImageIcon getImage(String postfix) {
    Object name = getAttribute("img."+postfix);
    if (name==null) {
      // check err
      if ("err".equals(postfix))
        return IMG_ERROR;
      else
        return getImage() ;
    } 
    return loadImage(name.toString());
  }

  /**
   * Accessor - tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Accessor - type
   */
  public Class getType() {
    // check cached type
    if (type==null) {
      String attrType = getAttribute("type");
      if (attrType==null)
        type = PropertySimpleValue.class;
      else try {
        type = Class.forName("genj.gedcom."+attrType);
      } catch (Throwable t) {
        Debug.log(Debug.WARNING, this, "Property type genj.gedcom."+attrType+" couldn't be instantiated", t);    
        type = PropertySimpleValue.class;
      }
      // resolved
    }
    // done
    return type;
  }

  /**
   * Accessor - some explanationary information about the meta
   */
  public String getName() {
    String name = Gedcom.getResources().getString(tag+".name", false);
    return name==null ? tag : name;
  }
  
  /**
   * Accessor - some explanationary information about the meta
   */
  public String getInfo() {
    // check cached info
    if (info==null) {
      info = Gedcom.getResources().getString(tag+".info", false);
      if (info==null)
        info = "";
      else
        info = getName() + ":\n" + info;
    }
    // done
    return info;
  }
  
  /**
   * Resolve sub by tag
   */
  public MetaProperty getNested(String tag, boolean persist) {
    // current tag in map?
    MetaProperty result = (MetaProperty)tag2nested.get(tag);
    if (result==null) {
      result = new MetaProperty(tag, Collections.EMPTY_MAP, false);
      if (persist) addNested(result);
    }
    // done
    return result;
  }
  
  /**
   * Returns index of given subtag
   * @return zero based index or Integer.MAX_VALUE if unknown
   */
  public int getNestedIndex(String subtag) {
    // make sure CHAN get's a high one (this should probably be defined in grammar)
    if (subtag.equals("CHAN"))
      return Integer.MAX_VALUE;
    // look through grammar defined subs
    for (int i=0;i<nested.size();i++) {
      if (((MetaProperty)nested.get(i)).getTag().equals(subtag))
        return i;
    }
    //20040518 make the index of an unknown subtag as large as possible
    return Integer.MAX_VALUE;
  }
  
  /**
   * Load image (once)
   */
  private static ImageIcon loadImage(String name) {
    // look up
    ImageIcon result = (ImageIcon)name2images.get(name);
    if (result==null) {
      result = new ImageIcon(MetaProperty.class, "images/"+name);
      name2images.put(name, result);
    }
    // done
    return result;
  }
  
} //MetaProperty
