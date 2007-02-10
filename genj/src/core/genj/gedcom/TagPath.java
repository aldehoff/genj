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

import java.util.Collection;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Class for encapsulating a path of tags that describe the way throug
 * a tree of properties. An example for a path is TagPath("INDI:BIRT:DATE")
 * which denotes the <em>date</em> of property <em>birth</em> of an
 * individual.
 * @author  Nils Meier
 * @version 0.1 04/21/98
 * @version 2004/08/25 made immutable
 */
public class TagPath {
  
  /** a logical name */
  private String name = null;

  /** the list of tags that describe the path */
  private String tags[];
  
  /** the length (= number of elements) in tags (length<=tags.length) */
  private int len;
  
  /** the hash of this path (immutable) */
  private int hash = 0;
  
  /** our marker */
  public final static char SEPARATOR = ':';
  public final static String SEPARATOR_STRING = String.valueOf(SEPARATOR);

  /**
   * Constructor for TagPath
   * @param path path as colon separated string value a:b:c
   * @exception IllegalArgumentException in case format isn't o.k.
   */
  public TagPath(String path) throws IllegalArgumentException {
    this(path, null);
  }
  
  /**
   * Constructor for TagPath
   * @param path path as colon separated string value a:b:c
   * @exception IllegalArgumentException in case format isn't o.k.
   */
  public TagPath(String path, String name) throws IllegalArgumentException {
    
    // keep name
    this.name = name;

    // Parse path
    StringTokenizer tokens = new StringTokenizer(path,SEPARATOR_STRING,false);
    len = tokens.countTokens();
    if (len==0||path.charAt(0)==SEPARATOR||path.charAt(path.length()-1)==SEPARATOR)
      throw new IllegalArgumentException("No valid path '"+path+"'");

    // ... setup data
    tags = new String[len];
    for (int i=0;i<len;i++) {
      String token = tokens.nextToken().trim();
      if (token.length()==0) 
        throw new IllegalArgumentException("No valid path token #"+(i+1)+" in '"+path+"'");
      tags[i] = token;
      hash += tags[i].hashCode();
    }
    
    // Done
  }
  
  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other) {
    this(other, other.len);
  }

  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other, int length) {
    // copyup to len and rehash
    len = length;
    tags = other.tags;
    for (int i=0; i<len; i++)
      hash += tags[i].hashCode();
    // done
  }

  /**
   * Constructor for TagPath
   */
  public TagPath(TagPath other, String tag) {
    
    // setup len
    len = other.len+1;
  
    // copy and append   
    tags = new String[len];
    System.arraycopy(other.tags, 0, tags, 0, other.len);
    tags[len-1] = tag;
    
    // prepare our hash
    hash = other.hash+tag.hashCode();
  }
  
  /**
   * Constructor for TagPath
   * @param path path as colon separated string value c:b:a
   * @exception IllegalArgumentException in case format isn't o.k.
   * @return the path [a:b:c]
   */
  /*package*/ TagPath(Stack path) throws IllegalArgumentException {
    // grab stack elements
    len = path.size();
    tags = new String[len];
    for (int i=0;i<len;i++) {
      tags[i] = path.pop().toString();
      hash += tags[i].hashCode();
    }
    // done
  }
  
  /**
   * Wether this path starts with prefix
   */
  public boolean startsWith(TagPath prefix) {
    // not if longer
    if (prefix.len>len) 
      return false;
    // check
    for (int i=0;i<prefix.len;i++) {
      if (!tags[i].equals(prefix.tags[i])) return false;
    }
    // yes
    return true;
  }

  /**
   * Returns comparison between two TagPaths
   */
  public boolean equals(Object obj) {

    // Me ?
    if (obj==this) 
      return true;

    // TagPath ?
    if (!(obj instanceof TagPath))
      return false;

    // Size ?
    TagPath other = (TagPath)obj;
    if (other.len!=len) 
      return false;

    // Elements ?
    for (int i=0;i<len;i++) {
      if (!tags[i].equals(other.tags[i])) 
        return false;
    }

    // Equal
    return true;
  }
  
  public final static int
    MATCH_NONE = 0,
    MATCH_TAG  = 1,
    MATCH_ALL  = 2;

  /**
   * Compares the tag at given position with argument
   */
  public boolean equals(int at, String tag) {
    
    // what's the element 'at'
    String element = tags[at];
    return element.equals(tag);
  }
  
  /**
   * Returns the n-th tag of this path 
   * @param which 0-based number
   * @return tag as <code>String</code>
   */
  public String get(int which) {
    return tags[which];
  }

  /**
   * Returns the first tag of this path
   * @return first tag as <code>String</code>
   */
  public String getFirst() {
    return get(0);
  }

  /**
   * Returns the last tag of this path
   * @return last tag as <code>String</code>
   */
  public String getLast() {
    return get(len-1);
  }

  /**
   * Returns the length of this path
   * @return length of this path
   */
  public int length() {
    return len;
  }
    
  /**
   * Returns the path as a string
   */
  public String toString() {
    String result = tags[0];
    for (int i=1;i<len;i++)
      result = result + ":" + tags[i];
    return result;
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return hash;
  }
  
  /**
   * Accessor - name
   */
  public String getName() {
    if (name==null) {
      // try to find a reasonable tag to display as text (that's not '.' or '*')
      int i = length()-1;
      String tag = get(i);
      while (i>1&&!Character.isLetter(tag.charAt(0))) 
        tag = get(--i);
      
      // as text
      name = Gedcom.getName(tag);
      
      // date or place?
      //if (i>1&&(tag.equals("DATE")||tag.equals("PLAC"))) 
      if (i>1) 
        name = name + " - " + Gedcom.getName(get(i-1));
    }
    return name;
  }

  /**
   * Resolve a path from given property
   */
  public static TagPath get(Property prop) {
    
    String p = prop.getTag();
    while (!(prop instanceof Entity)) {
      prop = prop.getParent();
      p = prop.getTag() + ":" + p;
    }
    
    // done
    return new TagPath(p);
  }

  /**
   * Get an array out of collection
   */
  public static TagPath[] toArray(Collection c) {
    return (TagPath[])c.toArray(new TagPath[c.size()]);
  }
  
  /**
   * Get an array of tag Paths out of an array of strings
   */
  public static TagPath[] toArray(String[] paths) {
    TagPath[] result = new TagPath[paths.length];
    for (int i=0; i<result.length; i++) {
      result[i] = new TagPath(paths[i]);
    }
    return result;
  }

  /**
   * Iterate a properties nodes corresponding to this path
   */
  public void iterate(Property root, PropertyVisitor visitor) {
    iterate(0, root, visitor, get(0));
  }

  private boolean iterate(int pos, Property prop, PropertyVisitor visitor, String unconfirmedTag) {
    
    // traversed path?
    if (pos==length())
      return visitor.leaf(prop);
    
    // a '..'?
    if (get(pos).equals("..")) {
      Property parent = prop.getParent();
      // no parent?
      if (parent==null)
        return false;
      // continue with parent
      return iterate(pos+1, parent, visitor, null);
    }
    
    // a '.'?
    if (get(pos).equals( ".")) {
      // continue with self
      return iterate(pos+1, prop, visitor, null);
    }

    // a '*'?
    if (get(pos).equals( "*")) {
      // check out target
      if (!(prop instanceof PropertyXRef))
        return false;
      prop = ((PropertyXRef)prop).getTarget();
      if (prop==null)
        return false;
      // continue with target
      return iterate(pos+1, prop, visitor, null);
    }

    // still have to confirm a tag?
    if (unconfirmedTag!=null) {
      if (!get(pos).equals(unconfirmedTag))
        return true;
      // go with prop then
      return iterate(pos+1, prop, visitor, null);
    }
    
    // let visitor know that we're recursing now
    if (!visitor.recursion(prop, get(pos)))
      return false;
    
    // recurse into each appropriate child
    for (int i=0;i<prop.getNoOfProperties();i++) {

      Property ith = prop.getProperty(i);

      // tag is good?
      if (get(pos).equals(ith.getTag())) {
        if (!iterate(pos+1, ith, visitor, null))
          return false;
      }
    }
    
    // backtrack
    return true;
  }
  
} //TagPath
