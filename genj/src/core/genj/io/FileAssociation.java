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

import genj.util.Resources;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * A file association
 */
public class FileAssociation {
  
  private static Logger LOG = Logger.getLogger("genj.io");
  
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
   * Constructor
   */
  public FileAssociation(String suffixes, String name, String executable) throws IllegalArgumentException {
    setSuffixes(suffixes);
    this.name = name;
    this.executable = executable;
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
  public void execute(URL url) {
    // check simple % placeholders in cmd
    String cmd = getExecutable();
    if (cmd.indexOf('%')<0) 
      cmd += " " + url;
    else
      cmd.replaceAll("%", url.toString());
    // go
    new Thread(new Sequence(cmd)).start();
  }
  
  /**
   * Execute
   */
  public void execute(File file) {
    // no placeholders at all in cmd?
    String cmd = getExecutable();
    if (cmd.indexOf('%')<0) {
      cmd += " \"" + file.getAbsolutePath() + "\"";
    } else {
      // example - the forward slash is meant to be a backward slash here
      // file = c:/documents and settings/user/foo.ps
      // path = c://documents and settings//user//foo.ps
      // suffix = ps
      // nosuffix = c://documents and settings//user//foo
      String path = file.getAbsolutePath().replaceAll("\\\\","\\\\\\\\");
      String suffix = getSuffix(file);
      String nosuffix = path.substring(0, path.length()-suffix.length()-1);
      
      // replace file placeholders %.suffix first
      cmd = Pattern.compile("%(\\.[a-zA-Z]*)").matcher(cmd).replaceAll(nosuffix+"$1");
      // replace file placholders % next
      cmd = Pattern.compile("%").matcher(cmd).replaceAll(path);
    }
    
    // go
    new Thread(new Sequence(cmd)).start();
  }
  
  private class Sequence implements Runnable {
    private StringTokenizer cmds;
    Sequence(String cmd) {
      cmds =  new StringTokenizer(cmd, "&");
    }
    public void run() {
      try {
        while (cmds.hasMoreTokens()) {
          String cmd = cmds.nextToken();
          int rc = Runtime.getRuntime().exec(cmd).waitFor(); 
          if (rc!=0) 
            LOG.log(Level.INFO, "External application "+cmd+" returned "+rc);
        }
      } catch (Throwable t) {
        LOG.log(Level.WARNING, "Couldn't start all external applications in "+cmds, t);
      }
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
   * Get the file suffix for given file
   */
  public static String getSuffix(File file) {
    
    // grab extension
    Matcher m = Pattern.compile(".*\\.(.*)$").matcher(file.getName());
    
    // done
    return m.matches() ? m.group(1) : "";
  }
  
  /**
   * Gets first available association or asks the user for appropriate one
   */
  public static FileAssociation get(File file, String name, JComponent owner) {
    String suffix = getSuffix(file);
    if (suffix.length()==0)
      return null;
    return get(suffix, suffix, name, owner);
  }
  
  /**
   * Gets first available association or asks the user for appropriate one
   */
  public static FileAssociation get(String suffix, String suffixes, String name, JComponent owner) {
    // look for it
    Iterator it = associations.iterator();
    while (it.hasNext()) {
      FileAssociation fa = (FileAssociation)it.next();
      if (fa.suffixes.contains(suffix))
        return fa;
    }
    // not found - ask for it
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(Resources.get(FileAssociation.class).getString("assocation.choose", suffixes));
    int rc = chooser.showOpenDialog(owner);
    File file = chooser.getSelectedFile(); 
    if (rc!=JFileChooser.APPROVE_OPTION||file==null)
      return null;
    // keep it
    FileAssociation association = new FileAssociation(suffixes, name, file.getAbsolutePath());
    add(association);
    // done
    return association;
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
