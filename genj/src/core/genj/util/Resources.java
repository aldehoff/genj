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
package genj.util;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class which provides localized text-resources for a package
 */
public class Resources {
  
  /** keep track of loaded resources */
  private static Map instances = new HashMap();

  /** the wrapped ResourceBundle */
  private ResourceBundle rb;

  /** the package name this resource is for */
  private String pkg;

  /** a cached message format object */
  private MessageFormat format;
  
  /**
   * Accessor (cached) 
   */
  public static Resources get(Object packgeMember) {
    return get(calcPackage(packgeMember));
  }

  /**
   * Accessor  (cached)
   */
  public static Resources get(String packge) {
    Resources result = (Resources)instances.get(packge);
    if (result==null) {
      result = new Resources(packge);
      instances.put(packge, result);
    }
    return result;
  }
  
  /**
   * Calc package for instance
   */
  private static String calcPackage(Object object) {
    Class clazz = object instanceof Class ? (Class)object : object.getClass();
    String name = clazz.getName();
    return name.substring(0, name.lastIndexOf('.'));
  }

  /**
   * Constructor
   */
  private Resources(String pkg) {
    
    // init simple members
    this.format=new MessageFormat("");
    this.pkg=pkg;

    // try to find language
    String lang = "en";
    try {
      lang = System.getProperty("user.language");
    } catch (Throwable t) {
    }
    
    // try to load it
    try {

      // calculate resource-name package.resources[.properties]
      String file;
      if (pkg.length()==0) {
        file = "resources";
        pkg = "<default>";
      } else {
        file = pkg+".resources";
      }

      // the locale we'll use
      Locale locale = new Locale(lang,"");
      
      // o.k. here's the scoop - by the time we're here after
      // possibly changing System.setProperty("user.language")
      // the default Locale is already set (e.g. "FR_FR"). 
      // Instead of our english resource (which are in
      // resources.properties instead of resources_en.properties)
      // we might end up with resources_fr.properties. So
      // let's kill the default here 
      try { 
        Locale.setDefault(new Locale("","",""));
      } catch (Throwable t) {
      }
      
      // get it
      rb = ResourceBundle.getBundle(file, locale);

    } catch (RuntimeException e) {

      Debug.log(Debug.WARNING, this,"Couldn't read resources for package '"+pkg+"'");

    }

    // Done
  }

  
  /**
   * Returns localized strings
   */
  public String[] getStrings(String[] keys) {
    
    String[] result = new String[keys.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = getString(keys[i]);
    }
    return result;
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   */
  public String getString(String key) {

    try {

      if (rb!=null)
        return rb.getString(key);

    } catch (RuntimeException e) {
    }

    // 20030321 removed - too verbose
    //Debug.log(Debug.WARNING, this,"Resource '"+key+"' for pkg '"+pkg+"' is missing");

    return key;
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
  public String getString(String key, Object substitute) {
    return getString(key, new Object[]{ substitute });
  }

  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param values array of values to replace placeholders in value
   */
  public String getString(String key, Object[] substitutes) {

    try {

      if (rb!=null) {

        // Get Value
        String value = rb.getString(key);

        // .. this is our pattern
        format.applyPattern(value);

        // .. which we fill with substitutes
        String result = format.format(substitutes);

        // Done
        return result;
      }

    } catch (RuntimeException e) {
    }

    Debug.log(Debug.WARNING, this,"Resource '"+key+"' for pkg '"+pkg+"' is missing");

    return key;

  }

  /**
   * Returns the available Keys
   */
  public Enumeration getKeys() {
    if (rb==null) {
      return null;
    }
    return rb.getKeys();
  }
}
