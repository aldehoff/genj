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

import java.awt.*;
import javax.swing.*;

/**
 * Interface between Printer and Renderer
 */
public interface PrintRenderer {

  /**
   * Returns a panel for editing this renderers properties
   */
  public JPanel getEditor();

  /**
   * Returns size of rendering object in pixels
   */
  public Dimension getSize();

  /**
   * Renders print data for page
   * @param g Graphics to render on
   */
  public void renderPage(Graphics g, Dimension dimPages);

  /**
   * Renders a preview
   * @param g Graphics to render on
   * @param zoom zoom rate to use
   */
  public void renderPreview(Graphics g, Dimension dimPreview, float zoomPreview);

  /**
   * Tells the renderer which printer it is used by
   */
  public void setPrinter(Printer printer);
}
