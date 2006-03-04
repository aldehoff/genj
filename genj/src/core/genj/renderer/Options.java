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

import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.awt.Font;
import java.util.List;

/**
 * Blueprint/Renderer Options
 */
public class Options extends OptionProvider {
  
  /** singleton */
  private final static Options instance = new Options();
  
  /** the default font */
  private Font defaultFont = new Font("SansSerif", 0, 11);
  
  /**
   * singleton access
   */
  public static Options getInstance() {
    return instance;
  }
  
  /**
   * Accessor - font
   */
  public Font getDefaultFont() {
    return defaultFont;
  }

  /**
   * Accessor - font
   */
  public void setDefaultFont(Font set) {
    defaultFont = set;
  }

  /**
   * Access to our options (one)
   */
  public List getOptions() {
    return PropertyOption.introspect(getInstance());
  }
  
} //Options
