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
package genj.io;

import genj.util.Debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A file association
 */
public class FileAssociation {
  
  /** instances */
  private static List associations = new LinkedList();
  
  /** suffix */
  private Set suffixes = new HashSet();
  
  /** action name e.g. OPEN */
  private String name = "";
  
  /** external app */
  private String executable = "";
  
  /**
   * Constructor
   */
  public FileAssociation() {
  }
  
  /**
   * Constructor
   */
  public FileAssociation(String s) throws IllegalArgumentException {
    // break down into '*'
    StringTokenizer tokens = new StringTokenizer(s,"*");
    if (tokens.countTokens()!=3)
      throw new IllegalArgumentException("need three *-separators");
    // Set comma separated suffixes
    setSuffixes(tokens.nextToken());
    // get name and executable
    name = tokens.nextToken();
    executable = tokens.nextToken();
    // done
  }
  
  /**
   * String representation - usable for constructor
   */
  public String toString() {
    return getSuffixes()+"*"+name+"*"+executable;
  }
  
  /**
   * setter
   */
  public void setName(String set) {
    name = set;
  }
  
  /**
   * Accessor - name
   */
  public String getName() {
    return name;
  }
  
  /**
   * setter
   */
  public void setExecutable(String set) {
    executable = set;
  }
  
  /**
   * Accessor - exec
   */
  public String getExecutable() {
    return executable;
  }
  
  /**
   * Accessor - suffixes as comma separated list
   */
  public void setSuffixes(String set) {
    
    StringTokenizer ss = new StringTokenizer(set,",");
    if (ss.countTokens()==0)
      throw new IllegalArgumentException("need at least one suffix");
    suffixes.clear();
    while (ss.hasMoreTokens())
      suffixes.add(ss.nextToken().trim());
  }
  
  /**
   * Accessor - suffixes as comma separated list
   */
  public String getSuffixes() {
    StringBuffer result = new StringBuffer();
    Iterator it = suffixes.iterator();
    while (it.hasNext()) {
      result.append(it.next());
      if (it.hasNext()) result.append(',');
    }
    return result.toString();
  }
  
  /**
   * Execute
   */
  public boolean execute(String parm) {
    // run the executable
    try {
      String cmd = getExecutable(); 
      // does executable contain '%'?
      int i = cmd.indexOf('%');
      if (i<0) {
        cmd += ' ' + parm;
      } else {
        // 20050522 removed extra spaces around parm since user might have enclosed % in quotes already
        cmd = cmd.substring(0, i) + parm + cmd.substring(i+1);
      }
      // exec' it 
      if (null!=Runtime.getRuntime().exec(cmd)) return true;
      Debug.log(Debug.WARNING, this, "Couldn't start external application "+getExecutable());
      return false;
    } catch (IOException e) {
      Debug.log(Debug.WARNING, this, "Couldn't start external application "+getExecutable(), e);
      return false;
    }
  }

  /**
   * Gets all
   */
  public static List getAll() {
    return new ArrayList(associations);
  }

  /**
   * Gets associations   */
  public static List getAll(String suffix) {
    List result = new ArrayList();
    Iterator it = associations.iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next();
      if (fa.suffixes.contains(suffix))
        result.add(fa);
    }
    return result;
  }
  
  /**
   * Deletes an association
   */
  public static boolean del(FileAssociation fa) {
    return associations.remove(fa);
  }

  /**
   * Add an association   */
  public static FileAssociation add(FileAssociation fa) {
    if (!associations.contains(fa))
      associations.add(fa);
    return fa;
  }
  
} //FileAssociation
