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

import java.util.ArrayList;
import java.util.List;

/**
 * A manager for our blueprints */
public class BlueprintManager {

  /** blueprints per entity */
  private List[] blueprints = new List[Gedcom.NUM_TYPES];

  /** singleton */
  private static BlueprintManager instance;
  
  /**
   * Singleton access   */
  public static BlueprintManager getInstance() {
    if (instance==null) instance = new BlueprintManager();
    return instance;
  }
  
  /**
   * Constructor   */
  private BlueprintManager() {
    // FIXME : where do we get/store those from/to
    for (int i=0; i<blueprints.length; i++) {
      List bs = new ArrayList();
      bs.add(new Blueprint("Default", "<b>def</b>ault"));
      bs.add(new Blueprint("Complete", "<b>compl</b>ete"));
      bs.add(new Blueprint("Minimum" , "<b>min!</b>"));
      blueprints[i] = bs;
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
    // not found! create a dummy
    return new Blueprint(name, Gedcom.getNameFor(type, false));
  }
  
  /**
   * Blueprints for a given type   */
  public List getBlueprints(int type) {
    return blueprints[type];
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
  
} //BlueprintManager
