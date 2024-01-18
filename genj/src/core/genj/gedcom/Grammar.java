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

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The grammar of Gedcom files initialized from grammar.xml
 */
public class Grammar {

  /** singleton */
  private static Grammar instance;
  
  /** meta roots */
  private Map tag2root = new HashMap();
  
  /**
   * Singleton Constructor
   */
  private Grammar() {
  }
  
  /**
   * Singleton access
   */
  public static Grammar getInstance() {
    // already instantiated?
    if (instance==null) {
      synchronized (Grammar.class) {
        // check again
        if (instance==null) {
          
          // create lazyly
          instance = new Grammar();
          
          // parse descriptor (happening once only)
          try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(new InputSource(new InputStreamReader(Grammar.class.getResourceAsStream("grammar.xml"))), instance.new Parser());
          } catch (Throwable t) {
            Debug.log(Debug.ERROR, Grammar.class, "Couldn't parse grammar", t);
            throw new Error(t);
          }

          // instantiated now
        }
      }
    }
    // done
    return instance;
  }
  
  public static void main(String[] args) {
   getInstance();
  }
  
  /**
   * All used paths for given type 
   * @param etag tag of entity or null for all
   */
  public static TagPath[] getAllPaths(String etag, Class property) {
    return getInstance().getPathsRecursively(etag, property);
  }
  
  private TagPath[] getPathsRecursively(String etag, Class property) {
    
    // prepare result
    List result = new ArrayList();
    // loop through roots
    for (Iterator it=tag2root.values().iterator(); it.hasNext(); ) {
      MetaProperty root = (MetaProperty)it.next();
      String tag = root.getTag();
      if (etag==null||tag.equals(etag))
        getPathsRecursively(root, property, new TagPath(tag), result);
    }
    // done
    return TagPath.toArray(result);
  }

  private void getPathsRecursively(MetaProperty meta, Class property, TagPath path, Collection result) {
  
    // something worthwhile to dive into?
    if (!meta.isInstantiated) 
      return;
    
    // type match?
    if (property.isAssignableFrom(meta.getType())) 
      result.add(path);
      
    // recurse into
    for (Iterator it=meta.nested.iterator();it.hasNext();) {
      MetaProperty nested = (MetaProperty)it.next();
      getPathsRecursively(nested, property, new TagPath(path, nested.getTag()), result);
    }
    
    // done
  }

  /**
   * Get a MetaProperty by path
   */
  public static MetaProperty getMeta(TagPath path) {
    return getMeta(path, true);
  }

  /**
   * Get a MetaProperty by path
   */
  public static MetaProperty getMeta(TagPath path, boolean persist) {
    return getInstance().getMetaRecursively(path, persist);
  }
  
  /**
   * Get a MetaProperty by path
   */
  private MetaProperty getMetaRecursively(TagPath path, boolean persist) {
    
    String tag = path.get(0);
    
    MetaProperty root = (MetaProperty)tag2root.get(tag);
    
    // something we didn't know about yet?
    if (root==null) {
      root = new MetaProperty(tag, Collections.EMPTY_MAP, false);
      tag2root.put(tag, root);
    }
    
    // recurse into      
    return getMetaRecursively(root, path, 1, persist);
  }

  private MetaProperty getMetaRecursively(MetaProperty meta, TagPath path, int pos, boolean persist) {

    // is this it?
    if (pos==path.length())
      return meta;

    // get meta for next tag
    MetaProperty nested = meta.getNested(path.get(pos++), persist);
    return getMetaRecursively(nested, path, pos, persist);
  }

  /**
   * Grammar Parser
   */
  private class Parser extends DefaultHandler {
    
    private Stack stack = null;
    
    /* element callback */
    public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName, Attributes attributes) throws org.xml.sax.SAXException {
      
      // in case we don't already have a stack running this better be GEDCOM
      if (stack==null) {
        if (!"GEDCOM".equals(qName)) 
          throw new RuntimeException("expected GEDCOM");
        stack = new Stack();
        return;
      }
      
      // grab attributes
      Map properties = new HashMap();
      for (int i=0,j=attributes.getLength();i<j;i++)
        properties.put(attributes.getQName(i), attributes.getValue(i));
      
      // create a meta property for element
      MetaProperty meta = new MetaProperty(qName, properties, true);

      // a property root (a.k.a entity) or a nested one?
      if (stack.isEmpty()) 
        tag2root.put(qName, meta);
      else
        ((MetaProperty)stack.peek()).addNested(meta);
        
      // push on stack
      stack.push(meta);
      
      // done
    }

    /*/element callback */
    public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws org.xml.sax.SAXException {
      // end of gedcom or normal pop?
      if ("GEDCOM".equals(qName))
        stack = null;
      else
        stack.pop();
    }
  } //Parser
  
} //Grammar
