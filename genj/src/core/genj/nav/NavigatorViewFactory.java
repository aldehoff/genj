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
package genj.nav;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JComponent;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.ImgIcon;
import genj.util.Registry;
import genj.view.ViewFactory;

/**
 * The factory for the TableView
 */
public class NavigatorViewFactory implements ViewFactory {

  /**
   * @see genj.app.ViewFactory#createSettingsComponent(Component)
   */
  public JComponent createSettingsComponent(JComponent view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createPrintRenderer(Component)
   */
  public PrintRenderer createPrintRenderer(JComponent view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createViewComponent(Gedcom, Registry, Frame)
   */
  public JComponent createViewComponent(Gedcom gedcom, Registry registry, Frame frame) {
    return new NavigatorView(gedcom,registry,frame);
  }
  
  /**
   * @see genj.view.ViewFactory#getDefaultDimension()
   */
  public Dimension getDefaultDimension() {
    return new Dimension(140,200);
  }

  /**
   * @see genj.view.ViewFactory#getImage()
   */
  public ImgIcon getImage() {
    return Images.imgView;
  }
  
  /**
   * @see genj.view.ViewFactory#getKey()
   */
  public String getKey() {
    return "navigator";
  }
  
  /**
   * @see genj.view.ViewFactory#getName(boolean)
   */
  public String getTitle(boolean abbreviate) {
    return NavigatorView.resources.getString("title" + (abbreviate?".short":""));
  }

} //NavigatorViewFactory
