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
 */
public class TagPath {

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
  public TagPath(Property[] props) {
    
    // decompose property path
    len = props.length;
    tags = new String[len];

    // grab prop's tags and hash
    for (int i=0; i<len; i++) {
      tags[i] = props[i].getTag();
      hash += tags[i].hashCode();
    }
    
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
    tags = new String[len];
    for (int i=0; i<len; i++) {
      tags[i] = other.tags[i];
      hash += tags[i].hashCode();
    }
    // done
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
   * Add another path element to the end of this path
   */
  public void add(String element) {
    // ensure capacity
    if (len==tags.length) {
      String[] tmp = new String[len+1];
      System.arraycopy(tags, 0, tmp, 0, len);
      tags = tmp;
    }
    // add element
    tags[len++] = element;
    hash += element.hashCode();
    // done
  }
  
  /**
   * Removes the last path element from the end of this path
   */
  public String pop() {
    // won't allow illegal path
    if (len==1)
      throw new IllegalArgumentException("can't pop < 1");
    // remove it
    String result = tags[--len];  
    hash -= result.hashCode();
    // done
    return result;
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
    return tags[0];
  }

  /**
   * Returns the last tag of this path
   * @return last tag as <code>String</code>
   */
  public String getLast() {
    return tags[len-1];
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
   * Simple test for path : contains ':'
   */
  public static boolean isPath(String path) {
    return path.indexOf(':')>0;
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
  

} //TagPath
