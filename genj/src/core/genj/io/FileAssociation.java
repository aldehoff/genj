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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    suffix = s;
    action = a;
    executable = e;
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
  public boolean execute(String[] parms) {
    // run the executable
    boolean rc = false;
    try {
      String[] args = new String[1+parms.length];
      args[0] = getExecutable(); 
      System.arraycopy(parms, 0, args, 1, parms.length);
      rc = null!=Runtime.getRuntime().exec(args);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // done
    return rc;
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
    List result = (List)instances.get(suffix);
    if (result==null) result = new ArrayList(0);
    return result;
  }

  /**
   * Add an association   */
  public static void add(FileAssociation fa) {
    // do we know that suffix one alreay?
    List list = (List)instances.get(fa.getSuffix());
    if (list==null) {
      list = new ArrayList();
      instances.put(fa.getSuffix(), list);
    }
    // keep it
    list.add(fa);
    // done
  }
   
  static {
    add(new FileAssociation("jpg", "View", "C:/Program Files/Internet Explorer/IEXPLORE.EXE"));
    add(new FileAssociation("jpg", "Edit", "C:/winnt/System32/mspaint.exe"));
  }

} //FileAssociation
