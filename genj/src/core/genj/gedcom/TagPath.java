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
   */
  public TagPath(TagPath other, int pos, int selector) {
    
    // setup len
    len = other.len;
  
    // copy and change
    tags = new String[len];
    System.arraycopy(other.tags, 0, tags, 0, other.len);
    
    tags[pos] = get(pos)+"#"+selector;
    
    // prepare our hash
    hash = other.hash-other.tags[pos].hashCode()+tags[pos].hashCode();
    
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
    
    // gotta match for at least tag.length
    if (!element.regionMatches(0, tag, 0, tag.length()))
      return false;
    
    // simplest case of 'BIRT' matches 'BIRT' (implying 0 selector)
    if (element.length()==tag.length())
      return true;
  
    // no good if followed by other than selector
    if (element.charAt(tag.length())!='#')
      return false;
    
    // is good
    return true;
  }
  
  /**
   * Compares the selector at given position with argument
   */
  public boolean equals(int at, int selector) {
    
    // what's the element 'at'
    String element = tags[at];

    // no selector - gotta be 0 
    int i = element.indexOf('#');
    if (i<0)
      return selector==0;
    
    // selector has to fit after '#'?
    String s = Integer.toString(selector);
    if (element.length()-i-1!=s.length())
      return false;
    
    return element.regionMatches(i+1, s, 0, s.length());
  }
  
//  /**
//   * Matches a path element with given tag
//   * @param which the path element to match
//   * @param tag the tag to match it with
//   * @return true if tag equals path[which]
//   */
//  public boolean match(int which, String tag) {
//    return match(which, tag, 0)!=MATCH_NONE;
//  }
//  
//  /**
//   * Matches a path element with given tag and selector
//   * @param which the path element to match
//   * @param tag the tag to match it with
//   * @param selector the selector (e.g. '2') to match it with
//   * @return MATCH_NONE if tag is not the same, 
//   *          MATCH_TAG if tag is the same but selector is different,
//   *          MATCH_ALL if tag and selector are the same
//   */
//  public int match(int which, String tag, int selector) {
//
//    // compare path element 'which' 
//    String element = tags[which];
//    
//    // gotta match for tag.length - e.g. 'BIRT#0' matches 'BIRT'
//    if (!element.regionMatches(0, tag, 0, tag.length()))
//      return MATCH_NONE;
//      
//    // simplest case of 'BIRT' matches 'BIRT' (implying 0 selector)
//    if (element.length()==tag.length())
//      return selector==0 ? MATCH_ALL : MATCH_TAG;
//    
//    // gotta be selector case case then 'BIRT#x' matching 'BIRT'
//    if (element.charAt(tag.length())!='#')
//      return MATCH_NONE;
//      
//    // selector wrong? e.g. in case of 'BIRT#1' match 1 with 'selector' #2?
//    if (!element.regionMatches(tag.length()+1, Integer.toString(selector), 0, element.length()-tag.length()-1))
//      return MATCH_TAG;
//      
//    // all well
//    return MATCH_ALL;
//  }

  /**
   * Returns the n-th tag of this path (this won't return the selector information e.g. BIRT#1 but only BIRT)
   * @param which 0-based number
   * @return tag as <code>String</code>
   */
  public String get(int which) {
    String result = tags[which];
    int selector = result.indexOf('#');
    if (selector>=0)
      result = result.substring(0,selector);
    return result;
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


} //TagPath
