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
import genj.util.Registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * A file association
 */
public class FileAssociation {
  
  /** instances */
  private static Map instances = new TreeMap();
  
  /** suffix */
  private String suffix;
  
  /** action e.g. OPEN */
  private String action;
  
  /** external app */
  private String executable;
  
  /**
   * Constructor
   */
  public FileAssociation(String s, String a, String e) {
    suffix = s.toLowerCase();
    action = a;
    executable = e;
  }
  
  /**
   * Constructor
   */
  public FileAssociation(String s) {
    StringTokenizer tokens = new StringTokenizer(s,"*");
    suffix = tokens.hasMoreTokens() ? tokens.nextToken() : "";
    action = tokens.hasMoreTokens() ? tokens.nextToken() : "";
    executable = tokens.hasMoreTokens() ? tokens.nextToken() : "";
  }
  
  /**
   * setter
   */
  public void setSuffix(String s) {
    del(this);
    suffix = s;
    add(this);
  }
  
  /**
   * setter
   */
  public void setAction(String a) {
    action = a;
  }
  
  /**
   * setter
   */
  public void setExecutable(String e) {
    executable = e;
  }
  
  /**
   * String representation   */
  public String toString() {
    return suffix+'*'+action+'*'+executable;
  }
  
  /**
   * Accessor - exec
   */
  public String getExecutable() {
    return executable;
  }
  
  /**
   * Accessor - action
   */
  public String getAction() {
    return action;
  }
  
  /**
   * Accessor - suffix
   */
  public String getSuffix() {
    return suffix;
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
        cmd = cmd.substring(0, i) + ' ' + parm + ' ' + cmd.substring(i+1);
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
    List result = new ArrayList();
    Iterator lists = instances.values().iterator();
    while (lists.hasNext()) {
      List list = (List)lists.next();
      Iterator fas = list.iterator();
      while (fas.hasNext()) {
        result.add(fas.next());
      }
    }
    return result;
  }

  /**
   * Gets associations   */
  public static List get(String suffix) {
    List result = (List)instances.get(suffix.toLowerCase());
    if (result==null) result = new ArrayList(0);
    return result;
  }
  
  /**
   * Deletes an association
   */
  public static void del(FileAssociation fa) {
    // do we know that suffix one alreay?
    List list = (List)instances.get(fa.getSuffix());
    if (list==null) return;
    // remove it
    list.remove(fa);
    // done
  }

  /**
   * Add an association   */
  public static void add(FileAssociation fa) {
    // do we know that suffix one alreay?
    List list = (List)instances.get(fa.getSuffix());
    if (list==null) {
      list = new ArrayList();
      if (fa.getSuffix().length()>0)
        instances.put(fa.getSuffix(), list);
    }
    // keep it
    list.add(fa);
    // done
  }
  
  /**
   * Reads associations from registry   */
  public static void read(Registry r) {
    // read it and don't change what isn't there
    String[] as = r.get("associations", (String[])null);
    if (as==null) return;
    // replace
    instances.clear();
    for (int i=0; i<as.length; i++) {
      add(new FileAssociation(as[i]));
    }
    // done
  }
   
  /**
   * Writes associations to registry
   */
  public static void write(Registry r) {
    List all = getAll();
    String[] as = new String[all.size()];
    for (int i=0; i<as.length; i++) {
      as[i] = all.get(i).toString();
    }
    r.put("associations", as);
  }
  
  /**
   * Defaults   */
  static {
    add(new FileAssociation("jpg", "View", "C:/Program Files/Internet Explorer/IEXPLORE.EXE"));
    add(new FileAssociation("jpg", "Edit", "C:/winnt/System32/mspaint.exe"));
    add(new FileAssociation("txt", "Edit", "C:/winnt/notepad.exe"));
  }
} //FileAssociation
