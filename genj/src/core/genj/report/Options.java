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
 * 
 * $Revision: 1.3 $ $Author: nmeier $ $Date: 2004-05-26 20:02:59 $
 */
package genj.report;

import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.io.File;
import java.util.List;

/**
 * Options for report package
 */
public class Options implements OptionProvider {
  
  /** 'singleton' instance */
  private static Options instance = new Options();
  
  /** option - browser executable */
  public File browser = new File("");

  /**
   * callback - provide options
   */
  public List getOptions() {
    return PropertyOption.introspect(getInstance());
  }

  /**
   * accessor - singleton instance
   */
  public static Options getInstance() {
    return instance;
  }

} //Options
