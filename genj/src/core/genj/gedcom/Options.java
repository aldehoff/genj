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
package genj.gedcom;

import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.util.List;

/**
 * Application options
 */
public class Options implements OptionProvider {
  
  /** singleton */
  private final static Options instance = new Options();

  /** option - maximum image files size to be loaded */  
  private int maxImageFileSizeKB = 128;
  
  /** option - where lines of multi line values should be broken */
  private int valueLineBreak = 255;
  
  /** option - text symbol for marriage */
  private String txtMarriageSymbol = "+";

  /**
   * Singleton access
   */
  public static Options getInstance() {
    return instance;
  }

  /**
   * accessor - maxImageFileSizeKB
   */
  public void setMaxImageFileSizeKB(int max) {
    maxImageFileSizeKB = Math.max(4,max);
  }
  
  /**
   * accessor - maxImageFileSizeKB
   */
  public int getMaxImageFileSizeKB() {
    return maxImageFileSizeKB;
  }
  
  /**
   * accessor - valueLineBreak
   */
  public int getValueLineBreak() {
    return valueLineBreak;
  }

  /**
   * accessor - valueLineBreak
   */
  public void setValueLineBreak(int set) {
    valueLineBreak = Math.max(40,set);
  }

  /**
   * accessor - text marriage symbol
   */
  public String getTxtMarriageSymbol() {
    return txtMarriageSymbol;
  }

  /**
   * accessor - text marriage symbol
   */
  public void setTxtMarriageSymbol(String set) {
    if (set!=null&&set.trim().length()>0)
      txtMarriageSymbol = set;
    else
      txtMarriageSymbol = "+";
  }

  /** 
   * Provider callback 
   */
  public List getOptions() {
    return PropertyOption.introspect(instance);
  }

} //Options
