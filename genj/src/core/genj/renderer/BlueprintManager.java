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
import java.util.List;
import java.util.StringTokenizer;

/**
 * A manager for our blueprints */
public class BlueprintManager {

  /** blueprints per entity */
  private List[] blueprints = new List[Gedcom.NUM_TYPES];

  /** singleton */
  private static BlueprintManager instance;
  
  /** resources */
  private Resources resources = Resources.get(BlueprintManager.class);
  
  /** registry */
  private Registry registry;
  
  /**
   * Constructor   */
  public BlueprintManager(Registry reGistry) {
    
    registry = reGistry;
    
    // load readonly/predefined blueprints
    StringBuffer html = new StringBuffer(256);
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      blueprints[t] = new ArrayList(10);
      String tag = Gedcom.getTagFor(t);
      StringTokenizer names = new StringTokenizer(resources.getString("blueprints."+tag));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        StringTokenizer lines = new StringTokenizer(resources.getString("blueprints."+tag+"."+name), "|");
        html.setLength(0);
        while (lines.hasMoreTokens()) {
          html.append(lines.nextToken());
          html.append('\n');
        }
        blueprints[t].add(new Blueprint(name, html.toString(), true));
      }
    }
    
    // load user-defined blueprints
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      String tag = Gedcom.getTagFor(t);
      StringTokenizer names = new StringTokenizer(registry.get("blueprints."+tag,""));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        addBlueprint(t, name, registry.get("blueprints."+tag+"."+name, ""));
      }
    }
    
    // done
  }
  
  /**
   * Takes a snapshot of current configuration   */
  public void snapshot() {
    // Store non read-only blueprints
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      // tag for type
      String tag = Gedcom.getTagFor(t);
      WordBuffer names = new WordBuffer(); 
      for (int b=0; b<blueprints[t].size(); b++) {
        // .. blueprint
      	Blueprint blueprint = (Blueprint)blueprints[t].get(b);
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
  public Blueprint getBlueprint(int type, String name) {
    // look through blueprints for that type
    List bps = getBlueprints(type);
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
  public List getBlueprints(int type) {
    return new ArrayList(blueprints[type]);
  }
  
  /**
   * Adds a blueprint   */
  public Blueprint addBlueprint(int type, String name, String html) {
    
    // fix name for duplicates
    List others = getBlueprints(type);
    int num = 0;
    for (int i=0; i < others.size(); i++) {
      Blueprint other = (Blueprint)others.get(i);
      if (other.getName().startsWith(name)) num++;
    }
    if (num>0) name = name+"-"+num;
    
    // create it
    Blueprint result = new Blueprint(name, html, false);
    
    // keep it
    blueprints[type].add(result);
    
    // done 
    return result;
  }
  
  /**
   * Deletes a blueprint   */
  public void delBlueprint(Blueprint blueprint) {
    // allowed?
    if (blueprint.isReadOnly()) throw new IllegalArgumentException("Can't delete read-only Blueprint");
    // what's its type?
    int type = getType(blueprint);
    blueprints[type].remove(blueprint);
    // done
  }
  
  /**
   * Type of given blueprint   */
  public int getType(Blueprint blueprint) {
    // look for it
    for (int i = 0; i < blueprints.length; i++) {
      if (blueprints[i].contains(blueprint)) return i;
    }
    // not found
    throw new IllegalArgumentException("Blueprint is not registered"); 
  }

  /**
   * Helper that reads blueprints from a registry   */
  public Blueprint[] recallBlueprints(Registry registry) {
    // resolve blueprints
    Blueprint[] bps = new Blueprint[Gedcom.NUM_TYPES];
    String[] names = registry.get("blueprints", (String[])null);
    for (int i=0; i<bps.length; i++) {
      String name = names!=null&&i<names.length ? names[i] : "";
      bps[i] = getBlueprint(i, name);
    }
    // done
    return bps;
  }

  /**
   * Helper that writes blueprings to a registry
   */
  public void rememberBlueprints(Blueprint[] bps, Registry registry) {
    String[] names = new String[bps.length];
    for (int i=0; i<names.length; i++) {
      names[i] = bps[i].getName();     
    }
    registry.put("blueprints", names);
  }

} //BlueprintManager
