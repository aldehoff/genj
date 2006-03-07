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
package genj.edit.beans;

import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A proxy for a property that links entities
 */
public class XRefBean extends PropertyBean {

  private Preview preview;
  
  /**
   * Initialization
   */
  protected void initializeImpl() {
    
    preview = new Preview();
    
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, preview);
    
  }
  
  /**
   * Nothing to edit
   */  
  public boolean isEditable() {
    return false;
  }
  
  /**
   * Set context to edit
   */
  protected void setPropertyImpl(Property property) {

    // set preview
    PropertyXRef xref = (PropertyXRef)property;
    if (xref!=null&&xref.getTargetEntity()!=null) 
      preview.setEntity(xref.getTargetEntity());
    
  }
  
  /**
   * Preferred
   */
  public Dimension getPreferredSize() {
    return new Dimension(64,48);
  }

    
} //ProxyXRef
