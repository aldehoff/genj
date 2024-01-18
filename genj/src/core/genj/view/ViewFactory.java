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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.List;

import javax.swing.JComponent;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.print.PrintRenderer;
import genj.util.ImgIcon;
import genj.util.Registry;

/**
 * The interface to a view's factory
 */
public interface ViewFactory {

  /**
   * Callback for creating a view 
   */
  public Component createViewComponent(Gedcom gedcom, Registry registry, Frame frame);
  
  /**
   * Callback for creating settings for a view
   */
  public JComponent createSettingsComponent(Component view);
  
  /**
   * Callback for creating a printer for a view
   */
  public PrintRenderer createPrintRenderer(Component view);
  
  /**
   * Returns an image for this view
   */
  public ImgIcon getImage();
  
  /**
   * Returns a key for this view
   */
  public String getKey();
  
  /**
   * Returns a localized title for this view
   */
  public String getTitle(boolean abbreviate);

  /**
   * Returns the default size of the view
   */
  public Dimension getDefaultDimension();
  
} //ViewFactory
