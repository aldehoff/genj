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
 * $Revision: 1.1 $ $Author: nmeier $ $Date: 2004-05-22 11:05:41 $
 */
package genj.report;

import genj.option.Option;
import genj.option.OptionMetaInfo;
import genj.option.OptionProvider;
import genj.option.PropertyOption;
import genj.util.Resources;

import java.util.List;

/**
 * Options for report package
 */
public class Options extends OptionProvider implements OptionMetaInfo {

  /** 'singleton' instance */
  private static Options instance = new Options();
  
  /** option - browser executable */
  public String browser = "";

  /**
   * callback - provide options
   */
  public List getOptions() {
    return PropertyOption.introspect(getInstance());
  }

  /**
   * callback - provide localized option names
   *
   */
  public String getLocalizedName(Option option) {
    PropertyOption poption = (PropertyOption)option;
    return Resources.get(this).getString("option."+poption.getProperty());
  }
  
  /**
   * accessor - singleton instance
   */
  public static Options getInstance() {
    return instance;
  }

} //Options
