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
import java.util.StringTokenizer;

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
    
  /** static - root for entities  */
  private static Map tag2root = new HashMap();
  
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
  private boolean isInstantiated = false;
  
  /** whether this is grammar conform */
  private boolean isGrammar;
  
  /** properties */
  private Map props;
  
  /** subs */
  private Map mapOfSubs = new HashMap();
  private List listOfSubs = new ArrayList();

  /**
   * Constructor
   */
  private MetaProperty(String tag, Map props, boolean grammar) {
    // remember tags&props
    this.tag = tag;
    this.props = props;
    this.isGrammar = grammar;
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
    MetaProperty meta = (MetaProperty)mapOfSubs.get(sub);
    return meta==null ? false : meta.isGrammar;
  }
  
  /**
   * Acessor - subs
   * This is package private to make callees go through
   * indvidual properties rather than accessing this directly.
   */
  /*package*/ MetaProperty[] getSubs(int filter) {
    
    // Loop over subs
    List result = new ArrayList(listOfSubs.size());
    for (int s=0;s<listOfSubs.size();s++) {
      
      // .. next sub
      MetaProperty sub = (MetaProperty)listOfSubs.get(s);

      // default only?
      if ((filter&FILTER_DEFAULT)!=0&&sub.getAttribute("default",null)==null)
        continue;
        
      // hidden at all?
      if ((filter&FILTER_NOT_HIDDEN)!=0&&sub.getAttribute("hide",null)!=null)
        continue;

      //FILTER_XREF       = 4; 
      // xref && not !xref
      if ((filter&FILTER_XREF)!=0) {
        if (sub.getAttribute("!xref",null)!=null)
          continue;
      } else {
        if (sub.getAttribute("xref",null)!=null)
          continue;
      }
        
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
      String s = getAttribute("img", null);
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
  public String getName() {
    return Gedcom.getResources().getString(tag+".name");
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
      result = new MetaProperty(tag, Collections.EMPTY_MAP, false);
      if (persist) addSub(result);
    }
    // done
    return result;
  }
  
  /**
   * Returns index of given subtag
   * @return zero based index or Integer.MAX_VALUE if unknown
   */
  public int getIndex(String subtag) {
    for (int i=0;i<listOfSubs.size();i++) {
      if (((MetaProperty)listOfSubs.get(i)).getTag().equals(subtag))
        return i;
    }
    //20040518 make the index of an unknown subtag as large as possible
    return Integer.MAX_VALUE;
  }
  
  /**
   * Static - resolve instance
   */
  public static MetaProperty get(TagPath path, boolean persist) {
    String tag = path.get(0);
    MetaProperty root = (MetaProperty)tag2root.get(tag);
    // something we didn't know about yet?
    if (root==null) {
      root = new MetaProperty(tag, Collections.EMPTY_MAP, false);
      tag2root.put(tag, root);
    }
    // recurse into      
    return getRecursively(root, path, 1, persist);
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
   * Static - paths for given type (use etag==null for all)
   */
  public static TagPath[] getPaths(String etag, Class property) {
    // prepare result
    List result = new ArrayList();
    // loop through roots
    for (Iterator it=tag2root.values().iterator();it.hasNext();) {
      MetaProperty root = (MetaProperty)it.next();
      String tag = root.getTag();
      if (etag==null||tag.equals(etag))
        getPathsRecursively(root, property, new TagPath(tag), result);
    }
    // done
    return TagPath.toArray(result);
  }
  
  private static void getPathsRecursively(MetaProperty meta, Class property, TagPath path, Collection result) {

    // something worthwhile to dive into?
    if (!meta.isInstantiated) 
      return;
    
    // type match?
    if (property.isAssignableFrom(meta.getType())) 
      result.add(new TagPath(path));
      
    // recurse into
    for (Iterator it=meta.listOfSubs.iterator();it.hasNext();) {
      MetaProperty sub = (MetaProperty)it.next();
      path.add(sub.tag);
      getPathsRecursively(sub, property, path, result);
      path.pop();
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
      MetaProperty meta = new MetaProperty(tag, props, true);
      if (level==0) {
        tag2root.put(tag, meta);
        meta.isInstantiated = true; // fake instantiated
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
