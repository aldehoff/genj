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
package genj.app;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.event.*;

import genj.gedcom.*;
import genj.util.ImgIcon;
import genj.util.swing.ImgIconConverter;

/**
 * A component that allows to select an entity - the entity is
 * selected by clicking on it in any view. This component will
 * display that selected entity
 */
public class EntitySelector extends JLabel implements GedcomListener {

  /** member */
  private Gedcom gedcom;
  private int filter[];
  private Entity entity;

  /**
   * Constructor
   */
  public EntitySelector() {

    // Initial look
    setEntity(null);

    // Done
  }

  /**
   * A helper checks wether an an entity matches the filter
   * @return wether it fits or not
   */
  private boolean checkFilter(Entity entity) {
    // No entity or filter - o.k.
    if ((entity==null)||(filter==null))
      return true;
    // Loop through filter
    for (int i=0;i<filter.length;i++) {
      if (filter[i]==entity.getType())
      return true;
    }
    // No candidate
    return false;
  }

  /**
   * Returns the selected entity
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * Notification of changes in Gedcom - we check
   * wether our entity has been removed so have to forget it
   */
  public void handleChange(Change change) {
    if ((entity!=null)&&(change.isChanged(change.EDEL))&&(change.getEntities(change.EDEL).contains(entity)))
      setEntity(null);
  }

  /**
   * Notification of closed Gedcom - we stop listening
   * to it
   */
  public void handleClose(Gedcom which) {
    setGedcom(null);
  }

  /**
   * Notification of a selection in Gedcom - we remember
   * selected entity according to filter
   */
  public void handleSelection(Selection selection) {
    if (checkFilter(selection.getEntity()))
      setEntity(selection.getEntity());
  }

  /**
   * This control isn't used anymore
   */
  public void removeNotify() {
    super.removeNotify();
    setGedcom(null);
  }

  /**
   * Selects an entity
   */
  public void setEntity(Entity pEntity) {
    // Remember
    entity=pEntity;
    // Show
    if (entity!=null) {
      setIcon(ImgIconConverter.get(entity.getProperty().getImage(false)));
      setText(entity.toString());
    } else {
      setIcon(ImgIconConverter.get(genj.gedcom.Images.get("?")));
      setText("");
    }
    // Done
  }

  /**
   * Sets the types this selector will accept as being selected
   */
  public void setFilter(int[] pFilter) {
    filter = pFilter;

    if (!checkFilter(entity)) {
      setEntity(null);
    }
  }

  /**
   * Sets the Gedcom to listen to
   */
  public void setGedcom(Gedcom pGedcom) {
    // Stop listening
    if (gedcom!=null) {
      gedcom.removeListener(this);
    }
    // Remember
    gedcom = pGedcom;
    // Start listening
    if (gedcom!=null) {
      gedcom.addListener(this);
    }
    // Done
  }
}
