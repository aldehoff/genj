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

  /** static - flags */
  public final static int
    FILTER_NOT_HIDDEN = 1,
    FILTER_DEFAULT    = 2,
    FILTER_LINKED     = 4,
    FILTER_NOT_LINKED = 8; 
  
  /** static - loaded images */    
  private static Map name2images = new HashMap();
  
  /** static - images */
  public final static ImageIcon
    IMG_LINK    = loadImage("Association.gif"),
    IMG_UNKNOWN = loadImage("Question.gif"),
    IMG_ERROR   = loadImage("Error.gif");
    
  /** static - root for entities  */
  private static MetaProperty[] roots = new MetaProperty[Gedcom.NUM_TYPES];
  
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
  
  /** whether this has been instantiated */
  private boolean instantiated = false;
  
  /** properties */
  private Map props;
  
  /** subs */
  private Map mapOfSubs = new HashMap();
  private List listOfSubs = new ArrayList();

  /**
   * Constructor
   */
  private MetaProperty(String tag, Map props) {
    // remember tags&props
    this.tag = tag;
    this.props = props;
    // find super
    String path = getAttribute("super", null);
    if (path!=null) supr = MetaProperty.get(new TagPath(path));
    // inherit from super?
    if (supr!=null) {
      // subs
      mapOfSubs.putAll(supr.mapOfSubs);
      listOfSubs.addAll(supr.listOfSubs);
      // type & image
      if (getAttribute("type",null)==null)
        props.put("type", supr.getAttribute("type",null));
      if (getAttribute("img",null)==null)
        props.put("img", supr.getAttribute("img",null));
    }
    // done
  }
  
  /**
   * Resolve property
   */
  private String getAttribute(String key, String fallback) {
    String result = (String)props.get(key);
    if (result==null) result = fallback;
    return result;
  }

  /**
   * Add a sub
   */
  private void addSub(MetaProperty sub) {
    // keep key->sub
    mapOfSubs.put(sub.tag, sub);
    // keep list (replace existing!)
    for (int i=0; i<listOfSubs.size(); i++) {
      MetaProperty other = (MetaProperty)listOfSubs.get(i);
      if (other.tag.equals(sub.tag)) {
        listOfSubs.set(i, sub);
        return;       
      }
    }
    listOfSubs.add(sub);
    // done
  }
  
  /**
   * Acessor - subs
   */
  /*package*/ MetaProperty[] getSubs(int filter) {
    
    // Loop over subs
    List result = new ArrayList(listOfSubs.size());
    for (int s=0;s<listOfSubs.size();s++) {
      
      // .. next sub
      MetaProperty sub = (MetaProperty)listOfSubs.get(s);

      // .. filter
      if ((filter&FILTER_DEFAULT)!=0&&sub.getAttribute("default",null)==null)
        continue;
      if ((filter&FILTER_NOT_HIDDEN)!=0&&sub.getAttribute("hide",null)!=null)
        continue;
      if ((filter&FILTER_LINKED)!=0&&sub.getAttribute("linked",null)==null)
        continue;
      if ((filter&FILTER_NOT_LINKED)!=0&&sub.getAttribute("linked",null)!=null)
        continue;
      
      // .. keep
      result.add(sub);
    }
    // done
    return toArray(result);
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
      result = result.init(tag, value);
    } catch (Exception e) {
      // 20030530 catch exceptions only - during load
      // an outofmemoryerrror could happen here
      Debug.log(Debug.WARNING, this, e);
      result = new PropertySimpleValue(); 
      ((PropertySimpleValue)result).init(tag, value);
    }
    
    // increate count
    instantiated = true;

    // done 
    return result;
  }
  
  /**
   * Accessor - image
   */
  public ImageIcon getImage() {
    if (image==null) {
      String s = getAttribute("img", null);
      image = s==null ? IMG_UNKNOWN : loadImage(s);
    }
    return image;
  }

  /**
   * Accessor - image
   */
  public ImageIcon getImage(String postfix) {
    Object name = props.get("img."+postfix);
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
      String clazz = "genj.gedcom."+getAttribute("type", "PropertySimpleValue");
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
   * Resolve sub by tag
   */
  public MetaProperty get(String tag, boolean persist) {
    // current tag in map?
    MetaProperty result = (MetaProperty)mapOfSubs.get(tag);
    if (result==null) {
      result = new MetaProperty(tag, Collections.EMPTY_MAP);
      if (persist) addSub(result);
    }
    // done
    return result;
  }
  
  /**
   * Static - resolve instance
   */
  public static MetaProperty get(TagPath path, boolean persist) {
    return getRecursively(roots[Gedcom.getTypeFor(path.get(0))], path, 1, persist);
  }
  
  public static MetaProperty get(TagPath path) {
    return get(path, true);
  }
  
  private static MetaProperty getRecursively(MetaProperty meta, TagPath path, int pos, boolean persist) {

    // is this it?
    if (pos==path.length())
      return meta;

    // get meta for next tag
    MetaProperty next = meta.get(path.get(pos++), persist);
    return getRecursively(next, path, pos, persist);
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
    return get(prop).get(sub, true);    
  }
  
  /**
   * Static - paths for given type
   */
  public static TagPath[] getPaths(int entity, Class property) {
    // prepare result
    List result = new ArrayList();
    // loop through roots
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      if (entity<0||entity==t)
        getPathsRecursively(roots[t], property, new Stack(), result);
    }
    // done
    return TagPath.toArray(result);
  }
  
  private static void getPathsRecursively(MetaProperty meta, Class property, Stack stack, Collection result) {

    // something worthwhile to dive into?
    if (!meta.instantiated) 
      return;
    
    // trace it
    stack.push(meta.tag);

    // type match?
    if (property.isAssignableFrom(meta.getType())) 
      result.add(new TagPath(stack));
      
    // recurse into
    for (Iterator it=meta.listOfSubs.iterator();it.hasNext();) {
      MetaProperty sub = (MetaProperty)it.next();
      getPathsRecursively(sub, property, stack, result);
    }
    
    // rewind
    stack.pop();
      
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
      Map props = new HashMap();
      
      while (tokens.hasMoreTokens()) {
        String prop = tokens.nextToken();
        int i = prop.indexOf('=');
        // .. either 'abc=def' or 'xyz'
        if (i>0) props.put(prop.substring(0,i), prop.substring(i+1));
        else props.put(prop, "");
      }
      
      // do we have to take elements from the stack first?
      while (stack.size()>level) pop();
      
      // instantiate
      MetaProperty meta = new MetaProperty(tag, props);
      if (level==0) {
        roots[Gedcom.getTypeFor(tag)] = meta;
        meta.instantiated = true; // fake instantiated
      } else {
        peek().addSub(meta);
      }
      
      // add to end of stack
      stack.add(meta);
    }

    /**
     * Pop from stack
     */
    private void pop() {
      stack.remove(stack.size()-1);
    }
    
    /**
     * Peek from stack
     */
    private MetaProperty peek() {
      return (MetaProperty)stack.get(stack.size()-1);
    }
    
  } //Parser
  
} //MetaDefinition
