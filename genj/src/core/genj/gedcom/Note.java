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
 * Class for encapsulating a note
 */
public class Note extends PropertyNote implements Entity {

  private String id = "";
  private Gedcom gedcom;
  private Delegate delegate = new Delegate();

  /**
   * Constructor for Note
   */
  /*package*/ Note() {
    super(null);
  }

  /**
   * Notification to entity that it has been added to a Gedcom
   */
  public void addNotify(Gedcom gedcom) {
    this.gedcom = gedcom;
    addProperty(delegate);
  }

  /**
   * Gedcom this entity's in
   * @return containing Gedcom
   */
  public Gedcom getGedcom() {
    return gedcom;
  }

  /**
   * Returns this entity's id.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns this entity's first property
   */
  public Property getProperty() {
    return this;
  }

  /**
   * Returns the type to which this entity belongs
   * INDIVIDUALS, FAMILIES, MULTIMEDIAS, NOTES, ...
   */
  public int getType() {
    return Gedcom.NOTES;
  }

  /**
   * Set Gedcom this entity's in
   */
  public void setGedcom(Gedcom gedcom) {
    this.gedcom=gedcom;
  }

  /**
   * Sets entity's id.
   * @param id new id
   */
  public void setId(String id) {
    this.id=id;
  }

  /**
   * Returns this property as a string
   */
  public String toString() {
    return getId()+":"+delegate.getLinesValue();
  }
  
  /**
   * @see genj.gedcom.PropertyNote#getProxy()
   */
  public String getProxy() {
    return "Entity";
  }
  
  /**
   * @see genj.gedcom.PropertyNote#link()
   */
  public void link() throws GedcomException {
    throw new IllegalArgumentException();
  }

  /**
   * @see genj.gedcom.PropertyNote#setValue(java.lang.String)
   */
  public void setValue(String v) {
    delegate.setValue(v);
  }
  
  /**
   * @see genj.gedcom.Property#delProperty(genj.gedcom.Property)
   */
  public boolean delProperty(Property which) {
    // ignore request to delete delegate
    if (which==delegate) return false;
    // o.k.
    return super.delProperty(which);
  }
  
  /**
   * @see genj.gedcom.PropertyNote#getLines()
   */
  public Line getLines() {
    return delegate.getLines();
  }

  /**
   * Delegate 
   */
  private class Delegate extends PropertyMultilineValue {
    
    /**
     * Constructor
     */
    private Delegate() {
      super("NOTE", "");
    };
    
    /**
     * @see genj.gedcom.Property#getImage(boolean)
     */
    public ImageIcon getImage(boolean checkValid) {
      return Note.this.getProperty().getImage(false);
    }

    /**
     * @see genj.gedcom.Property#isTransient()
     */
    public boolean isTransient() {
      return true;
    }
    
  } //Delegate
  
} //Note
