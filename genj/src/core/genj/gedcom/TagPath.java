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

  /** the position in the path */
  private int position;

  /**
   * Constructor for TagPath
   * @param path path as colon separated string value a:b:c
   * @exception IllegalArgumentException in case format isn't o.k.
   */
  public TagPath(String path) throws IllegalArgumentException {

    // Parse path
    StringTokenizer tokens = new StringTokenizer(path,":",false);
    int num = tokens.countTokens();
    if (num==0)
      throw new IllegalArgumentException("No valid path :"+path);

    // ... setup data
    tags = new String[num];
    for (int i=0;i<num;i++) {
      tags[i] = tokens.nextToken();
    }

    position = 0;

    // Done
  }

  /**
   * Returns the path as a string
   * @return a colon separated string representation of this path a:b:c
   */
  public String asString() {
    String result = tags[0];
    for (int i=1;i<tags.length;i++)
      result = result + ":" + tags[i];
    return result;
  }

  /**
   * Sets this path's internal postition one position back
   * @exception IllegalArgumentException in case internal position can't be backed
   */
  public void back() {
    if (position==0)
      throw new IllegalArgumentException("Path's internal position can't be backed");
    position--;
  }

  /**
   * Returns comparison between two TagPaths
   */
  public boolean equals(Object obj) {

    // Me ?
    if (obj==this) {
      return true;
    }

    // TagPath ?
    if (!(obj instanceof TagPath)) {
      return false;
    }

    // Size ?
    TagPath other = (TagPath)obj;
    if (other.length()!=length()) {
      return false;
    }

    // Elements ?
    for (int i=0;i<tags.length;i++) {
      if (!tags[i].equals(other.tags[i])) {
        return false;
      }
    }

    // Equal
    return true;
  }

  /**
   * Fill Hashtable with paths to properties
   */
  static private void fillHashtableWithPaths(Hashtable hash, Property prop, String path) {

    String p = path+prop.getTag();

    if (prop.getNoOfProperties()==0) {
      if (!hash.contains(p)) {
        hash.put(p,new TagPath(p));
      }
      return;
    }

    p+=":";
    for (int c=0;c<prop.getNoOfProperties();c++) {
      fillHashtableWithPaths(hash,prop.getProperty(c),p);
    }

  }
  /**
   * Returns the n-th tag of this path
   * @param which 1-based number
   * @return tag as <code>String</code>
   */
  public String get(int which) {
    return tags[which];
  }

  /**
   * Returns the last tag of this path
   * @return last tag as <code>String</code>
   */
  public String getLast() {
    return tags[tags.length-1];
  }

  /**
   * Returns the next tag in internal kept order
   * @return next tag as <code>String</code>
   */
  public String getNext() {
    String result = tags[position];
    position++;
    return result;
  }

  /**
   * Calculate all appearing TagPaths in given gedcom
   */
  static public TagPath[] getUsedTagPaths(Gedcom gedcom,int type) {

    Hashtable hash = new Hashtable(32);

    // Loop through all entities of current type
    EntityList entities  = gedcom.getEntities(type);

    for (int e=0;e<entities.getSize();e++) {
      Entity entity = entities.get(e);
      fillHashtableWithPaths(hash,entity.getProperty(),"");
    }

    // Done
    TagPath[] result = new TagPath[hash.size()];
    Enumeration e = hash.elements();
    for (int i=0;e.hasMoreElements();i++) {
      result[i] = (TagPath)e.nextElement();
    }

    return result;
  }

  /**
   * Returns true when this path's internal postition is not at the end
   * @return <code>true</code> is internal position is not at the end
   */
  public boolean hasMore() {
    if (position>=length()) {
      return false;
    }
    return true;
  }

  /**
   * Returns the length of this path
   * @return length of this path
   */
  public int length() {
    return tags.length;
  }

  /**
   * Resets this path's internal postition
   */
  public void setToFirst() {
    position = 0;
  }

  /**
   * Returns the path as a string
   * @see #asString()
   */
  public String toString() {
    return asString();
  }
}
