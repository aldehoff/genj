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
package genj.view;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Support for a popup menu on entities
 */
public interface ContextPopupSupport {

  /**
   * The component where the popup support is installed on
   */
  public JComponent getContextPopupContainer();
  
  /** 
   * Context (entity/property) by position
   */
  public Context getContextAt(Point pos);
  
  /**
   * A context
   */
  public class Context {
    
    /** actions */
    private List actions;
    
    /** the content of the context */
    private Object content;
    
    /**
     * Constructor
     */
    public Context(Object content) {
      this(content, new ArrayList());
    }
    
    /**
     * Constructor
     */
    public Context(Object content, List actions) {
      this.content = content;
      this.actions = actions;
    }
    
    /**
     * Accessor - content
     */
    public Object getContent() {
      return content;
    }
    
    /**
     * Accessor - actions
     */
    public List getActions() {
      return actions;
    }
    
  }//Context

} //EntityPopupSupport
