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
 * Class for encapsulating a note
 */
public class Note extends Entity implements MultiLineSupport {

  /** a delegate for keep the text data crammed in here by Gedcom grammar */
  private PropertyMultilineValue delegate;
  
  /**
   * Notification to entity that it has been added to a Gedcom
   */
  /*package*/ void addNotify(Gedcom ged, String tag) {
    
    // continue
    super.addNotify(ged, tag);

    // create a delegate we're using for storing the 
    // note's multiline value
    if (delegate==null) {
      delegate = (PropertyMultilineValue)MetaProperty.get(this, "NOTE").create("");
      delegate.isTransient = true;
      addProperty(delegate);
    }
    
    // done
  }

  /**
   * Returns this property as a string
   */
  public String toString() {
    return delegate.getValue();
  }

  /**
   * @see genj.gedcom.Entity#setValue(java.lang.String)
   */
  public void setValue(String newValue) {
    // keep it in delegate
    delegate.setValue(newValue);
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
   * @see genj.gedcom.MultiLineSupport#getLinesValue()
   */
  public String getLinesValue() {
    return delegate.getLinesValue();
  }
  
  /**
   * @see genj.gedcom.PropertyNote#getLines()
   */
  public Line getLines() {
    return delegate.getLines();
  }
  
  /**
   * @see genj.gedcom.MultiLineSupport#append(int, java.lang.String, java.lang.String)
   */
  public boolean append(int level, String tag, String value) {
    // keep it in delegate
    return delegate.append(level, tag, value);
  }

} //Note
