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
 * Class for encapsulating a repository
 */
public class Repository extends Entity {

  /**
   * Constructor for Repository
   */
  /*package*/ Repository() {
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.REPOSITORIES;
  }

  /**
   * @see genj.gedcom.Entity#toString()
   */
  public String toString() {
    return super.toString()+getName();
  }

  /**
   * Returns the name of this repository
   */
  private String getName() {
    Property name = getProperty("NAME");
    return name!=null ? name.getValue() : ""; 
  }
  
  /**
   * @see genj.gedcom.Property#getTag()
   */
  public String getTag() {
    return "REPO";
  }
  
} //Repository
