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
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A manager for our blueprints */
public class BlueprintManager {

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
      
      List blueprints = new ArrayList(10);
      
      String tag = Gedcom.ENTITIES[t];
      
      StringTokenizer names = new StringTokenizer(resources.getString("blueprints."+tag,""));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        String html =  resources.getString("blueprints."+tag+"."+name);
        blueprints.add(new Blueprint(tag, name, html.toString(), true));
      }
      
      tag2blueprints.put(tag, blueprints);
    }
    
    // done
  }
  
  /**
   * read from registry
   */
  /*package*/ void read(Registry registry) {
    
    // load user-defined blueprints (from registry)
    for (int t=0;t<Gedcom.ENTITIES.length;t++) {
      
      String tag = Gedcom.ENTITIES[t];
      StringTokenizer names = new StringTokenizer(registry.get("blueprints."+tag,""));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        addBlueprint(tag, name, registry.get("blueprints."+tag+"."+name, ""));
      }
    }
    
  }
  
  /**
   * store to registry   */
  /*package*/ void write(Registry registry) {
    // Store non read-only blueprints
    for (Iterator it=tag2blueprints.keySet().iterator(); it.hasNext(); ) {
      String tag = (String)it.next();
      List blueprints = (List)tag2blueprints.get(tag);
      // tag for type
      WordBuffer names = new WordBuffer(); 
      for (int b=0; b<blueprints.size(); b++) {
        // .. blueprint
      	Blueprint blueprint = (Blueprint)blueprints.get(b);
        if (blueprint.isReadOnly()) continue;
        // .. name and store
        String name = blueprint.getName();
        String html = blueprint.getHTML();
        names.append(name);
        registry.put("blueprints."+tag+"."+name, html);
        // .. next
      }
      // store names
      registry.put("blueprints."+tag, names.toString());
      // next type     
    }
    // done   
  }
  
  /**
   * Blueprint for given type with given name   */
  public Blueprint getBlueprint(String tag, String name) {
    // look through blueprints for that type
    List bps = getBlueprints(tag);
    for (int i=0; i<bps.size(); i++) {
      Blueprint bp = (Blueprint)bps.get(i);
      // .. found! return
      if (bp.getName().equals(name)) return bp;   	
    }
    // not found! try first
    return (Blueprint)bps.get(0);
  }
  
  /**
   * Blueprints for a given type   */
  public List getBlueprints(String tag) {
    return Collections.unmodifiableList(getBlueprintsInternal(tag));
  }
  
  private List getBlueprintsInternal(String tag) {
    List result = (List)tag2blueprints.get(tag);
    return result!=null ? result : new ArrayList();
  }
  
  /**
   * Adds a blueprint   */
  public Blueprint addBlueprint(String tag, String name, String html) {
    
    // fix name for duplicates
    List blueprints = getBlueprintsInternal(tag);
    int num = 0;
    for (int i=0; i < blueprints.size(); i++) {
      Blueprint other = (Blueprint)blueprints.get(i);
      if (other.getName().startsWith(name)) num++;
    }
    if (num>0) name = name+"-"+num;
    
    // create it
    Blueprint result = new Blueprint(tag, name, html, false);
    
    // keep it
    blueprints.add(result);
    
    // done 
    return result;
  }
  
  /**
   * Deletes a blueprint   */
  public void delBlueprint(Blueprint blueprint) {
    // allowed?
    if (blueprint.isReadOnly()) 
      throw new IllegalArgumentException("Can't delete read-only Blueprint");
    // remove it
    getBlueprintsInternal(blueprint.getTag()).remove(blueprint);
    // done
  }
  
//  /**
//   * Helper that reads blueprints from a registry//   */
//  public Blueprint[] recallBlueprints(Registry registry) {
//    // resolve blueprints
//    Blueprint[] bps = new Blueprint[Gedcom.NUM_TYPES];
//    String[] names = registry.get("blueprints", (String[])null);
//    for (int i=0; i<bps.length; i++) {
//      String name = names!=null&&i<names.length ? names[i] : "";
//      bps[i] = getBlueprint(i, name);
//    }
//    // done
//    return bps;
//  }
//
//  /**
//   * Helper that writes blueprings to a registry
//   */
//  public void rememberBlueprints(Blueprint[] bps, Registry registry) {
//    String[] names = new String[bps.length];
//    for (int i=0; i<names.length; i++) {
//      names[i] = bps[i].getName();     
//    }
//    registry.put("blueprints", names);
//  }

} //BlueprintManager
