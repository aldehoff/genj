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
package genj.edit.actions;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.view.ViewManager;

/**
 * knows how to create a sibling for an individual
 */
public class CreateSibling extends CreateRelationship {
  
  private Indi sibling;
  
  /** constructor */
  public CreateSibling(Indi sibling, ViewManager mgr) {
    super(resources.getString("create.sibling"), sibling.getGedcom(), Gedcom.INDI, mgr);
    this.sibling = sibling;
  }
  
  /** more about what we do */
  public String getDescription() {
    // "Sibling of Meier, Nils (I1)"
    return resources.getString("create.sibling.of", sibling);
  }

  /** do the change */
  protected Property change(Entity target, boolean targetIsNew) throws GedcomException {
    
    // try to add target to sibling's family or vice versa
    Fam[] fams = sibling.getFamiliesWhereChild();
    if (fams.length>0) {
      fams[0].addChild((Indi)target);
    } else {
      
      // try to add sibling to target's family
      fams = ((Indi)target).getFamiliesWhereChild();
      if (fams.length>0) {
        fams[0].addChild(sibling);
      } else {

        // both indis are not children yet - create a new family
        Gedcom ged = sibling.getGedcom();
        Fam fam = (Fam)ged.createEntity(Gedcom.FAM);
        try {
          fam.addChild((Indi)target);
        } catch (GedcomException e) {
          ged.deleteEntity(fam);
          throw e;
        }
        
        // 20040619 adding missing spouse automatically now
        Indi husband = (Indi)ged.createEntity(Gedcom.INDI).addDefaultProperties();
        Indi wife = (Indi)ged.createEntity(Gedcom.INDI).addDefaultProperties();
        
        husband.setName("", sibling.getLastName());
        wife.setName("", sibling.getLastName());
        
        fam.setHusband(husband);
        fam.setWife(wife);
        fam.addChild(sibling);
      }

    }
    
    // set it's name if new
    if (targetIsNew) 
      ((Indi)target).setName("", sibling.getLastName());        
    
    // focus stays with sibling
    return sibling;
  }

}
