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
package genj.edit.beans;

import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.Transaction;
import genj.util.Registry;
import genj.view.ViewManager;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A complex bean displaying families of an individual
 */
public class FamiliesBean extends PropertyBean {

  /**
   * Finish editing a property through proxy
   */
  public void commit(Transaction tx) {
  }

  /**
   * Initialize
   */
  public void init(Gedcom setGedcom, Property setProp, TagPath setPath, ViewManager setMgr, Registry setReg) {

    Indi indi = (Indi)setProp;
    
    super.init(setGedcom, indi, setPath, setMgr, setReg);

    setLayout(new FlowLayout());
    
    int num = indi.getNoOfFams();
    if (num==0)
      return;
    
    add(new JLabel(Gedcom.getName("FAM")));
    
    for (int i=0;i<num;i++) {
      Fam fam = indi.getFam(i);
      Indi spouse = fam.getOtherSpouse(indi);
      JLabel label;
      if (spouse!=null)
        label = new JLabel(spouse.toString(), spouse.getImage(false), SwingConstants.LEFT);
      else
        label = new JLabel("Unknown", Indi.IMG_UNKNOWN, SwingConstants.LEFT);
      add(label);
    }
    
    // done
  }

} //FamiliesBean
