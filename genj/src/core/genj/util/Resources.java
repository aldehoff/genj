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

import java.util.*;
import java.text.MessageFormat;

/**
 * Class which provides localized text-resources for a package
 */
public class Resources {

  /** the wrapped ResourceBundle */
  private ResourceBundle rb;

  /** the package name this resource is for */
  private String pkg;

  /** a cached message format object */
  private MessageFormat format;

  /**
   * Constructor
   */
  public Resources(Object packageMember) {

    // Prepare information
    String me = packageMember.getClass().getName();
    String pkg = me.substring(0, me.lastIndexOf('.'));

    init(pkg);

  }

  /**
   * Constructor
   */
  public Resources(Class packageMemberClass) {

    // Prepare information
    String me = packageMemberClass.getName();
    String pkg = me.substring(0, me.lastIndexOf('.'));

    init(pkg);

  }

  /**
   * Constructor
   */
  public Resources(String pkg) {
    init(pkg);
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

    Debug.log(Debug.WARNING, this,"Resource '"+key+"' for pkg '"+pkg+"' is missing");

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
   * Init
   */
  private void init(String pkg) {

    this.format=new MessageFormat("");
    this.pkg=pkg;

    String lang = "en";
    try {
      lang = System.getProperty("user.language");
    } catch (Throwable t) {
    }

    try {

      String file;
      if (pkg.length()==0) {
        file = "resources";
        pkg = "<default>";
      } else {
        file = pkg+".resources";
      }

      rb = ResourceBundle.getBundle(
        file,
        // Using Local.getDefault() doesn't seem to work
        // under Linux ... will do our own constructor here :/
        new Locale(lang,"")
      );

    } catch (RuntimeException e) {

      Debug.log(Debug.WARNING, this,"Couldn't read resources for package '"+pkg+"'");

    }

    // Done
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
