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
package genj.edit;

import genj.gedcom.Property;
import genj.util.Resources;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * A Proxy knows how to generate interaction components that the user
 * will use to change a property
 */
abstract class Proxy  {
  
  /** the resources */
  protected final static Resources resources = EditView.resources;
  
  /** the proxied property */
  protected Property property;
  
  /** the edit view */
  protected EditView view;
  
  /** the label header */
  protected JLabel label;

  /**
   * Stop editing a property through proxy. Return <b>true</b>
   * in case that sub-properties have been created/removed.
   */
  protected abstract void finish();

  /**
   * Returns change state of proxy
   */
  protected abstract boolean hasChanged();

  /**
   * Start editing a property through proxy
   * @return component to receive focus
   */
  protected final JComponent start(JPanel panel, Property prop, EditView edit) {
    // remember
    property = prop;
    view = edit;
    // setup a label
    label = new JLabel(prop.getTag(), prop.getImage(true), SwingConstants.LEFT);
    panel.add(label);
    // continue with sub-implementation dependent 
    return start(panel);
  }
  
  /**
   * Implementation
   */
  protected abstract JComponent start(JPanel panel);

} //Proxy
