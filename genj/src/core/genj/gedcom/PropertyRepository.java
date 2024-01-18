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
 * Gedcom Property : REPO 
 * Class for encapsulating a repository as property
 */
public class PropertyRepository extends PropertyXRef {

  /** applicable target types */
  public final static int[] 
    TARGET_TYPES = new int[]{ Gedcom.REPOSITORIES };

  /** the repository's content */
  private String repository;

  /**
   * Empty Constructor
   */
  public PropertyRepository() {
  }
  
  /**
   * Constructor with reference
   * @param entity reference of entity this property links to
   */
  public PropertyRepository(PropertyXRef target) {
    super(target);
  }

  /**
   * Returns the tag of this property
   */
  public String getTag() {	
    return "REPO";
  }

  /**
   * Links reference to entity (if not already done)
   * @exception GedcomException when processing link would result in inconsistent state
   */
  public void link() throws GedcomException {

    // No Property Repository?
    if (repository!=null) {
      return;
    }

    // Get enclosing entity ?
    Entity entity = getEntity();

    // Something to do ?
    if (getReferencedEntity()!=null) {
      return;
    }

    // Look for Repository
    String id = getReferencedId();
    if (id.length()==0) {
      return;
    }

    Repository repository = (Repository)getGedcom().getEntity(id, Gedcom.REPOSITORIES);
    if (repository == null) {
      throw new GedcomException("Couldn't find entity with ID "+id);
    }

    // Create Backlink
    PropertyForeignXRef fxref = new PropertyForeignXRef(this);
    repository.addProperty(fxref);

    // ... and point
    setTarget(fxref);

    // don't delete anything because we may have children, like PAGE
  }

  /**
   * The expected referenced type
   */
  public int[] getTargetTypes() {
    return TARGET_TYPES;
  }
  
  /**
   * @see genj.gedcom.PropertyXRef#isValid()
   */
  public boolean isValid() {
    // always
    return true;
  }
  
} //PropertyRepository
