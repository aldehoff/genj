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
package genj.renderer;

import genj.util.Registry;

/**
 * Encapsulating name and html for rendering an entity */
public class Blueprint {
  
  /** whether we've changed */
  /*package*/ boolean hasChanged = false;
  
  /** the name of this scheme */
  private String name;
  
  /** the html of this scheme */
  private String html;

  /**
   * Constructor
   */
  public Blueprint(String hTml) {
    html = hTml;
  }
    
  
  /**
   * Constructor
   */
  /*package*/ Blueprint(Registry registry, String nAme, String hTml) {
    name = nAme;
    html = hTml;
  }
    
  /**
   * Accessor - html
   */
  public void setHTML(String hTml) {
    // FIXME update registry
    hasChanged = true;
    html = hTml;
  }
  
  /**
   * Accessor - html
   */
  public String getHTML() {
    return html;
  }

  /**
   * Accessor - name
   */
  public String getName() {
    return name;
  }

} //RenderingScheme
