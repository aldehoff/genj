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

/**
 * RELA property as sub-property for ASSOciation
 */
public class PropertyRelationship extends PropertyChoiceValue {

  /** an anchor is appended to the default gedcom RELA value e.g. Witness@INDI:BIRT */
  private TagPath anchor = null;

  /**
   * Compute Gedcom compliant value which includes our anchor information
   */
  public String getValue() {
    String value = super.getValue();
    TagPath anchor = getAnchor();
    if (anchor!=null&&anchor.length()>0)
      value += '@' + anchor.toString();
    return value;
  }
  
  public String getDisplayValue() {
    return super.getValue();
  }
  
  /**
   * Parse value
   */
  public void setValue(String value) {

    // parse anchor if one is still needed
    if (getTarget()==null) {
      int i = value.lastIndexOf('@');
      if (i>=0) {
        try {
          anchor = new TagPath(value.substring(i+1));
          value = value.substring(0,i);
        } catch (IllegalArgumentException e) {
        }
      }
    }
    // continue
    super.setValue(value);
  }
  
  /**
   * Compute target of associated Association
   */
  /*package*/ Property getTarget() {
    // look for it through ASSO
    Property parent = getParent();
    if (parent instanceof PropertyAssociation)
      return ((PropertyAssociation)parent).getTarget();
    return null;
  }
  
  /**
   * Compute anchor
   * @return might be null
   */
  /*package*/ TagPath getAnchor() {

    // fallback to target?
    Property target = getTarget();
    if (target==null||target instanceof PropertyForeignXRef)
      return anchor;

    // use target's path
    return target.getPath();
  }

} //PropertyRelationship
