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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Wrapper for a Property
 */
public class MetaProperty {

  /** static - images */
  private final static String
    IMG_UNKNOWN = "Question.gif",
    IMG_ERROR   = "Error.gif";
    
  /** static - loaded images */    
  private static Map name2images = new HashMap();
  
  /** static - root for entities  */
  private static Map roots = new HashMap();
  
  /** static - one parser that is triggered */
  private static GrammerParser parser = new GrammerParser();
  
  /** super */
  private MetaProperty supr;
  
  /** tag */
  private String tag;
  
  /** cached - image */
  private ImageIcon image;
  
  /** cached - type */
  private Class type;

  /** cached - info */
  private String info;
  
  /** properties */
  private Map props;
  
  /** subs */
  private List defSubs = new ArrayList(16);
  private List visibleSubs = new ArrayList(16);
  private Map  allSubs = new HashMap();
    

  /**
   * Constructor
   */
  private MetaProperty(String tag, Map props, MetaProperty supr) {
    // remember
    this.tag = tag;
    this.props = props;
    this.supr = supr;
    // done
  }
  
  /**
   * Resolve property
   */
  private String getProperty(String key, String fallback) {
    String result = (String)props.get(key);
    if (result==null) result = fallback;
    return result;
  }

  /**
   * Inherit subs from super
   */
  private void inherit() {
      
    // super?
    if (supr==null) return;
    
    // grab subs
    allSubs.putAll(supr.allSubs);
    defSubs.addAll(supr.defSubs);
    visibleSubs.addAll(supr.visibleSubs);
    
    // done
  }
  
  /**
   * Load image (once)
   */
  /*package*/ static ImageIcon loadImage(String name) {
    // look up
    ImageIcon result = (ImageIcon)name2images.get(name);
    if (result==null) {
      result = new ImageIcon(MetaProperty.class, "images/"+name);
      name2images.put(name, result);
    }
    // done
    return result;
  }
  
  /**
   * Create an instance
   */
  public Property create(String value) {

    // let's try to instantiate    
    Property result;
    
    try {
      result = (Property)getType().newInstance();
      result.setTag(tag);
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this, t);
      result = new PropertySimpleValue(); 
      ((PropertySimpleValue)result).setTag(tag);
    }
    
    // initialize value
    result.setValue(value);

    // done 
    return result;
  }
  
  /**
   * Accessor - image
   */
  public ImageIcon getImage() {
    if (image==null)
      image = loadImage(getProperty("img", IMG_UNKNOWN));
    return image;
  }

  /**
   * Accessor - image
   */
  public ImageIcon getImage(String postfix) {
    Object name = props.get("img."+postfix);
    if (name==null) return getImage() ;
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
      String clazz = "genj.gedcom."+getProperty("type", "PropertySimpleValue");
      try {
        type = Class.forName(clazz);

      } catch (ClassNotFoundException e) {
        Debug.log(Debug.WARNING, this, "Property type "+clazz+" can't be loaded", e);    
        type = PropertySimpleValue.class;
      }
      // resolved
    }
    // done
    return type;
  }
  
  /**
   * Accessor - Type Chec
   */
  public boolean isType(Class clazz) {
    return clazz.isAssignableFrom(getType());
  }

  /**
   * Accessor - some explanationary information about the meta
   */
  public String getInfo() {
    // check cached info
    if (info==null) {
      String tag = getTag();
      info = Gedcom.getResources().getString(tag+".name")
        +":\n"+Gedcom.getResources().getString(tag+".info");
    }
    // done
    return info;
  }
  
  /**
   * Acessor - subs
   */
  /*package*/ MetaProperty[] getDefaultSubs() {
    return toArray(defSubs);
  }
  
  /**
   * Acessor - subs
   */
  /*package*/ MetaProperty[] getVisibleSubs() {
    return toArray(visibleSubs);
  }
  
  /**
   * Acessor - subs
   */
  /*package*/ MetaProperty[] getAllSubs() {
    return toArray(allSubs.values());
  }
  
  /**
   * Resolve sub by tag
   */
  public MetaProperty get(String tag) {
    // current tag in map?
    MetaProperty result = (MetaProperty)allSubs.get(tag);
    if (result==null) {
      result = new MetaProperty(tag, Collections.EMPTY_MAP, null);
      allSubs.put(tag, result);
    }
    // done
    return result;
  }
  
  /**
   * Static - resolve instance
   */
  public static MetaProperty get(TagPath path, boolean persist) {
    return getRecursively(roots, path, 0, persist);
  }
  
  public static MetaProperty get(TagPath path) {
    return get(path, true);
  }
  
  private static MetaProperty getRecursively(Map map, TagPath path, int pos, boolean persist) {
    
    // current tag in map?
    String tag = path.get(pos++);
    MetaProperty result = (MetaProperty)map.get(tag);
    if (result==null) {
      result = new MetaProperty(tag, Collections.EMPTY_MAP, null);
      if (persist) map.put(tag, result);
    }
    
    // more to go?
    if (pos<path.length()) return getRecursively(result.allSubs, path, pos, persist);
    
    // done
    return result;
  }

  /**
   * Static - resolve instance
   */
  public static MetaProperty get(Property prop) {
    return get(prop.getPath());    
  }

  /**
   * Static - resolve instance
   */
  public static MetaProperty get(Property prop, String sub) {
    return get(prop).get(sub);    
  }
  
  /**
   * Static - paths for given type
   */
  public static TagPath[] getPaths(Class property) {
    // prepare result
    List result = new ArrayList();
    // loop through roots
    getPathsRecursively(roots, property, new Stack(), result);
    // done
    return TagPath.toArray(result);
  }
  
  private static void getPathsRecursively(Map map, Class property, Stack stack, Collection result) {
    
    // loop subs
    Iterator it = map.values().iterator();
    while (it.hasNext()) {
      MetaProperty sub = (MetaProperty)it.next();
      // something worthwhile to dive into?
      if (sub.type==null) continue;
      // trace it
      stack.push(sub.tag);
      // type match?
      if (sub.type!=null&&property.isAssignableFrom(sub.type)) 
        result.add(new TagPath(stack));
      // recurse into
      getPathsRecursively(sub.allSubs, property, stack, result);
      // rewind
      stack.pop();
    }
    
    // done
  }
    
  /**
   * Get an array out of collection
   */
  public static MetaProperty[] toArray(Collection c) {
    return (MetaProperty[])c.toArray(new MetaProperty[c.size()]);
  }
  
  /**
   * The Gedcom Grammer read
   */
  private static class GrammerParser {
    
    /** the current stack of MetaProperties */
    private List stack = new ArrayList();
    
    /**
     * Constructor
     */
    GrammerParser() {
      // parse grammar.properties
      try {
        parse(MetaProperty.class.getResourceAsStream("grammar.properties"));
      } catch (IOException e) {
        Debug.log(Debug.ERROR, this, e.getMessage(), e);
        throw new Error();
      }
      // done    
    }
    
    /**
     * parse input
     */
    private void parse(InputStream in) throws IOException {

      BufferedReader reader = new BufferedReader(new InputStreamReader(in));      
    
      // loop over lines
      while (true) {

        // read next line
        String line = reader.readLine();
        if (line==null) break;
        
        // .. and trim
        line = line.trim();
        if (line.length()==0||line.startsWith("#")) continue;
        
        // work on line
        push(line);
        
        // continue 
      }
      
      // done     
    }
    
    /**
     * push a line
     */
    private void push(String line) {

      // break into tokens
      StringTokenizer tokens = new StringTokenizer(line);
        
      // grab level and tag
      int level = Integer.parseInt(tokens.nextToken());
      String tag = tokens.nextToken();
        
      // grab props
      Map props = new FIFO();
      
      while (tokens.hasMoreTokens()) {
        String prop = tokens.nextToken();
        int i = prop.indexOf('=');
        // .. either 'abc=def' or 'xyz'
        if (i>0) props.put(prop.substring(0,i), prop.substring(i+1));
        else props.put(prop, "");
      }
      
      boolean isDefault = props.containsKey("default"); 
      boolean isHidden  = props.containsKey("hide");
      
      // do we have to take elements from the stack first?
      while (stack.size()>level) pop();
      
      // check super's properties first
      MetaProperty supr = null;
      Object path = props.get("super");
      if (path!=null) {
        supr = MetaProperty.get(new TagPath(path.toString()));
        props.putAll(supr.props);
      }
      
      // instantiate
      MetaProperty meta = new MetaProperty(tag, props, supr);
      if (level==0) {
        roots.put(tag, meta);
        meta.getType(); // resolve type otherwise getPaths won't find anything
      } else {
        MetaProperty parent = peek();
        parent.allSubs.put(tag, meta);
        if (!isHidden) parent.visibleSubs.add(meta);
        if (isDefault) parent.defSubs.add(meta);
      }
      
      // add to end of stack
      stack.add(meta);
    }

    /**
     * Pop from stack
     */
    private MetaProperty pop() {
      // here's the one going away
      MetaProperty meta = (MetaProperty)stack.remove(stack.size()-1);
      // tell it to inherit now (after it has been read completely!)
      meta.inherit();
      // done 
      return meta;
    }
    
    /**
     * Peek from stack
     */
    private MetaProperty peek() {
      return (MetaProperty)stack.get(stack.size()-1);
    }
    
  } //Parser
  
  /**
   * Map that doesn't overwrite values
   */
  private static class FIFO extends HashMap {
    /**
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
      if (containsKey(key)) return null;
      return super.put(key, value);
    }

  } //FIFO
  
} //MetaDefinition
