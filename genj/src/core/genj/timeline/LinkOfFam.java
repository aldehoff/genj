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
package genj.timeline;

import genj.gedcom.*;

/**
 * A Link represents a marker on the timeline - event of a family
 */
/*package*/ class LinkOfFam extends Link {

  /**
   * Constructor
   */
  protected LinkOfFam(PropertyEvent pevent, float minYear, float maxYear) {
    super(pevent,minYear,maxYear);
  }

  /**
   * Entity dependent text for link description
   */
  protected String getText() {

    Fam fam = (Fam)pevent.getEntity();
    Indi husband,wife;

    String name;

    husband = fam.getHusband();
    wife = fam.getWife();

    while (true) {

      // Husband and Wife ?
      if ((husband!=null)&&(wife!=null)) {
        name = husband.getName() + " and " + wife.getName();
        break;
      }

      // Husband ?
      if (husband!=null) {
        name = husband.getName();
        break;
      }

      // Wife ?
      if (wife!=null) {
        name = wife.getName();
        break;
      }

      // None !
      name = "@"+fam.getId()+"@";

    }

    return name;
  }          
}
