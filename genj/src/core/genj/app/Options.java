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
package genj.app;

import genj.option.Option;
import genj.option.OptionMetaInfo;
import genj.option.OptionProvider;
import genj.util.Resources;

import java.util.StringTokenizer;

/**
 * Application options
 */
public class Options extends OptionProvider implements OptionMetaInfo {

  /** singleton */
  private final static Options instance = new Options();

  /** resources */
  private final static Resources resources = Resources.get(Options.class);
  
  /** the current language code */    
  public int language = 0;
  
  /** all available language codes */
  public static String[] languages;

  /** all available language codes */
  public static String[] codes;
  
  /**
   * Init available languages
   */
  static {

    StringTokenizer langs = new StringTokenizer(resources.getString("option.languages", ""));
    
    languages = new String[langs.countTokens()+1];
    codes     = new String[langs.countTokens()+1];

    codes    [0] = "";    
    languages[0] = "";    
    for (int i=1;langs.hasMoreElements();i++) {
      codes    [i] = langs.nextToken();
      languages[i] = resources.getString("option.language."+codes[i]);
    }
    
  }
  
  /** 
   * Provider callback 
   */
  public Option[] getOptions() {
    return Option.getOptions(instance);
  }
  
  /**
   * OptionMetaInfo callback - provide a name for an option
   */
  public String getLocalizedName(Option option) {
    return resources.getString("option."+option.getKey());
  }

} //Options
