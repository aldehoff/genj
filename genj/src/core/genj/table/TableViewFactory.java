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
package genj.table;

import genj.gedcom.Gedcom;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.swing.ImageIcon;
import genj.view.ViewFactory;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JComponent;

/**
 * The factory for the TableView
 */
public class TableViewFactory implements ViewFactory {

  /**
   * @see genj.view.ViewFactory#createView(genj.gedcom.Gedcom, genj.util.Registry, java.awt.Frame)
   */
  public JComponent createView(Gedcom gedcom, Registry registry, Frame frame) {
    return new TableView(gedcom,registry,frame);
  }

  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImageIcon getImage() {
    return new ImageIcon(this, "images/View.gif");
  }
  
  /**
   * @see genj.view.ViewFactory#getDefaultDimension()
   */
  public Dimension getDefaultDimension() {
    return new Dimension(480,320);
  }

  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle(boolean abbreviate) {
    return Resources.get(this).getString("title" + (abbreviate?".short":""));
  }

} //TableViewFactory
