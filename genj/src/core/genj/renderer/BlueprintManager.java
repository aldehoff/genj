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
  private Resources resources = new Resources(BlueprintManager.class);
  
  /**
   * Singleton access   */
  public static BlueprintManager getInstance() {
    if (instance==null) instance = new BlueprintManager();
    return instance;
  }
  
  /**
   * Constructor   */
  private BlueprintManager() {
    
    // FIXME save/load
    
    // load blueprints
    for (int t=0;t<Gedcom.NUM_TYPES;t++) {
      blueprints[t] = new ArrayList(10);
      String tag = Gedcom.getTagFor(t);
      StringTokenizer names = new StringTokenizer(resources.getString("blueprints."+tag));
      while (names.hasMoreTokens()) {
        String name = names.nextToken();
        String html = resources.getString("blueprints."+tag+"."+name);
        addBlueprint(t, new Blueprint(name,html));
      }
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
    if (!bps.isEmpty()) return (Blueprint)bps.get(0);
    // create a dummy
    return new Blueprint(name, Gedcom.getNameFor(type, false));
  }
  
  /**
   * Blueprints for a given type   */
  public List getBlueprints(int type) {
    return new ArrayList(blueprints[type]);
  }
  
  /**
   * Adds a blueprint   */
  public void addBlueprint(int type, Blueprint blueprint) {
    // fix name for duplicates
    String name = blueprint.getName();
    List others = getBlueprints(type);
    int num = 0;
    for (int i=0; i < others.size(); i++) {
      Blueprint other = (Blueprint)others.get(i);
      if (other.getName().startsWith(name)) num++;
    }
    if (num>0) blueprint.setName(name+"-"+num);
    // keep it
    blueprints[type].add(blueprint);
    
    // done 
  }
  
  /**
   * Deletes a blueprint   */
  public void delBlueprint(Blueprint blueprint) {
    // look for it
    for (int i = 0; i < blueprints.length; i++) {
      if (blueprints[i].remove(blueprint)) return;
    }
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
   * Helper that reads blueprings from a registry   */
  public static Blueprint[] readBlueprints(Registry registry) {
    // resolve blueprints
    Blueprint[] bps = new Blueprint[Gedcom.NUM_TYPES];
    String[] names = registry.get("blueprints", (String[])null);
    for (int i=0; i<bps.length; i++) {
      String name = names!=null&&i<names.length ? names[i] : "";
      bps[i] = getInstance().getBlueprint(i, name);
    }
    // done
    return bps;
  }

  /**
   * Helper that writes blueprings to a registry
   */
  public static void writeBlueprints(Blueprint[] bps, Registry registry) {
    String[] names = new String[bps.length];
    for (int i=0; i<names.length; i++) {
      names[i] = bps[i].getName();     
    }
    registry.put("blueprints", names);
  }

} //BlueprintManager
