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

import java.awt.Component;
import java.awt.Frame;

import genj.app.ViewFactory;
import genj.app.ViewSettingsWidget;
import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.Registry;

/**
 * The factory for the TableView
 */
public class NavigatorViewFactory implements ViewFactory {

  /**
   * @see genj.app.ViewFactory#createSettingsComponent(Component)
   */
  public ViewSettingsWidget createSettingsComponent(Component view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createPrintRenderer(Component)
   */
  public PrintRenderer createPrintRenderer(Component view) {
    return null;
  }

  /**
   * @see genj.app.ViewFactory#createViewComponent(Gedcom, Registry, Frame)
   */
  public Component createViewComponent(Gedcom gedcom, Registry registry, Frame frame) {
    return new NavigatorView(gedcom,registry,frame);
  }

}
