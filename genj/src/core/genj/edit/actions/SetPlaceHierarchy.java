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
package genj.edit.actions;

import javax.swing.JComponent;

import genj.gedcom.GedcomException;
import genj.gedcom.PropertyPlace;
import genj.util.swing.TextFieldWidget;
import genj.view.ViewManager;

/**
 * Set the place hierarchy used in a gedcom file
 */
public class SetPlaceHierarchy extends AbstractChange {

    /** the place to use as the global example */
    private PropertyPlace place;
    
    /** textfield for hierarchy */
    private TextFieldWidget hierarchy;
    
    /**
     * Constructor
     */
    public SetPlaceHierarchy(PropertyPlace place, ViewManager mgr) {
      super(place.getGedcom(), place.getImage(false), resources.getString("place.hierarchy"), mgr);

      this.place = place;
    }

    /**
     * no confirmation message needed
     */    
    protected String getConfirmMessage() {
      return resources.getString("place.hierarchy.msg", place.getGedcom().getName());
   }
    
    protected JComponent getOptions() {
      hierarchy = new TextFieldWidget(place.getGedcom().getPlaceHierarchy());
      return hierarchy;
    }

    /**
     * set the submitter
     */
    protected void change() throws GedcomException {
      place.setHierarchy(true, hierarchy.getText().trim());
    }

} //SetPlaceFormat

