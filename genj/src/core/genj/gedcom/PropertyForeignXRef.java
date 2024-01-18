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
package genj.gedcom;

import genj.util.swing.ImageIcon;

/**
 * Gedcom Property : - (internal)
 * This XRef is for pointing back to XRefs in case
 * Gedcom does only support uni-direction
 */
public class PropertyForeignXRef extends PropertyXRef {

  /**
   * Empty Constructor
   */
  public PropertyForeignXRef() {
  }
  
  /**
   * Constructor with reference
   */
  public PropertyForeignXRef(PropertyXRef target) {
    super(target);
  }

  /**
   * getTag method comment.
   */
  public String getTag() {
    return EMPTY_STRING;
  }

  /**
   * getValue method comment.
   */
  public String getValue() {
    // the property/entity we're originating from
    Property p = getTarget();
    Entity e = p.getEntity();
    // "linked through INDI:BIRT:ASSO in I001"
    String result = "linked by "+new TagPath(e.getPathTo(p))+" in "+e.getId();
    // relationship information?    
    Property rela = p.getProperty("RELA");
    if (rela!=null&&rela.getValue().length()>0) { 
      result += " ("+rela.getValue()+")";
    } 
    
    // done
    return result;
  }

  /**
   * link method comment.
   */
  public void link() {
    throw new RuntimeException("link is not support by ForeignXRefs");
  }

  /**
   * setValue method comment.
   */
  public void setValue(String newValue) {
    throw new RuntimeException("setValue is not support by ForeignXRefs");
  }

  /**
   * @see genj.gedcom.PropertyXRef#getImage(boolean)
   */
  public ImageIcon getImage(boolean checkValid) {
    return overlay(getTarget().getEntity().getImage(false));
  }

  /**
   * The expected referenced type
   */
  public String[] getTargetTypes() {
    throw new RuntimeException("getExpectedReferencedType is not support by ForeignXRefs");
  }

  /**
   * @see genj.gedcom.PropertyForeignXRef#isValid()
   */
  public boolean isValid() {
    return false;
  }

  /**
   * @see genj.gedcom.PropertyForeignXRef#isTransient()
   */
  public boolean isTransient() {
    return true; //YES!
  }

} //PropertyForeignXRef
