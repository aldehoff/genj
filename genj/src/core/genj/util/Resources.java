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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Class which provides localized text-resources for a package
 * Resource files all follow these rules
 * <il>
 *  <li>reside in directory relative to class being used in e.g. ./genj/app
 *  <li>are names resources[_xy[_ab]].properties
 *  <li>are UTF-8 encoded
 *  <li>contain comment lines starting with '#'
 *  <li>contain content lines "key = value"
 *  <li>value can contain \n for newline
 *  <li>contain content continuation starting with '+'
 * </il>
 */
public class Resources {
  
  /** keep track of loaded resources */
  private static Map instances = new HashMap();

  /** the mapping key, resource  */
  private HashMap key2resource = new HashMap();

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
   * Calc file for package (package/resources.properties)
   */
  private String calcFile(String pkg, String lang, String country) {

    // dots in package name become slashs - /pkg/sub/resources
    String file = '/'+pkg.replace('.','/')+"/resources";
    
    // add language and country '/resources[_ll[_CC]].properties' 
    if (lang!=null) {
      file += '_'+lang;
      if (country!=null) {
        file += '_'+country;
      }
    }
    
    return file+".properties";   
  }

  /**
   * Constructor
   */
  private Resources(String pkg) {
    
    // init simple members
    this.format=new MessageFormat("");
    this.pkg=pkg;

    // try to find language
    Locale locale = Locale.getDefault();

    // loading english first (primary language)
    try {
      load(getClass().getResourceAsStream(calcFile(pkg, null, null)));
    } catch (Throwable t) {
      Debug.log(Debug.WARNING, this,"Couldn't read default resources for package '"+pkg+"'");
    }
    
    // trying to load language specific next
    try {
      load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), null)));
    } catch (Throwable t) {
    }

    // trying to load language and country specific next
    try {
      load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), locale.getCountry())));
    } catch (Throwable t) {
    }

    // Done
  }
  
  /**
   * Loads key/value pairs from inputstream with unicode content
   */
  private void load(InputStream in) throws IOException {
    try {
      BufferedReader lines = new BufferedReader(new InputStreamReader(in, "UTF-8"));
      // loop over all lines
      String key, val, last = null;
      while (true) {
        // next line
        String line = lines.readLine();
        if (line==null) 
          break;
        // trim and check
        line = line.trim();
        // .. nothing?
        if (line.length()==0)
          continue;
        // .. comment?
        char c = line.charAt(0); 
        if (c=='#') 
          continue;
        // .. continuation or key=value
        if (last!=null&&(c=='+'||c=='&')) {
          key = last;
          val = getString(key, "");
          if (c=='+') val += '\n';
          val += line.substring(1);
        } else {
          int i = line.indexOf('=');
          if (i<0) continue;
          key = line.substring(0, i).trim();
          val = line.substring(i+1).trim();
        }
        // remember
        key2resource.put(key, val);
        // next
        last = key;
      }
    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }
  }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param notNull will return key if resource is not defined
   */
  public String getString(String key, boolean notNull) {
    String result = (String)key2resource.get(key);
    if (result==null&&notNull) result = key;
    return result;
  }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   */
  public String getString(String key) {
    return getString(key, true);
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

    // Get Value
    String value = getString(key);

    // .. this is our pattern
    format.applyPattern(value);

    // .. which we fill with substitutes
    String result = format.format(substitutes);

    // Done
    return result;

  }

  /**
   * Returns the available Keys
   */
  public Iterator getKeys() {
    return key2resource.keySet().iterator();
  }
  
} //Resources
