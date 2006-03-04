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
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.util.EnvironmentChecker;
import genj.util.Origin;
import genj.util.Registry;
import genj.util.Resources;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manager for our blueprints */
public class BlueprintManager {
  
  private final static String SUFFIX = ".html";
  
  /*package*/ final static Logger LOG = Logger.getLogger("genj.renderer");

  /** blueprints per entity */
  private Map tag2blueprints = new HashMap();

  /** singleton */
  private static BlueprintManager instance;
  
  /** resources */
  private Resources resources = Resources.get(BlueprintManager.class);
  
  /**
   * Singleton access
   */
  public static BlueprintManager getInstance() {
    if (instance==null)
      instance = new BlueprintManager();
    return instance;
  }
  
  /**
   * Constructor   */
  private BlueprintManager() {
    
    // load readonly/predefined blueprints (from resources)
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      
      String tag = Gedcom.ENTITIES[t];
      
      StringTokenizer names = new StringTokenizer(resources.getString("blueprints."+tag,""));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        String html =  resources.getString("blueprints."+tag+"."+name);
        addBlueprint(new Blueprint(tag, name, html.toString(), true));
      }
      
    }
    
    // try to load old style blueprints from registry
    Registry registry = Registry.lookup("genj", null);
    // load user-defined blueprints (from registry)
    try {
      for (int t=0;t<Gedcom.ENTITIES.length;t++) {
        String tag = Gedcom.ENTITIES[t];
        StringTokenizer names = new StringTokenizer(registry.get("options.blueprints."+tag,""));
        while (names.hasMoreTokens()) {
          String name = names.nextToken();
          String html = registry.get("options.blueprints."+tag+"."+name, (String)null);
          if (html!=null&&html.length()>0)
            saveBlueprint(addBlueprint(new Blueprint(tag, name, html, false)));
        }
      }
      
      // remove old-style settings
      registry.remove("options.blueprints");
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "couldn't convert old-style blueprints",t);
    }
    
    // load user defined blueprints from disk
    loadBlueprints();
    
    // done
  }
  
  /**
   * Resolve blueprint directory
   */
  private File getBlueprintDirectory() {
    return new File(EnvironmentChecker.getProperty(this, "user.home.genj/blueprints", "?", "Looking for blueprints"));
  }
  
  /**
   * Resolve blueprint filename
   */
  private File getBlueprintFile(Blueprint blueprint) {
    return new File(getBlueprintDirectory(), "/"+blueprint.getTag()+"/"+blueprint.getName()+SUFFIX); 
  }
  
  /**
   * Save a blueprint to disk
   */
  /*package*/ boolean saveBlueprint(Blueprint blueprint) {
    
    // put it back to where it belongs
    File file = getBlueprintFile(blueprint); 
    File parent = file.getParentFile();
    parent.mkdirs();
    if (!parent.exists()||!parent.isDirectory())
      return false;
    
    try {
      readwrite(new StringReader(blueprint.getHTML()), new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
      LOG.log(Level.INFO, "saved blueprint "+file);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "unexpected throwable saving blueprint "+file, e);
      return false;
    }
    
    // done
    return true;
  }
  
  /**
   * Load all available blueprints from disk
   */
  private void loadBlueprints() {
    
    File dir = getBlueprintDirectory();
    try {
      
      // exists?
      if (!dir.isDirectory()||!dir.exists())
        return;
  
      // do it for each entity we know about
      for (int i=0; i<Gedcom.ENTITIES.length; i++) 
        loadBlueprints(dir, Gedcom.ENTITIES[i]);
    
    } catch (Throwable t) {
      LOG.log(Level.WARNING, "unexpected throwable loading blueprints from "+dir, t);
    }
  }
  
  /**
   * Load blueprints for one entity from disk
   */
  private void loadBlueprints(File dir, String tag) throws IOException {
    
    // exists?
    dir = new File(dir, tag);
    if (!dir.isDirectory()||!dir.exists())
      return;
    
    // loop over blueprints
    File[] blueprints = dir.listFiles();
    for (int b=0;b<blueprints.length;b++) {
      
      // check name of blueprint
      File blueprint = blueprints[b];
      String name = blueprint.getName();
      if (!name.endsWith(SUFFIX))
        continue;
      name = name.substring(0, name.length()-SUFFIX.length());
      
      addBlueprint(loadBlueprint(new FileInputStream(blueprint), tag, name, false));
      
    }
    
    // done
  }
  
  /**
   * Load one blueprint from inputstream
   *
   */
  private Blueprint loadBlueprint(InputStream in, String tag, String name, boolean readOnly) throws IOException {
    
    StringWriter html = new StringWriter(512);
    readwrite(new InputStreamReader(in, "UTF8"), html);
    in.close();
    
    return new Blueprint(tag, name, html.toString(), readOnly);
  }
  
  /**
   * transfer lines
   */
  private void readwrite(Reader in, Writer out) throws IOException {
    // transfer in to out
    BufferedReader bin = new BufferedReader(in);
    BufferedWriter bout = new BufferedWriter(out);
    while (true) {
      String line = bin.readLine();
      if (line==null) break;
      bout.write(line);
      bout.newLine();
    }
    bin.close();
    bout.close();
    // done
  }
  
  /**
   * Blueprint for given type with given name
   * @param origin an optional context that blueprints are loaded from if necessary
   * @param tag the entity tag the blueprint is supposed to be for
   * @param the name of the blueprint   */
  public Blueprint getBlueprint(Origin origin, String tag, String name) {
    // patch name if default
    if (name.length()==0)
      name = "Default";
    // look through global blueprints for that type
    List bps = getBlueprints(tag);
    for (int i=0; i<bps.size(); i++) {
      Blueprint bp = (Blueprint)bps.get(i);
      // .. found! return
      if (bp.getName().equals(name)) 
        return bp;   	
    }
    // not found - try origin
    try {
      return loadBlueprint(origin.open("blueprints/"+tag+"/name"+SUFFIX), tag, name, true);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Failed to load blueprint "+tag+"/"+name+" from "+origin, e);
    }
    // fallback try first
    return (Blueprint)bps.get(0);
  }
  
  /**
   * Blueprints for a given type   */
  public List getBlueprints(String tag) {
    return Collections.unmodifiableList(getBlueprintsInternal(tag));
  }
  
  private List getBlueprintsInternal(String tag) {
    List result = (List)tag2blueprints.get(tag);
    if (result==null) {
      result = new ArrayList();
      tag2blueprints.put(tag, result);
    }
    return result;
  }
  
  /**
   * Adds a blueprint   */
  public Blueprint addBlueprint(Blueprint blueprint) {
    
    // patch name - no slashes and colons etc.
    String name = blueprint.getName();
    char[] replace = { '\\', '/', ':', '*', '?', '\"', '<', '>', '|' };
    for (int i=0;i<replace.length;i++)
      name = name.replace(replace[i], '-');
    blueprint.setName(name);
          
    // keep it overriding same name unless read-only
    List blueprints = getBlueprintsInternal(blueprint.getTag());
    for (ListIterator it=blueprints.listIterator(); it.hasNext(); ) {
      Blueprint other = (Blueprint)it.next();
      // found one with same name?
      if (other.getName().equalsIgnoreCase(blueprint.getName())) {
        // don't allow if read only
        if (other.isReadOnly())
          return other;
        // exchange
        it.set(blueprint);
        return blueprint;
      }
    }
    
    // keep it
    blueprints.add(blueprint);
    
    // done 
    return blueprint;
  }
  
  /**
   * Deletes a blueprint   */
  public void delBlueprint(Blueprint blueprint) {
    // allowed?
    if (blueprint.isReadOnly()) 
      throw new IllegalArgumentException("Can't delete read-only Blueprint");
    // remove it from disk
    if (!blueprint.isReadOnly()) {
      if (!getBlueprintFile(blueprint).delete()) {
        LOG.warning("Couldn't delete blueprint "+blueprint);
        return;
      }
    }
    // remove it
    getBlueprintsInternal(blueprint.getTag()).remove(blueprint);
    // done
  }
  
} //BlueprintManager
