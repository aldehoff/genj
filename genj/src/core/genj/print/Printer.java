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
package genj.print;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

/**
 * Interface between Printer and Renderer
 */
public interface Printer {
  
  /**
   * Sets the view to print   */
  public void setView(JComponent view);
  
  /**
   * Calculates the size of the content to be renderer with
   * given resolution (dpi)     */
  public Dimension calcSize(Point resolution);
  
  /**
   * Renders page content (x,y) on given context (dots) and 
   * resolution (dpi) - g is clipped and translated already
   */  
  public void renderPage(Graphics2D g, Point page, Point resolution);
  
} //PrintRenderer
