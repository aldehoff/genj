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

import gj.ui.UnitGraphics;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Interface between Printer and Renderer
 */
public interface PrintRenderer {
  
  /**
   * Calculates the number of pages (horizontally and vertically)
   * needed to render the content on pages with given imageable
   * size (in centimeters)     */
  public Point getNumPages(Point2D pageSize);
  
  /**
   * Renders page content (x,y) on given context (units in centimeters)
   */  
  public void renderPage(Point page, UnitGraphics g);
  
} //PrintRenderer
