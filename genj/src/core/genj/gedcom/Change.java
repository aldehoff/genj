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

import java.util.*;

/**
 * Class that encapsulated changes after writelock
 */
public class Change {

  public static final int
    EADD    = 1,
    EDEL    = 2,
    PADD    = 4,
    PDEL    = 8,
    PMOD    = 16,
    EMOD    = 32;

  private Gedcom gedcom;

  private Vector eadd , edel,  emod;
  private Vector padd , pdel , pmod ;

  private int change;

  private GedcomListener cause;

  /**
   * Constructor
   * @param gedcom Gedcom that has been changed
   * @param eadd Added entities
   * @param edel Deleted entities
   * @param padd Added properties
   * @param pdel Deleted properties
   * @param pmod Modified properties
   */
  public Change(Gedcom gedcom, Vector eadd, Vector edel, Vector padd, Vector pdel, Vector pmod ) {

    // Remember
    this.gedcom=gedcom;

    this.eadd = eadd;
    this.edel = edel;
    this.padd = padd;
    this.pdel = pdel;
    this.pmod = pmod;

    this.cause= cause;

    // What's the change like?
    change = 0;
    if ((eadd!=null)&&(eadd.size()>0)) {
      change |= EADD;
    }
    if ((edel!=null)&&(edel.size()>0)) {
      change |= EDEL;
    }
    if ((padd!=null)&&(padd.size()>0)) {
      change |= PADD;
    }
    if ((pdel!=null)&&(pdel.size()>0)) {
      change |= PDEL;
    }
    if ((pmod!=null)&&(pmod.size()>0)) {
      change |= PMOD;
    }
  }

  /**
   * Property-Class in change?
   */
  public boolean containsProperty(int which, Class clazz) {

    Enumeration e = getProperties(which).elements();
    while (e.hasMoreElements()) {
      if (e.nextElement().getClass()==clazz) {
        return true;
      }
    }
    return false;
  }

  /**
   * Changing Listener
   */
  public GedcomListener getCause() {
    return cause;
  }

  /**
   * What has changed
   * @return change information as <code>int</code>
   */
  public int getChange() {
    return change;
  }

  /**
   * Resolves all entities which has properties that haveve been changed,
   * added or deleted
   */
  private Vector getEMOD() {

    // Already  calculated?
    if (emod!=null) {
      return emod;
    }

    // Calculate
    emod = new Vector(pmod.size()+padd.size()+pdel.size());

    Enumeration e = pmod.elements();
    while (e.hasMoreElements()) {
      emod.addElement( ((Property)e.nextElement()).getEntity() );
    }
    e = padd.elements();
    while (e.hasMoreElements()) {
      emod.addElement( ((Property)e.nextElement()).getEntity() );
    }
    e = pdel.elements();
    while (e.hasMoreElements()) {
      emod.addElement( ((Property)e.nextElement()).getEntity() );
    }

    return emod;
  }

  /**
   * Returns Entities which have been changed
   * @param which one of
   *  EADD added entities
   *  EDEL deleted entities
   *  EMOD entities with modified/added/deleted properties
   */
  public Vector getEntities(int which) {

    switch (which) {
    case EADD:
      return eadd;
    case EDEL:
      return edel;
    case EMOD:
      return getEMOD();
    }

    throw new IllegalArgumentException("Unknown type of entities");
  }

  /**
   * Changed Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Added/Deleted/Modified Properties
   */
  public Vector getProperties(int which) {
    switch (which) {
    case PADD:
      return padd;
    case PDEL:
      return pdel;
    case PMOD:
      return pmod;
    }
    throw new IllegalArgumentException("Unknown type of properties");
  }

  /**
   * What has changed
   * @param change what information to test
   * @return bool
   */
  public boolean isChanged(int change) {
    return (this.change&change) != 0;
  }
}
