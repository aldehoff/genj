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
import java.util.WeakHashMap;

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
  private Map key2string;

  /** the package name this resource is for */
  private String pkg;

  /** cached message formats */
  private WeakHashMap msgFormats = new WeakHashMap();
  
  /**
   * Constructor for resources from explicit input stream
   */
  public Resources(InputStream in) {
    
    key2string = new HashMap();
    
    try {
      load(in, key2string);
    } catch (IOException e) {
      // swallow
    }
  }
  
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
    synchronized (instances) {
      Resources result = (Resources)instances.get(packge);
      if (result==null) {
        result = new Resources(packge);
        instances.put(packge, result);
      }
      return result;
    }
  }
  
  /**
   * Calc package for instance
   */
  private static String calcPackage(Object object) {
    Class clazz = object instanceof Class ? (Class)object : object.getClass();
    String name = clazz.getName();
    int last = name.lastIndexOf('.');
    return last<0 ? name : name.substring(0, last);
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
    // remember
    this.pkg=pkg;
  }
  
  /**
   * Loads key/value pairs from inputstream with unicode content
   */
  private static void load(InputStream in, Map out) throws IOException {
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
          val = (String)out.get(key);
          if (c=='+') val += '\n';
          val += line.substring(1);
        } else {
          int i = line.indexOf('=');
          if (i<0) continue;
          key = line.substring(0, i).trim();
          val = line.substring(i+1).trim();
        }
        // remember
        out.put(key, val);
        // next
        last = key;
      }
    } catch (UnsupportedEncodingException e) {
      throw new IOException(e.getMessage());
    }
  }
  
  /**
   * Lazy getter for resource map
   */
  private Map getKey2String() {
    
    // easy if already initialized
    if (key2string!=null)
      return key2string;
    
    synchronized (this) {
      
      // check again
      if (key2string!=null)
        return key2string;
      
      // load resources for current locale now
      Locale locale = Locale.getDefault();
      Map result = new HashMap();    

      // loading english first (primary language)
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, null, null)), result);
      } catch (Throwable t) {
      }
      
      // trying to load language specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), null)), result);
      } catch (Throwable t) {
      }
  
      // trying to load language and country specific next
      try {
        load(getClass().getResourceAsStream(calcFile(pkg, locale.getLanguage(), locale.getCountry())), result);
      } catch (Throwable t) {
      }

      // remember
      key2string = result;
    }
    
    // done
    return key2string;
  }
  
  /**
   * Checks for given key
   */
  public boolean contains(String key) {
    return getString(key, false) != null;
  }
  
  /**
   * Returns a localized string
   * @param key identifies string to return
   * @param notNull will return key if resource is not defined
   */
  public String getString(String key, boolean notNull) {
    String result = (String)getKey2String().get(key);
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

    // do we have a message format already?
    MessageFormat format = (MessageFormat)msgFormats.get(key);
    if (format==null) {
      format = getMessageFormat(getString(key));
      msgFormats.put(key, format);
    }

    // fill with substitutes
    return format.format(substitutes);
  }
  
  /**
   * Generate a MessageFormat for given pattern
   */
  public static MessageFormat getMessageFormat(String pattern) {
    // have to patch single quotes to doubles because
    // MessageFormat doesn't like those 
    if (pattern.indexOf('\'')>=0) {
      StringBuffer buffer = new StringBuffer(pattern.length()+8);
      for (int i=0,j=pattern.length();i<j;i++) {
        char c = pattern.charAt(i);
        buffer.append(c);
        if (c=='\'') buffer.append('\'');
      }
      pattern = buffer.toString();
    }
    // got it
    return new MessageFormat(pattern);
  }

  /**
   * Returns the available Keys
   */
  public Iterator getKeys() {
    return getKey2String().keySet().iterator();
  }
  
} //Resources
