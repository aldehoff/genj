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
 * Gedcom Property : - (internal)
 * This XRef is for pointing back to XRefs in case
 * Gedcom does only support uni-direction
 */
public class PropertyForeignXRef extends PropertyXRef {

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
    throw new RuntimeException("getTag is not support by ForeignXRefs");
  }

  /**
   * getValue method comment.
   */
  public String getValue() {
    throw new RuntimeException("getValue is not support by ForeignXRefs");
  }

  /**
   * Marks this Property as being invisible
   */
  public boolean isVisible() {
    return false;
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
  public boolean setValue(String newValue) {
    throw new RuntimeException("setValue is not support by ForeignXRefs");
  }

  /**
   * getImage method comment.
   */
  public genj.util.ImgIcon getImage(boolean checkValid) {
    throw new RuntimeException("getImage is not support by ForeignXRefs");
  }

  /**
   * The expected referenced type
   */
  public int getExpectedReferencedType() {
    return -1;
  }

}
